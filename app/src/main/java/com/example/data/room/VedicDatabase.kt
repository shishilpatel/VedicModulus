package com.example.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.util.Log
import com.example.data.room.report.*

@Database(
    entities = [
        UserProfileEntity::class,
        MeditationSessionEntity::class,
        PredictionCacheEntity::class,
        ClientProfileEntity::class,
        AppSettingEntity::class,
        DailyPanchangEntity::class,
        KundliReportEntity::class,
        ReportRemedyEntity::class,
        ReportConfigEntity::class,
        ReportAuditLogEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class VedicDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun meditationSessionDao(): MeditationSessionDao
    abstract fun predictionCacheDao(): PredictionCacheDao
    abstract fun clientProfileDao(): ClientProfileDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun dailyPanchangDao(): DailyPanchangDao
    abstract fun kundliReportDao(): KundliReportDao
    abstract fun reportRemedyDao(): ReportRemedyDao
    abstract fun reportConfigDao(): ReportConfigDao
    abstract fun reportAuditLogDao(): ReportAuditLogDao

    companion object {
        private const val TAG = "VedicDatabase"
        @Volatile
        private var INSTANCE: VedicDatabase? = null

        fun getDatabase(context: Context): VedicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VedicDatabase::class.java,
                    "vedic_profiles.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                Log.d(TAG, "VedicDatabase instance created successfully.")
                instance
            }
        }
    }
}
