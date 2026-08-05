package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class ZipExportResult(
    val zipFile: File,
    val fileCount: Int,
    val totalSizeBytes: Long
) {
    val formattedSize: String
        get() {
            val kb = totalSizeBytes / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1.0) String.format("%.2f MB", mb) else String.format("%.1f KB", kb)
        }
}

object ProjectZipExporter {

    private const val TAG = "ProjectZipExporter"

    /**
     * Exports project structure, configurations, Gradle setup, manifests, and source files
     * into a standalone ZIP file that can be opened in Android Studio.
     */
    fun exportProjectZip(context: Context): ZipExportResult {
        val exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        val zipFile = File(exportDir, "PhoneMigrate_AndroidStudio_Project.zip")

        var fileCount = 0
        var totalSizeBytes = 0L

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // 1. Root configuration files
            fileCount += addZipEntry(zos, "settings.gradle.kts", getSettingsGradleContent())
            fileCount += addZipEntry(zos, "build.gradle.kts", getRootBuildGradleContent())
            fileCount += addZipEntry(zos, "gradle/libs.versions.toml", getLibsVersionsTomlContent())
            fileCount += addZipEntry(zos, "DATA_SAFETY.md", getFileOrFallback(context, "/app/DATA_SAFETY.md", "# Data Safety Guide"))
            fileCount += addZipEntry(zos, "PLAY_STORE_CHECKLIST.md", getFileOrFallback(context, "/app/PLAY_STORE_CHECKLIST.md", "# Play Store Checklist"))
            fileCount += addZipEntry(zos, "metadata.json", getMetadataJsonContent())

            // 2. App Module Build & Config
            fileCount += addZipEntry(zos, "app/build.gradle.kts", getAppBuildGradleContent())
            fileCount += addZipEntry(zos, "app/proguard-rules.pro", "# Add project specific ProGuard rules here.\n-keep class com.example.data.** { *; }\n")
            fileCount += addZipEntry(zos, "app/src/main/AndroidManifest.xml", getAndroidManifestContent())

            // 3. App Resources
            fileCount += addZipEntry(zos, "app/src/main/res/values/strings.xml", getStringsXmlContent())
            fileCount += addZipEntry(zos, "app/src/main/res/xml/file_paths.xml", getFilePathsXmlContent())
            fileCount += addZipEntry(zos, "app/src/main/res/xml/device_admin.xml", getDeviceAdminXmlContent())

            // 4. Source code files
            val appSrcDir = File(context.filesDir.parentFile, "src/main/java")
            if (appSrcDir.exists()) {
                fileCount += zipDirectory(zos, appSrcDir, "app/src/main/java")
            } else {
                // Fallback: package core Kotlin entry points
                fileCount += addZipEntry(zos, "app/src/main/java/com/example/MainActivity.kt", getMainActivityContent())
                fileCount += addZipEntry(zos, "app/src/main/java/com/example/ui/Navigation.kt", getNavigationContent())
            }

            zos.flush()
        }

        totalSizeBytes = zipFile.length()
        Log.i(TAG, "Project exported successfully to ZIP: ${zipFile.absolutePath} ($fileCount files, $totalSizeBytes bytes)")

        return ZipExportResult(
            zipFile = zipFile,
            fileCount = fileCount,
            totalSizeBytes = totalSizeBytes
        )
    }

    /**
     * Shares the exported ZIP file using an Android ACTION_SEND intent.
     */
    fun shareZipFile(context: Context, zipFile: File) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, zipFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "PhoneMigrate - Android Studio Project Export")
            putExtra(Intent.EXTRA_TEXT, "קובץ ZIP של פרויקט ה-Android Studio לשחזור, פיתוח ופריסה.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "שתף קובץ פרויקט ZIP ל-Android Studio")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun addZipEntry(zos: ZipOutputStream, entryPath: String, content: String): Int {
        val bytes = content.toByteArray(Charsets.UTF_8)
        val entry = ZipEntry(entryPath)
        entry.size = bytes.size.toLong()
        zos.putNextEntry(entry)
        zos.write(bytes)
        zos.closeEntry()
        return 1
    }

    private fun zipDirectory(zos: ZipOutputStream, folder: File, baseName: String): Int {
        var count = 0
        val files = folder.listFiles() ?: return 0
        for (file in files) {
            val childPath = "$baseName/${file.name}"
            if (file.isDirectory) {
                count += zipDirectory(zos, file, childPath)
            } else {
                val bytes = file.readBytes()
                val entry = ZipEntry(childPath)
                entry.size = bytes.size.toLong()
                zos.putNextEntry(entry)
                zos.write(bytes)
                zos.closeEntry()
                count++
            }
        }
        return count
    }

    private fun getFileOrFallback(context: Context, path: String, fallback: String): String {
        return try {
            val f = File(path)
            if (f.exists()) f.readText() else fallback
        } catch (e: Exception) {
            fallback
        }
    }

    private fun getSettingsGradleContent(): String = """
        pluginManagement {
            repositories {
                google()
                mavenCentral()
                gradlePluginPortal()
            }
        }
        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                google()
                mavenCentral()
            }
        }
        rootProject.name = "PhoneMigrate"
        include(":app")
    """.trimIndent()

    private fun getRootBuildGradleContent(): String = """
        // Top-level build file where you can add configuration options common to all sub-projects/modules.
        plugins {
            alias(libs.plugins.android.application) apply false
            alias(libs.plugins.kotlin.android) apply false
            alias(libs.plugins.kotlin.compose) apply false
            alias(libs.plugins.ksp) apply false
        }
    """.trimIndent()

    private fun getLibsVersionsTomlContent(): String = """
        [versions]
        agp = "8.7.3"
        kotlin = "2.0.21"
        coreKtx = "1.15.0"
        lifecycleRuntimeKtx = "2.8.7"
        activityCompose = "1.9.3"
        composeBom = "2024.11.00"

        [libraries]
        androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
        androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
        androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
        androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }

        [plugins]
        android-application = { id = "com.android.application", version.ref = "agp" }
        kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
        kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
        ksp = { id = "com.google.devtools.ksp", version = "2.0.21-1.0.27" }
    """.trimIndent()

    private fun getMetadataJsonContent(): String = """
        {
          "name": "PhoneMigrate",
          "description": "Smart Device Migration & Android Studio Exportable Project",
          "requestFramePermissions": [],
          "majorCapabilities": [
            "MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API"
          ]
        }
    """.trimIndent()

    private fun getAppBuildGradleContent(): String = """
        plugins {
            alias(libs.plugins.android.application)
            alias(libs.plugins.kotlin.android)
            alias(libs.plugins.kotlin.compose)
            alias(libs.plugins.ksp)
        }

        android {
            namespace = "com.example"
            compileSdk = 35

            defaultConfig {
                applicationId = "com.aistudio.phonemigrate"
                minSdk = 26
                targetSdk = 35
                versionCode = 1
                versionName = "1.0.0"
            }
        }
    """.trimIndent()

    private fun getAndroidManifestContent(): String = """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest xmlns:android="http://schemas.android.com/apk/res/android">
            <uses-permission android:name="android.permission.INTERNET" />
            <uses-permission android:name="android.permission.READ_CONTACTS" />
            <application
                android:allowBackup="true"
                android:icon="@mipmap/ic_launcher"
                android:label="@string/app_name"
                android:supportsRtl="true"
                android:theme="@style/Theme.MyApplication">
                <activity
                    android:name=".MainActivity"
                    android:exported="true">
                    <intent-filter>
                        <action android:name="android.intent.action.MAIN" />
                        <category android:name="android.intent.category.LAUNCHER" />
                    </intent-filter>
                </activity>
            </application>
        </manifest>
    """.trimIndent()

    private fun getStringsXmlContent(): String = """
        <resources>
            <string name="app_name">PhoneMigrate</string>
        </resources>
    """.trimIndent()

    private fun getFilePathsXmlContent(): String = """
        <?xml version="1.0" encoding="utf-8"?>
        <paths xmlns:android="http://schemas.android.com/apk/res/android">
            <external-path name="external_files" path="." />
            <cache-path name="cache_files" path="." />
        </paths>
    """.trimIndent()

    private fun getDeviceAdminXmlContent(): String = """
        <?xml version="1.0" encoding="utf-8"?>
        <device-admin xmlns:android="http://schemas.android.com/apk/res/android">
            <uses-policies>
                <limit-password />
                <watch-login />
                <reset-password />
                <force-lock />
                <wipe-data />
            </uses-policies>
        </device-admin>
    """.trimIndent()

    private fun getMainActivityContent(): String = """
        package com.example

        import android.os.Bundle
        import androidx.activity.ComponentActivity
        import androidx.activity.compose.setContent
        import com.example.ui.MigrationNavigation
        import com.example.ui.theme.MyApplicationTheme

        class MainActivity : ComponentActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                    MyApplicationTheme {
                        MigrationNavigation()
                    }
                }
            }
        }
    """.trimIndent()

    private fun getNavigationContent(): String = """
        package com.example.ui

        import androidx.compose.runtime.Composable
        import androidx.navigation.compose.NavHost
        import androidx.navigation.compose.composable
        import androidx.navigation.compose.rememberNavController
        import com.example.ui.screens.WelcomeScreen

        @Composable
        fun MigrationNavigation() {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "welcome") {
                composable("welcome") {
                    WelcomeScreen(onStartClick = {})
                }
            }
        }
    """.trimIndent()
}
