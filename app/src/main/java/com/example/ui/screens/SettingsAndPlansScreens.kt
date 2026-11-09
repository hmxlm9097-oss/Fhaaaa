package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.AppPreferences
import com.example.data.remote.HealthResponse
import com.example.ui.theme.BadgeTone
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SectionHeader
import com.example.ui.theme.StatusBadge
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun UserSettingsScreen(
    preferences: AppPreferences,
    serverHealth: HealthResponse?,
    isBusy: Boolean,
    onSaveEndpoints: (baseUrl: String, wsUrl: String, stunUrl: String) -> Unit,
    onTestConnection: (baseUrl: String) -> Unit,
    onUpdateTelemetryInterval: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var baseUrl by remember(preferences.serverBaseUrl) {
        mutableStateOf(preferences.serverBaseUrl.ifBlank { "http://10.0.2.2:4000" })
    }
    var wsUrl by remember(preferences.wsControlUrl) {
        mutableStateOf(preferences.wsControlUrl.ifBlank { "ws://10.0.2.2:4000/ws/control" })
    }
    var stunUrl by remember(preferences.webrtcStunUrl) {
        mutableStateOf(preferences.webrtcStunUrl)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NeonCyan,
        unfocusedBorderColor = GlassBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "User Settings & Infrastructure",
                subtitle = "Node.js API, WebSocket tunnel & WebRTC ICE config"
            )
        }

        item {
            GlassCard(accentBorder = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Control Plane Gateway", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    StatusBadge(
                        text = if (serverHealth != null) "CONNECTED" else "NOT CONNECTED",
                        tone = if (serverHealth != null) BadgeTone.ONLINE else BadgeTone.PENDING
                    )
                }
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("REST API Base URL") },
                    leadingIcon = { Icon(Icons.Rounded.Dns, contentDescription = null, tint = NeonCyan) },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().testTag("settings_base_url_input")
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = wsUrl,
                    onValueChange = { wsUrl = it },
                    label = { Text("WebSocket Control URL") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().testTag("settings_ws_url_input")
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = stunUrl,
                    onValueChange = { stunUrl = it },
                    label = { Text("WebRTC STUN / TURN Server") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().testTag("settings_stun_url_input")
                )

                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onTestConnection(baseUrl) },
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("settings_test_connection_button")
                    ) {
                        Text("Test Health", color = NeonCyan)
                    }
                    Button(
                        onClick = { onSaveEndpoints(baseUrl, wsUrl, stunUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("settings_save_endpoints_button")
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save URLs")
                    }
                }
            }
        }

        item {
            GlassCard {
                Text("Local Telemetry Refresh Interval", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 5, 15, 30).forEach { sec ->
                        FilterChip(
                            selected = preferences.telemetryIntervalSec == sec,
                            onClick = { onUpdateTelemetryInterval(sec) },
                            label = { Text("${sec}s") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CobaltContainer,
                                selectedLabelColor = NeonCyan
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
            }
        }

        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Terminal, contentDescription = null, tint = NeonCyan)
                    Spacer(Modifier.width(8.dp))
                    Text("Cloud Emulator Deployment Blueprint", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "1. Provision a KVM-enabled Linux host with Android SDK Platform-Tools (adb) & Android Emulator AVDs.\n" +
                        "2. Deploy backend/src/server.js (Node.js 20+ Express + ws WebSocket + Multer + JWT + Speakeasy 2FA).\n" +
                        "3. Put Nginx/Caddy TLS proxy + coturn TURN server in front of port 4000.\n" +
                        "4. Optional Firebase Sync: Firebase setup was declined on this build; to enable cloud Firestore/Auth/Storage later, link google-services.json and deploy backend/firestore.rules in Firebase Console.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("settings_logout_button")
            ) {
                Icon(Icons.Rounded.Logout, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out & Lock Workspace", color = CrimsonAlert)
            }
        }
    }
}

private data class PlanTier(
    val name: String,
    val priceLabel: String,
    val quota: String,
    val highlights: List<String>
)

@Composable
fun SubscriptionPlansScreen(
    activePlan: String,
    onSelectPlan: (String) -> Unit
) {
    val tiers = remember {
        listOf(
            PlanTier(
                name = "Starter",
                priceLabel = "$0 / Local & Self-Hosted",
                quota = "1 Virtual Profile • Local Telemetry & Self-Hosted Gateway",
                highlights = listOf(
                    "Full Android device diagnostics (Battery, Storage, Network)",
                    "Self-hosted Node.js + ADB WebSocket gateway support",
                    "RFC-6238 TOTP 2FA & zero-permission File Picker"
                )
            ),
            PlanTier(
                name = "Pro",
                priceLabel = "$29 / mo (Cloud Compute Quota)",
                quota = "8 Virtual Phones • 4 vCPU / 8 GB RAM per Node",
                highlights = listOf(
                    "Multi-node fleet sync & WebRTC HD TURN relay",
                    "Authorized remote screen recording & APK deployment",
                    "Extended 90-day immutable audit log retention"
                )
            ),
            PlanTier(
                name = "Enterprise",
                priceLabel = "$119 / mo (Dedicated KVM Cluster)",
                quota = "32+ Virtual Phones • Dedicated AVD Host & RBAC",
                highlights = listOf(
                    "Dedicated Linux KVM + Redroid GPU acceleration",
                    "Hardware-backed TLS & custom coturn cluster",
                    "Full compliance audit export & multi-session governance"
                )
            )
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Subscription Plans",
                subtitle = "Infrastructure compute tiers & node quotas"
            )
        }

        item {
            Text(
                text = "Active quota selection is stored locally and synced when connected to a backend billing endpoint. No payment card data is collected in this client.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }

        tiers.forEach { tier ->
            item(key = tier.name) {
                val isSelected = activePlan == tier.name
                GlassCard(accentBorder = isSelected) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tier.name, style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                            Text(tier.priceLabel, style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                        }
                        if (isSelected) {
                            StatusBadge(text = "SELECTED", tone = BadgeTone.ONLINE)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(tier.quota, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        tier.highlights.forEach { bullet ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(bullet, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onSelectPlan(tier.name) },
                        enabled = !isSelected,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) EmeraldOnline else CobaltPrimary),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("select_plan_${tier.name.lowercase()}")
                    ) {
                        Text(if (isSelected) "Current Plan" else "Select ${tier.name} Tier")
                    }
                }
            }
        }
    }
}
