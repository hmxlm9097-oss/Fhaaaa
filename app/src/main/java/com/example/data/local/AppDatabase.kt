package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CloudPhoneEntity::class,
        ActivityLogEntity::class,
        LocationHistoryEntity::class,
        FileTransferEntity::class,
        DeviceSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cloudPhoneDao(): CloudPhoneDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun locationHistoryDao(): LocationHistoryDao
    abstract fun fileTransferDao(): FileTransferDao
    abstract fun deviceSessionDao(): DeviceSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "h4xml_cloud_phone.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
