package com.example.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.MainActivity
import com.example.R
import com.example.core.model.BirthProfile
import com.example.core.model.DailyVedicProfile
import com.example.data.UserPreferencesRepository
import com.example.engines.personalization.VedicPersonalizationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.Locale
import android.content.res.Configuration
import com.example.core.model.LanguageDisplay

object DailyVedicNotificationManager {

    const val CHANNEL_ID = "vedic_daily_panchang_channel"
    const val CHANNEL_NAME = "Daily Vedic Events & Auspicious Timings"
    const val WORK_NAME = "DailyVedicPanchangPeriodicWork"
    const val TAG = "DailyVedicNotificationManager"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily morning notifications for Vedic festivals, Tithi, Nakshatra, Brahma Muhurta, and Rahu Kaal."
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Enqueues a manual 'Sync Now' one-time background worker request to fetch and process
     * the latest Panchang and Vedic cosmic data immediately. Returns a Flow of WorkInfo to track state.
     */
    fun triggerOneTimeSync(context: Context): kotlinx.coroutines.flow.Flow<androidx.work.WorkInfo?> {
        val workManager = WorkManager.getInstance(context)
        val syncRequest = androidx.work.OneTimeWorkRequestBuilder<DailyPanchangNotificationWorker>()
            .addTag("MANUAL_PANCHANG_SYNC")
            .build()

        workManager.enqueueUniqueWork(
            "ManualPanchangSyncWork",
            androidx.work.ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        return workManager.getWorkInfoByIdFlow(syncRequest.id)
    }

    /**
     * Schedules a periodic 24-hour worker that fires each day at the user's preferred morning time
     * (e.g. 06:30 AM) in their saved location's timezone.
     */
    fun scheduleDailyNotification(context: Context, hour: Int = 6, minute: Int = 30) {
        createNotificationChannel(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsRepo = UserPreferencesRepository(context)
                val birthProfile = prefsRepo.birthProfileFlow.first()
                val zoneId = try {
                    ZoneId.of(birthProfile?.birthLocation?.timezoneId ?: ZoneId.systemDefault().id)
                } catch (e: Exception) {
                    ZoneId.systemDefault()
                }

                val now = ZonedDateTime.now(zoneId)
                var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
                if (now.isAfter(target)) {
                    target = target.plusDays(1)
                }

                val initialDelayMinutes = Duration.between(now, target).toMinutes().coerceAtLeast(1)
                Log.d(TAG, "Scheduling Daily Vedic Notification: target $target in $initialDelayMinutes minutes")

                val workRequest = PeriodicWorkRequestBuilder<DailyPanchangNotificationWorker>(24, TimeUnit.HOURS)
                    .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
                    .addTag(WORK_NAME)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling daily notifications: ${e.message}", e)
            }
        }
    }

    fun cancelDailyNotification(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Immediately triggers a notification preview using the user's saved location and today's cosmic data.
     */
    fun sendInstantNotification(context: Context, onSent: (() -> Unit)? = null) {
        createNotificationChannel(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsRepo = UserPreferencesRepository(context)
                val birthProfile = prefsRepo.birthProfileFlow.first() ?: return@launch
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

                buildAndPostNotification(
                    context = context,
                    birthProfile = birthProfile,
                    dailyProfile = dailyProfile,
                    notifyFestivals = notifyFestivals,
                    notifyMuhurtas = notifyMuhurtas,
                    notifyRahuKaal = notifyRahuKaal,
                    notificationId = 2002
                )
                onSent?.invoke()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send instant notification: ${e.message}", e)
            }
        }
    }


    fun buildAndPostNotification(
        context: Context,
        birthProfile: BirthProfile,
        dailyProfile: DailyVedicProfile,
        notifyFestivals: Boolean = true,
        notifyMuhurtas: Boolean = true,
        notifyRahuKaal: Boolean = true,
        notificationId: Int = 2001
    ) {
        val langCode = com.example.core.language.LanguageManager.getLanguageCode(birthProfile.language)
        val localizedContext = com.example.core.language.LanguageManager.createLocalizedContext(context, langCode)

        val zoneId = try {
            ZoneId.of(birthProfile.birthLocation.timezoneId)
        } catch (e: Exception) {
            ZoneId.systemDefault()
        }
        val now = ZonedDateTime.now(zoneId)

        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

        fun formatTimingWithStatus(window: com.example.core.model.TimingWindow?): String {
            if (window == null) return "N/A"
            val start = window.startTime.format(timeFormatter)
            val end = window.endTime.format(timeFormatter)
            return when {
                now.isAfter(window.endTime) -> "$start - $end (✓ Completed)"
                now.isAfter(window.startTime) && now.isBefore(window.endTime) -> "$start - $end (🟢 Active Now)"
                else -> "$start - $end (Upcoming)"
            }
        }

        val cityName = birthProfile.birthLocation.cityName
        val tithiName = dailyProfile.panchanga.tithi.name
        val nakshatraName = dailyProfile.panchanga.nakshatra.name
        val festival = if (notifyFestivals) dailyProfile.festivals.firstOrNull() else null
        val vrat = if (notifyFestivals) dailyProfile.vrats.firstOrNull() else null

        val brahmaText = formatTimingWithStatus(dailyProfile.brahmaMuhurta)
        val abhijitText = formatTimingWithStatus(dailyProfile.abhijitMuhurta)
        val rahuText = formatTimingWithStatus(dailyProfile.rahuKalam)
        val brahmaStart = dailyProfile.brahmaMuhurta.startTime.format(timeFormatter)

        val strVedicPanchang = localizedContext.getString(R.string.notif_vedic_panchang)
        val strLocation = localizedContext.getString(R.string.notif_location)
        val strTithi = localizedContext.getString(R.string.notif_tithi)
        val strNakshatra = localizedContext.getString(R.string.notif_nakshatra)
        val strYoga = localizedContext.getString(R.string.notif_yoga)
        val strKarana = localizedContext.getString(R.string.notif_karana)
        val strBrahmaMuhurta = localizedContext.getString(R.string.notif_brahma_muhurta)
        val strAbhijitMuhurta = localizedContext.getString(R.string.notif_abhijit_muhurta)
        val strRahuKaal = localizedContext.getString(R.string.notif_rahu_kaal)
        val strFestival = localizedContext.getString(R.string.notif_festival)
        val strVrat = localizedContext.getString(R.string.notif_vrat)
        val strCosmicEnergy = localizedContext.getString(R.string.notif_cosmic_energy)
        val strActiveHora = localizedContext.getString(R.string.notif_active_hora)
        val strChoghadiya = localizedContext.getString(R.string.notif_choghadiya)

        // Formulate Title
        val title = when {
            festival != null -> "🎉 ${festival.name} • $cityName $strVedicPanchang"
            vrat != null -> "🙏 ${vrat.name} • $cityName $strVedicPanchang"
            else -> "🕉 $strVedicPanchang • $cityName"
        }

        // Formulate Short Content
        val shortContent = when {
            festival != null -> "${festival.name}: $tithiName, $nakshatraName. $strBrahmaMuhurta $brahmaStart"
            vrat != null -> "${vrat.name}: $tithiName, $nakshatraName. $strBrahmaMuhurta $brahmaStart"
            else -> "$tithiName • $nakshatraName | $strBrahmaMuhurta $brahmaText"
        }

        // Formulate Expanded Body
        val expandedBuilder = StringBuilder()
        expandedBuilder.append("📍 $strLocation ${birthProfile.birthLocation.cityName}, ${birthProfile.birthLocation.state}\n")
        expandedBuilder.append("🌕 $strTithi $tithiName (${dailyProfile.panchanga.tithi.paksha.name})\n")
        expandedBuilder.append("⭐ $strNakshatra $nakshatraName (Pada ${dailyProfile.panchanga.nakshatraPada})\n")
        expandedBuilder.append("🌿 $strYoga ${dailyProfile.panchanga.yoga} | $strKarana ${dailyProfile.panchanga.karana}\n\n")

        if (notifyMuhurtas) {
            expandedBuilder.append("✨ $strBrahmaMuhurta: $brahmaText\n")
            if (dailyProfile.abhijitMuhurta != null) {
                expandedBuilder.append("🌟 $strAbhijitMuhurta: $abhijitText\n")
            }
        }

        if (notifyRahuKaal) {
            expandedBuilder.append("⚠️ $strRahuKaal: $rahuText\n")
        }

        if (festival != null) {
            expandedBuilder.append("\n🎉 $strFestival ${festival.name}\n${festival.description}\n")
        }
        if (vrat != null) {
            expandedBuilder.append("\n🙏 $strVrat ${vrat.name}\n${vrat.description}\n")
        }

        if (dailyProfile.cosmicScore != null) {
            val score = dailyProfile.cosmicScore
            expandedBuilder.append("$strCosmicEnergy ${score.overall}/100 • Luck: ${score.luck} • Calm: ${score.calm}\n")
        }

        val activeHora = dailyProfile.currentHora.rulingGraha.displayName
        val activeChoghadiyaText = "${dailyProfile.currentChoghadiya.type.displayName} (${dailyProfile.currentChoghadiya.type.quality})"
        
        expandedBuilder.append("🪐 $strActiveHora $activeHora | $strChoghadiya $activeChoghadiyaText")

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_NAV_TAB", "DAILY_HIGHLIGHTS")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val panchangIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_NAV_TAB", "PANCHANGA")
        }
        val panchangPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 10,
            panchangIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val muhurtaIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_NAV_TAB", "MUHURTAS")
        }
        val muhurtaPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 20,
            muhurtaIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(shortContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedBuilder.toString()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Panchang", panchangPendingIntent)
            .addAction(0, "Muhurtas", muhurtaPendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
