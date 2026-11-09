package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.local.CloudPhoneEntity
import com.example.ui.theme.BadgeTone
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.ConfirmAuthorizationDialog
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SectionHeader
import com.example.ui.theme.StatusBadge
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CloudPhonesScreen(
    cloudPhones: List<CloudPhoneEntity>,
    selectedDeviceId: String?,
    isServerAuthenticated: Boolean,
    isBusy: Boolean,
    onCreateNew: () -> Unit,
    onSyncFromServer: () -> Unit,
    onPowerAction: (CloudPhoneEntity, String) -> Unit,
    onDeleteDevice: (CloudPhoneEntity) -> Unit,
    onOpenRemoteControl: (CloudPhoneEntity) -> Unit,
    onOpenLiveScreen: (CloudPhoneEntity) -> Unit
) {
    var pendingDelete by remember { mutableStateOf<CloudPhoneEntity?>(null) }

    pendingDelete?.let { target ->
        ConfirmAuthorizationDialog(
            title = "Delete Virtual Phone",
            message = "Delete '${target.name}' (${target.androidVersion})? If linked to your gateway server, this also removes the remote instance.",
            confirmText = "Delete Phone",
            isDestructive = true,
            onConfirm = {
                onDeleteDevice(target)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "My Cloud Phones",
                subtitle = "${cloudPhones.size} configured virtual Android instance(s)",
                actionLabel = if (isServerAuthenticated) "Sync" else "Create",
                onActionClick = if (isServerAuthenticated) onSyncFromServer else onCreateNew
            )
        }

        item {
            GlassCard(contentPadding = PaddingValues(0.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(126.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_virtual_device_rack),
                        contentDescription = "Virtual Android Cloud Rack",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Virtual Android Fleet",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isServerAuthenticated) "Gateway Synced • Ready for ADB control"
                                else "Local Profiles • Connect Node.js ADB gateway to boot",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonCyan
                            )
                        }
                        Button(
                            onClick = onCreateNew,
                            colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("cloud_phones_new_button")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("New")
                        }
                    }
                }
            }
        }

        if (cloudPhones.isEmpty()) {
            item {
                GlassCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhoneAndroid,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "No Virtual Cloud Phones Yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Create your first Android virtual device profile with custom OS version, vCPU, RAM, and storage specifications.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = onCreateNew,
                            colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("empty_create_cloud_phone_button")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Create Cloud Phone")
                        }
                    }
                }
            }
        } else {
            items(cloudPhones, key = { it.id }) { phone ->
                val tone = when (phone.status) {
                    "RUNNING" -> BadgeTone.ONLINE
                    "STARTING" -> BadgeTone.INFO
                    "STOPPED" -> BadgeTone.MUTED
                    else -> BadgeTone.PENDING
                }
                GlassCard(accentBorder = phone.id == selectedDeviceId) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                color = CobaltContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PhoneAndroid,
                                    contentDescription = phone.name,
                                    tint = NeonCyan,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = phone.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${phone.androidVersion} • ${phone.avdProfile}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                        StatusBadge(
                            text = if (phone.status == "LOCAL_PROFILE") "LOCAL PROFILE" else phone.status,
                            tone = tone
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${phone.cpuCores} vCPU • ${phone.ramGb} GB RAM • ${phone.storageGb} GB • ${phone.resolution} • ${phone.region}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onPowerAction(phone, "START") },
                            enabled = !isBusy,
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("start_device_${phone.id}")
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = "Start", tint = EmeraldOnline, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Start", color = EmeraldOnline)
                        }
                        OutlinedButton(
                            onClick = { onPowerAction(phone, "STOP") },
                            enabled = !isBusy,
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("stop_device_${phone.id}")
                        ) {
                            Icon(Icons.Rounded.Stop, contentDescription = "Stop", tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Stop", color = CrimsonAlert)
                        }
                        OutlinedButton(
                            onClick = { onPowerAction(phone, "RESTART") },
                            enabled = !isBusy,
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("restart_device_${phone.id}")
                        ) {
                            Icon(Icons.Rounded.RestartAlt, contentDescription = "Restart", tint = NeonCyan, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = { pendingDelete = phone },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("delete_device_${phone.id}")
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete phone", tint = CrimsonAlert)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onOpenRemoteControl(phone) },
                            colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("open_remote_control_${phone.id}")
                        ) {
                            Icon(Icons.Rounded.Gamepad, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Control")
                        }
                        OutlinedButton(
                            onClick = { onOpenLiveScreen(phone) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("open_live_screen_${phone.id}")
                        ) {
                            Icon(Icons.Rounded.Tv, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Screen", color = NeonCyan)
                        }
                    }
                }
            }
        }
    }
}
