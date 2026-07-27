package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MigrationDao {

    @Query("SELECT * FROM detected_apps ORDER BY appName ASC")
    fun getAllAppsFlow(): Flow<List<AppEntity>>

    @Query("SELECT * FROM detected_apps ORDER BY appName ASC")
    suspend fun getAllApps(): List<AppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Query("UPDATE detected_apps SET completed = :completed WHERE packageName = :packageName")
    suspend fun updateAppCompletion(packageName: String, completed: Boolean)

    @Query("SELECT * FROM media_status")
    fun getMediaStatusFlow(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_status")
    suspend fun getMediaStatus(): List<MediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaStatus(media: List<MediaEntity>)

    @Query("UPDATE media_status SET completed = :completed WHERE mediaType = :mediaType")
    suspend fun updateMediaCompletion(mediaType: String, completed: Boolean)

    @Query("SELECT * FROM contact_status WHERE id = 'contacts_summary'")
    fun getContactStatusFlow(): Flow<ContactEntity?>

    @Query("SELECT * FROM contact_status WHERE id = 'contacts_summary'")
    suspend fun getContactStatus(): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactStatus(contact: ContactEntity)

    @Query("UPDATE contact_status SET completed = :completed WHERE id = 'contacts_summary'")
    suspend fun updateContactCompletion(completed: Boolean)

    @Query("DELETE FROM detected_apps")
    suspend fun clearApps()

    @Query("DELETE FROM media_status")
    suspend fun clearMedia()

    @Query("DELETE FROM contact_status")
    suspend fun clearContacts()
}
