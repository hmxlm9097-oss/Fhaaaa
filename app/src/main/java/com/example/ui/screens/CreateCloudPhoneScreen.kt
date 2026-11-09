package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SectionHeader
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateCloudPhoneScreen(
    isServerAuthenticated: Boolean,
    isBusy: Boolean,
    onCreatePhone: (
        name: String,
        androidVersion: String,
        cpuCores: Int,
        ramGb: Int,
        storageGb: Int,
        region: String,
        avdProfile: String,
        resolution: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("CloudNode-01") }
    var androidVersion by remember { mutableStateOf("Android 14") }
    var cpuCores by remember { mutableIntStateOf(4) }
    var ramGb by remember { mutableIntStateOf(8) }
    var storageGb by remember { mutableIntStateOf(64) }
    var region by remember { mutableStateOf("us-west-1") }
    var avdProfile by remember { mutableStateOf("Pixel_8_API_34") }
    var resolution by remember { mutableStateOf("1080x2400") }

    val versions = listOf("Android 12", "Android 13", "Android 14", "Android 15", "Android 16")
    val cpuOptions = listOf(2, 4, 8, 16)
    val ramOptions = listOf(2, 4, 8, 16)
    val storageOptions = listOf(32, 64, 128, 256)
    val regions = listOf("us-west-1", "us-east-1", "eu-central-1", "ap-southeast-1")
    val resolutions = listOf("720x1600", "1080x2400", "1440x3120")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Create Cloud Phone",
                subtitle = if (isServerAuthenticated) {
                    "Provisions directly on your connected Node.js + AVD server"
                } else {
                    "Creates local hardware spec ready for gateway sync"
                }
            )
        }

        item {
            GlassCard(accentBorder = true) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Instance Name") },
                    leadingIcon = { Icon(Icons.Rounded.PhoneAndroid, contentDescription = null, tint = NeonCyan) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_phone_name_input")
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = avdProfile,
                    onValueChange = { avdProfile = it },
                    label = { Text("Android Emulator AVD Profile") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_phone_avd_input")
                )

                Spacer(Modifier.height(16.dp))
                Text("Android OS Version", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    versions.forEach { ver ->
                        FilterChip(
                            selected = androidVersion == ver,
                            onClick = { androidVersion = ver },
                            label = { Text(ver) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CobaltContainer,
                                selectedLabelColor = NeonCyan
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("vCPU Cores", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    cpuOptions.forEach { c ->
                        FilterChip(
                            selected = cpuCores == c,
                            onClick = { cpuCores = c },
                            label = { Text("$c Core") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CobaltContainer,
                                selectedLabelColor = NeonCyan
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("RAM Allocation", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ramOptions.forEach { r ->
                        FilterChip(
                            selected = ramGb == r,
                            onClick = { ramGb = r },
                            label = { Text("${r}GB") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CobaltContainer,
                                selectedLabelColor = NeonCyan
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("Virtual Disk Storage", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    storageOptions.forEach { s ->
                        FilterChip(
                            selected = storageGb == s,
                            onClick = { storageGb = s },
                            label = { Text("${s}GB") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CobaltContainer,
                                selectedLabelColor = NeonCyan
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("Display Resolution", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    resolutions.forEach { res ->
                        FilterChip(
                            selected = resolution == res,
                            onClick = { resolution = res },
                            label = { Text(res) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CobaltContainer,
                                selectedLabelColor = NeonCyan
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("Target Region / Node Cluster", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    regions.forEach { reg ->
                        FilterChip(
                            selected = region == reg,
                            onClick = { region = reg },
                            label = { Text(reg) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CobaltContainer,
                                selectedLabelColor = NeonCyan
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        onCreatePhone(name, androidVersion, cpuCores, ramGb, storageGb, region, avdProfile, resolution)
                    },
                    enabled = !isBusy && name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("submit_create_cloud_phone_button")
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = TextPrimary)
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Rounded.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isServerAuthenticated) "Provision Virtual Android Phone" else "Save Local Cloud Phone Spec"
                    )
                }
            }
        }
    }
}
