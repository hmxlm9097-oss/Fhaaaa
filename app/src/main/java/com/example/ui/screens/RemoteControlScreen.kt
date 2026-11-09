package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.RecentActors
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Tv
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.CloudPhoneEntity
import com.example.data.remote.RemoteSocketTelemetry
import com.example.data.remote.SocketStatus
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

@Composable
fun RemoteControlScreen(
    cloudPhones: List<CloudPhoneEntity>,
    selectedDevice: CloudPhoneEntity?,
    socketTelemetry: RemoteSocketTelemetry,
    isRecording: Boolean,
    isLandscape: Boolean,
    clipboardPreview: String,
    visibleIndicatorEnabled: Boolean,
    onSelectDevice: (String) -> Unit,
    onConnectSocket: (CloudPhoneEntity) -> Unit,
    onDisconnectSessions: () -> Unit,
    onSendCommand: (type: String, params: Map<String, Any>) -> Unit,
    onToggleRotation: () -> Unit,
    onToggleRecording: () -> Unit,
    onSyncClipboard: () -> Unit,
    onOpenLiveScreen: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var remoteText by remember { mutableStateOf("") }
    var lastTouchPoint by remember { mutableStateOf<Offset?>(null) }
    var pendingAuthAction by remember { mutableStateOf<String?>(null) } // LOCK, SCREENSHOT

    val isConnected = socketTelemetry.status is SocketStatus.Connected

    pendingAuthAction?.let { action ->
        ConfirmAuthorizationDialog(
            title = if (action == "LOCK") "Authorize Device Lock" else "Authorize Remote Screenshot",
            message = if (action == "LOCK") {
                "Send an authorized KEYCODE_POWER lock command to '${selectedDevice?.name ?: "target"}'?"
            } else {
                "Capture a screenshot on '${selectedDevice?.name ?: "target"}' with visible session audit logging?"
            },
            confirmText = if (action == "LOCK") "Lock Device" else "Capture Screenshot",
            isDestructive = action == "LOCK",
            onConfirm = {
                if (action == "LOCK") {
                    onSendCommand("keyevent", mapOf("keycode" to 26))
                } else {
                    onSendCommand("screenshot", mapOf("authorized" to true))
                }
                pendingAuthAction = null
            },
            onDismiss = { pendingAuthAction = null }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            VisibleMonitoringBanner(
                isMonitoringActive = visibleIndicatorEnabled,
                isStreamActive = isConnected,
                onDisconnectClick = onDisconnectSessions
            )
        }

        item {
            SectionHeader(
                title = "Remote Control Deck",
                subtitle = "Secure WebSocket ADB input & navigation",
                actionLabel = "Live View",
                onActionClick = onOpenLiveScreen
            )
        }

        if (cloudPhones.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cloudPhones, key = { it.id }) { phone ->
                        FilterChip(
                            selected = selectedDevice?.id == phone.id,
                            onClick = { onSelectDevice(phone.id) },
                            label = { Text(phone.name) },
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
            GlassCard(accentBorder = isConnected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedDevice?.name ?: "No Device Selected",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = socketTelemetry.lastMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    StatusBadge(
                        text = when (socketTelemetry.status) {
                            is SocketStatus.Connected -> "WS LIVE"
                            is SocketStatus.Connecting -> "LINKING"
                            is SocketStatus.Error -> "ERROR"
                            SocketStatus.Disconnected -> "STANDBY"
                        },
                        tone = when (socketTelemetry.status) {
                            is SocketStatus.Connected -> BadgeTone.ONLINE
                            is SocketStatus.Connecting -> BadgeTone.INFO
                            is SocketStatus.Error -> BadgeTone.ALERT
                            SocketStatus.Disconnected -> BadgeTone.PENDING
                        }
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "TX: ${socketTelemetry.txPacketCount} • RX: ${socketTelemetry.rxPacketCount} • Orientation: ${if (isLandscape) "Landscape" else "Portrait"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isConnected) {
                        Button(
                            onClick = onDisconnectSessions,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("remote_disconnect_ws_button")
                        ) {
                            Icon(Icons.Rounded.LinkOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Disconnect")
                        }
                    } else {
                        Button(
                            onClick = {
                                if (selectedDevice != null) onConnectSocket(selectedDevice)
                                else onOpenSettings()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("remote_connect_ws_button")
                        ) {
                            Icon(Icons.Rounded.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (selectedDevice != null) "Connect WebSocket" else "Configure Gateway")
                        }
                    }
                    OutlinedButton(
                        onClick = onOpenLiveScreen,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("remote_open_screen_button")
                    ) {
                        Icon(Icons.Rounded.Tv, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Screen", color = NeonCyan)
                    }
                }
            }
        }

        item {
            GlassCard {
                Text(
                    text = "Interactive Remote Touchpad (Tap, Long Press, Swipe)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Coordinates map to 1080x2400 virtual display space and transmit via WebSocket.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianBlack.copy(alpha = 0.85f))
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    lastTouchPoint = offset
                                    val x = (offset.x / size.width.coerceAtLeast(1)) * 1080f
                                    val y = (offset.y / size.height.coerceAtLeast(1)) * 2400f
                                    onSendCommand("tap", mapOf("x" to x, "y" to y))
                                },
                                onLongPress = { offset ->
                                    lastTouchPoint = offset
                                    val x = (offset.x / size.width.coerceAtLeast(1)) * 1080f
                                    val y = (offset.y / size.height.coerceAtLeast(1)) * 2400f
                                    onSendCommand(
                                        "swipe",
                                        mapOf("x1" to x, "y1" to y, "x2" to x, "y2" to y, "durationMs" to 800)
                                    )
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            var start = Offset.Zero
                            var end = Offset.Zero
                            detectDragGestures(
                                onDragStart = { start = it; end = it; lastTouchPoint = it },
                                onDrag = { change, _ ->
                                    change.consume()
                                    end = change.position
                                    lastTouchPoint = end
                                },
                                onDragEnd = {
                                    val x1 = (start.x / size.width.coerceAtLeast(1)) * 1080f
                                    val y1 = (start.y / size.height.coerceAtLeast(1)) * 2400f
                                    val x2 = (end.x / size.width.coerceAtLeast(1)) * 1080f
                                    val y2 = (end.y / size.height.coerceAtLeast(1)) * 2400f
                                    onSendCommand(
                                        "swipe",
                                        mapOf("x1" to x1, "y1" to y1, "x2" to x2, "y2" to y2, "durationMs" to 280)
                                    )
                                }
                            )
                        }
                        .testTag("remote_touchpad_surface"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val step = 32.dp.toPx()
                        var gx = step
                        while (gx < size.width) {
                            drawLine(GlassBorder.copy(alpha = 0.25f), Offset(gx, 0f), Offset(gx, size.height), 1f)
                            gx += step
                        }
                        var gy = step
                        while (gy < size.height) {
                            drawLine(GlassBorder.copy(alpha = 0.25f), Offset(0f, gy), Offset(size.width, gy), 1f)
                            gy += step
                        }
                        lastTouchPoint?.let { pt ->
                            drawCircle(NeonCyan.copy(alpha = 0.3f), radius = 26.dp.toPx(), center = pt)
                            drawCircle(NeonCyan, radius = 6.dp.toPx(), center = pt)
                        }
                    }
                    Text(
                        text = if (isConnected) "TOUCH & DRAG ACTIVE" else "TOUCHPAD STANDBY (CONNECT GATEWAY TO DISPATCH)",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isConnected) NeonCyan else TextMuted
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSendCommand("keyevent", mapOf("keycode" to 4)) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("remote_nav_back")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = NeonCyan)
                    }
                    OutlinedButton(
                        onClick = { onSendCommand("keyevent", mapOf("keycode" to 3)) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("remote_nav_home")
                    ) {
                        Icon(Icons.Rounded.Home, contentDescription = "Home", tint = NeonCyan)
                    }
                    OutlinedButton(
                        onClick = { onSendCommand("keyevent", mapOf("keycode" to 187)) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("remote_nav_recents")
                    ) {
                        Icon(Icons.Rounded.RecentActors, contentDescription = "Recent Apps", tint = NeonCyan)
                    }
                    OutlinedButton(
                        onClick = onToggleRotation,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("remote_rotate_button")
                    ) {
                        Icon(Icons.Rounded.ScreenRotation, contentDescription = "Rotate Screen", tint = NeonCyan)
                    }
                }
            }
        }

        item {
            GlassCard {
                Text("Remote Keyboard & Clipboard", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = remoteText,
                        onValueChange = { remoteText = it },
                        label = { Text("Send Text to Cloud Phone") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("remote_keyboard_input")
                    )
                    Button(
                        onClick = {
                            if (remoteText.isNotBlank()) {
                                onSendCommand("text", mapOf("text" to remoteText))
                                remoteText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                        modifier = Modifier.heightIn(min = 52.dp).testTag("remote_send_text_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send text")
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSyncClipboard,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("remote_clipboard_sync_button")
                    ) {
                        Icon(Icons.Rounded.ContentPaste, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Push Clipboard", color = NeonCyan)
                    }
                    OutlinedButton(
                        onClick = { pendingAuthAction = "SCREENSHOT" },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("remote_screenshot_button")
                    ) {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Screenshot", color = NeonCyan)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onToggleRecording,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("remote_record_button")
                    ) {
                        Icon(
                            Icons.Rounded.FiberManualRecord,
                            contentDescription = null,
                            tint = if (isRecording) CrimsonAlert else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (isRecording) "Stop Rec" else "Record Screen", color = if (isRecording) CrimsonAlert else TextPrimary)
                    }
                    OutlinedButton(
                        onClick = { pendingAuthAction = "LOCK" },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("remote_lock_button")
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Lock Device", color = CrimsonAlert)
                    }
                }

                if (clipboardPreview.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Last clipboard read: \"${clipboardPreview.take(64)}\"",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldOnline
                    )
                }
            }
        }
    }
}
