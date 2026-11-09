package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.remote.SocketStatus
import com.example.data.repository.CloudPhoneRepository
import com.example.ui.navigation.AllDashboardPages
import com.example.ui.navigation.NavRoutes
import com.example.ui.screens.ActivityLogsScreen
import com.example.ui.screens.ApplicationManagerScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CloudPhonesScreen
import com.example.ui.screens.CreateCloudPhoneScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeviceMonitoringScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.LiveScreenView
import com.example.ui.screens.LocationDashboardScreen
import com.example.ui.screens.RemoteControlScreen
import com.example.ui.screens.SecurityCenterScreen
import com.example.ui.screens.SubscriptionPlansScreen
import com.example.ui.screens.UserSettingsScreen
import com.example.ui.theme.BadgeTone
import com.example.ui.theme.CobaltContainer
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.DeepNavySurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.H4xmlCloudTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBackdrop
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StatusBadge
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            H4xmlCloudTheme {
                H4xmlRootApp()
            }
        }
    }
}

private data class PrimaryTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun H4xmlRootApp() {
    val viewModel: AppViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                AppViewModel(CloudPhoneRepository(app))
            }
        }
    )

    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val cloudPhones by viewModel.cloudPhones.collectAsStateWithLifecycle()
    val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
    val locationHistory by viewModel.locationHistory.collectAsStateWithLifecycle()
    val fileTransfers by viewModel.fileTransfers.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val socketTelemetry by viewModel.socketTelemetry.collectAsStateWithLifecycle()
    val liveTelemetry by viewModel.liveTelemetry.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val selectedDeviceId by viewModel.selectedDeviceId.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val banner by viewModel.banner.collectAsStateWithLifecycle()
    val serverHealth by viewModel.serverHealth.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecordingRemote.collectAsStateWithLifecycle()
    val isLandscape by viewModel.isLandscapeStream.collectAsStateWithLifecycle()
    val clipboardPreview by viewModel.clipboardPreview.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(banner) {
        val current = banner ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(current.message)
        viewModel.dismissBanner()
    }

    if (preferences.workspaceMode == "LOCKED") {
        Box(modifier = Modifier.fillMaxSize()) {
            AuthScreen(
                preferences = preferences,
                isBusy = isBusy,
                onLogin = { url, email, pass, totp -> viewModel.loginOnServer(url, email, pass, totp) },
                onRegister = { url, email, pass, name -> viewModel.registerOnServer(url, email, pass, name) },
                onTestServer = { url -> viewModel.testServerConnection(url) },
                onEnterLocalWorkspace = { name, email -> viewModel.enterLocalWorkspace(name, email) }
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (banner?.isError == true) CrimsonContainer else DeepNavySurface,
                    contentColor = TextPrimary
                )
            }
        }
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: NavRoutes.DASHBOARD
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val primaryTabs = remember {
        listOf(
            PrimaryTab(NavRoutes.DASHBOARD, "Dashboard", Icons.Rounded.Dashboard),
            PrimaryTab(NavRoutes.CLOUD_PHONES, "Phones", Icons.Rounded.PhoneAndroid),
            PrimaryTab(NavRoutes.DEVICE_MONITORING, "Monitor", Icons.Rounded.MonitorHeart),
            PrimaryTab(NavRoutes.SECURITY_CENTER, "Security", Icons.Rounded.Security)
        )
    }

    val navigateTo: (String) -> Unit = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val selectedDevice = remember(cloudPhones, selectedDeviceId) {
        cloudPhones.firstOrNull { it.id == selectedDeviceId } ?: cloudPhones.firstOrNull()
    }
    val isServerAuth = preferences.workspaceMode == "SERVER_AUTH" && preferences.authToken.isNotBlank()
    val isSocketConnected = socketTelemetry.status is SocketStatus.Connected

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideLayout = maxWidth >= 720.dp

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = DeepNavySurface,
                    drawerContentColor = TextPrimary,
                    modifier = Modifier.widthIn(max = 310.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 16.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = "H4XML CLOUD PHONE",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonCyan,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Text(
                            text = preferences.userEmail.ifBlank { "Local Operator" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = GlassBorder)
                        Spacer(Modifier.height(10.dp))

                        AllDashboardPages.groupBy { it.group }.forEach { (group, pages) ->
                            Text(
                                text = group.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            pages.forEach { item ->
                                NavigationDrawerItem(
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navigateTo(item.route)
                                    },
                                    icon = { Icon(item.icon, contentDescription = null) },
                                    label = {
                                        Text(
                                            text = item.title,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = CobaltContainer,
                                        selectedIconColor = NeonCyan,
                                        selectedTextColor = TextPrimary,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("drawer_item_${item.route}")
                                )
                            }
                        }
                    }
                }
            }
        ) {
            ObsidianBackdrop {
                Scaffold(
                    containerColor = Color.Transparent,
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = DeepNavySurface,
                                contentColor = TextPrimary
                            )
                        }
                    },
                    topBar = {
                        val currentMeta = AllDashboardPages.firstOrNull { it.route == currentRoute }
                        val isTopTab = primaryTabs.any { it.route == currentRoute }
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = ObsidianBlack.copy(alpha = 0.88f),
                                titleContentColor = TextPrimary
                            ),
                            navigationIcon = {
                                if (!isTopTab) {
                                    IconButton(
                                        onClick = { navController.popBackStack() },
                                        modifier = Modifier.size(48.dp).testTag("topbar_back_button")
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = NeonCyan)
                                    }
                                } else {
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                        modifier = Modifier.size(48.dp).testTag("topbar_menu_button")
                                    ) {
                                        Icon(Icons.Rounded.Menu, contentDescription = "Open navigation menu", tint = NeonCyan)
                                    }
                                }
                            },
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = currentMeta?.title ?: "H4XML Cloud",
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            actions = {
                                StatusBadge(
                                    text = if (isSocketConnected) "LIVE WS" else if (isServerAuth) "API OK" else "LOCAL",
                                    tone = if (isSocketConnected) BadgeTone.ONLINE else if (isServerAuth) BadgeTone.INFO else BadgeTone.PENDING,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                        )
                    },
                    bottomBar = {
                        if (!isWideLayout) {
                            NavigationBar(
                                containerColor = DeepNavySurface.copy(alpha = 0.96f),
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                primaryTabs.forEach { tab ->
                                    val selected = currentRoute == tab.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTo(tab.route) },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label, maxLines = 1) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = NeonCyan,
                                            selectedTextColor = NeonCyan,
                                            indicatorColor = CobaltContainer,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        ),
                                        modifier = Modifier.testTag("bottom_nav_${tab.route}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (isWideLayout) {
                            NavigationRail(
                                containerColor = DeepNavySurface.copy(alpha = 0.94f),
                                header = {
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Rounded.Menu, contentDescription = "All 14 pages", tint = NeonCyan)
                                    }
                                }
                            ) {
                                primaryTabs.forEach { tab ->
                                    NavigationRailItem(
                                        selected = currentRoute == tab.route,
                                        onClick = { navigateTo(tab.route) },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = NeonCyan,
                                            selectedTextColor = NeonCyan,
                                            indicatorColor = CobaltContainer,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary
                                        )
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = NavRoutes.DASHBOARD,
                                modifier = Modifier.widthIn(max = 960.dp).fillMaxSize()
                            ) {
                                composable(NavRoutes.DASHBOARD) {
                                    DashboardScreen(
                                        preferences = preferences,
                                        telemetry = liveTelemetry,
                                        cloudPhones = cloudPhones,
                                        recentLogs = activityLogs,
                                        serverHealth = serverHealth,
                                        socketTelemetry = socketTelemetry,
                                        onNavigate = navigateTo,
                                        onRefreshTelemetry = { viewModel.refreshTelemetryNow() },
                                        onPauseMonitoring = {
                                            viewModel.updateConsents(visibleIndicator = false)
                                            viewModel.disconnectAllRemoteSessions()
                                        }
                                    )
                                }

                                composable(NavRoutes.CLOUD_PHONES) {
                                    CloudPhonesScreen(
                                        cloudPhones = cloudPhones,
                                        selectedDeviceId = selectedDevice?.id,
                                        isServerAuthenticated = isServerAuth,
                                        isBusy = isBusy,
                                        onCreateNew = { navigateTo(NavRoutes.CREATE_CLOUD_PHONE) },
                                        onSyncFromServer = { viewModel.syncDevicesFromServer() },
                                        onPowerAction = { phone, action -> viewModel.performPowerAction(phone, action) },
                                        onDeleteDevice = { phone -> viewModel.deleteCloudPhone(phone) },
                                        onOpenRemoteControl = { phone ->
                                            viewModel.selectDevice(phone.id)
                                            navigateTo(NavRoutes.REMOTE_CONTROL)
                                        },
                                        onOpenLiveScreen = { phone ->
                                            viewModel.selectDevice(phone.id)
                                            navigateTo(NavRoutes.LIVE_SCREEN)
                                        }
                                    )
                                }

                                composable(NavRoutes.CREATE_CLOUD_PHONE) {
                                    CreateCloudPhoneScreen(
                                        isServerAuthenticated = isServerAuth,
                                        isBusy = isBusy,
                                        onCreatePhone = { name, ver, cpu, ram, storage, region, avd, res ->
                                            viewModel.createCloudPhone(name, ver, cpu, ram, storage, region, avd, res) {
                                                navigateTo(NavRoutes.CLOUD_PHONES)
                                            }
                                        }
                                    )
                                }

                                composable(NavRoutes.REMOTE_CONTROL) {
                                    RemoteControlScreen(
                                        cloudPhones = cloudPhones,
                                        selectedDevice = selectedDevice,
                                        socketTelemetry = socketTelemetry,
                                        isRecording = isRecording,
                                        isLandscape = isLandscape,
                                        clipboardPreview = clipboardPreview,
                                        visibleIndicatorEnabled = preferences.visibleIndicatorEnabled,
                                        onSelectDevice = { viewModel.selectDevice(it) },
                                        onConnectSocket = { viewModel.connectRemoteControl(it) },
                                        onDisconnectSessions = { viewModel.disconnectAllRemoteSessions() },
                                        onSendCommand = { type, params -> viewModel.sendControlCommand(type, params) },
                                        onToggleRotation = { viewModel.toggleScreenRotation() },
                                        onToggleRecording = { viewModel.toggleRemoteRecording() },
                                        onSyncClipboard = { viewModel.syncClipboardFromLocal() },
                                        onOpenLiveScreen = { navigateTo(NavRoutes.LIVE_SCREEN) },
                                        onOpenSettings = { navigateTo(NavRoutes.USER_SETTINGS) }
                                    )
                                }

                                composable(NavRoutes.LIVE_SCREEN) {
                                    LiveScreenView(
                                        selectedDevice = selectedDevice,
                                        preferences = preferences,
                                        socketTelemetry = socketTelemetry,
                                        isLandscape = isLandscape,
                                        isRecording = isRecording,
                                        onConnectStream = { viewModel.connectRemoteControl(it) },
                                        onDisconnectStream = { viewModel.disconnectAllRemoteSessions() },
                                        onToggleRotation = { viewModel.toggleScreenRotation() },
                                        onToggleRecording = { viewModel.toggleRemoteRecording() },
                                        onSendScreenshot = { viewModel.sendControlCommand("screenshot", mapOf("authorized" to true)) },
                                        onOpenSettings = { navigateTo(NavRoutes.USER_SETTINGS) }
                                    )
                                }

                                composable(NavRoutes.DEVICE_MONITORING) {
                                    DeviceMonitoringScreen(
                                        telemetry = liveTelemetry,
                                        preferences = preferences,
                                        onRefreshTelemetry = { viewModel.refreshTelemetryNow() },
                                        onToggleNotificationConsent = { enabled ->
                                            viewModel.updateConsents(notificationConsent = enabled)
                                        },
                                        onToggleVisibleIndicator = { enabled ->
                                            viewModel.updateConsents(visibleIndicator = enabled)
                                        },
                                        onLogSystemSettingsOpen = { detail ->
                                            viewModel.logPermissionAttempt("SECURITY", "OS_SETTINGS", detail)
                                        }
                                    )
                                }

                                composable(NavRoutes.LOCATION_DASHBOARD) {
                                    LocationDashboardScreen(
                                        preferences = preferences,
                                        hasLocationPermission = viewModel.hasLocationPermission(),
                                        locationHistory = locationHistory,
                                        onToggleLocationConsent = { enabled ->
                                            viewModel.updateConsents(locationConsent = enabled)
                                        },
                                        onCaptureLocation = { viewModel.captureLocation() },
                                        onClearHistory = { viewModel.clearLocationHistory() },
                                        onLogPermissionResult = { detail ->
                                            viewModel.logPermissionAttempt("SECURITY", "LOCATION_PERM", detail)
                                        }
                                    )
                                }

                                composable(NavRoutes.APP_MANAGER) {
                                    ApplicationManagerScreen(
                                        installedApps = installedApps,
                                        onRefreshApps = { viewModel.refreshInstalledApps() },
                                        onLaunchLocalApp = { pkg, appName -> viewModel.launchLocalApp(pkg, appName) },
                                        onRemoteAppCommand = { action, pkg ->
                                            viewModel.sendControlCommand(action, mapOf("packageName" to pkg))
                                        }
                                    )
                                }

                                composable(NavRoutes.FILE_MANAGER) {
                                    FileManagerScreen(
                                        files = fileTransfers,
                                        onFilePicked = { uri -> viewModel.enqueuePickedFile(uri) },
                                        onDeleteFile = { id -> viewModel.deleteFileRecord(id) },
                                        onRequestRemoteDownload = { name ->
                                            viewModel.sendControlCommand("download_file", mapOf("fileName" to name))
                                        }
                                    )
                                }

                                composable(NavRoutes.ACTIVITY_LOGS) {
                                    ActivityLogsScreen(
                                        logs = activityLogs,
                                        onClearLogs = { viewModel.clearLogs() },
                                        onCopyExport = { label, text -> viewModel.copyToLocalClipboard(label, text) }
                                    )
                                }

                                composable(NavRoutes.SECURITY_CENTER) {
                                    SecurityCenterScreen(
                                        preferences = preferences,
                                        sessions = sessions,
                                        onGenerateTotp = { viewModel.generateTotpSecret() },
                                        onVerifyTotp = { code -> viewModel.verifyTotp(code) },
                                        onDisableTotp = { viewModel.disableTotp() },
                                        onGeneratePairingCode = { name -> viewModel.generatePairingCode(name) },
                                        onApproveSession = { id -> viewModel.approveSession(id) },
                                        onRevokeSession = { id -> viewModel.revokeSession(id) },
                                        onDisconnectAllSessions = { viewModel.disconnectAllRemoteSessions() },
                                        onToggleVisibleIndicator = { enabled ->
                                            viewModel.updateConsents(visibleIndicator = enabled)
                                        },
                                        onToggleAutoEncrypt = { enabled ->
                                            viewModel.updateConsents(autoEncrypt = enabled)
                                        },
                                        onCopyText = { label, text -> viewModel.copyToLocalClipboard(label, text) },
                                        onLogoutAllDevices = { viewModel.logoutAllDevices() }
                                    )
                                }

                                composable(NavRoutes.USER_SETTINGS) {
                                    UserSettingsScreen(
                                        preferences = preferences,
                                        serverHealth = serverHealth,
                                        isBusy = isBusy,
                                        onSaveEndpoints = { base, ws, stun -> viewModel.saveEndpoints(base, ws, stun) },
                                        onTestConnection = { base -> viewModel.testServerConnection(base) },
                                        onUpdateTelemetryInterval = { sec ->
                                            viewModel.updateConsents(telemetryIntervalSec = sec)
                                        },
                                        onLogout = { viewModel.logoutAllDevices() }
                                    )
                                }

                                composable(NavRoutes.SUBSCRIPTION_PLANS) {
                                    SubscriptionPlansScreen(
                                        activePlan = preferences.subscriptionPlan,
                                        onSelectPlan = { plan -> viewModel.selectSubscriptionPlan(plan) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
