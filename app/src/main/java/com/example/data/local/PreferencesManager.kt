package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "h4xml_prefs")

data class AppPreferences(
    val serverBaseUrl: String = "",
    val wsControlUrl: String = "",
    val webrtcStunUrl: String = "stun:stun.l.google.com:19302",
    val authToken: String = "",
    val userEmail: String = "",
    val userDisplayName: String = "",
    val workspaceMode: String = "LOCKED", // LOCKED, LOCAL_ADMIN, SERVER_AUTH
    val emailVerified: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val totpSecretBase32: String = "",
    val subscriptionPlan: String = "Starter",
    val visibleIndicatorEnabled: Boolean = true,
    val locationConsentEnabled: Boolean = false,
    val notificationConsentEnabled: Boolean = false,
    val clipboardSyncEnabled: Boolean = false,
    val autoEncryptTransport: Boolean = true,
    val telemetryIntervalSec: Int = 5,
    val activePairingCode: String = ""
)

class PreferencesManager(private val context: Context) {
    private object Keys {
        val SERVER_BASE_URL = stringPreferencesKey("server_base_url")
        val WS_CONTROL_URL = stringPreferencesKey("ws_control_url")
        val WEBRTC_STUN_URL = stringPreferencesKey("webrtc_stun_url")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val WORKSPACE_MODE = stringPreferencesKey("workspace_mode")
        val EMAIL_VERIFIED = booleanPreferencesKey("email_verified")
        val TWO_FACTOR_ENABLED = booleanPreferencesKey("two_factor_enabled")
        val TOTP_SECRET_BASE32 = stringPreferencesKey("totp_secret_base32")
        val SUBSCRIPTION_PLAN = stringPreferencesKey("subscription_plan")
        val VISIBLE_INDICATOR = booleanPreferencesKey("visible_indicator")
        val LOCATION_CONSENT = booleanPreferencesKey("location_consent")
        val NOTIFICATION_CONSENT = booleanPreferencesKey("notification_consent")
        val CLIPBOARD_SYNC = booleanPreferencesKey("clipboard_sync")
        val AUTO_ENCRYPT = booleanPreferencesKey("auto_encrypt")
        val TELEMETRY_INTERVAL = intPreferencesKey("telemetry_interval")
        val PAIRING_CODE = stringPreferencesKey("active_pairing_code")
    }

    val preferencesFlow: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        AppPreferences(
            serverBaseUrl = prefs[Keys.SERVER_BASE_URL] ?: "",
            wsControlUrl = prefs[Keys.WS_CONTROL_URL] ?: "",
            webrtcStunUrl = prefs[Keys.WEBRTC_STUN_URL] ?: "stun:stun.l.google.com:19302",
            authToken = prefs[Keys.AUTH_TOKEN] ?: "",
            userEmail = prefs[Keys.USER_EMAIL] ?: "",
            userDisplayName = prefs[Keys.USER_DISPLAY_NAME] ?: "",
            workspaceMode = prefs[Keys.WORKSPACE_MODE] ?: "LOCKED",
            emailVerified = prefs[Keys.EMAIL_VERIFIED] ?: false,
            twoFactorEnabled = prefs[Keys.TWO_FACTOR_ENABLED] ?: false,
            totpSecretBase32 = prefs[Keys.TOTP_SECRET_BASE32] ?: "",
            subscriptionPlan = prefs[Keys.SUBSCRIPTION_PLAN] ?: "Starter",
            visibleIndicatorEnabled = prefs[Keys.VISIBLE_INDICATOR] ?: true,
            locationConsentEnabled = prefs[Keys.LOCATION_CONSENT] ?: false,
            notificationConsentEnabled = prefs[Keys.NOTIFICATION_CONSENT] ?: false,
            clipboardSyncEnabled = prefs[Keys.CLIPBOARD_SYNC] ?: false,
            autoEncryptTransport = prefs[Keys.AUTO_ENCRYPT] ?: true,
            telemetryIntervalSec = prefs[Keys.TELEMETRY_INTERVAL] ?: 5,
            activePairingCode = prefs[Keys.PAIRING_CODE] ?: ""
        )
    }

    suspend fun updateServerEndpoints(baseUrl: String, wsUrl: String, stunUrl: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SERVER_BASE_URL] = baseUrl.trim().trimEnd('/')
            prefs[Keys.WS_CONTROL_URL] = wsUrl.trim()
            prefs[Keys.WEBRTC_STUN_URL] = stunUrl.trim()
        }
    }

    suspend fun saveServerSession(
        token: String,
        email: String,
        displayName: String,
        emailVerified: Boolean,
        twoFactorEnabled: Boolean,
        plan: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTH_TOKEN] = token
            prefs[Keys.USER_EMAIL] = email
            prefs[Keys.USER_DISPLAY_NAME] = displayName
            prefs[Keys.EMAIL_VERIFIED] = emailVerified
            prefs[Keys.TWO_FACTOR_ENABLED] = twoFactorEnabled
            prefs[Keys.SUBSCRIPTION_PLAN] = plan
            prefs[Keys.WORKSPACE_MODE] = "SERVER_AUTH"
        }
    }

    suspend fun enterLocalDiagnosticsWorkspace(operatorName: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_DISPLAY_NAME] = operatorName.ifBlank { "Local Operator" }
            prefs[Keys.USER_EMAIL] = email.ifBlank { "local-device@offline" }
            prefs[Keys.WORKSPACE_MODE] = "LOCAL_ADMIN"
            prefs[Keys.AUTH_TOKEN] = ""
        }
    }

    suspend fun setTotpConfig(secretBase32: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOTP_SECRET_BASE32] = secretBase32
            prefs[Keys.TWO_FACTOR_ENABLED] = enabled
        }
    }

    suspend fun setConsentFlags(
        visibleIndicator: Boolean? = null,
        locationConsent: Boolean? = null,
        notificationConsent: Boolean? = null,
        clipboardSync: Boolean? = null,
        autoEncrypt: Boolean? = null,
        telemetryIntervalSec: Int? = null
    ) {
        context.dataStore.edit { prefs ->
            visibleIndicator?.let { prefs[Keys.VISIBLE_INDICATOR] = it }
            locationConsent?.let { prefs[Keys.LOCATION_CONSENT] = it }
            notificationConsent?.let { prefs[Keys.NOTIFICATION_CONSENT] = it }
            clipboardSync?.let { prefs[Keys.CLIPBOARD_SYNC] = it }
            autoEncrypt?.let { prefs[Keys.AUTO_ENCRYPT] = it }
            telemetryIntervalSec?.let { prefs[Keys.TELEMETRY_INTERVAL] = it }
        }
    }

    suspend fun setSubscriptionPlan(plan: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SUBSCRIPTION_PLAN] = plan
        }
    }

    suspend fun setPairingCode(code: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PAIRING_CODE] = code
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTH_TOKEN] = ""
            prefs[Keys.WORKSPACE_MODE] = "LOCKED"
            prefs[Keys.PAIRING_CODE] = ""
        }
    }
}
