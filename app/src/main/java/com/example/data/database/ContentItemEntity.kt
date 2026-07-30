package com.example.data.database

import androidx.room.Entity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "content_items")
data class ContentItem(
    @PrimaryKey val id: String,
    val itemType: String, // "APP", "MEDIA", "CONTACT"
    val name: String,
    val sizeOrCount: Long,
    val isSynced: Boolean = false,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val metadataJson: String = ""
)

@Dao
interface ContentItemDao {
    @Query("SELECT * FROM content_items ORDER BY itemType, name ASC")
    fun getAllContentItemsFlow(): Flow<List<ContentItem>>

    @Query("SELECT * FROM content_items WHERE itemType = :type ORDER BY name ASC")
    fun getContentItemsByTypeFlow(type: String): Flow<List<ContentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentItem(item: ContentItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContentItems(items: List<ContentItem>)

    @Query("UPDATE content_items SET isSynced = :synced, lastSyncTimestamp = :timestamp WHERE id = :id")
    suspend fun updateSyncStatus(id: String, synced: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM content_items WHERE id = :id")
    suspend fun deleteContentItem(id: String)

    @Query("DELETE FROM content_items")
    suspend fun clearAll()
}
