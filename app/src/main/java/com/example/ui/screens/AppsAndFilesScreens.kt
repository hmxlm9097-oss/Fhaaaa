package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.FileTransferEntity
import com.example.data.system.InstalledAppItem
import com.example.ui.theme.BadgeTone
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SectionHeader
import com.example.ui.theme.StatusBadge
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ApplicationManagerScreen(
    installedApps: List<InstalledAppItem>,
    onRefreshApps: () -> Unit,
    onLaunchLocalApp: (packageName: String, appName: String) -> Unit,
    onRemoteAppCommand: (action: String, packageName: String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var showSystemApps by remember { mutableStateOf(false) }
    var remotePackageInput by remember { mutableStateOf("") }

    val filtered = remember(installedApps, search, showSystemApps) {
        installedApps.filter { app ->
            (showSystemApps || !app.isSystemApp) &&
                (search.isBlank() ||
                    app.appName.contains(search, ignoreCase = true) ||
                    app.packageName.contains(search, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Application Manager",
                subtitle = "${filtered.size} launcher package(s) visible",
                actionLabel = "Scan",
                onActionClick = onRefreshApps
            )
        }

        item {
            GlassCard(accentBorder = true) {
                Text("Remote Cloud Phone APK Dispatch", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    text = "Send install, launch, or uninstall ADB actions to your connected cloud phone node.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = remotePackageInput,
                    onValueChange = { remotePackageInput = it },
                    label = { Text("Package Name (e.g. org.mozilla.firefox)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("remote_package_input")
                )
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (remotePackageInput.isNotBlank()) onRemoteAppCommand("launch_app", remotePackageInput.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("remote_launch_pkg_button")
                    ) {
                        Text("Launch")
                    }
                    OutlinedButton(
                        onClick = {
                            if (remotePackageInput.isNotBlank()) onRemoteAppCommand("install_app", remotePackageInput.trim())
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("remote_install_pkg_button")
                    ) {
                        Text("Install", color = NeonCyan)
                    }
                    OutlinedButton(
                        onClick = {
                            if (remotePackageInput.isNotBlank()) onRemoteAppCommand("uninstall_app", remotePackageInput.trim())
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("remote_uninstall_pkg_button")
                    ) {
                        Text("Remove", color = CrimsonAlert)
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Filter local installed apps") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = NeonCyan) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("app_manager_search_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !showSystemApps,
                        onClick = { showSystemApps = false },
                        label = { Text("User Apps") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CobaltContainer, selectedLabelColor = NeonCyan),
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                    FilterChip(
                        selected = showSystemApps,
                        onClick = { showSystemApps = true },
                        label = { Text("Include System Apps") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CobaltContainer, selectedLabelColor = NeonCyan),
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }
        }

        items(filtered, key = { it.packageName }) { app ->
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Apps, contentDescription = app.appName, tint = NeonCyan)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(app.appName, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${app.packageName} • v${app.versionName}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { onLaunchLocalApp(app.packageName, app.appName) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("launch_app_${app.packageName}")
                    ) {
                        Icon(Icons.Rounded.OpenInNew, contentDescription = "Open ${app.appName}", tint = NeonCyan, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FileManagerScreen(
    files: List<FileTransferEntity>,
    onFilePicked: (Uri) -> Unit,
    onDeleteFile: (String) -> Unit,
    onRequestRemoteDownload: (String) -> Unit
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(onFilePicked)
    }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let(onFilePicked)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "File Manager",
                subtitle = "Zero-permission Photo Picker & Storage Access Framework"
            )
        }

        item {
            GlassCard(accentBorder = true) {
                Text("Authorized File Transfer", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    text = "Select media or files via system pickers without broad storage permissions. Files upload directly when your Node.js gateway is signed in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("pick_media_button")
                    ) {
                        Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Pick Media")
                    }
                    OutlinedButton(
                        onClick = { documentPicker.launch(arrayOf("*/*")) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("pick_document_button")
                    ) {
                        Icon(Icons.Rounded.UploadFile, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Pick File", color = NeonCyan)
                    }
                }
            }
        }

        if (files.isEmpty()) {
            item {
                GlassCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No Transfers Queued", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            text = "Use Pick Media or Pick File above to inspect and upload authorized local assets.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(files, key = { it.id }) { item ->
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (item.mimeType.startsWith("image/")) {
                            AsyncImage(
                                model = Uri.parse(item.uriString),
                                contentDescription = item.fileName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.fileName, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${item.mimeType} • ${item.sizeBytes / 1024} KB", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            StatusBadge(
                                text = item.status,
                                tone = when (item.status) {
                                    "UPLOADED" -> BadgeTone.ONLINE
                                    "FAILED" -> BadgeTone.ALERT
                                    else -> BadgeTone.PENDING
                                }
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { onRequestRemoteDownload(item.fileName) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = "Pull file", tint = NeonCyan)
                            }
                            IconButton(
                                onClick = { onDeleteFile(item.id) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete record", tint = CrimsonAlert)
                            }
                        }
                    }
                }
            }
        }
    }
}
