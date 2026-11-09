package com.example.data.system

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

data class InstalledAppItem(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val isSystemApp: Boolean
)

class InstalledAppsProvider(private val context: Context) {

    fun loadLauncherApps(): List<InstalledAppItem> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
        val unique = LinkedHashMap<String, InstalledAppItem>()
        for (info in resolveInfos) {
            val pkgName = info.activityInfo?.packageName ?: continue
            if (unique.containsKey(pkgName)) continue
            val label = info.loadLabel(pm).toString()
            val pkgInfo = runCatching { pm.getPackageInfo(pkgName, 0) }.getOrNull()
            val isSystem = ((pkgInfo?.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM) != 0
            val version = pkgInfo?.versionName ?: "1.0"
            unique[pkgName] = InstalledAppItem(
                appName = label,
                packageName = pkgName,
                versionName = version,
                isSystemApp = isSystem
            )
        }
        return unique.values.sortedBy { it.appName.lowercase() }
    }

    fun launchApp(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
