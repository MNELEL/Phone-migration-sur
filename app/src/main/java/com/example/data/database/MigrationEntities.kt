package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detected_apps")
data class AppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val versionName: String,
    val installTime: Long,
    val canBackup: Boolean,
    val category: String,
    val completed: Boolean = false
)

@Entity(tableName = "media_status")
data class MediaEntity(
    @PrimaryKey val mediaType: String, // "PHOTOS" or "VIDEOS"
    val totalCount: Int,
    val totalSize: Long,
    val completed: Boolean = false
)

@Entity(tableName = "contact_status")
data class ContactEntity(
    @PrimaryKey val id: String = "contacts_summary",
    val totalContacts: Int,
    val completed: Boolean = false
)
