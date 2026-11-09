package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.NetworkWifi
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ActivityLogEntity
import com.example.data.local.AppPreferences
import com.example.data.local.CloudPhoneEntity
import com.example.data.remote.HealthResponse
import com.example.data.remote.RemoteSocketTelemetry
import com.example.data.remote.SocketStatus
import com.example.data.system.LiveDeviceTelemetry
import com.example.ui.navigation.AllDashboardPages
import com.example.ui.navigation.NavRoutes
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BadgeTone
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.GlassCard
import com.example.ui.theme.MetricTile
import com.example.ui.theme.NeonCyan
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
fun DashboardScreen(
    preferences: AppPreferences,
    telemetry: LiveDeviceTelemetry,
    cloudPhones: List<CloudPhoneEntity>,
    recentLogs: List<ActivityLogEntity>,
    serverHealth: HealthResponse?,
    socketTelemetry: RemoteSocketTelemetry,
    onNavigate: (String) -> Unit,
    onRefreshTelemetry: () -> Unit,
    onPauseMonitoring: () -> Unit
) {
    val isServerAuth = preferences.workspaceMode == "SERVER_AUTH" && preferences.authToken.isNotBlank()
    val isSocketConnected = socketTelemetry.status is SocketStatus.Connected

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            VisibleMonitoringBanner(
                isMonitoringActive = preferences.visibleIndicatorEnabled,
                isStreamActive = isSocketConnected,
                onDisconnectClick = onPauseMonitoring
            )
        }

        item {
            GlassCard(accentBorder = true, contentPadding = PaddingValues(0.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(152.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "H4XML Cloud Infrastructure Hero",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            StatusBadge(
                                text = if (isServerAuth) "GATEWAY AUTHENTICATED" else "LOCAL WORKSPACE MODE",
                                tone = if (isServerAuth) BadgeTone.ONLINE else BadgeTone.PENDING
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Welcome, ${preferences.userDisplayName.ifBlank { "Operator" }}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${preferences.subscriptionPlan} Tier • ${cloudPhones.size} Virtual Node(s)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonCyan
                            )
                        }
                        Icon(
                            imageVector = if (serverHealth != null) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                            contentDescription = "Gateway connection indicator",
                            tint = if (serverHealth != null) EmeraldOnline else AmberPending,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (serverHealth != null) {
                            "Gateway online • ADB=${serverHealth.adbAvailable} • ${serverHealth.activeDeviceCount} active cloud node(s)."
                        } else {
                            "External Node.js + ADB Emulator Gateway not connected yet. Configure your server URL in User Settings or create local virtual phone profiles below."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(NavRoutes.CREATE_CLOUD_PHONE) },
                            colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("dashboard_create_phone_button")
                        ) {
                            Icon(Icons.Rounded.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Create Phone")
                        }
                        OutlinedButton(
                            onClick = { onNavigate(NavRoutes.USER_SETTINGS) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("dashboard_infra_setup_button")
                        ) {
                            Text("Server Setup", color = NeonCyan)
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Live Device Pulse",
                subtitle = "${telemetry.deviceModel} (${telemetry.androidVersion})",
                actionLabel = "Refresh",
                onActionClick = onRefreshTelemetry
            )
        }

        item {
            BoxWithConstraints {
                val twoColumns = maxWidth < 680.dp
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricTile(
                            label = "Battery",
                            value = "${telemetry.batteryPercent}%",
                            detail = "${telemetry.batteryChargeSource} • ${telemetry.batteryTempCelsius}°C",
                            icon = Icons.Rounded.BatteryChargingFull,
                            tint = if (telemetry.batteryPercent > 25) EmeraldOnline else CrimsonAlert,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(NavRoutes.DEVICE_MONITORING) }
                        )
                        MetricTile(
                            label = "Storage",
                            value = String.format(Locale.US, "%.1f GB", telemetry.storageFreeGb),
                            detail = String.format(Locale.US, "of %.1f GB free", telemetry.storageTotalGb),
                            icon = Icons.Rounded.Storage,
                            tint = NeonCyan,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(NavRoutes.DEVICE_MONITORING) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricTile(
                            label = "Network",
                            value = telemetry.networkTransport,
                            detail = if (telemetry.isNetworkConnected) "${telemetry.downstreamKbps / 1000} Mbps down" else "Disconnected",
                            icon = Icons.Rounded.NetworkWifi,
                            tint = if (telemetry.isNetworkConnected) CobaltPrimary else CrimsonAlert,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(NavRoutes.DEVICE_MONITORING) }
                        )
                        MetricTile(
                            label = "Cloud Nodes",
                            value = "${cloudPhones.count { it.status == "RUNNING" }} / ${cloudPhones.size}",
                            detail = if (isSocketConnected) "WS Stream Live" else "WS Standby",
                            icon = Icons.Rounded.PhoneAndroid,
                            tint = if (isSocketConnected) EmeraldOnline else AmberPending,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(NavRoutes.CLOUD_PHONES) }
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Platform Modules",
                subtitle = "All 14 control, monitoring & security views"
            )
        }

        item {
            BoxWithConstraints {
                val cols = if (maxWidth >= 700.dp) 3 else 2
                val items = AllDashboardPages.filter { it.route != NavRoutes.DASHBOARD }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items.chunked(cols).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { page ->
                                GlassCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("module_tile_${page.route}"),
                                    cornerRadius = 16.dp,
                                    contentPadding = PaddingValues(12.dp),
                                    onClick = { onNavigate(page.route) }
                                ) {
                                    Surface(
                                        color = CobaltContainer,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = page.icon,
                                            contentDescription = page.title,
                                            tint = NeonCyan,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(20.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = page.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = page.subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            repeat(cols - rowItems.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Recent Audit Trail",
                actionLabel = "All Logs",
                onActionClick = { onNavigate(NavRoutes.ACTIVITY_LOGS) }
            )
        }

        item {
            GlassCard {
                if (recentLogs.isEmpty()) {
                    Text(
                        text = "No activity events recorded yet. Actions you trigger across devices, location consent, and server settings appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    val fmt = SimpleDateFormat("HH:mm:ss", Locale.US)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        recentLogs.take(5).forEach { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${log.category} • ${log.action}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = NeonCyan
                                    )
                                    Text(
                                        text = log.detail,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = fmt.format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
