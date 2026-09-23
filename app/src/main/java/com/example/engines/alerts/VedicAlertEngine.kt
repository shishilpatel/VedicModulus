package com.example.engines.alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.core.model.DailyVedicProfile
import java.time.ZonedDateTime

class VedicAlertEngine(private val context: Context) {
    
    // In a real app, preferences would be stored in DataStore/SharedPreferences
    var enableBrahmaMuhurtaAlert = true
    var enableAbhijitMuhurtaAlert = true
    var enableRahuKaalAlert = true
    var enableSandhyaAlert = true
    var enableChoghadiyaTransitions = false
    var enableHoraTransitions = false
    
    fun scheduleAlertsForDay(dailyProfile: DailyVedicProfile) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (enableBrahmaMuhurtaAlert) {
            scheduleAlarm(
                alarmManager = alarmManager,
                time = dailyProfile.brahmaMuhurta.startTime,
                title = "🕉 Brahma Muhurta Begins",
                message = "The sacred ambrosial hour for meditation and mantra japa has begun.",
                type = VedicAlertType.BRAHMA_MUHURTA,
                requestCode = 101
            )
        }
        
        if (enableAbhijitMuhurtaAlert && dailyProfile.abhijitMuhurta != null) {
            scheduleAlarm(
                alarmManager = alarmManager,
                time = dailyProfile.abhijitMuhurta.startTime,
                title = "⭐ Abhijit Muhurta Active",
                message = "Auspicious midday victory window for commencing important endeavors.",
                type = VedicAlertType.ABHIJIT_MUHURTA,
                requestCode = 102
            )
        }

        if (enableRahuKaalAlert) {
            // Alert 5 minutes prior to Rahu Kaal commencement
            val rahuKaalWarning = dailyProfile.rahuKalam.startTime.minusMinutes(5)
            scheduleAlarm(
                alarmManager = alarmManager,
                time = rahuKaalWarning,
                title = "⚠️ Rahu Kaal Commences in 5 Mins",
                message = "Inauspicious Rahu period from ${dailyProfile.rahuKalam.startTime.toLocalTime()} to ${dailyProfile.rahuKalam.endTime.toLocalTime()}. Avoid commencing new ventures or travels.",
                type = VedicAlertType.RAHU_KAAL,
                requestCode = 103
            )
        }

        if (enableSandhyaAlert) {
            // Prataha Sandhya (Sunrise)
            scheduleAlarm(
                alarmManager = alarmManager,
                time = dailyProfile.panchanga.sunrise,
                title = "🌅 Prataha Sandhya (Morning Twilight)",
                message = "Sacred dawn twilight for Gayatri japa, Surya Arghya, and breath mindfulness.",
                type = VedicAlertType.SANDHYA,
                requestCode = 104
            )

            // Sayam Sandhya (Sunset)
            scheduleAlarm(
                alarmManager = alarmManager,
                time = dailyProfile.panchanga.sunset.minusMinutes(15),
                title = "🌇 Sayam Sandhya (Evening Twilight)",
                message = "Sunset twilight commences. Traditional time for evening lamp lighting, Gayatri japa, and tranquil meditation.",
                type = VedicAlertType.SANDHYA,
                requestCode = 105
            )
        }
    }
    
    @android.annotation.SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(
        alarmManager: AlarmManager,
        time: ZonedDateTime,
        title: String,
        message: String,
        type: VedicAlertType,
        requestCode: Int
    ) {
        val intent = Intent(context, VedicAlertReceiver::class.java).apply {
            putExtra("ALERT_TITLE", title)
            putExtra("ALERT_MESSAGE", message)
            putExtra("ALERT_TYPE", type.name)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val triggerTime = time.toInstant().toEpochMilli()
        if (triggerTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @android.annotation.SuppressLint("ScheduleExactAlarm")
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }
}
