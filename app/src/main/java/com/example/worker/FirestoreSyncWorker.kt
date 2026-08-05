package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.data.database.MigrationDatabase
import com.example.service.AppCategorizationService
import com.example.service.AppQueryService
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class FirestoreSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "FirestoreSyncWorker"
        const val KEY_SYNC_CODE = "syncCode"
        const val PREFS_NAME = "migration_sync_prefs"
        const val PREF_KEY_LAST_SYNC_CODE = "last_sync_code"
        const val PREF_KEY_LAST_SYNC_TIME = "last_sync_time"
        const val UNIQUE_WORK_NAME_ONETIME = "firestore_one_time_migration_sync"
        const val UNIQUE_WORK_NAME_PERIODIC = "firestore_periodic_migration_sync"

        fun scheduleOneTimeSync(context: Context, syncCode: String? = null) {
            val codeToUse = syncCode ?: getSavedSyncCode(context) ?: "DEFAULT_USER_BACKUP"
            saveSyncCode(context, codeToUse)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputData = Data.Builder()
                .putString(KEY_SYNC_CODE, codeToUse)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<FirestoreSyncWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME_ONETIME,
                ExistingWorkPolicy.REPLACE,
                syncWorkRequest
            )
            Log.i(TAG, "Enqueued one-time Firestore migration sync work for code: $codeToUse")
        }

        fun schedulePeriodicSync(context: Context, syncCode: String? = null) {
            val codeToUse = syncCode ?: getSavedSyncCode(context) ?: "DEFAULT_USER_BACKUP"
            saveSyncCode(context, codeToUse)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputData = Data.Builder()
                .putString(KEY_SYNC_CODE, codeToUse)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<FirestoreSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )
            Log.i(TAG, "Enqueued periodic Firestore migration sync work for code: $codeToUse")
        }

        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME_PERIODIC)
        }

        private fun saveSyncCode(context: Context, syncCode: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(PREF_KEY_LAST_SYNC_CODE, syncCode).apply()
        }

        fun getSavedSyncCode(context: Context): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(PREF_KEY_LAST_SYNC_CODE, null)
        }

        fun getLastSyncTime(context: Context): Long {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getLong(PREF_KEY_LAST_SYNC_TIME, 0L)
        }
    }

    override suspend fun doWork(): Result {
        val syncCode = inputData.getString(KEY_SYNC_CODE)
            ?: getSavedSyncCode(context)
            ?: "DEFAULT_USER_BACKUP"

        Log.i(TAG, "Starting WorkManager Firestore sync task for syncCode: $syncCode")

        return try {
            val firestore = initFirestore(context)
            if (firestore == null) {
                Log.w(TAG, "Firestore is unavailable. WorkManager task will retry later.")
                return Result.retry()
            }

            val db = MigrationDatabase.getDatabase(context)
            val dao = db.migrationDao()
            val categorizationService = AppCategorizationService()
            val appQueryService = AppQueryService(context)

            // Fetch local migration data from Room DB
            val storedApps = dao.getAllApps()
            val mediaStatus = dao.getMediaStatus()
            val contactStatus = dao.getContactStatus()

            // Fetch installed apps from system to ensure accurate categorization
            val installedUserApps = try {
                appQueryService.queryUserApplications()
            } catch (e: Exception) {
                emptyList()
            }

            // Group applications by folders
            val folderGroups = categorizationService.groupAppsByFolder(
                apps = if (installedUserApps.isNotEmpty()) installedUserApps else storedApps,
                getPackageName = {
                    if (it is com.example.service.UserAppInfo) it.packageName else (it as com.example.data.database.AppEntity).packageName
                },
                getAppName = {
                    if (it is com.example.service.UserAppInfo) it.appName else (it as com.example.data.database.AppEntity).appName
                },
                getCategoryOverride = {
                    if (it is com.example.data.database.AppEntity) it.category else null
                }
            )

            val categorizedFoldersPayload = folderGroups.map { group ->
                mapOf(
                    "id" to group.category.id,
                    "title" to group.category.titleHebrew,
                    "description" to group.category.descriptionHebrew,
                    "appCount" to group.count,
                    "packages" to group.items.map { item ->
                        if (item is com.example.service.UserAppInfo) item.packageName else (item as com.example.data.database.AppEntity).packageName
                    }
                )
            }

            val appCompletionMap = storedApps.associate { it.packageName to it.completed }
            val mediaCompletionMap = mediaStatus.associate { it.mediaType to it.completed }
            val contactsCompleted = contactStatus?.completed ?: false

            val appDetailsList = storedApps.map { app ->
                val categoryEnum = categorizationService.classifyApp(app.packageName, app.appName)
                mapOf(
                    "packageName" to app.packageName,
                    "appName" to app.appName,
                    "versionName" to app.versionName,
                    "installTime" to app.installTime,
                    "canBackup" to app.canBackup,
                    "category" to app.category,
                    "folderCategory" to categoryEnum.id,
                    "folderTitle" to categoryEnum.titleHebrew,
                    "completed" to app.completed,
                    "size" to app.size,
                    "usageFrequency" to app.usageFrequency
                )
            }

            val mediaDetailsList = mediaStatus.map {
                mapOf(
                    "mediaType" to it.mediaType,
                    "totalCount" to it.totalCount,
                    "totalSize" to it.totalSize,
                    "completed" to it.completed
                )
            }

            val contactDetailsMap = mapOf(
                "totalContacts" to (contactStatus?.totalContacts ?: 0),
                "completed" to contactsCompleted
            )

            val now = System.currentTimeMillis()

            val payload = mapOf(
                "syncCode" to syncCode,
                "apps" to appCompletionMap,
                "media" to mediaCompletionMap,
                "contactsCompleted" to contactsCompleted,
                "appDetails" to appDetailsList,
                "categorizedFolders" to categorizedFoldersPayload,
                "mediaDetails" to mediaDetailsList,
                "contactDetails" to contactDetailsMap,
                "hasFullBackup" to true,
                "lastWorkManagerSync" to now,
                "updatedAt" to now
            )

            // Write payload to Firestore
            firestore.collection("checklists")
                .document(syncCode)
                .set(payload)
                .await()

            // Also write a user migration resume session document
            val migrationResumeDoc = mapOf(
                "syncCode" to syncCode,
                "deviceModel" to (android.os.Build.MODEL ?: "Android Device"),
                "androidVersion" to android.os.Build.VERSION.RELEASE,
                "totalApps" to appDetailsList.size,
                "completedAppsCount" to storedApps.count { it.completed },
                "categorizedFolderCount" to folderGroups.size,
                "lastSyncTime" to now,
                "status" to "READY_FOR_RESUME"
            )

            firestore.collection("user_migrations")
                .document(syncCode)
                .set(migrationResumeDoc)
                .await()

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putLong(PREF_KEY_LAST_SYNC_TIME, now).apply()

            Log.i(TAG, "Successfully synced categorized applications and migration data to Firestore via WorkManager.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in WorkManager Firestore sync task: ${e.message}", e)
            Result.retry()
        }
    }

    private fun initFirestore(context: Context): FirebaseFirestore? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(context)
                } catch (e: Exception) {
                    try {
                        val options = FirebaseOptions.Builder()
                            .setProjectId("phone-migration-demo")
                            .setApplicationId("1:1234567890:android:abcdef123456")
                            .setApiKey("AIzaSyDemoKeyForLocalTestingMode12345")
                            .build()
                        FirebaseApp.initializeApp(context, options)
                    } catch (ex: Exception) {
                        Log.i(TAG, "Custom FirebaseApp init skipped: ${ex.message}")
                    }
                }
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FirebaseFirestore: ${e.message}")
            null
        }
    }
}
