package com.example.domain

enum class InventoryType { APP, CONTACTS, PHOTOS, VIDEOS, ACCOUNT, DOCUMENT }
enum class Priority { ESSENTIAL, NORMAL, OPTIONAL }
enum class CoverageSource { GOOGLE_BUILTIN, MANUFACTURER, AUTO_VERIFIED, MANUAL }

data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val installTime: Long,
    val canBackup: Boolean
)

data class ContactSummary(val total: Int)
data class MediaSummary(val photos: Int, val videos: Int, val totalSize: Long)

data class InventoryItem(
    val id: String,
    val name: String,
    val type: InventoryType,
    val packageName: String?,
    val size: Long,
    val priority: Priority,
    val category: String
)

data class ScanReport(
    val apps: List<AppInfo>,
    val contacts: ContactSummary,
    val media: MediaSummary,
    val accounts: List<String>
)

data class CoverageDecision(
    val source: CoverageSource,
    val method: String,
    val instruction: String,
    val verified: Boolean = false
)

data class ChecklistItem(
    val id: String,
    val title: String,
    val source: CoverageSource,
    val instruction: String,
    val action: String?,
    val verified: Boolean,
    val completed: Boolean
)
