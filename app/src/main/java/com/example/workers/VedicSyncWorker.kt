package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.model.BirthProfile
import com.example.core.model.WatchFaceStyle
import com.example.data.CityRepository
import com.example.data.UserPreferencesRepository
import com.example.data.VedicWearableSyncManager
import com.example.engines.personalization.VedicPersonalizationEngine
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class VedicSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("VedicSyncWorker", "Running background sync for Wear OS...")
        
        try {
            // Retrieve latest user preferences
            val prefsRepo = UserPreferencesRepository(context)
            val isBackgroundSyncEnabled = prefsRepo.wearBackgroundSyncEnabledFlow.first()
            if (!isBackgroundSyncEnabled) {
                Log.d("VedicSyncWorker", "Wear OS background sync is disabled by user to save battery. Skipping periodic sync.")
                return Result.success()
            }

            val birthProfile = prefsRepo.birthProfileFlow.first() ?: return Result.success() // Or fetch default profile
            
            val now = ZonedDateTime.now(ZoneId.of(birthProfile.birthLocation.timezoneId))
            
            val personalizationEngine = VedicPersonalizationEngine()
            val personalizedResult = personalizationEngine.createPersonalizedProfile(birthProfile)

            val dailyResult = personalizationEngine.createDailyVedicProfile(
                now = now,
                location = birthProfile.birthLocation,
                ayanamsha = birthProfile.ayanamsha,
                tradition = birthProfile.calendarTradition,
                personalizedProfile = personalizedResult
            )
            
            // Using a default style here, though ideally it should be cached
            val watchConfig = personalizationEngine.generateWatchFaceConfig(personalizedResult, WatchFaceStyle.COSMIC)

            // Sync to Wear OS
            // Because we removed the timestamp from the DataMap in VedicWearableSyncManager,
            // the Wearable API will automatically deduplicate and ONLY wake up the watch 
            // if the Hora, Muhurat, or other data has significantly changed!
            VedicWearableSyncManager.syncProfileAndHoroscopeToWatch(
                context = context,
                profile = birthProfile,
                personalizedProfile = personalizedResult,
                dailyProfile = dailyResult,
                watchConfig = watchConfig
            )
            
            Log.d("VedicSyncWorker", "Background sync completed successfully.")
            return Result.success()
        } catch (e: Exception) {
            Log.e("VedicSyncWorker", "Failed to perform background sync", e)
            return Result.retry()
        }
    }
}
