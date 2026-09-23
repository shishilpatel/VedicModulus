package com.example.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.core.model.BirthProfile
import com.example.core.model.DailyVedicProfile
import com.example.data.CityRepository
import com.example.data.UserPreferencesRepository
import com.example.engines.personalization.VedicPersonalizationEngine
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Background worker that delivers a rich morning notification highlighting
 * the day's special Vedic festivals, fasts (vrats), Tithi, Nakshatra,
 * Brahma Muhurta, Abhijit Muhurta, and Rahu Kaal calculated for the user's saved location.
 */
class DailyPanchangNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "DailyPanchangNotificationWorker"
        const val NOTIFICATION_ID = 2001
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Executing Daily Vedic Panchang Notification calculation...")
        try {
            val prefsRepo = UserPreferencesRepository(context)
            val isEnabled = prefsRepo.dailyNotificationsEnabledFlow.first()
            if (!isEnabled) {
                Log.d(TAG, "Daily push notifications are disabled in user settings.")
                return Result.success()
            }

            val birthProfile = prefsRepo.birthProfileFlow.first() ?: return Result.success()
            val notifyFestivals = prefsRepo.notifyFestivalsFlow.first()
            val notifyMuhurtas = prefsRepo.notifyMuhurtasFlow.first()
            val notifyRahuKaal = prefsRepo.notifyRahuKaalFlow.first()

            val location = birthProfile.birthLocation
            val zoneId = try {
                ZoneId.of(location.timezoneId)
            } catch (e: Exception) {
                ZoneId.systemDefault()
            }
            val now = ZonedDateTime.now(zoneId)

            val personalizationEngine = VedicPersonalizationEngine()
            val personalizedProfile = personalizationEngine.createPersonalizedProfile(birthProfile)
            val dailyProfile = personalizationEngine.createDailyVedicProfile(
                now = now,
                location = location,
                ayanamsha = birthProfile.ayanamsha,
                tradition = birthProfile.calendarTradition,
                personalizedProfile = personalizedProfile
            )

            DailyVedicNotificationManager.buildAndPostNotification(
                context = context,
                birthProfile = birthProfile,
                dailyProfile = dailyProfile,
                notifyFestivals = notifyFestivals,
                notifyMuhurtas = notifyMuhurtas,
                notifyRahuKaal = notifyRahuKaal,
                notificationId = NOTIFICATION_ID
            )

            Log.d(TAG, "Daily Vedic Notification posted successfully for ${location.cityName}.")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error generating daily Vedic notification: ${e.message}", e)
            return Result.failure()
        }
    }
}
