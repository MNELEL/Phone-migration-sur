package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AppEntity::class, MediaEntity::class, ContactEntity::class, ContentItem::class],
    version = 3,
    exportSchema = false
)
abstract class MigrationDatabase : RoomDatabase() {
    abstract fun migrationDao(): MigrationDao
    abstract fun contentItemDao(): ContentItemDao

    companion object {
        @Volatile
        private var INSTANCE: MigrationDatabase? = null

        fun getDatabase(context: Context): MigrationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MigrationDatabase::class.java,
                    "migration_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
