package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AdLogEntity::class, TripEntity::class, PartnerCampaignEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DriveDatabase : RoomDatabase() {

    abstract fun driveDao(): DriveDao

    companion object {
        @Volatile
        private var INSTANCE: DriveDatabase? = null

        fun getDatabase(context: Context): DriveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DriveDatabase::class.java,
                    "drive_ai_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
