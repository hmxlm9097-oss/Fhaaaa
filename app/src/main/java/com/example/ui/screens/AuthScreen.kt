package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AppPreferences
import com.example.ui.theme.BadgeTone
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBackdrop
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StatusBadge
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    preferences: AppPreferences,
    isBusy: Boolean,
    onLogin: (serverUrl: String, email: String, password: String, totpCode: String) -> Unit,
    onRegister: (serverUrl: String, email: String, password: String, displayName: String) -> Unit,
    onTestServer: (serverUrl: String) -> Unit,
    onEnterLocalWorkspace: (operatorName: String, email: String) -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var serverUrl by remember(preferences.serverBaseUrl) {
        mutableStateOf(preferences.serverBaseUrl.ifBlank { "http://10.0.2.2:4000" })
    }
    var displayName by remember { mutableStateOf(preferences.userDisplayName) }
    var email by remember { mutableStateOf(preferences.userEmail) }
    var password by remember { mutableStateOf("") }
    var totpCode by remember { mutableStateOf("") }

    ObsidianBackdrop {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp)
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "H4XML Cloud Phone datacenter illustration",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            StatusBadge(text = "ZERO-TRUST CONTROL PLANE", tone = BadgeTone.INFO)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "H4XML CLOUD PHONE",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Remote Emulator Fleet & Authorized Telemetry",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonCyan
                            )
                        }
                    }
                }

                GlassCard(accentBorder = true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = !isSignUp,
                            onClick = { isSignUp = false },
                            label = { Text("Sign In") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CobaltContainer,
                                selectedLabelColor = NeonCyan
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("auth_mode_login_chip")
                        )
                        FilterChip(
                            selected = isSignUp,
                            onClick = { isSignUp = true },
                            label = { Text("Create Account") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CobaltContainer,
                                selectedLabelColor = NeonCyan
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("auth_mode_signup_chip")
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = NeonCyan,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )

                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Node.js Gateway URL (REST / WebSocket)") },
                        leadingIcon = { Icon(Icons.Rounded.Dns, contentDescription = null, tint = NeonCyan) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_server_url_input")
                    )

                    Spacer(Modifier.height(10.dp))

                    AnimatedVisibility(visible = isSignUp) {
                        Column {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Operator Display Name") },
                                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = NeonCyan) },
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_input")
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = NeonCyan) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (min 8 chars)") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = NeonCyan) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    AnimatedVisibility(visible = !isSignUp) {
                        Column {
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = totpCode,
                                onValueChange = { totpCode = it },
                                label = { Text("2FA TOTP Code (if enabled)") },
                                leadingIcon = { Icon(Icons.Rounded.Key, contentDescription = null, tint = NeonCyan) },
                                singleLine = true,
                                colors = fieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_totp_input")
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onTestServer(serverUrl) },
                            enabled = !isBusy,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("auth_test_server_button")
                        ) {
                            Text("Ping API", color = NeonCyan)
                        }

                        Button(
                            onClick = {
                                if (isSignUp) {
                                    onRegister(serverUrl, email, password, displayName)
                                } else {
                                    onLogin(serverUrl, email, password, totpCode)
                                }
                            },
                            enabled = !isBusy && email.isNotBlank() && password.length >= 8,
                            colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("auth_submit_button")
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(if (isSignUp) "Register & Connect" else "Authenticate")
                        }
                    }
                }

                GlassCard(cornerRadius = 18.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Terminal,
                            contentDescription = "Offline Workspace",
                            tint = EmeraldOnline
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Local Diagnostics & Infrastructure Setup",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Firebase Cloud sync was skipped. Inspect live device sensors, configure TOTP 2FA, manage local cloud-phone specs, and review Node.js + ADB deployment steps without a live server.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onEnterLocalWorkspace(displayName, email) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("enter_local_workspace_button")
                    ) {
                        Icon(Icons.Rounded.Security, contentDescription = null, tint = EmeraldOnline, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open Local Workspace & Setup Guide", color = EmeraldOnline)
                    }
                }

                Text(
                    text = "Notice: Real cloud phone streaming & remote shell execution require your Node.js + ADB + WebRTC host running. Local workspace never fabricates remote screen streams or fake logs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }
    }
}
