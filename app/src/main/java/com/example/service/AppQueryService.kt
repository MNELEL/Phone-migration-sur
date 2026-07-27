package com.example.service

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.example.domain.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppQueryService(private val context: Context) {

    suspend fun queryUserApplications(): List<UserAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        }

        installedApps.filter { app ->
            // Filter out system apps, keep only user-installed applications
            (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }.mapNotNull { app ->
            try {
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(app.packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(app.packageName, 0)
                }

                val appName = pm.getApplicationLabel(app).toString()
                val icon = pm.getApplicationIcon(app)

                UserAppInfo(
                    packageName = app.packageName,
                    appName = appName,
                    versionName = packageInfo.versionName ?: "",
                    installTime = packageInfo.firstInstallTime,
                    canBackup = (app.flags and ApplicationInfo.FLAG_ALLOW_BACKUP) != 0,
                    icon = icon
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getAppIcon(packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }
}

data class UserAppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val installTime: Long,
    val canBackup: Boolean,
    val icon: Drawable?
)
