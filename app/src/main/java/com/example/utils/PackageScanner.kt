package com.example.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log

data class AppInfo(
    val name: String,
    val packageName: String,
    val isSystem: Boolean,
    val category: String = "Social"
)

object PackageScanner {

    private const val TAG = "PackageScanner"

    // High quality presets for quick cloning if packages aren't fully found or as templates
    val PRESETS = listOf(
        AppInfo("Telegram", "org.telegram.messenger", false, "Communication"),
        AppInfo("WhatsApp", "com.whatsapp", false, "Communication"),
        AppInfo("Discord", "com.discord", false, "Social"),
        AppInfo("Slack", "com.Slack", false, "Productivity"),
        AppInfo("Instagram", "com.instagram.android", false, "Social"),
        AppInfo("Facebook", "com.facebook.katana", false, "Social"),
        AppInfo("TikTok", "com.zhiliaoapp.musically", false, "Entertainment"),
        AppInfo("Twitter / X", "com.twitter.android", false, "News"),
        AppInfo("Messenger", "com.facebook.orca", false, "Communication"),
        AppInfo("LinkedIn", "com.linkedin.android", false, "Social")
    )

    fun getScanList(context: Context): List<AppInfo> {
        val result = mutableListOf<AppInfo>()
        val pm = context.packageManager
        
        try {
            // Get installed apps
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in apps) {
                // Filter out standard system core services, keep launchable apps
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    val label = pm.getApplicationLabel(app).toString()
                    val isSys = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    
                    // Exclude this app itself to avoid inception clone unless requested
                    if (app.packageName != context.packageName) {
                        result.add(
                            AppInfo(
                                name = label,
                                packageName = app.packageName,
                                isSystem = isSys,
                                category = if (isSys) "System Utility" else "Installed App"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning packages", e)
        }

        // De-duplicate and align with presets.
        // If an installed app is in the presets, we can prefer the installed app version, 
        // and we can output presets for those not installed yet so they have plenty of mock choices.
        val installedPackageNames = result.map { it.packageName }.toSet()
        val formattedPresets = PRESETS.filter { it.packageName !in installedPackageNames }
        
        return (result + formattedPresets).sortedBy { it.isSystem }
    }
}
