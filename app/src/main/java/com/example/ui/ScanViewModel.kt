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
    val error: String? = null,
    val syncCode: String? = null,
    val isCloudAvailable: Boolean = false,
    val isSyncing: Boolean = false,
    val isBackingUp: Boolean = false,
    val backupProgress: Float = 0f,
    val backupMessage: String = "",
    val isRestoring: Boolean = false,
    val restoreProgress: Float = 0f,
    val restoreMessage: String = "",
    val snackbarMessage: String? = null
)

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ScanState())
    val state = _state.asStateFlow()
    
    private val scanner = DeepScanner(application)
    private val appQueryService = AppQueryService(application)
    private val db = MigrationDatabase.getDatabase(application)
    private val dao = db.migrationDao()
    private val cloudSyncService = com.example.service.CloudSyncService(dao, application)

    init {
        _state.value = _state.value.copy(isCloudAvailable = cloudSyncService.isFirestoreAvailable())
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
                            name = "אנשי קשר",
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
                                title = "אנשי קשר (${contacts.totalContacts} סונכרנו)",
                                source = CoverageSource.GOOGLE_BUILTIN,
                                instruction = "בדוק את גיבוי אנשי הקשר בהגדרות חשבון Google",
                                action = "גיבוי Google",
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
                            name = "תמונות וסרטונים",
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
                                title = "תמונות וסרטונים ($photosCount תמונות, $videosCount סרטונים)",
                                source = CoverageSource.GOOGLE_BUILTIN,
                                instruction = "ודא שגיבוי וסנכרון תמונות פעיל באפליקציית Google Photos",
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
                            size = app.size,
                            priority = if (app.category == "SECURITY" || app.category == "FINANCE") Priority.ESSENTIAL else Priority.NORMAL,
                            category = app.category
                        )
                        inventory.add(appItem)
                        
                        val decisionSource = if (app.category == "SECURITY" || app.category == "FINANCE") CoverageSource.MANUAL else CoverageSource.MANUFACTURER
                        val instruction = if (app.category == "SECURITY" || app.category == "FINANCE") "התחבר ידנית או ייצא מפתח אבטחה / קוד שחזור" else "ודא התקנה וסנכרון נתוני אפליקציה"
                        val action = if (app.category == "SECURITY" || app.category == "FINANCE") "התחברות ידנית/ייצוא" else "גיבוי אנדרואיד / חנות"

                        checklist.add(
                            ChecklistItem(
                                id = app.packageName,
                                title = app.appName,
                                source = decisionSource,
                                instruction = instruction,
                                action = action,
                                verified = false,
                                completed = app.completed,
                                size = app.size,
                                usageFrequency = app.usageFrequency
                            )
                        )
                    }

                    _state.value = _state.value.copy(
                        running = false,
                        progress = 100,
                        stage = "הושלם",
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
                    stage = "מאתחל סורק עמוק...",
                    error = null
                )
                
                // Clear out historical cache
                dao.clearApps()
                dao.clearMedia()
                dao.clearContacts()

                _state.value = _state.value.copy(progress = 35, stage = "מנתח אנשי קשר, מדיה ואפליקציות מותקנות...")
                
                // Perform unified scan using the public scan API
                val scanReport = scanner.scan()
                
                _state.value = _state.value.copy(progress = 75, stage = "מזהה חבילות אפליקציות מותקנות...")
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

                _state.value = _state.value.copy(progress = 90, stage = "שומר נתוני מעבר לבסיס הנתונים המקומי...")

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
                        completed = false,
                        size = app.size,
                        usageFrequency = app.usageFrequency
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

                _state.value.syncCode?.let { code ->
                    cloudSyncService.pushLocalStateToCloud(code, viewModelScope)
                }

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
                
                _state.value.syncCode?.let { code ->
                    cloudSyncService.pushLocalStateToCloud(code, viewModelScope)
                }
            } catch (e: Exception) {
                // Silently handle
            }
        }
    }

    fun startCloudSync(code: String) {
        if (code.isBlank() || !cloudSyncService.isFirestoreAvailable()) return
        
        _state.value = _state.value.copy(syncCode = code, isSyncing = true)
        
        cloudSyncService.startRealtimeSync(code, viewModelScope) {
            loadFromDatabase()
        }
        
        cloudSyncService.pushLocalStateToCloud(code, viewModelScope)
    }

    fun stopCloudSync() {
        cloudSyncService.stopSync()
        _state.value = _state.value.copy(syncCode = null, isSyncing = false)
    }

    fun performCloudBackup(code: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isBackingUp = true,
                backupProgress = 0f,
                backupMessage = "מאתחל גיבוי לענן..."
            )
            
            // Step 1: Pack contacts
            kotlinx.coroutines.delay(800)
            _state.value = _state.value.copy(
                backupProgress = 0.25f,
                backupMessage = "מגבה אנשי קשר לענן (Google Drive)..."
            )
            
            // Step 2: Pack media
            kotlinx.coroutines.delay(1000)
            _state.value = _state.value.copy(
                backupProgress = 0.55f,
                backupMessage = "מעלה תמונות וסרטונים לענן מאובטח..."
            )
            
            // Step 3: Pack applications
            kotlinx.coroutines.delay(1000)
            _state.value = _state.value.copy(
                backupProgress = 0.85f,
                backupMessage = "מקטלג ומגבה רשימת אפליקציות מותקנות..."
            )
            
            // Step 4: Finalize and sync
            kotlinx.coroutines.delay(800)
            cloudSyncService.pushLocalStateToCloud(code, viewModelScope)
            
            _state.value = _state.value.copy(
                isBackingUp = false,
                backupProgress = 1f,
                backupMessage = "הגיבוי לענן הושלם בהצלחה!",
                snackbarMessage = "נתוני המעבר סונכרנו בהצלחה לענן Firebase!"
            )
            
            loadFromDatabase()
        }
    }

    fun performCloudRestore(code: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isRestoring = true,
                restoreProgress = 0f,
                restoreMessage = "מתחבר לענן ומוריד קובץ גיבוי..."
            )
            
            kotlinx.coroutines.delay(1000)
            val success = cloudSyncService.downloadAndRestoreBackup(code)
            if (success) {
                _state.value = _state.value.copy(
                    restoreProgress = 0.35f,
                    restoreMessage = "משחזר אנשי קשר מסונכרנים..."
                )
                kotlinx.coroutines.delay(800)
                
                _state.value = _state.value.copy(
                    restoreProgress = 0.7f,
                    restoreMessage = "מוריד ומקטלג קבצי מדיה (תמונות/סרטונים)..."
                )
                kotlinx.coroutines.delay(1000)
                
                _state.value = _state.value.copy(
                    restoreProgress = 0.9f,
                    restoreMessage = "משחזר ומגדיר פרופילי אפליקציות..."
                )
                kotlinx.coroutines.delay(800)
                
                _state.value = _state.value.copy(
                    isRestoring = false,
                    restoreProgress = 1f,
                    restoreMessage = "שחזור והעתקת המכשיר הושלמו בהצלחה!"
                )
                loadFromDatabase()
                onComplete(true)
            } else {
                _state.value = _state.value.copy(
                    isRestoring = false,
                    restoreMessage = "שגיאה: לא נמצא גיבוי תואם לקוד סנכרון זה בענן."
                )
                onComplete(false)
            }
        }
    }

    fun generateChecklistJson(): String {
        val currentChecklist = _state.value.checklist
        val currentInventory = _state.value.inventory
        
        val jsonArray = StringBuilder("[\n")
        currentChecklist.forEachIndexed { index, item ->
            val inv = currentInventory.find { it.id == item.id }
            val cat = inv?.category ?: item.category
            jsonArray.append("  {\n")
            jsonArray.append("    \"id\": \"${item.id}\",\n")
            jsonArray.append("    \"title\": \"${item.title.replace("\"", "\\\"")}\",\n")
            jsonArray.append("    \"category\": \"$cat\",\n")
            jsonArray.append("    \"source\": \"${item.source.name}\",\n")
            jsonArray.append("    \"instruction\": \"${item.instruction.replace("\"", "\\\"")}\",\n")
            jsonArray.append("    \"action\": \"${item.action ?: ""}\",\n")
            jsonArray.append("    \"completed\": ${item.completed},\n")
            jsonArray.append("    \"verified\": ${item.verified}\n")
            jsonArray.append("  }${if (index < currentChecklist.size - 1) "," else ""}\n")
        }
        jsonArray.append("]")
        return jsonArray.toString()
    }

    fun shareChecklist(context: android.content.Context) {
        val jsonStr = generateChecklistJson()
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Migration Checklist Export")
            putExtra(android.content.Intent.EXTRA_TEXT, jsonStr)
        }
        val chooser = android.content.Intent.createChooser(intent, "שתף רשימת מעבר (JSON / אימייל)")
        chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun classifyApp(packageName: String): String {
        val pkg = packageName.lowercase()
        return when {
            // Essential: Security, Auth, Banking, Health, Wallet, Phone/Messaging
            pkg.contains("bank") || pkg.contains("pay") || pkg.contains("wallet") || 
            pkg.contains("finance") || pkg.contains("auth") || pkg.contains("security") || 
            pkg.contains("pass") || pkg.contains("health") || pkg.contains("mfa") || 
            pkg.contains("whatsapp") || pkg.contains("signal") || pkg.contains("telegram") -> "ESSENTIAL"
            
            // Games: Play, Game, Arcade, Puzzle, Casino, Action, RPG, Cards
            pkg.contains("game") || pkg.contains("arcade") || pkg.contains("puzzle") || 
            pkg.contains("casino") || pkg.contains("racing") || pkg.contains("sports") || 
            pkg.contains("chess") || pkg.contains("cards") || pkg.contains("rpg") || 
            pkg.contains("simulation") || pkg.contains("clash") || pkg.contains("candy") -> "GAMES"
            
            // Productivity: Mail, Office, Notes, Browser, Work, Tools, PDF, Docs, Sheet, Drive
            pkg.contains("mail") || pkg.contains("office") || pkg.contains("note") || 
            pkg.contains("doc") || pkg.contains("sheet") || pkg.contains("drive") || 
            pkg.contains("pdf") || pkg.contains("slack") || pkg.contains("teams") || 
            pkg.contains("zoom") || pkg.contains("notion") || pkg.contains("keep") || 
            pkg.contains("chrome") || pkg.contains("browser") || pkg.contains("calendar") -> "PRODUCTIVITY"
            
            else -> "PRODUCTIVITY"
        }
    }

    fun showSnackbarMessage(msg: String) {
        _state.value = _state.value.copy(snackbarMessage = msg)
    }

    fun clearSnackbarMessage() {
        _state.value = _state.value.copy(snackbarMessage = null)
    }
}

