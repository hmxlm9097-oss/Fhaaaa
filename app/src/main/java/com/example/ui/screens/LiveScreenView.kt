package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.AppPreferences
import com.example.data.local.CloudPhoneEntity
import com.example.data.remote.RemoteSocketTelemetry
import com.example.data.remote.SocketStatus
import com.example.ui.theme.BadgeTone
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.ConfirmAuthorizationDialog
import com.example.ui.theme.CrimsonAlert
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

@Composable
fun LiveScreenView(
    selectedDevice: CloudPhoneEntity?,
    preferences: AppPreferences,
    socketTelemetry: RemoteSocketTelemetry,
    isLandscape: Boolean,
    isRecording: Boolean,
    onConnectStream: (CloudPhoneEntity) -> Unit,
    onDisconnectStream: () -> Unit,
    onToggleRotation: () -> Unit,
    onToggleRecording: () -> Unit,
    onSendScreenshot: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val isConnected = socketTelemetry.status is SocketStatus.Connected
    var confirmScreenshot by remember { mutableStateOf(false) }

    if (confirmScreenshot) {
        ConfirmAuthorizationDialog(
            title = "Authorize Live Screenshot",
            message = "Trigger authorized frame capture on ${selectedDevice?.name ?: "connected device"}?",
            confirmText = "Capture",
            onConfirm = {
                onSendScreenshot()
                confirmScreenshot = false
            },
            onDismiss = { confirmScreenshot = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            VisibleMonitoringBanner(
                isMonitoringActive = preferences.visibleIndicatorEnabled,
                isStreamActive = isConnected,
                onDisconnectClick = onDisconnectStream
            )
        }

        item {
            SectionHeader(
                title = "Live Screen View",
                subtitle = "WebRTC signaling & real-time screen HUD"
            )
        }

        item {
            GlassCard(accentBorder = isConnected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDevice?.name ?: "No Cloud Node Selected",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    StatusBadge(
                        text = if (isConnected) "SIGNALING ACTIVE" else "STREAM OFFLINE",
                        tone = if (isConnected) BadgeTone.ONLINE else BadgeTone.ALERT
                    )
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (isLandscape) 16f / 9f else 9f / 14f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(ObsidianBlack)
                        .border(1.dp, if (isConnected) NeonCyan.copy(alpha = 0.6f) else GlassBorder, RoundedCornerShape(18.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.Tv,
                            contentDescription = "Live WebRTC viewport",
                            tint = if (isConnected) NeonCyan else TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (isConnected) {
                                "WebSocket Signaling Linked • Ready for WebRTC Video Track"
                            } else {
                                "No Active Video Stream"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (isConnected) {
                                "STUN: ${preferences.webrtcStunUrl}\nSession: ${(socketTelemetry.status as? SocketStatus.Connected)?.sessionId ?: "Negotiating..."}\nAttach scrcpy/WebRTC media worker on your host server to render live H.264 frames."
                            } else {
                                "H4XML never simulates fake phone video frames.\nConnect your Node.js + Android Emulator WebRTC host in Settings to stream real pixels."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!isConnected && selectedDevice != null) {
                                Button(
                                    onClick = { onConnectStream(selectedDevice) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("live_screen_connect_button")
                                ) {
                                    Text("Connect Signaling")
                                }
                            }
                            OutlinedButton(
                                onClick = onOpenSettings,
                                modifier = Modifier.heightIn(min = 48.dp).testTag("live_screen_settings_button")
                            ) {
                                Icon(Icons.Rounded.Settings, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Gateway Config", color = NeonCyan)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onToggleRotation,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("live_screen_rotate_button")
                    ) {
                        Icon(Icons.Rounded.ScreenRotation, contentDescription = "Rotate", tint = NeonCyan)
                    }
                    OutlinedButton(
                        onClick = { confirmScreenshot = true },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("live_screen_screenshot_button")
                    ) {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = "Screenshot", tint = NeonCyan)
                    }
                    OutlinedButton(
                        onClick = onToggleRecording,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("live_screen_record_button")
                    ) {
                        Icon(
                            Icons.Rounded.FiberManualRecord,
                            contentDescription = "Record",
                            tint = if (isRecording) CrimsonAlert else TextSecondary
                        )
                    }
                    Button(
                        onClick = onDisconnectStream,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("live_screen_disconnect_button")
                    ) {
                        Icon(Icons.Rounded.LinkOff, contentDescription = "Disconnect")
                    }
                }
            }
        }
    }
}
