package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppEntity
import com.example.data.database.ContactEntity
import com.example.data.database.MediaEntity
import com.example.data.database.MigrationDatabase
import com.example.domain.*
import com.example.scanner.DeepScanner
import com.example.service.AppQueryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScanState(
    val running: Boolean = false,
    val progress: Int = 0,
    val stage: String = "",
    val report: ScanReport? = null,
    val inventory: List<InventoryItem> = emptyList(),
    val checklist: List<ChecklistItem> = emptyList(),
    val error: String? = null
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ScanState())
    val state = _state.asStateFlow()
    
    private val scanner = DeepScanner(application)
    private val appQueryService = AppQueryService(application)
    private val db = MigrationDatabase.getDatabase(application)
    private val dao = db.migrationDao()

    init {
        loadFromDatabase()
    }

    private fun loadFromDatabase() {
        viewModelScope.launch {
            try {
                val apps = dao.getAllApps()
                val media = dao.getMediaStatus()
                val contacts = dao.getContactStatus()

                if (apps.isNotEmpty() || media.isNotEmpty() || contacts != null) {
                    val inventory = mutableListOf<InventoryItem>()
                    val checklist = mutableListOf<ChecklistItem>()

                    if (contacts != null) {
                        val contactItem = InventoryItem(
                            id = "contacts",
                            name = "Contacts",
                            type = InventoryType.CONTACTS,
                            packageName = null,
                            size = 0L,
                            priority = Priority.ESSENTIAL,
                            category = "DATA"
                        )
                        inventory.add(contactItem)
                        checklist.add(
                            ChecklistItem(
                                id = "contacts",
                                title = "Contacts (${contacts.totalContacts} synced)",
                                source = CoverageSource.GOOGLE_BUILTIN,
                                instruction = "Verify contacts backup in Google Account settings",
                                action = "Google Backup",
                                verified = false,
                                completed = contacts.completed
                            )
                        )
                    }

                    val totalMediaSize = media.sumOf { it.totalSize }
                    val photosCount = media.find { it.mediaType == "PHOTOS" }?.totalCount ?: 0
                    val videosCount = media.find { it.mediaType == "VIDEOS" }?.totalCount ?: 0
                    if (photosCount > 0 || videosCount > 0) {
                        val mediaItem = InventoryItem(
                            id = "photos",
                            name = "Photos & Videos",
                            type = InventoryType.PHOTOS,
                            packageName = null,
                            size = totalMediaSize,
                            priority = Priority.ESSENTIAL,
                            category = "MEDIA"
                        )
                        inventory.add(mediaItem)
                        checklist.add(
                            ChecklistItem(
                                id = "photos",
                                title = "Photos & Videos ($photosCount photos, $videosCount videos)",
                                source = CoverageSource.GOOGLE_BUILTIN,
                                instruction = "Verify Photos back up & sync in Google Photos",
                                action = "Google Photos",
                                verified = false,
                                completed = media.any { it.completed }
                            )
                        )
                    }

                    apps.forEach { app ->
                        val appItem = InventoryItem(
                            id = app.packageName,
                            name = app.appName,
                            type = InventoryType.APP,
                            packageName = app.packageName,
                            size = 0L,
                            priority = if (app.category == "SECURITY" || app.category == "FINANCE") Priority.ESSENTIAL else Priority.NORMAL,
                            category = app.category
                        )
                        inventory.add(appItem)
                        
                        val decisionSource = if (app.category == "SECURITY" || app.category == "FINANCE") CoverageSource.MANUAL else CoverageSource.MANUFACTURER
                        val instruction = if (app.category == "SECURITY" || app.category == "FINANCE") "Log in manually or export tokens" else "Verify installation and app data"
                        val action = if (app.category == "SECURITY" || app.category == "FINANCE") "Manual Login/Export" else "Android Backup / Store"

                        checklist.add(
                            ChecklistItem(
                                id = app.packageName,
                                title = app.appName,
                                source = decisionSource,
                                instruction = instruction,
                                action = action,
                                verified = false,
                                completed = app.completed
                            )
                        )
                    }

                    _state.value = ScanState(
                        running = false,
                        progress = 100,
                        stage = "Complete",
                        report = ScanReport(
                            apps = apps.map { AppInfo(it.packageName, it.appName, it.versionName, it.installTime, it.canBackup) },
                            contacts = ContactSummary(contacts?.totalContacts ?: 0),
                            media = MediaSummary(photosCount, videosCount, totalMediaSize),
                            accounts = emptyList()
                        ),
                        inventory = inventory,
                        checklist = checklist
                    )
                }
            } catch (e: Exception) {
                // Handle fallback gracefully
            }
        }
    }

    fun startScan() {
        if (_state.value.running) return
        
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    running = true,
                    progress = 10,
                    stage = "Initializing Deep Scanner...",
                    error = null
                )
                
                // Clear out historical cache
                dao.clearApps()
                dao.clearMedia()
                dao.clearContacts()

                _state.value = _state.value.copy(progress = 35, stage = "Analyzing contacts, media and user-installed apps...")
                
                // Perform unified scan using the public scan API
                val scanReport = scanner.scan()
                
                _state.value = _state.value.copy(progress = 75, stage = "Querying installed application packages...")
                val resultApps = appQueryService.queryUserApplications()
                
                val appInfos = resultApps.map { 
                    AppInfo(
                        packageName = it.packageName,
                        appName = it.appName,
                        versionName = it.versionName,
                        installTime = it.installTime,
                        canBackup = it.canBackup
                    )
                }

                _state.value = _state.value.copy(progress = 90, stage = "Saving migration parameters to offline database...")

                // Persist scanning outcomes to SQLite Room
                val appEntities = resultApps.map { app ->
                    val category = classifyApp(app.packageName)
                    AppEntity(
                        packageName = app.packageName,
                        appName = app.appName,
                        versionName = app.versionName,
                        installTime = app.installTime,
                        canBackup = app.canBackup,
                        category = category,
                        completed = false
                    )
                }
                dao.insertApps(appEntities)

                val mediaEntities = listOf(
                    MediaEntity("PHOTOS", scanReport.media.photos, 0L, false),
                    MediaEntity("VIDEOS", scanReport.media.videos, 0L, false)
                )
                dao.insertMediaStatus(mediaEntities)

                val contactEntity = ContactEntity(totalContacts = scanReport.contacts.total, completed = false)
                dao.insertContactStatus(contactEntity)

                // Refresh state from database
                loadFromDatabase()

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    running = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun toggleChecklistItem(id: String) {
        viewModelScope.launch {
            try {
                if (id == "contacts") {
                    val current = dao.getContactStatus()
                    if (current != null) {
                        dao.insertContactStatus(current.copy(completed = !current.completed))
                    }
                } else if (id == "photos") {
                    val currentPhotos = dao.getMediaStatus().find { it.mediaType == "PHOTOS" }
                    val currentVideos = dao.getMediaStatus().find { it.mediaType == "VIDEOS" }
                    val isCompleted = currentPhotos?.completed == true
                    dao.insertMediaStatus(listOf(
                        MediaEntity("PHOTOS", currentPhotos?.totalCount ?: 0, currentPhotos?.totalSize ?: 0L, !isCompleted),
                        MediaEntity("VIDEOS", currentVideos?.totalCount ?: 0, currentVideos?.totalSize ?: 0L, !isCompleted)
                    ))
                } else {
                    val app = dao.getAllApps().find { it.packageName == id }
                    if (app != null) {
                        dao.updateAppCompletion(id, !app.completed)
                    }
                }
                loadFromDatabase()
            } catch (e: Exception) {
                // Silently handle
            }
        }
    }

    private fun classifyApp(packageName: String): String {
        return when {
            packageName.contains("auth") || packageName.contains("security") || packageName.contains("pass") -> "SECURITY"
            packageName.contains("bank") || packageName.contains("pay") || packageName.contains("wallet") || packageName.contains("finance") -> "FINANCE"
            packageName.contains("chat") || packageName.contains("social") || packageName.contains("messenger") || packageName.contains("facebook") || packageName.contains("instagram") -> "SOCIAL"
            else -> "UTILITIES"
        }
    }
}

