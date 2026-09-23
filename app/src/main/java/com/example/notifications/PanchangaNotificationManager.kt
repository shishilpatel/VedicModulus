package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.workers.MuhurtaNotificationReceiver
import java.util.Calendar

/**
 * Schedules daily local alarms for Shubh Muhurtas (Abhijit, Brahma) and Rahu Kaal alerts.
 */
object PanchangaNotificationManager {

    fun scheduleDailyMuhurtaAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Brahma Muhurta Reminder (04:30 AM)
        scheduleAlarm(
            context = context,
            alarmManager = alarmManager,
            id = 2001,
            hour = 4,
            minute = 30,
            title = "ॐ Brahma Muhurta Arrived",
            message = "Ideal time for Sadhana, Meditation, and Gayatri Mantra japa."
        )

        // 2. Abhijit Muhurta Reminder (11:45 AM)
        scheduleAlarm(
            context = context,
            alarmManager = alarmManager,
            id = 2002,
            hour = 11,
            minute = 45,
            title = "⭐ Abhijit Muhurta Started",
            message = "Most auspicious window of the day for commencing important tasks."
        )

        // 3. Rahu Kaal Warning Alert (03:00 PM)
        scheduleAlarm(
            context = context,
            alarmManager = alarmManager,
            id = 2003,
            hour = 15,
            minute = 0,
            title = "⚠️ Rahu Kaal Window Active",
            message = "Avoid starting new financial contracts or long journeys until 04:30 PM."
        )
    }

    fun triggerImmediateTestNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, MuhurtaNotificationReceiver::class.java).apply {
            putExtra(MuhurtaNotificationReceiver.EXTRA_TITLE, title)
            putExtra(MuhurtaNotificationReceiver.EXTRA_MESSAGE, message)
            putExtra(MuhurtaNotificationReceiver.EXTRA_NOTIFICATION_ID, (1000..9999).random())
        }
        context.sendBroadcast(intent)
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        id: Int,
        hour: Int,
        minute: Int,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MuhurtaNotificationReceiver::class.java).apply {
            putExtra(MuhurtaNotificationReceiver.EXTRA_TITLE, title)
            putExtra(MuhurtaNotificationReceiver.EXTRA_MESSAGE, message)
            putExtra(MuhurtaNotificationReceiver.EXTRA_NOTIFICATION_ID, id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
