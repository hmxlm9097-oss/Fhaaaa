package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ActivityLogEntity
import com.example.data.local.AppPreferences
import com.example.data.local.DeviceSessionEntity
import com.example.data.system.TotpAuthenticator
import com.example.ui.theme.BadgeTone
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.ConfirmAuthorizationDialog
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityLogsScreen(
    logs: List<ActivityLogEntity>,
    onClearLogs: () -> Unit,
    onCopyExport: (String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var confirmClear by remember { mutableStateOf(false) }
    val categories = listOf("ALL", "SECURITY", "CONTROL", "DEVICE", "TELEMETRY", "FILES", "SYSTEM")

    val filtered = remember(logs, selectedCategory) {
        if (selectedCategory == "ALL") logs else logs.filter { it.category == selectedCategory }
    }

    if (confirmClear) {
        ConfirmAuthorizationDialog(
            title = "Clear Audit Logs",
            message = "Clear local chronological activity logs?",
            confirmText = "Clear",
            isDestructive = true,
            onConfirm = {
                onClearLogs()
                confirmClear = false
            },
            onDismiss = { confirmClear = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Activity Logs",
                subtitle = "${filtered.size} immutable event record(s)",
                actionLabel = "Export",
                onActionClick = {
                    val exportText = filtered.take(50).joinToString("\n") {
                        "[${Date(it.timestamp)}] ${it.category}/${it.action}: ${it.detail}"
                    }
                    onCopyExport("H4XML Audit Logs", exportText.ifBlank { "No events recorded." })
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CobaltContainer,
                                selectedLabelColor = NeonCyan
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { confirmClear = true },
                    enabled = logs.isNotEmpty(),
                    modifier = Modifier.heightIn(min = 48.dp).testTag("clear_logs_button")
                ) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear logs", tint = CrimsonAlert)
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                GlassCard {
                    Text("No events in this category yet.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        } else {
            val fmt = SimpleDateFormat("MMM dd HH:mm:ss", Locale.US)
            items(filtered, key = { it.id }) { log ->
                GlassCard(contentPadding = PaddingValues(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(
                            text = "${log.category} • ${log.action}",
                            tone = when (log.severity) {
                                "ALERT" -> BadgeTone.ALERT
                                "WARN" -> BadgeTone.PENDING
                                else -> BadgeTone.INFO
                            }
                        )
                        Text(
                            text = fmt.format(Date(log.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(log.detail, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
fun SecurityCenterScreen(
    preferences: AppPreferences,
    sessions: List<DeviceSessionEntity>,
    onGenerateTotp: () -> Unit,
    onVerifyTotp: (String) -> Unit,
    onDisableTotp: () -> Unit,
    onGeneratePairingCode: (String) -> Unit,
    onApproveSession: (String) -> Unit,
    onRevokeSession: (String) -> Unit,
    onDisconnectAllSessions: () -> Unit,
    onToggleVisibleIndicator: (Boolean) -> Unit,
    onToggleAutoEncrypt: (Boolean) -> Unit,
    onCopyText: (String, String) -> Unit,
    onLogoutAllDevices: () -> Unit
) {
    var totpCodeInput by remember { mutableStateOf("") }
    var currentTotpPreview by remember { mutableStateOf("------") }
    var remainingSec by remember { mutableIntStateOf(30) }
    var confirmLogoutAll by remember { mutableStateOf(false) }

    LaunchedEffect(preferences.totpSecretBase32) {
        while (true) {
            if (preferences.totpSecretBase32.isNotBlank()) {
                currentTotpPreview = TotpAuthenticator.currentCode(preferences.totpSecretBase32)
                remainingSec = TotpAuthenticator.remainingSeconds()
            } else {
                currentTotpPreview = "------"
            }
            delay(1000L)
        }
    }

    if (confirmLogoutAll) {
        ConfirmAuthorizationDialog(
            title = "Logout From All Devices",
            message = "Revoke all active session tokens on the gateway and disconnect every remote WebSocket tunnel?",
            confirmText = "Logout All",
            isDestructive = true,
            onConfirm = {
                onLogoutAllDevices()
                confirmLogoutAll = false
            },
            onDismiss = { confirmLogoutAll = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Security Center",
                subtitle = "2FA TOTP, pairing approvals, & session revocation"
            )
        }

        item {
            GlassCard(accentBorder = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.img_security_vault),
                        contentDescription = "Zero Trust Security Shield",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        StatusBadge(
                            text = if (preferences.twoFactorEnabled) "2FA TOTP ENFORCED" else "2FA RECOMMENDED",
                            tone = if (preferences.twoFactorEnabled) BadgeTone.ONLINE else BadgeTone.PENDING
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Account: ${preferences.userEmail.ifBlank { "Local Operator" }}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Email Verification: ${if (preferences.emailVerified) "Verified" else "Pending Gateway Verification"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item {
            GlassCard {
                Text("Two-Factor Authentication (RFC-6238 TOTP)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (preferences.totpSecretBase32.isBlank()) {
                        "Generate a cryptographic TOTP key to bind an Authenticator app."
                    } else {
                        "Active TOTP Code: $currentTotpPreview (${remainingSec}s remaining)"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan
                )
                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onGenerateTotp,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("security_generate_totp_button")
                    ) {
                        Icon(Icons.Rounded.Key, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("New 2FA Key", color = NeonCyan)
                    }
                    if (preferences.totpSecretBase32.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                val uri = TotpAuthenticator.buildOtpAuthUri(preferences.userEmail, preferences.totpSecretBase32)
                                onCopyText("TOTP Auth URI", uri)
                            },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("security_copy_totp_uri_button")
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy URI", tint = NeonCyan)
                        }
                    }
                }

                if (preferences.totpSecretBase32.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = totpCodeInput,
                            onValueChange = { totpCodeInput = it },
                            label = { Text("Enter 6-digit code") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f).testTag("security_verify_totp_input")
                        )
                        Button(
                            onClick = { onVerifyTotp(totpCodeInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                            modifier = Modifier.heightIn(min = 52.dp).testTag("security_verify_totp_button")
                        ) {
                            Text("Verify")
                        }
                    }
                    if (preferences.twoFactorEnabled) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onDisableTotp,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) {
                            Text("Disable 2FA", color = CrimsonAlert)
                        }
                    }
                }
            }
        }

        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Visible Monitoring Indicator", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text("Always display live banner when telemetry or control is active.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Switch(
                        checked = preferences.visibleIndicatorEnabled,
                        onCheckedChange = onToggleVisibleIndicator,
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = CobaltContainer)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enforce TLS / WSS Encryption", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text("Prefer encrypted HTTPS/WSS transport outside local emulator debug.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Switch(
                        checked = preferences.autoEncryptTransport,
                        onCheckedChange = onToggleAutoEncrypt,
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = CobaltContainer)
                    )
                }
            }
        }

        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Secure Device Pairing & Session Approvals", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            text = if (preferences.activePairingCode.isNotBlank()) {
                                "Active Ephemeral Code: ${preferences.activePairingCode}"
                            } else "Generate a 6-digit code to pair an authorized cloud device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonCyan
                        )
                    }
                    OutlinedButton(
                        onClick = { onGeneratePairingCode("Authorized-Node") },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("security_generate_pairing_button")
                    ) {
                        Icon(Icons.Rounded.QrCode2, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Pair", color = NeonCyan)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDisconnectAllSessions,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("security_disconnect_all_button")
                    ) {
                        Icon(Icons.Rounded.LinkOff, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Disconnect Sessions", color = CrimsonAlert)
                    }
                    Button(
                        onClick = { confirmLogoutAll = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("security_logout_all_button")
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Logout All")
                    }
                }
            }
        }

        items(sessions, key = { it.id }) { session ->
            GlassCard(contentPadding = PaddingValues(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${session.deviceName} • ${session.sessionType}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(session.endpointOrTransport, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    StatusBadge(
                        text = session.approvalState,
                        tone = when (session.approvalState) {
                            "APPROVED" -> BadgeTone.ONLINE
                            "PENDING_APPROVAL" -> BadgeTone.PENDING
                            else -> BadgeTone.MUTED
                        }
                    )
                }
                if (session.approvalState != "DISCONNECTED") {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (session.approvalState == "PENDING_APPROVAL") {
                            Button(
                                onClick = { onApproveSession(session.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldOnline),
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Approve", color = TextPrimary)
                            }
                        }
                        OutlinedButton(
                            onClick = { onRevokeSession(session.id) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Revoke", color = CrimsonAlert)
                        }
                    }
                }
            }
        }
    }
}
