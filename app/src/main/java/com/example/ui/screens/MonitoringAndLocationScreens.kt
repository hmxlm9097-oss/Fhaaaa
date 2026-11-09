package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NetworkWifi
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.AppPreferences
import com.example.data.local.LocationHistoryEntity
import com.example.data.system.LiveDeviceTelemetry
import com.example.ui.theme.BadgeTone
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.ConfirmAuthorizationDialog
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SectionHeader
import com.example.ui.theme.StatusBadge
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VisibleMonitoringBanner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeviceMonitoringScreen(
    telemetry: LiveDeviceTelemetry,
    preferences: AppPreferences,
    onRefreshTelemetry: () -> Unit,
    onToggleNotificationConsent: (Boolean) -> Unit,
    onToggleVisibleIndicator: (Boolean) -> Unit,
    onLogSystemSettingsOpen: (String) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            VisibleMonitoringBanner(
                isMonitoringActive = preferences.visibleIndicatorEnabled,
                isStreamActive = false,
                onDisconnectClick = { onToggleVisibleIndicator(false) }
            )
        }

        item {
            SectionHeader(
                title = "Device Monitoring",
                subtitle = "Live Android hardware telemetry & permission boundaries",
                actionLabel = "Poll Now",
                onActionClick = onRefreshTelemetry
            )
        }

        item {
            GlassCard(accentBorder = true) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null, tint = EmeraldOnline)
                        Spacer(Modifier.width(8.dp))
                        Text("Battery & Power", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    StatusBadge(text = "${telemetry.batteryPercent}%", tone = BadgeTone.ONLINE)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (telemetry.batteryPercent / 100f).coerceIn(0f, 1f) },
                    color = EmeraldOnline,
                    trackColor = GlassBorder,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Source: ${telemetry.batteryChargeSource} • Temp: ${telemetry.batteryTempCelsius}°C • Voltage: ${telemetry.batteryVoltageMv} mV • Health: ${telemetry.batteryHealthLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            val storageFraction = if (telemetry.storageTotalGb > 0f) {
                (telemetry.storageUsedGb / telemetry.storageTotalGb).coerceIn(0f, 1f)
            } else 0f
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Storage, contentDescription = null, tint = NeonCyan)
                        Spacer(Modifier.width(8.dp))
                        Text("Internal Storage", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    StatusBadge(
                        text = String.format(Locale.US, "%.1f GB FREE", telemetry.storageFreeGb),
                        tone = BadgeTone.INFO
                    )
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { storageFraction },
                    color = NeonCyan,
                    trackColor = GlassBorder,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = String.format(Locale.US, "%.2f GB used of %.2f GB total (/data partition)", telemetry.storageUsedGb, telemetry.storageTotalGb),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.NetworkWifi, contentDescription = null, tint = CobaltPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Network Diagnostics", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    }
                    StatusBadge(
                        text = if (telemetry.isNetworkConnected) telemetry.networkTransport.uppercase() else "OFFLINE",
                        tone = if (telemetry.isNetworkConnected) BadgeTone.ONLINE else BadgeTone.ALERT
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Metered: ${if (telemetry.isUnmetered) "No (Unmetered)" else "Yes / Restricted"} • Downstream: ${telemetry.downstreamKbps} kbps • Upstream: ${telemetry.upstreamKbps} kbps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            SectionHeader(
                title = "Permission-Gated & External Feeds",
                subtitle = "No covert scraping • Explicit OS role or Cloud Agent required"
            )
        }

        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = NeonCyan)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("App Notification Bridge (WhatsApp / Messenger / Instagram)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Text(
                                text = if (telemetry.notificationListenerGranted) {
                                    "OS Notification Listener permission granted by user."
                                } else {
                                    "Requires explicit user toggle in Android Notification Access settings or managed Cloud Emulator agent."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = preferences.notificationConsentEnabled,
                        onCheckedChange = onToggleNotificationConsent,
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = CobaltContainer),
                        modifier = Modifier.testTag("monitoring_notification_consent_switch")
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        onLogSystemSettingsOpen("Opened Android Notification Listener Settings")
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("open_notification_listener_settings_button")
                ) {
                    Text("Manage Android Notification Access", color = NeonCyan)
                }
            }
        }

        item {
            val restrictedItems = listOf(
                Triple("Call History Feed", "Requires Default Phone/Dialer Role or Cloud Emulator ADB Agent (Play Policy restricted)", Icons.Rounded.Call),
                Triple("SMS Access Feed", "Requires explicit Default SMS Role + user authorization (Play Policy restricted)", Icons.Rounded.Sms),
                Triple("Browser History Feed", "Requires Cloud Emulator accessibility agent or authorized browser extension (removed from Android SDK API 23+)", Icons.Rounded.Language)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                restrictedItems.forEach { (title, reason, icon) ->
                    GlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(icon, contentDescription = title, tint = TextSecondary)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                    Text(reason, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            StatusBadge(text = "AGENT / ROLE REQ", tone = BadgeTone.PENDING)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationDashboardScreen(
    preferences: AppPreferences,
    hasLocationPermission: Boolean,
    locationHistory: List<LocationHistoryEntity>,
    onToggleLocationConsent: (Boolean) -> Unit,
    onCaptureLocation: () -> Unit,
    onClearHistory: () -> Unit,
    onLogPermissionResult: (String) -> Unit
) {
    var permGranted by remember(hasLocationPermission) { mutableStateOf(hasLocationPermission) }
    var confirmWipe by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        permGranted = granted
        onLogPermissionResult(if (granted) "Android location permission granted" else "Android location permission denied")
        if (granted) onCaptureLocation()
    }

    if (confirmWipe) {
        ConfirmAuthorizationDialog(
            title = "Clear Location History",
            message = "Delete all locally stored consented GPS coordinates?",
            confirmText = "Clear History",
            isDestructive = true,
            onConfirm = {
                onClearHistory()
                confirmWipe = false
            },
            onDismiss = { confirmWipe = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Location Dashboard",
                subtitle = "Foreground GPS capture with explicit user consent"
            )
        }

        item {
            GlassCard(accentBorder = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Explicit Location Consent", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            text = "Coordinates are captured only when consent is ON and Android permission is granted.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = preferences.locationConsentEnabled,
                        onCheckedChange = onToggleLocationConsent,
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = CobaltContainer),
                        modifier = Modifier.testTag("location_consent_switch")
                    )
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(
                        text = if (preferences.locationConsentEnabled) "CONSENT ON" else "CONSENT OFF",
                        tone = if (preferences.locationConsentEnabled) BadgeTone.ONLINE else BadgeTone.MUTED
                    )
                    StatusBadge(
                        text = if (permGranted) "OS PERMISSION GRANTED" else "PERMISSION NEEDED",
                        tone = if (permGranted) BadgeTone.INFO else BadgeTone.PENDING
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (!permGranted) {
                                launcher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                )
                            } else {
                                onCaptureLocation()
                            }
                        },
                        enabled = preferences.locationConsentEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("location_capture_button")
                    ) {
                        Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (permGranted) "Capture GPS Fix" else "Grant & Capture")
                    }

                    OutlinedButton(
                        onClick = { confirmWipe = true },
                        enabled = locationHistory.isNotEmpty(),
                        modifier = Modifier.heightIn(min = 48.dp).testTag("location_clear_button")
                    ) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear history", tint = CrimsonAlert)
                    }
                }
            }
        }

        item {
            GlassCard {
                Text("Telemetry Coordinate Plot", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianBlack)
                        .border(1.dp, GlassBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        listOf(0.25f, 0.55f, 0.85f).forEach { r ->
                            drawCircle(
                                color = GlassBorder.copy(alpha = 0.5f),
                                radius = (size.minDimension / 2f) * r,
                                center = Offset(cx, cy),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                            )
                        }
                        drawLine(GlassBorder.copy(alpha = 0.4f), Offset(0f, cy), Offset(size.width, cy), 1f)
                        drawLine(GlassBorder.copy(alpha = 0.4f), Offset(cx, 0f), Offset(cx, size.height), 1f)
                        if (locationHistory.isNotEmpty()) {
                            drawCircle(NeonCyan.copy(alpha = 0.25f), radius = 18.dp.toPx(), center = Offset(cx, cy))
                            drawCircle(EmeraldOnline, radius = 6.dp.toPx(), center = Offset(cx, cy))
                        }
                    }
                    if (locationHistory.isEmpty()) {
                        Text(
                            text = "No authorized GPS points recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    } else {
                        val latest = locationHistory.first()
                        Text(
                            text = String.format(Locale.US, "%.5f, %.5f (±%.0fm)", latest.latitude, latest.longitude, latest.accuracyMeters),
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonCyan,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)
                        )
                    }
                }
            }
        }

        items(locationHistory, key = { it.id }) { entry ->
            val fmt = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.US)
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = String.format(Locale.US, "%.6f, %.6f", entry.latitude, entry.longitude),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Provider: ${entry.provider} • Accuracy: ±${entry.accuracyMeters.toInt()}m",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = fmt.format(Date(entry.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan
                    )
                }
            }
        }
    }
}
