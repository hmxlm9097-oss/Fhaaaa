package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.ui.graphics.vector.ImageVector

object NavRoutes {
    const val LOGIN = "login_signup"
    const val DASHBOARD = "main_dashboard"
    const val CLOUD_PHONES = "my_cloud_phones"
    const val CREATE_CLOUD_PHONE = "create_cloud_phone"
    const val REMOTE_CONTROL = "remote_control"
    const val LIVE_SCREEN = "live_screen_view"
    const val DEVICE_MONITORING = "device_monitoring"
    const val LOCATION_DASHBOARD = "location_dashboard"
    const val APP_MANAGER = "application_manager"
    const val FILE_MANAGER = "file_manager"
    const val ACTIVITY_LOGS = "activity_logs"
    const val SECURITY_CENTER = "security_center"
    const val USER_SETTINGS = "user_settings"
    const val SUBSCRIPTION_PLANS = "subscription_plans"
}

data class DrawerMenuItem(
    val route: String,
    val title: String,
    val subtitle: String,
    val group: String,
    val icon: ImageVector
)

val AllDashboardPages: List<DrawerMenuItem> = listOf(
    DrawerMenuItem(NavRoutes.DASHBOARD, "Main Dashboard", "Control hub & telemetry pulse", "Command", Icons.Rounded.Dashboard),
    DrawerMenuItem(NavRoutes.CLOUD_PHONES, "My Cloud Phones", "Virtual Android instances", "Command", Icons.Rounded.PhoneAndroid),
    DrawerMenuItem(NavRoutes.CREATE_CLOUD_PHONE, "Create Cloud Phone", "Provision OS, CPU & RAM", "Command", Icons.Rounded.AddCircle),
    DrawerMenuItem(NavRoutes.REMOTE_CONTROL, "Remote Control", "Touch, keys, clipboard & ADB", "Operations", Icons.Rounded.Gamepad),
    DrawerMenuItem(NavRoutes.LIVE_SCREEN, "Live Screen View", "WebRTC stream & HUD", "Operations", Icons.Rounded.Tv),
    DrawerMenuItem(NavRoutes.DEVICE_MONITORING, "Device Monitoring", "Battery, net, storage & consent", "Telemetry", Icons.Rounded.MonitorHeart),
    DrawerMenuItem(NavRoutes.LOCATION_DASHBOARD, "Location Dashboard", "Consented GPS & history", "Telemetry", Icons.Rounded.LocationOn),
    DrawerMenuItem(NavRoutes.APP_MANAGER, "Application Manager", "Installed & cloud APKs", "Telemetry", Icons.Rounded.Apps),
    DrawerMenuItem(NavRoutes.FILE_MANAGER, "File Manager", "Authorized upload & picker", "Telemetry", Icons.Rounded.FolderOpen),
    DrawerMenuItem(NavRoutes.ACTIVITY_LOGS, "Activity Logs", "Chronological audit trail", "Security", Icons.Rounded.History),
    DrawerMenuItem(NavRoutes.SECURITY_CENTER, "Security Center", "2FA TOTP, pairing & sessions", "Security", Icons.Rounded.Security),
    DrawerMenuItem(NavRoutes.USER_SETTINGS, "User Settings", "Server URL, WebSocket & infra", "Platform", Icons.Rounded.Settings),
    DrawerMenuItem(NavRoutes.SUBSCRIPTION_PLANS, "Subscription Plans", "Compute quotas & billing", "Platform", Icons.Rounded.CreditCard)
)
