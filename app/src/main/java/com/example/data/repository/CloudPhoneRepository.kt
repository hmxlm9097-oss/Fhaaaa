package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.local.ActivityLogEntity
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.CloudPhoneEntity
import com.example.data.local.DeviceSessionEntity
import com.example.data.local.FileTransferEntity
import com.example.data.local.LocationHistoryEntity
import com.example.data.local.PreferencesManager
import com.example.data.remote.AuthLoginRequest
import com.example.data.remote.AuthRegisterRequest
import com.example.data.remote.AuthResponse
import com.example.data.remote.CreateDeviceRequest
import com.example.data.remote.HealthResponse
import com.example.data.remote.NetworkClientFactory
import com.example.data.remote.RemoteControlSocketClient
import com.example.data.remote.RemoteSocketTelemetry
import com.example.data.remote.VerifyTwoFactorRequest
import com.example.data.system.ClipboardBridge
import com.example.data.system.DeviceTelemetryProvider
import com.example.data.system.InstalledAppItem
import com.example.data.system.InstalledAppsProvider
import com.example.data.system.LiveDeviceTelemetry
import com.example.data.system.LocationCaptureProvider
import com.example.data.system.TotpAuthenticator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.SecureRandom
import java.util.UUID

class CloudPhoneRepository(context: Context) {
    private val appContext = context.applicationContext
    private val db = AppDatabase.getInstance(appContext)
    private val prefsManager = PreferencesManager(appContext)
    private val telemetryProvider = DeviceTelemetryProvider(appContext)
    private val locationProvider = LocationCaptureProvider(appContext)
    private val installedAppsProvider = InstalledAppsProvider(appContext)
    private val clipboardBridge = ClipboardBridge(appContext)
    val socketClient = RemoteControlSocketClient()

    val preferencesFlow: Flow<AppPreferences> = prefsManager.preferencesFlow
    val cloudPhonesFlow: Flow<List<CloudPhoneEntity>> = db.cloudPhoneDao().observeAll()
    val activityLogsFlow: Flow<List<ActivityLogEntity>> = db.activityLogDao().observeRecent()
    val locationHistoryFlow: Flow<List<LocationHistoryEntity>> = db.locationHistoryDao().observeAll()
    val fileTransfersFlow: Flow<List<FileTransferEntity>> = db.fileTransferDao().observeAll()
    val sessionsFlow: Flow<List<DeviceSessionEntity>> = db.deviceSessionDao().observeAll()
    val socketTelemetryFlow: StateFlow<RemoteSocketTelemetry> = socketClient.state

    suspend fun logEvent(category: String, action: String, detail: String, severity: String = "INFO") {
        db.activityLogDao().insert(
            ActivityLogEntity(
                category = category,
                action = action,
                detail = detail,
                severity = severity
            )
        )
    }

    suspend fun clearLogs() {
        db.activityLogDao().clearAll()
        logEvent("SYSTEM", "CLEAR_LOGS", "Cleared local audit log history")
    }

    fun captureTelemetrySnapshot(): LiveDeviceTelemetry = telemetryProvider.snapshot()

    fun hasLocationPermission(): Boolean = locationProvider.hasLocationPermission()

    suspend fun captureAndSaveLocation(): Result<LocationHistoryEntity> = withContext(Dispatchers.IO) {
        val prefs = prefsManager.preferencesFlow.first()
        if (!prefs.locationConsentEnabled) {
            return@withContext Result.failure(
                IllegalStateException("Enable Location Monitoring Consent first before capturing GPS coordinates.")
            )
        }
        val capture = locationProvider.captureLastKnownLocation()
        capture.mapCatching { res ->
            val entity = LocationHistoryEntity(
                latitude = res.latitude,
                longitude = res.longitude,
                accuracyMeters = res.accuracyMeters,
                provider = res.provider,
                timestamp = res.timestampMs
            )
            db.locationHistoryDao().insert(entity)
            logEvent(
                "TELEMETRY",
                "GPS_CAPTURE",
                "Captured authorized fix (${String.format("%.4f", res.latitude)}, ${String.format("%.4f", res.longitude)}) via ${res.provider}"
            )
            entity
        }
    }

    suspend fun clearLocationHistory() {
        db.locationHistoryDao().clearAll()
        logEvent("TELEMETRY", "LOCATION_WIPED", "Cleared consented location history records", "WARN")
    }

    suspend fun loadInstalledApps(): List<InstalledAppItem> = withContext(Dispatchers.IO) {
        installedAppsProvider.loadLauncherApps()
    }

    fun launchLocalApp(packageName: String): Boolean = installedAppsProvider.launchApp(packageName)

    fun readLocalClipboard(): String = clipboardBridge.readText()

    fun writeLocalClipboard(label: String, text: String) {
        clipboardBridge.writeText(label, text)
    }

    suspend fun saveServerEndpoints(baseUrl: String, wsUrl: String, stunUrl: String) {
        prefsManager.updateServerEndpoints(baseUrl, wsUrl, stunUrl)
        logEvent("SYSTEM", "ENDPOINTS_SAVED", "Updated control gateway endpoints: $baseUrl")
    }

    suspend fun testServerConnection(baseUrl: String): Result<HealthResponse> = withContext(Dispatchers.IO) {
        val serviceRes = NetworkClientFactory.createApiService(baseUrl)
        serviceRes.fold(
            onSuccess = { api ->
                runCatching { api.getHealth() }.also { result ->
                    result.onSuccess { h ->
                        logEvent("SYSTEM", "HEALTH_OK", "Gateway reachable (adb=${h.adbAvailable}, nodes=${h.activeDeviceCount})")
                    }.onFailure { err ->
                        logEvent("SYSTEM", "HEALTH_FAIL", "Gateway check failed: ${err.message}", "WARN")
                    }
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun registerOnServer(
        baseUrl: String,
        email: String,
        password: String,
        displayName: String
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        val api = NetworkClientFactory.createApiService(baseUrl).getOrElse { return@withContext Result.failure(it) }
        runCatching {
            val res = api.register(AuthRegisterRequest(email.trim(), password, displayName.trim()))
            prefsManager.updateServerEndpoints(baseUrl, deriveWsUrl(baseUrl), prefsManager.preferencesFlow.first().webrtcStunUrl)
            prefsManager.saveServerSession(
                token = res.token,
                email = res.user.email,
                displayName = res.user.displayName,
                emailVerified = res.user.emailVerified,
                twoFactorEnabled = res.user.twoFactorEnabled,
                plan = res.user.plan
            )
            logEvent("SECURITY", "SERVER_REGISTER", "Registered ${res.user.email} on control gateway")
            res
        }
    }

    suspend fun loginOnServer(
        baseUrl: String,
        email: String,
        password: String,
        totpCode: String?
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        val api = NetworkClientFactory.createApiService(baseUrl).getOrElse { return@withContext Result.failure(it) }
        runCatching {
            val res = api.login(AuthLoginRequest(email.trim(), password, totpCode?.ifBlank { null }))
            prefsManager.updateServerEndpoints(baseUrl, deriveWsUrl(baseUrl), prefsManager.preferencesFlow.first().webrtcStunUrl)
            prefsManager.saveServerSession(
                token = res.token,
                email = res.user.email,
                displayName = res.user.displayName,
                emailVerified = res.user.emailVerified,
                twoFactorEnabled = res.user.twoFactorEnabled,
                plan = res.user.plan
            )
            logEvent("SECURITY", "SERVER_LOGIN", "Authenticated ${res.user.email} against gateway")
            res
        }
    }

    suspend fun enterLocalWorkspace(operatorName: String, email: String) {
        prefsManager.enterLocalDiagnosticsWorkspace(operatorName, email)
        logEvent(
            "SECURITY",
            "LOCAL_WORKSPACE",
            "Entered Local Diagnostics & Offline Config Workspace (no cloud server linked yet)"
        )
    }

    suspend fun generateTotpSecret(): String {
        val secret = TotpAuthenticator.generateSecretBase32()
        prefsManager.setTotpConfig(secret, enabled = false)
        logEvent("SECURITY", "TOTP_GENERATED", "Generated new RFC-6238 TOTP secret for 2FA enrollment")
        return secret
    }

    suspend fun verifyAndEnableTotp(code: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val prefs = prefsManager.preferencesFlow.first()
        val secret = prefs.totpSecretBase32
        if (secret.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Generate a TOTP secret first"))
        }
        val localValid = TotpAuthenticator.verifyCode(secret, code)
        if (!localValid) {
            return@withContext Result.failure(IllegalArgumentException("6-digit authenticator code did not match"))
        }
        prefsManager.setTotpConfig(secret, enabled = true)
        if (prefs.authToken.isNotBlank() && prefs.serverBaseUrl.isNotBlank()) {
            NetworkClientFactory.createApiService(prefs.serverBaseUrl).getOrNull()?.let { api ->
                runCatching {
                    api.verifyTwoFactor("Bearer ${prefs.authToken}", VerifyTwoFactorRequest(code, true))
                }
            }
        }
        logEvent("SECURITY", "2FA_ENABLED", "Two-Factor Authentication (TOTP) verified and enabled")
        Result.success(true)
    }

    suspend fun disableTotp() {
        val prefs = prefsManager.preferencesFlow.first()
        prefsManager.setTotpConfig(prefs.totpSecretBase32, enabled = false)
        logEvent("SECURITY", "2FA_DISABLED", "Two-Factor Authentication disabled by user", "WARN")
    }

    suspend fun updateConsents(
        visibleIndicator: Boolean? = null,
        locationConsent: Boolean? = null,
        notificationConsent: Boolean? = null,
        clipboardSync: Boolean? = null,
        autoEncrypt: Boolean? = null,
        telemetryIntervalSec: Int? = null
    ) {
        prefsManager.setConsentFlags(
            visibleIndicator = visibleIndicator,
            locationConsent = locationConsent,
            notificationConsent = notificationConsent,
            clipboardSync = clipboardSync,
            autoEncrypt = autoEncrypt,
            telemetryIntervalSec = telemetryIntervalSec
        )
        logEvent("SECURITY", "CONSENT_UPDATED", "Updated explicit privacy & telemetry consent policy")
    }

    suspend fun createCloudPhone(
        name: String,
        androidVersion: String,
        cpuCores: Int,
        ramGb: Int,
        storageGb: Int,
        region: String,
        avdProfile: String,
        resolution: String
    ): Result<CloudPhoneEntity> = withContext(Dispatchers.IO) {
        val prefs = prefsManager.preferencesFlow.first()
        val hasServerAuth = prefs.serverBaseUrl.isNotBlank() && prefs.authToken.isNotBlank()
        var remoteId: String? = null
        var status = "LOCAL_PROFILE"
        var synced = false

        if (hasServerAuth) {
            val api = NetworkClientFactory.createApiService(prefs.serverBaseUrl).getOrNull()
            if (api != null) {
                val remoteResult = runCatching {
                    api.createDevice(
                        "Bearer ${prefs.authToken}",
                        CreateDeviceRequest(name, androidVersion, cpuCores, ramGb, storageGb, avdProfile, region)
                    )
                }
                remoteResult.onSuccess { resp ->
                    remoteId = resp.device.id
                    status = resp.device.status
                    synced = true
                }.onFailure { err ->
                    logEvent("DEVICE", "REMOTE_SYNC_WARN", "Saved locally; gateway sync failed: ${err.message}", "WARN")
                }
            }
        }

        val entity = CloudPhoneEntity(
            name = name.trim(),
            androidVersion = androidVersion,
            cpuCores = cpuCores,
            ramGb = ramGb,
            storageGb = storageGb,
            region = region,
            avdProfile = avdProfile,
            resolution = resolution,
            status = status,
            serverSynced = synced,
            remoteId = remoteId
        )
        db.cloudPhoneDao().upsert(entity)
        logEvent(
            "DEVICE",
            "CREATE_PHONE",
            if (synced) "Provisioned remote cloud phone ${entity.name}" else "Saved local cloud phone profile ${entity.name} (connect gateway to boot emulator)"
        )
        Result.success(entity)
    }

    suspend fun syncDevicesFromServer(): Result<Int> = withContext(Dispatchers.IO) {
        val prefs = prefsManager.preferencesFlow.first()
        if (prefs.serverBaseUrl.isBlank() || prefs.authToken.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Sign in to a connected control server first"))
        }
        val api = NetworkClientFactory.createApiService(prefs.serverBaseUrl).getOrElse { return@withContext Result.failure(it) }
        runCatching {
            val resp = api.listDevices("Bearer ${prefs.authToken}")
            val entities = resp.devices.map { d ->
                CloudPhoneEntity(
                    id = d.id,
                    name = d.name,
                    androidVersion = d.androidVersion,
                    cpuCores = d.cpuCores,
                    ramGb = d.ramGb,
                    storageGb = d.storageGb,
                    region = d.region ?: "default",
                    avdProfile = d.avdName ?: "Pixel_8_API_34",
                    resolution = "1080x2400",
                    status = d.status,
                    serverSynced = true,
                    remoteId = d.id,
                    createdAt = if (d.createdAt > 0L) d.createdAt else System.currentTimeMillis()
                )
            }
            db.cloudPhoneDao().upsertAll(entities)
            logEvent("DEVICE", "SYNC_DEVICES", "Synchronized ${entities.size} cloud phone nodes from gateway")
            entities.size
        }
    }

    suspend fun performDevicePowerAction(device: CloudPhoneEntity, action: String): Result<String> = withContext(Dispatchers.IO) {
        val prefs = prefsManager.preferencesFlow.first()
        val remoteId = device.remoteId
        if (prefs.serverBaseUrl.isBlank() || prefs.authToken.isBlank() || remoteId == null) {
            return@withContext Result.failure(
                IllegalStateException("Cannot $action '${device.name}': no live emulator gateway is connected. Configure and authenticate against the Node.js ADB server in User Settings.")
            )
        }
        val api = NetworkClientFactory.createApiService(prefs.serverBaseUrl).getOrElse { return@withContext Result.failure(it) }
        val bearer = "Bearer ${prefs.authToken}"
        runCatching {
            val resp = when (action) {
                "START" -> api.startDevice(bearer, remoteId)
                "STOP" -> api.stopDevice(bearer, remoteId)
                "RESTART" -> api.restartDevice(bearer, remoteId)
                else -> throw IllegalArgumentException("Unsupported action: $action")
            }
            db.cloudPhoneDao().updateStatus(device.id, resp.device.status)
            logEvent("DEVICE", "POWER_$action", "$action executed on ${device.name} -> ${resp.device.status}")
            resp.device.status
        }
    }

    suspend fun deleteCloudPhone(device: CloudPhoneEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val prefs = prefsManager.preferencesFlow.first()
        if (prefs.serverBaseUrl.isNotBlank() && prefs.authToken.isNotBlank() && device.remoteId != null) {
            NetworkClientFactory.createApiService(prefs.serverBaseUrl).getOrNull()?.let { api ->
                runCatching { api.deleteDevice("Bearer ${prefs.authToken}", device.remoteId) }
            }
        }
        db.cloudPhoneDao().deleteById(device.id)
        logEvent("DEVICE", "DELETE_PHONE", "Removed cloud phone profile ${device.name}", "WARN")
        Result.success(Unit)
    }

    suspend fun connectWebSocketForDevice(device: CloudPhoneEntity) {
        val prefs = prefsManager.preferencesFlow.first()
        val wsUrl = prefs.wsControlUrl.ifBlank { deriveWsUrl(prefs.serverBaseUrl) }
        socketClient.connect(wsUrl, prefs.authToken, device.name)
        db.deviceSessionDao().upsert(
            DeviceSessionEntity(
                deviceName = device.name,
                sessionType = "WEBSOCKET_CONTROL",
                approvalState = "APPROVED",
                endpointOrTransport = wsUrl.ifBlank { "No WS endpoint configured" }
            )
        )
        logEvent("CONTROL", "WS_CONNECT", "Initiated remote control socket for ${device.name} ($wsUrl)")
    }

    suspend fun sendRemoteCommand(type: String, params: Map<String, Any>): Boolean {
        val json = JSONObject().apply {
            put("type", type)
            params.forEach { (k, v) -> put(k, v) }
        }
        val dispatched = socketClient.sendCommand(json)
        logEvent(
            "CONTROL",
            "CMD_${type.uppercase()}",
            if (dispatched) "Sent $type $params" else "Queued/Blocked $type (WebSocket disconnected)",
            if (dispatched) "INFO" else "WARN"
        )
        return dispatched
    }

    suspend fun disconnectRemoteSessions() {
        socketClient.disconnect()
        db.deviceSessionDao().disconnectAll()
        logEvent("SECURITY", "SESSION_DISCONNECT", "Disconnected all active remote control & stream sessions", "WARN")
    }

    suspend fun generatePairingToken(deviceName: String): String {
        val code = (100000 + SecureRandom().nextInt(900000)).toString()
        prefsManager.setPairingCode(code)
        db.deviceSessionDao().upsert(
            DeviceSessionEntity(
                deviceName = deviceName,
                sessionType = "PAIRING_REQUEST",
                approvalState = "PENDING_APPROVAL",
                endpointOrTransport = "Code $code (TLS)"
            )
        )
        logEvent("SECURITY", "PAIRING_CODE", "Generated ephemeral pairing code for $deviceName")
        return code
    }

    suspend fun approveSession(sessionId: String) {
        db.deviceSessionDao().updateApprovalState(sessionId, "APPROVED")
        logEvent("SECURITY", "SESSION_APPROVED", "Explicitly approved remote session $sessionId")
    }

    suspend fun revokeSession(sessionId: String) {
        db.deviceSessionDao().updateApprovalState(sessionId, "DISCONNECTED")
        logEvent("SECURITY", "SESSION_REVOKED", "Disconnected remote session $sessionId", "WARN")
    }

    suspend fun enqueueOrUploadPickedFile(uri: Uri): Result<FileTransferEntity> = withContext(Dispatchers.IO) {
        val cr = appContext.contentResolver
        var fileName = "picked_file"
        var sizeBytes = 0L
        cr.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIdx >= 0) fileName = cursor.getString(nameIdx) ?: fileName
                if (sizeIdx >= 0) sizeBytes = cursor.getLong(sizeIdx)
            }
        }
        val mimeType = cr.getType(uri) ?: "application/octet-stream"
        val prefs = prefsManager.preferencesFlow.first()
        var status = "QUEUED_LOCAL"
        var remoteFileId: String? = null

        if (prefs.serverBaseUrl.isNotBlank() && prefs.authToken.isNotBlank()) {
            val api = NetworkClientFactory.createApiService(prefs.serverBaseUrl).getOrNull()
            if (api != null) {
                val bytes = cr.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("file", fileName, body)
                    runCatching { api.uploadFile("Bearer ${prefs.authToken}", part) }
                        .onSuccess { resp ->
                            status = "UPLOADED"
                            remoteFileId = resp.fileId
                        }
                        .onFailure {
                            status = "FAILED"
                        }
                }
            }
        }

        val item = FileTransferEntity(
            id = UUID.randomUUID().toString(),
            fileName = fileName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            uriString = uri.toString(),
            status = status,
            remoteFileId = remoteFileId
        )
        db.fileTransferDao().upsert(item)
        logEvent("FILES", "FILE_$status", "$fileName ($sizeBytes B) -> $status")
        Result.success(item)
    }

    suspend fun deleteFileRecord(id: String) {
        db.fileTransferDao().deleteById(id)
    }

    suspend fun selectSubscriptionPlan(plan: String) {
        prefsManager.setSubscriptionPlan(plan)
        logEvent("SYSTEM", "PLAN_SELECTED", "Selected infrastructure quota tier: $plan")
    }

    suspend fun logoutAllDevices(): Result<Unit> = withContext(Dispatchers.IO) {
        val prefs = prefsManager.preferencesFlow.first()
        if (prefs.serverBaseUrl.isNotBlank() && prefs.authToken.isNotBlank()) {
            NetworkClientFactory.createApiService(prefs.serverBaseUrl).getOrNull()?.let { api ->
                runCatching { api.logoutAll("Bearer ${prefs.authToken}") }
            }
        }
        socketClient.disconnect()
        db.deviceSessionDao().disconnectAll()
        prefsManager.clearSession()
        logEvent("SECURITY", "LOGOUT_ALL", "Signed out and revoked active device tokens", "WARN")
        Result.success(Unit)
    }

    private fun deriveWsUrl(httpUrl: String): String {
        val clean = httpUrl.trim().trimEnd('/')
        return when {
            clean.startsWith("https://") -> "wss://${clean.removePrefix("https://")}/ws/control"
            clean.startsWith("http://") -> "ws://${clean.removePrefix("http://")}/ws/control"
            else -> ""
        }
    }
}
