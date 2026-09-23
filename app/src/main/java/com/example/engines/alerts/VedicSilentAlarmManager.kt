package com.example.engines.alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.core.model.DailyVedicProfile
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Vedic Sacred Time Markers for Silent Vibration Alarms.
 */
enum class VedicTimeMarker(
    val id: String,
    val title: String,
    val devanagari: String,
    val description: String,
    val defaultEnabled: Boolean,
    val vibrationPattern: LongArray
) {
    BRAHMA_MUHURTA(
        id = "alarm_brahma_muhurta",
        title = "Brahma Muhurta",
        devanagari = "ब्रह्म मुहूर्त",
        description = "Pre-dawn cosmic window (~96 min before sunrise) ideal for meditation and Japa.",
        defaultEnabled = true,
        vibrationPattern = longArrayOf(0, 200, 150, 200, 150, 400)
    ),
    SURYA_UDAYA(
        id = "alarm_sunrise",
        title = "Surya Udaya (Sunrise)",
        devanagari = "सूर्योदय",
        description = "Dawn solar transition for Surya Namaskar and Gayatri mantra.",
        defaultEnabled = true,
        vibrationPattern = longArrayOf(0, 300, 100, 300, 100, 300)
    ),
    ABHIJIT_MUHURTA(
        id = "alarm_abhijit",
        title = "Abhijit Muhurat",
        devanagari = "अभिजित् मुहूर्त",
        description = "Auspicious solar midday victory window (11:45 AM - 12:35 PM).",
        defaultEnabled = false,
        vibrationPattern = longArrayOf(0, 250, 100, 250)
    ),
    SURYA_AST(
        id = "alarm_sunset",
        title = "Surya Ast / Sandhya",
        devanagari = "सूर्यास्त / सांध्य वेला",
        description = "Dusk twilight transition for evening contemplation and Aarti.",
        defaultEnabled = true,
        vibrationPattern = longArrayOf(0, 200, 200, 200, 200, 400)
    ),
    RAHU_KAAL_START(
        id = "alarm_rahu_kaal",
        title = "Rahu Kaal Alert",
        devanagari = "राहु काल सचेतक",
        description = "Silent notice 10 minutes before the inauspicious planetary window starts.",
        defaultEnabled = true,
        vibrationPattern = longArrayOf(0, 400, 200, 400)
    ),
    PRADOSHA_KAAL(
        id = "alarm_pradosha",
        title = "Pradosha Kaal",
        devanagari = "प्रदोष काल",
        description = "Twilight worship period (1.5 hours around sunset).",
        defaultEnabled = false,
        vibrationPattern = longArrayOf(0, 150, 100, 150, 100, 350)
    )
}

data class VedicAlarmStatus(
    val marker: VedicTimeMarker,
    val isEnabled: Boolean,
    val targetTimeFormatted: String,
    val targetTimestampMs: Long
)

/**
 * Manager for scheduling exact silent vibration alarms on mobile and synchronizing haptic triggers to Wear OS.
 */
object VedicSilentAlarmManager {
    private const val TAG = "VedicSilentAlarmManager"
    private const val PREFS_NAME = "vedic_silent_alarms_prefs"
    const val PATH_WEAR_SILENT_ALARM = "/vedic_silent_alarm"

    fun isAlarmEnabled(context: Context, marker: VedicTimeMarker): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(marker.id, marker.defaultEnabled)
    }

    fun setAlarmEnabled(context: Context, marker: VedicTimeMarker, enabled: Boolean, dailyProfile: DailyVedicProfile? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(marker.id, enabled).apply()

        if (dailyProfile != null) {
            if (enabled) {
                scheduleSpecificAlarm(context, marker, dailyProfile)
            } else {
                cancelSpecificAlarm(context, marker)
            }
        }
    }

    /**
     * Schedules all enabled silent alarms for today's dynamic Vedic profile.
     */
    fun syncAllAlarmsForToday(context: Context, dailyProfile: DailyVedicProfile) {
        for (marker in VedicTimeMarker.values()) {
            if (isAlarmEnabled(context, marker)) {
                scheduleSpecificAlarm(context, marker, dailyProfile)
            } else {
                cancelSpecificAlarm(context, marker)
            }
        }
    }

    /**
     * Computes the exact timestamp for a given Vedic time marker.
     */
    fun computeMarkerTargetTime(marker: VedicTimeMarker, dailyProfile: DailyVedicProfile): ZonedDateTime {
        val date = dailyProfile.currentZonedDateTime.toLocalDate()
        val zone = dailyProfile.currentZonedDateTime.zone

        return when (marker) {
            VedicTimeMarker.BRAHMA_MUHURTA -> dailyProfile.brahmaMuhurta.startTime
            VedicTimeMarker.SURYA_UDAYA -> dailyProfile.panchanga.sunrise
            VedicTimeMarker.ABHIJIT_MUHURTA -> dailyProfile.abhijitMuhurta?.startTime
                ?: dailyProfile.panchanga.solarNoon.minusMinutes(24)
            VedicTimeMarker.SURYA_AST -> dailyProfile.panchanga.sunset
            VedicTimeMarker.RAHU_KAAL_START -> dailyProfile.rahuKalam.startTime.minusMinutes(10)
            VedicTimeMarker.PRADOSHA_KAAL -> dailyProfile.panchanga.sunset.minusMinutes(45)
        }
    }

    fun getAlarmStatuses(context: Context, dailyProfile: DailyVedicProfile): List<VedicAlarmStatus> {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
        return VedicTimeMarker.values().map { marker ->
            val isEnabled = isAlarmEnabled(context, marker)
            val targetZdt = computeMarkerTargetTime(marker, dailyProfile)
            VedicAlarmStatus(
                marker = marker,
                isEnabled = isEnabled,
                targetTimeFormatted = targetZdt.format(formatter),
                targetTimestampMs = targetZdt.toInstant().toEpochMilli()
            )
        }
    }

    private fun scheduleSpecificAlarm(context: Context, marker: VedicTimeMarker, dailyProfile: DailyVedicProfile) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val targetZdt = computeMarkerTargetTime(marker, dailyProfile)
        val triggerMs = targetZdt.toInstant().toEpochMilli()

        // Don't schedule past events for today
        if (triggerMs <= System.currentTimeMillis()) {
            Log.d(TAG, "Skipping past alarm trigger for ${marker.title}: $targetZdt")
            return
        }

        val intent = Intent(context, VedicAlertReceiver::class.java).apply {
            putExtra("ALERT_TITLE", marker.title)
            putExtra("ALERT_MESSAGE", "${marker.devanagari} • ${marker.description}")
            putExtra("ALERT_TYPE", mapMarkerToAlertType(marker).name)
            putExtra("IS_SILENT_VIBRATION", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            marker.ordinal + 2000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
            }
            Log.d(TAG, "Scheduled exact silent alarm for ${marker.title} at $targetZdt ($triggerMs)")
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission not granted, falling back to standard alarm: ${e.message}")
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMs, pendingIntent)
        }
    }

    private fun cancelSpecificAlarm(context: Context, marker: VedicTimeMarker) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, VedicAlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            marker.ordinal + 2000,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Executes an immediate test vibration on the local device and sends a haptic buzz message to Wear OS.
     */
    fun testSilentVibration(context: Context, marker: VedicTimeMarker) {
        // 1. Phone tactile vibration
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                mgr?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                val timings = marker.vibrationPattern
                val amplitudes = IntArray(timings.size) { index ->
                    if (index % 2 == 1) 220 else 0
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(timings, -1)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Local vibration error: ${e.message}")
        }

        // 2. Wear OS Watch Haptic Message
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                for (node in nodes) {
                    val payload = marker.name.toByteArray(Charsets.UTF_8)
                    Wearable.getMessageClient(context).sendMessage(node.id, PATH_WEAR_SILENT_ALARM, payload).await()
                    Log.d(TAG, "Sent silent vibration test message to Wear node ${node.displayName}")
                }
            } catch (e: Exception) {
                Log.i(TAG, "Wear OS node unavailable for vibration test: ${e.message}")
            }
        }
    }

    private fun mapMarkerToAlertType(marker: VedicTimeMarker): VedicAlertType {
        return when (marker) {
            VedicTimeMarker.BRAHMA_MUHURTA -> VedicAlertType.BRAHMA_MUHURTA
            VedicTimeMarker.SURYA_UDAYA, VedicTimeMarker.SURYA_AST, VedicTimeMarker.PRADOSHA_KAAL -> VedicAlertType.SANDHYA
            VedicTimeMarker.ABHIJIT_MUHURTA -> VedicAlertType.ABHIJIT_MUHURTA
            VedicTimeMarker.RAHU_KAAL_START -> VedicAlertType.RAHU_KAAL
        }
    }
}
