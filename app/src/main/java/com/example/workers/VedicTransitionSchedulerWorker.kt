package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.core.model.BirthProfile
import com.example.data.UserPreferencesRepository
import com.example.engines.astronomy.AstronomicalEngineProvider
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Worker that calculates daily sunrise and sunset for the user's location,
 * and schedules notification tasks for those exact times.
 */
class VedicTransitionSchedulerWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "VedicTransitionSched"
        const val WORK_NAME = "VedicTransitionSchedulerPeriodicWork"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Executing VedicTransitionSchedulerWorker to calculate sunrise and sunset.")
        try {
            val prefsRepo = UserPreferencesRepository(context)
            // Optional: check if daily notifications are enabled before proceeding
            val isEnabled = prefsRepo.dailyNotificationsEnabledFlow.first()
            if (!isEnabled) {
                Log.d(TAG, "Notifications disabled, skipping transition scheduling.")
                return Result.success()
            }

            val birthProfile = prefsRepo.birthProfileFlow.first() ?: return Result.success()
            val location = birthProfile.birthLocation

            val zoneId = try {
                ZoneId.of(location.timezoneId)
            } catch (e: Exception) {
                ZoneId.systemDefault()
            }

            val now = ZonedDateTime.now(zoneId)
            val astronomicalEngine = AstronomicalEngineProvider.instance
            
            val workManager = WorkManager.getInstance(context)
            // Cancel any previously scheduled one-time transitions to avoid duplicates
            workManager.cancelAllWorkByTag("VedicTransitionNotification")

            // Calculate for today and tomorrow
            val daysToCheck = listOf(now, now.plusDays(1))
            
            for (date in daysToCheck) {
                val solarTimes = astronomicalEngine.calculateSolarTimes(
                    date = date, 
                    latitude = location.latitude, 
                    longitude = location.longitude
                )
                
                val sunrise = solarTimes.sunrise
                val sunset = solarTimes.sunset

                // Schedule Brahma Muhurta notification (90 minutes before sunrise)
                val brahmaStart = sunrise.minusMinutes(90)
                if (brahmaStart.isAfter(now)) {
                    val delayMinutes = Duration.between(now, brahmaStart).toMinutes()
                    if (delayMinutes < 48 * 60) {
                        scheduleTransitionNotification(
                            workManager,
                            delayMinutes,
                            "Brahma Muhurta Approaching",
                            "The sacred Brahma Muhurta begins shortly. Optimal window for meditation, yoga, and spiritual alignment."
                        )
                    }
                }
                
                // Schedule Sunrise Notification (only if it's in the future and within the next 48 hours)
                if (sunrise.isAfter(now)) {
                    val delayMinutes = Duration.between(now, sunrise).toMinutes()
                    // Don't schedule too far out just in case
                    if (delayMinutes < 48 * 60) {
                        scheduleTransitionNotification(
                            workManager, 
                            delayMinutes, 
                            "Sunrise: Pratah Kaal Begins", 
                            "Surya Deva has risen. Brahma Muhurta concludes and daytime planetary active hours begin."
                        )
                    }
                }

                // Schedule Sunset Notification
                if (sunset.isAfter(now)) {
                    val delayMinutes = Duration.between(now, sunset).toMinutes()
                    if (delayMinutes < 48 * 60) {
                        scheduleTransitionNotification(
                            workManager, 
                            delayMinutes, 
                            "Sunset: Godhuli / Sayankal Begins", 
                            "Surya Deva has set. Ideal time for evening aarti, gratitude, and transitioning to resting lunar energy."
                        )
                    }
                }
            }

            // If we want to schedule for tomorrow as well, we could, but a daily periodic worker will handle that.
            
            Log.d(TAG, "Sunrise/Sunset transition notifications scheduled successfully.")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating and scheduling transitions: ${e.message}", e)
            return Result.failure()
        }
    }

    private fun scheduleTransitionNotification(
        workManager: WorkManager, 
        delayMinutes: Long, 
        title: String, 
        message: String
    ) {
        if (delayMinutes < 0) return
        
        val inputData = Data.Builder()
            .putString(VedicTransitionWorker.EXTRA_TRANSITION_TITLE, title)
            .putString(VedicTransitionWorker.EXTRA_TRANSITION_MESSAGE, message)
            .build()

        val transitionRequest = OneTimeWorkRequestBuilder<VedicTransitionWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag("VedicTransitionNotification")
            .build()

        workManager.enqueue(transitionRequest)
    }
}
