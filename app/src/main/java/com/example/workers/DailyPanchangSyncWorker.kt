package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.model.AyanamshaSystem
import com.example.core.model.CalendarTradition
import com.example.data.CityRepository
import com.example.data.UserPreferencesRepository
import com.example.data.room.DailyPanchangEntity
import com.example.data.room.VedicDatabase
import com.example.engines.personalization.VedicPersonalizationEngine
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class DailyPanchangSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("DailyPanchangSyncWorker", "Starting daily Panchang sync to Room database...")
        try {
            val prefsRepo = UserPreferencesRepository(context)
            val birthProfile = prefsRepo.birthProfileFlow.first()
            val location = birthProfile?.birthLocation ?: CityRepository.getDefaultCity()

            val zoneId = ZoneId.of(location.timezoneId)
            val now = ZonedDateTime.now(zoneId)
            val dateStr = LocalDate.now(zoneId).toString()

            val personalizationEngine = VedicPersonalizationEngine()
            val personalizedResult = birthProfile?.let { personalizationEngine.createPersonalizedProfile(it) }
            val dailyProfile = personalizationEngine.createDailyVedicProfile(
                now = now,
                location = location,
                ayanamsha = birthProfile?.ayanamsha ?: AyanamshaSystem.LAHIRI,
                tradition = birthProfile?.calendarTradition ?: CalendarTradition.AMANTA,
                personalizedProfile = personalizedResult
            )

            val panchangEntity = DailyPanchangEntity.fromProfile(dateStr, dailyProfile)

            val db = VedicDatabase.getDatabase(context)
            db.dailyPanchangDao().insertPanchang(panchangEntity)

            Log.d("DailyPanchangSyncWorker", "Daily Panchang successfully synced to Room database for date: $dateStr")
            return Result.success()
        } catch (e: Exception) {
            Log.e("DailyPanchangSyncWorker", "Failed to sync daily Panchang to Room database", e)
            return Result.retry()
        }
    }
}
