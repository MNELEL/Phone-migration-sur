package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.database.MigrationDao
import com.example.data.database.AppEntity
import com.example.data.database.MediaEntity
import com.example.data.database.ContactEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CloudSyncService(
    private val dao: MigrationDao,
    private val context: Context? = null
) {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            context?.let { ctx ->
                if (FirebaseApp.getApps(ctx).isEmpty()) {
                    try {
                        FirebaseApp.initializeApp(ctx)
                    } catch (e: Exception) {
                        try {
                            val options = FirebaseOptions.Builder()
                                .setProjectId("phone-migration-demo")
                                .setApplicationId("1:1234567890:android:abcdef123456")
                                .setApiKey("AIzaSyDemoKeyForLocalTestingMode12345")
                                .build()
                            FirebaseApp.initializeApp(ctx, options)
                        } catch (ex: Exception) {
                            Log.i("CloudSyncService", "Custom FirebaseApp init skipped: ${ex.message}")
                        }
                    }
                }
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.i("CloudSyncService", "Firebase Firestore unavailable or running in offline mode: ${e.message}")
            null
        }
    }

    private var isUploading = false
    private var activeListener: ListenerRegistration? = null

    fun isFirestoreAvailable(): Boolean = firestore != null

    // Start listening to cloud updates for a specific sync code/account ID
    fun startRealtimeSync(syncCode: String, scope: CoroutineScope, onSyncCompleted: () -> Unit) {
        val fs = firestore ?: return
        activeListener?.remove()

        try {
            val docRef = fs.collection("checklists").document(syncCode)
            
            // Add snapshot listener
            activeListener = docRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("CloudSyncService", "Listen failed: $e")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    scope.launch(Dispatchers.IO) {
                        if (!isUploading) {
                            applyCloudSnapshotToLocalDb(snapshot)
                            onSyncCompleted()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CloudSyncService", "Error setting up listener: ${e.message}")
        }
    }

    fun stopSync() {
        activeListener?.remove()
        activeListener = null
    }

    // Push local state to Firestore
    fun pushLocalStateToCloud(syncCode: String, scope: CoroutineScope) {
        val fs = firestore ?: return
        if (isUploading) return
        
        scope.launch(Dispatchers.IO) {
            isUploading = true
            try {
                val apps = dao.getAllApps()
                val media = dao.getMediaStatus()
                val contacts = dao.getContactStatus()

                val appCompletionMap = apps.associate { it.packageName to it.completed }
                val mediaCompletionMap = media.associate { it.mediaType to it.completed }
                val contactsCompleted = contacts?.completed ?: false

                val appDetailsList = apps.map {
                    mapOf(
                        "packageName" to it.packageName,
                        "appName" to it.appName,
                        "versionName" to it.versionName,
                        "installTime" to it.installTime,
                        "canBackup" to it.canBackup,
                        "category" to it.category,
                        "completed" to it.completed
                    )
                }

                val mediaDetailsList = media.map {
                    mapOf(
                        "mediaType" to it.mediaType,
                        "totalCount" to it.totalCount,
                        "totalSize" to it.totalSize,
                        "completed" to it.completed
                    )
                }

                val contactDetailsMap = mapOf(
                    "totalContacts" to (contacts?.totalContacts ?: 0),
                    "completed" to contactsCompleted
                )

                val payload = mapOf(
                    "syncCode" to syncCode,
                    "apps" to appCompletionMap,
                    "media" to mediaCompletionMap,
                    "contactsCompleted" to contactsCompleted,
                    "appDetails" to appDetailsList,
                    "mediaDetails" to mediaDetailsList,
                    "contactDetails" to contactDetailsMap,
                    "hasFullBackup" to true,
                    "updatedAt" to System.currentTimeMillis()
                )

                fs.collection("checklists").document(syncCode).set(payload)
                    .addOnFailureListener { exception ->
                        Log.e("CloudSyncService", "Failed to upload: ${exception.message}")
                    }
            } catch (e: Exception) {
                Log.e("CloudSyncService", "Failed to upload local state: ${e.message}")
            } finally {
                isUploading = false
            }
        }
    }

    // Download and restore a backup from the cloud to the local database
    suspend fun downloadAndRestoreBackup(syncCode: String): Boolean {
        val fs = firestore ?: return false
        try {
            val document = fs.collection("checklists").document(syncCode).get().await()
            if (!document.exists()) return false

            val hasFullBackup = document.getBoolean("hasFullBackup") ?: false
            if (!hasFullBackup) return false

            // Clear existing database
            dao.clearApps()
            dao.clearMedia()
            dao.clearContacts()

            // Restore Apps
            val appDetails = document.get("appDetails") as? List<Map<String, Any>>
            if (appDetails != null) {
                val appsToInsert = appDetails.map { map ->
                    AppEntity(
                        packageName = map["packageName"] as? String ?: "",
                        appName = map["appName"] as? String ?: "",
                        versionName = map["versionName"] as? String ?: "",
                        installTime = (map["installTime"] as? Number)?.toLong() ?: 0L,
                        canBackup = map["canBackup"] as? Boolean ?: true,
                        category = map["category"] as? String ?: "OTHER",
                        completed = true // Fully restored
                    )
                }
                dao.insertApps(appsToInsert)
            }

            // Restore Media
            val mediaDetails = document.get("mediaDetails") as? List<Map<String, Any>>
            if (mediaDetails != null) {
                val mediaToInsert = mediaDetails.map { map ->
                    MediaEntity(
                        mediaType = map["mediaType"] as? String ?: "",
                        totalCount = (map["totalCount"] as? Number)?.toInt() ?: 0,
                        totalSize = (map["totalSize"] as? Number)?.toLong() ?: 0L,
                        completed = true // Fully restored
                    )
                }
                dao.insertMediaStatus(mediaToInsert)
            }

            // Restore Contacts
            val contactDetails = document.get("contactDetails") as? Map<String, Any>
            if (contactDetails != null) {
                val contactsToInsert = ContactEntity(
                    totalContacts = (contactDetails["totalContacts"] as? Number)?.toInt() ?: 0,
                    completed = true // Fully restored
                )
                dao.insertContactStatus(contactsToInsert)
            }

            return true
        } catch (e: Exception) {
            Log.e("CloudSyncService", "Error during cloud restore: ${e.message}")
            return false
        }
    }

    // Apply snapshots from Firestore to local Room
    private suspend fun applyCloudSnapshotToLocalDb(snapshot: DocumentSnapshot) {
        try {
            val appsMap = snapshot.get("apps") as? Map<*, *>
            val mediaMap = snapshot.get("media") as? Map<*, *>
            val contactsCompleted = snapshot.getBoolean("contactsCompleted") ?: false

            appsMap?.forEach { (pkg, completed) ->
                val packageName = pkg as? String ?: return@forEach
                val isCompleted = completed as? Boolean ?: return@forEach
                val app = dao.getAllApps().find { it.packageName == packageName }
                if (app != null && app.completed != isCompleted) {
                    dao.updateAppCompletion(packageName, isCompleted)
                }
            }

            mediaMap?.forEach { (type, completed) ->
                val mediaType = type as? String ?: return@forEach
                val isCompleted = completed as? Boolean ?: return@forEach
                val currentMedia = dao.getMediaStatus().find { it.mediaType == mediaType }
                if (currentMedia != null && currentMedia.completed != isCompleted) {
                    dao.updateMediaCompletion(mediaType, isCompleted)
                }
            }

            val contacts = dao.getContactStatus()
            if (contacts != null && contacts.completed != contactsCompleted) {
                dao.updateContactCompletion(contactsCompleted)
            }
        } catch (e: Exception) {
            Log.e("CloudSyncService", "Error parsing snapshot: ${e.message}")
        }
    }
}
