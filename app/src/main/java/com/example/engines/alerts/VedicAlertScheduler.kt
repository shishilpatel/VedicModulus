package com.example.engines.alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.core.model.BirthProfile
import com.example.core.model.DailyVedicProfile
import com.example.workers.MuhurtaNotificationReceiver
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Vedic Alert & Alarm Scheduler for Brahma Muhurta, Abhijit Muhurta, Rahu Kaal, and Sacred Tithis.
 */
object VedicAlertScheduler {

    data class VedicAlertPreferences(
        val enableBrahmaMuhurta: Boolean = true,
        val enableAbhijitMuhurta: Boolean = true,
        val enableRahuKaalWarning: Boolean = true,
        val enableChoghadiyaAlerts: Boolean = false,
        val enableEkadashiVrats: Boolean = true,
        val leadTimeMinutes: Int = 15 // minutes before event to alert
    )

    private const val PREFS_NAME = "vedic_alert_preferences"

    fun getPreferences(context: Context): VedicAlertPreferences {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return VedicAlertPreferences(
            enableBrahmaMuhurta = sp.getBoolean("brahma_muhurta", true),
            enableAbhijitMuhurta = sp.getBoolean("abhijit_muhurta", true),
            enableRahuKaalWarning = sp.getBoolean("rahu_kaal", true),
            enableChoghadiyaAlerts = sp.getBoolean("choghadiya_alerts", false),
            enableEkadashiVrats = sp.getBoolean("ekadashi_vrats", true),
            leadTimeMinutes = sp.getInt("lead_time_min", 15)
        )
    }

    fun savePreferences(context: Context, prefs: VedicAlertPreferences) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putBoolean("brahma_muhurta", prefs.enableBrahmaMuhurta)
            .putBoolean("abhijit_muhurta", prefs.enableAbhijitMuhurta)
            .putBoolean("rahu_kaal", prefs.enableRahuKaalWarning)
            .putBoolean("choghadiya_alerts", prefs.enableChoghadiyaAlerts)
            .putBoolean("ekadashi_vrats", prefs.enableEkadashiVrats)
            .putInt("lead_time_min", prefs.leadTimeMinutes)
            .apply()
    }

    /**
     * Schedules alarms for today's dynamic Vedic windows based on location-calculated times.
     */
    fun scheduleDailyAlerts(
        context: Context,
        dailyProfile: DailyVedicProfile,
        prefs: VedicAlertPreferences = getPreferences(context)
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")

        // 1. Brahma Muhurta Alert
        if (prefs.enableBrahmaMuhurta) {
            val triggerTime = dailyProfile.brahmaMuhurta.startTime.minusMinutes(prefs.leadTimeMinutes.toLong())
            if (triggerTime.isAfter(ZonedDateTime.now())) {
                val intent = Intent(context, MuhurtaNotificationReceiver::class.java).apply {
                    putExtra(MuhurtaNotificationReceiver.EXTRA_TITLE, "🌅 Sacred Brahma Muhurta Approaching")
                    putExtra(
                        MuhurtaNotificationReceiver.EXTRA_MESSAGE,
                        "Brahma Muhurta begins at ${dailyProfile.brahmaMuhurta.startTime.format(formatter)}. Best sacred window for Dhyana, Gayatri Japa & Pranayama."
                    )
                    putExtra(MuhurtaNotificationReceiver.EXTRA_NOTIFICATION_ID, 2001)
                }
                scheduleExactOrInexact(context, alarmManager, triggerTime.toInstant().toEpochMilli(), intent, 2001)
            }
        }

        // 2. Rahu Kaal Warning
        if (prefs.enableRahuKaalWarning) {
            val triggerTime = dailyProfile.rahuKalam.startTime.minusMinutes(prefs.leadTimeMinutes.toLong())
            if (triggerTime.isAfter(ZonedDateTime.now())) {
                val intent = Intent(context, MuhurtaNotificationReceiver::class.java).apply {
                    putExtra(MuhurtaNotificationReceiver.EXTRA_TITLE, "⚠️ Inauspicious Rahu Kaal Ahead")
                    putExtra(
                        MuhurtaNotificationReceiver.EXTRA_MESSAGE,
                        "Rahu Kaal window: ${dailyProfile.rahuKalam.startTime.format(formatter)} - ${dailyProfile.rahuKalam.endTime.format(formatter)}. Avoid starting new ventures, purchases or travel."
                    )
                    putExtra(MuhurtaNotificationReceiver.EXTRA_NOTIFICATION_ID, 2002)
                }
                scheduleExactOrInexact(context, alarmManager, triggerTime.toInstant().toEpochMilli(), intent, 2002)
            }
        }

        // 3. Abhijit Muhurta
        if (prefs.enableAbhijitMuhurta && dailyProfile.abhijitMuhurta != null) {
            val triggerTime = dailyProfile.abhijitMuhurta.startTime.minusMinutes(prefs.leadTimeMinutes.toLong())
            if (triggerTime.isAfter(ZonedDateTime.now())) {
                val intent = Intent(context, MuhurtaNotificationReceiver::class.java).apply {
                    putExtra(MuhurtaNotificationReceiver.EXTRA_TITLE, "✨ Victorious Abhijit Muhurta Today")
                    putExtra(
                        MuhurtaNotificationReceiver.EXTRA_MESSAGE,
                        "Abhijit window: ${dailyProfile.abhijitMuhurta.startTime.format(formatter)} - ${dailyProfile.abhijitMuhurta.endTime.format(formatter)}. Highly auspicious for all righteous beginnings."
                    )
                    putExtra(MuhurtaNotificationReceiver.EXTRA_NOTIFICATION_ID, 2003)
                }
                scheduleExactOrInexact(context, alarmManager, triggerTime.toInstant().toEpochMilli(), intent, 2003)
            }
        }
    }

    /**
     * Sends an instant test notification so the user can verify channel sound and vibration.
     */
    fun triggerInstantTestAlert(context: Context, title: String, message: String) {
        val intent = Intent(context, MuhurtaNotificationReceiver::class.java).apply {
            putExtra(MuhurtaNotificationReceiver.EXTRA_TITLE, title)
            putExtra(MuhurtaNotificationReceiver.EXTRA_MESSAGE, message)
            putExtra(MuhurtaNotificationReceiver.EXTRA_NOTIFICATION_ID, 2999)
        }
        context.sendBroadcast(intent)
    }

    private fun scheduleExactOrInexact(
        context: Context,
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        intent: Intent,
        requestCode: Int
    ) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }
}
