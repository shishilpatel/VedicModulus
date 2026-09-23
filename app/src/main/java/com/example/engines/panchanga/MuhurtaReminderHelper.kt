package com.example.engines.panchanga

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.widget.Toast
import com.example.workers.MuhurtaNotificationReceiver
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object MuhurtaReminderHelper {

    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    /**
     * Launches the native Android Calendar event creation screen with pre-filled Vedic details.
     */
    fun addToSystemCalendar(
        context: Context,
        title: String,
        description: String,
        startZoned: ZonedDateTime,
        endZoned: ZonedDateTime,
        location: String = "Puja Sthan / Home"
    ) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startZoned.toInstant().toEpochMilli())
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endZoned.toInstant().toEpochMilli())
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // If native calendar app is not present or intent fails, schedule an exact reminder
            scheduleAdvanceReminder(context, title, startZoned, 0)
            Toast.makeText(context, "Calendar app not found. Notification reminder scheduled instead.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Sets a device alarm for the start time of the Muhurta or spiritual practice.
     */
    fun setClockAlarm(
        context: Context,
        title: String,
        startZoned: ZonedDateTime
    ) {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, title)
                putExtra(AlarmClock.EXTRA_HOUR, startZoned.hour)
                putExtra(AlarmClock.EXTRA_MINUTES, startZoned.minute)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // If clock app is unavailable or fails, schedule an exact in-app alarm notification
            scheduleAdvanceReminder(context, title, startZoned, 0)
            Toast.makeText(context, "Clock app unavailable. In-app alarm scheduled for ${startZoned.format(timeFormatter)}.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Schedules a local system notification 10-15 minutes prior to the Muhurta start.
     */
    fun scheduleAdvanceReminder(
        context: Context,
        title: String,
        startZoned: ZonedDateTime,
        leadMinutes: Int = 15
    ) {
        val triggerZoned = startZoned.minusMinutes(leadMinutes.toLong())
        val triggerMillis = triggerZoned.toInstant().toEpochMilli()
        val nowMillis = System.currentTimeMillis()

        if (triggerMillis <= nowMillis) {
            Toast.makeText(context, "This period is already active or passed.", Toast.LENGTH_SHORT).show()
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Toast.makeText(context, "Alarm service unavailable", Toast.LENGTH_SHORT).show()
            return
        }

        val notificationId = (triggerMillis % 1000000).toInt()
        val intent = Intent(context, MuhurtaNotificationReceiver::class.java).apply {
            putExtra(MuhurtaNotificationReceiver.EXTRA_TITLE, "🔔 $title Starts in $leadMinutes Mins")
            putExtra(
                MuhurtaNotificationReceiver.EXTRA_MESSAGE,
                "Begins at ${startZoned.format(timeFormatter)}. Prepare your mind, space, and rituals."
            )
            putExtra(MuhurtaNotificationReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, flags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Toast.makeText(
                context,
                "✓ Reminder set for ${triggerZoned.format(timeFormatter)} ($leadMinutes mins before start)",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: SecurityException) {
            Toast.makeText(context, "Exact alarm permission required in device settings", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to schedule: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
