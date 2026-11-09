package com.example.data.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.core.app.NotificationManagerCompat

data class LiveDeviceTelemetry(
    val deviceModel: String,
    val androidVersion: String,
    val sdkInt: Int,
    val batteryPercent: Int,
    val isCharging: Boolean,
    val batteryChargeSource: String,
    val batteryTempCelsius: Float,
    val batteryVoltageMv: Int,
    val batteryHealthLabel: String,
    val storageTotalGb: Float,
    val storageUsedGb: Float,
    val storageFreeGb: Float,
    val networkTransport: String,
    val isNetworkConnected: Boolean,
    val isUnmetered: Boolean,
    val downstreamKbps: Int,
    val upstreamKbps: Int,
    val notificationListenerGranted: Boolean,
    val capturedAtMs: Long = System.currentTimeMillis()
)

class DeviceTelemetryProvider(private val context: Context) {

    fun snapshot(): LiveDeviceTelemetry {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 0
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val chargeSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Host"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> if (isCharging) "Charging" else "Discharging"
        }
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val voltageMv = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val healthCode = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
        val healthLabel = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Nominal"
        }

        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalBytes = statFs.totalBytes.toDouble()
        val availBytes = statFs.availableBytes.toDouble()
        val usedBytes = (totalBytes - availBytes).coerceAtLeast(0.0)
        val gbDiv = 1024.0 * 1024.0 * 1024.0

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = if (activeNetwork != null) cm.getNetworkCapabilities(activeNetwork) else null
        val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val transport = when {
            caps == null -> "Offline"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN Tunnel"
            else -> "Other Network"
        }
        val isUnmetered = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == true
        val downKbps = caps?.linkDownstreamBandwidthKbps ?: 0
        val upKbps = caps?.linkUpstreamBandwidthKbps ?: 0

        val notifEnabled = NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)

        return LiveDeviceTelemetry(
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            sdkInt = Build.VERSION.SDK_INT,
            batteryPercent = batteryPct,
            isCharging = isCharging,
            batteryChargeSource = chargeSource,
            batteryTempCelsius = tempTenths / 10f,
            batteryVoltageMv = voltageMv,
            batteryHealthLabel = healthLabel,
            storageTotalGb = (totalBytes / gbDiv).toFloat(),
            storageUsedGb = (usedBytes / gbDiv).toFloat(),
            storageFreeGb = (availBytes / gbDiv).toFloat(),
            networkTransport = transport,
            isNetworkConnected = isConnected,
            isUnmetered = isUnmetered,
            downstreamKbps = downKbps,
            upstreamKbps = upKbps,
            notificationListenerGranted = notifEnabled
        )
    }
}
