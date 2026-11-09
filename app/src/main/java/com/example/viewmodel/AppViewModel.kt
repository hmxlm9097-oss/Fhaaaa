package com.example.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ActivityLogEntity
import com.example.data.local.AppPreferences
import com.example.data.local.CloudPhoneEntity
import com.example.data.local.DeviceSessionEntity
import com.example.data.local.FileTransferEntity
import com.example.data.local.LocationHistoryEntity
import com.example.data.remote.HealthResponse
import com.example.data.remote.RemoteSocketTelemetry
import com.example.data.system.InstalledAppItem
import com.example.data.system.LiveDeviceTelemetry
import com.example.data.repository.CloudPhoneRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ActionBanner(
    val message: String,
    val isError: Boolean = false
)

class AppViewModel(private val repository: CloudPhoneRepository) : ViewModel() {

    val preferences: StateFlow<AppPreferences> = repository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())

    val cloudPhones: StateFlow<List<CloudPhoneEntity>> = repository.cloudPhonesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityLogs: StateFlow<List<ActivityLogEntity>> = repository.activityLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locationHistory: StateFlow<List<LocationHistoryEntity>> = repository.locationHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fileTransfers: StateFlow<List<FileTransferEntity>> = repository.fileTransfersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<DeviceSessionEntity>> = repository.sessionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val socketTelemetry: StateFlow<RemoteSocketTelemetry> = repository.socketTelemetryFlow

    private val _liveTelemetry = MutableStateFlow(repository.captureTelemetrySnapshot())
    val liveTelemetry: StateFlow<LiveDeviceTelemetry> = _liveTelemetry.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private val _selectedDeviceId = MutableStateFlow<String?>(null)
    val selectedDeviceId: StateFlow<String?> = _selectedDeviceId.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _banner = MutableStateFlow<ActionBanner?>(null)
    val banner: StateFlow<ActionBanner?> = _banner.asStateFlow()

    private val _serverHealth = MutableStateFlow<HealthResponse?>(null)
    val serverHealth: StateFlow<HealthResponse?> = _serverHealth.asStateFlow()

    private val _isRecordingRemote = MutableStateFlow(false)
    val isRecordingRemote: StateFlow<Boolean> = _isRecordingRemote.asStateFlow()

    private val _isLandscapeStream = MutableStateFlow(false)
    val isLandscapeStream: StateFlow<Boolean> = _isLandscapeStream.asStateFlow()

    private val _clipboardPreview = MutableStateFlow("")
    val clipboardPreview: StateFlow<String> = _clipboardPreview.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                _liveTelemetry.value = repository.captureTelemetrySnapshot()
                val intervalSec = preferences.value.telemetryIntervalSec.coerceIn(2, 60)
                delay(intervalSec * 1000L)
            }
        }
        refreshInstalledApps()
    }

    fun dismissBanner() {
        _banner.value = null
    }

    private fun showBanner(message: String, isError: Boolean = false) {
        _banner.value = ActionBanner(message, isError)
    }

    fun selectDevice(id: String) {
        _selectedDeviceId.value = id
    }

    fun refreshTelemetryNow() {
        _liveTelemetry.value = repository.captureTelemetrySnapshot()
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = repository.loadInstalledApps()
        }
    }

    fun launchLocalApp(packageName: String, appName: String) {
        val launched = repository.launchLocalApp(packageName)
        viewModelScope.launch {
            repository.logEvent("CONTROL", "LAUNCH_LOCAL_APP", "Launch $appName ($packageName): $launched")
        }
        if (!launched) showBanner("Unable to launch $appName on this device", isError = true)
    }

    fun testServerConnection(baseUrl: String) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.testServerConnection(baseUrl).fold(
                onSuccess = { health ->
                    _serverHealth.value = health
                    showBanner("Connected! ADB=${health.adbAvailable}, Active Nodes=${health.activeDeviceCount}")
                },
                onFailure = { err ->
                    _serverHealth.value = null
                    showBanner("Connection failed: ${err.localizedMessage ?: "Unreachable"}", isError = true)
                }
            )
            _isBusy.value = false
        }
    }

    fun saveEndpoints(baseUrl: String, wsUrl: String, stunUrl: String) {
        viewModelScope.launch {
            repository.saveServerEndpoints(baseUrl, wsUrl, stunUrl)
            showBanner("Control gateway endpoints saved")
        }
    }

    fun registerOnServer(baseUrl: String, email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.registerOnServer(baseUrl, email, password, displayName).fold(
                onSuccess = { showBanner("Account created & authenticated on gateway") },
                onFailure = { showBanner(it.localizedMessage ?: "Registration failed", isError = true) }
            )
            _isBusy.value = false
        }
    }

    fun loginOnServer(baseUrl: String, email: String, password: String, totpCode: String?) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.loginOnServer(baseUrl, email, password, totpCode).fold(
                onSuccess = { showBanner("Signed in as ${it.user.email}") },
                onFailure = { showBanner(it.localizedMessage ?: "Authentication failed", isError = true) }
            )
            _isBusy.value = false
        }
    }

    fun enterLocalWorkspace(operatorName: String, email: String) {
        viewModelScope.launch {
            repository.enterLocalWorkspace(operatorName, email)
        }
    }

    fun createCloudPhone(
        name: String,
        androidVersion: String,
        cpuCores: Int,
        ramGb: Int,
        storageGb: Int,
        region: String,
        avdProfile: String,
        resolution: String,
        onCreated: (String) -> Unit
    ) {
        if (name.isBlank()) {
            showBanner("Enter a cloud phone name", isError = true)
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            repository.createCloudPhone(name, androidVersion, cpuCores, ramGb, storageGb, region, avdProfile, resolution)
                .fold(
                    onSuccess = { device ->
                        _selectedDeviceId.value = device.id
                        showBanner(
                            if (device.serverSynced) "Cloud phone '${device.name}' created on gateway"
                            else "Profile '${device.name}' saved locally (connect gateway to boot)"
                        )
                        onCreated(device.id)
                    },
                    onFailure = { showBanner(it.localizedMessage ?: "Failed to create profile", isError = true) }
                )
            _isBusy.value = false
        }
    }

    fun syncDevicesFromServer() {
        viewModelScope.launch {
            _isBusy.value = true
            repository.syncDevicesFromServer().fold(
                onSuccess = { count -> showBanner("Synced $count cloud phone(s) from gateway") },
                onFailure = { showBanner(it.localizedMessage ?: "Sync unavailable", isError = true) }
            )
            _isBusy.value = false
        }
    }

    fun performPowerAction(device: CloudPhoneEntity, action: String) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.performDevicePowerAction(device, action).fold(
                onSuccess = { status -> showBanner("${device.name}: $action -> $status") },
                onFailure = { showBanner(it.localizedMessage ?: "Action failed", isError = true) }
            )
            _isBusy.value = false
        }
    }

    fun deleteCloudPhone(device: CloudPhoneEntity) {
        viewModelScope.launch {
            repository.deleteCloudPhone(device)
            if (_selectedDeviceId.value == device.id) _selectedDeviceId.value = null
            showBanner("Deleted ${device.name}")
        }
    }

    fun connectRemoteControl(device: CloudPhoneEntity) {
        viewModelScope.launch {
            _selectedDeviceId.value = device.id
            repository.connectWebSocketForDevice(device)
        }
    }

    fun sendControlCommand(type: String, params: Map<String, Any> = emptyMap()) {
        viewModelScope.launch {
            val ok = repository.sendRemoteCommand(type, params)
            if (!ok) {
                showBanner("WebSocket offline — command '$type' requires connected control gateway", isError = true)
            }
        }
    }

    fun toggleScreenRotation() {
        val next = !_isLandscapeStream.value
        _isLandscapeStream.value = next
        sendControlCommand("keyevent", mapOf("keycode" to 27, "orientation" to if (next) "LANDSCAPE" else "PORTRAIT"))
    }

    fun toggleRemoteRecording() {
        val next = !_isRecordingRemote.value
        _isRecordingRemote.value = next
        sendControlCommand(if (next) "start_recording" else "stop_recording", emptyMap())
    }

    fun syncClipboardFromLocal() {
        val text = repository.readLocalClipboard()
        _clipboardPreview.value = text
        if (text.isBlank()) {
            showBanner("Local Android clipboard is empty", isError = true)
        } else {
            sendControlCommand("clipboard_push", mapOf("text" to text))
        }
    }

    fun copyToLocalClipboard(label: String, text: String) {
        repository.writeLocalClipboard(label, text)
        _clipboardPreview.value = text
        showBanner("Copied '$label' to Android clipboard")
    }

    fun disconnectAllRemoteSessions() {
        viewModelScope.launch {
            repository.disconnectRemoteSessions()
            _isRecordingRemote.value = false
            showBanner("Remote sessions disconnected")
        }
    }

    fun captureLocation() {
        viewModelScope.launch {
            repository.captureAndSaveLocation().fold(
                onSuccess = { loc ->
                    showBanner("Captured GPS fix: ${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)}")
                },
                onFailure = { showBanner(it.localizedMessage ?: "Location capture unavailable", isError = true) }
            )
        }
    }

    fun clearLocationHistory() {
        viewModelScope.launch {
            repository.clearLocationHistory()
            showBanner("Cleared location history")
        }
    }

    fun hasLocationPermission(): Boolean = repository.hasLocationPermission()

    fun enqueuePickedFile(uri: Uri) {
        viewModelScope.launch {
            repository.enqueueOrUploadPickedFile(uri).fold(
                onSuccess = { item ->
                    showBanner(
                        if (item.status == "UPLOADED") "Uploaded ${item.fileName}"
                        else "Queued '${item.fileName}' locally (connect gateway to upload)"
                    )
                },
                onFailure = { showBanner("Failed to read picked file", isError = true) }
            )
        }
    }

    fun deleteFileRecord(id: String) {
        viewModelScope.launch { repository.deleteFileRecord(id) }
    }

    fun updateConsents(
        visibleIndicator: Boolean? = null,
        locationConsent: Boolean? = null,
        notificationConsent: Boolean? = null,
        clipboardSync: Boolean? = null,
        autoEncrypt: Boolean? = null,
        telemetryIntervalSec: Int? = null
    ) {
        viewModelScope.launch {
            repository.updateConsents(
                visibleIndicator = visibleIndicator,
                locationConsent = locationConsent,
                notificationConsent = notificationConsent,
                clipboardSync = clipboardSync,
                autoEncrypt = autoEncrypt,
                telemetryIntervalSec = telemetryIntervalSec
            )
        }
    }

    fun generateTotpSecret() {
        viewModelScope.launch {
            repository.generateTotpSecret()
            showBanner("Generated new 2FA secret key")
        }
    }

    fun verifyTotp(code: String) {
        viewModelScope.launch {
            repository.verifyAndEnableTotp(code).fold(
                onSuccess = { showBanner("2FA TOTP enabled & verified") },
                onFailure = { showBanner(it.localizedMessage ?: "Invalid TOTP code", isError = true) }
            )
        }
    }

    fun disableTotp() {
        viewModelScope.launch {
            repository.disableTotp()
            showBanner("2FA disabled")
        }
    }

    fun generatePairingCode(deviceName: String) {
        viewModelScope.launch {
            val code = repository.generatePairingToken(deviceName)
            showBanner("Pairing code $code created (awaiting approval)")
        }
    }

    fun approveSession(id: String) {
        viewModelScope.launch {
            repository.approveSession(id)
            showBanner("Session approved")
        }
    }

    fun revokeSession(id: String) {
        viewModelScope.launch {
            repository.revokeSession(id)
            showBanner("Session disconnected")
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            showBanner("Cleared audit log history")
        }
    }

    fun selectSubscriptionPlan(plan: String) {
        viewModelScope.launch {
            repository.selectSubscriptionPlan(plan)
            showBanner("Selected $plan infrastructure tier")
        }
    }

    fun logoutAllDevices() {
        viewModelScope.launch {
            repository.logoutAllDevices()
            showBanner("Signed out from all devices")
        }
    }

    fun logPermissionAttempt(category: String, action: String, detail: String) {
        viewModelScope.launch {
            repository.logEvent(category, action, detail)
        }
    }

    class Factory(private val repository: CloudPhoneRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppViewModel(repository) as T
        }
    }
}
