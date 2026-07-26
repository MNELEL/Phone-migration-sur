package com.example.scanner

import android.accounts.AccountManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import com.example.domain.AppInfo
import com.example.domain.ContactSummary
import com.example.domain.MediaSummary
import com.example.domain.ScanReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeepScanner(private val context: Context) {

    suspend fun scan(): ScanReport = withContext(Dispatchers.IO) {
        val apps = scanApps()
        val contacts = scanContacts()
        val media = scanMedia()
        val accounts = scanAccounts()
        
        ScanReport(apps, contacts, media, accounts)
    }

    private fun scanApps(): List<AppInfo> {
        val pm = context.packageManager
        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
        }
        
        return installedApps.filter {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }.mapNotNull { app ->
            try {
                val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(app.packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(app.packageName, 0)
                }
                AppInfo(
                    packageName = app.packageName,
                    appName = pm.getApplicationLabel(app).toString(),
                    versionName = packageInfo.versionName ?: "",
                    installTime = packageInfo.firstInstallTime,
                    canBackup = (app.flags and ApplicationInfo.FLAG_ALLOW_BACKUP) != 0
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun scanContacts(): ContactSummary {
        return try {
            val cursor = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(ContactsContract.Contacts._ID),
                null, null, null
            )
            val count = cursor?.count ?: 0
            cursor?.close()
            ContactSummary(count)
        } catch (e: Exception) {
            ContactSummary(0)
        }
    }

    private fun scanMedia(): MediaSummary {
        return try {
            val photos = countMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val videos = countMedia(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            MediaSummary(photos, videos, 0L)
        } catch (e: Exception) {
            MediaSummary(0, 0, 0L)
        }
    }

    private fun countMedia(uri: Uri): Int {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(MediaStore.MediaColumns._ID),
            null, null, null
        )
        val count = cursor?.count ?: 0
        cursor?.close()
        return count
    }

    private fun scanAccounts(): List<String> {
        return try {
            val manager = AccountManager.get(context)
            manager.accounts.map { it.type }.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
