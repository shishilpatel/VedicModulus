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
import androidx.work.Data
import com.example.MainActivity
import com.example.R

/**
 * Worker that sends a notification for a Vedic transition (Sunrise / Sunset).
 */
class VedicTransitionWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "VedicTransitionWorker"
        const val EXTRA_TRANSITION_TITLE = "transition_title"
        const val EXTRA_TRANSITION_MESSAGE = "transition_message"
        const val NOTIFICATION_ID = 2005
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Executing VedicTransitionWorker to notify transition.")
        try {
            val title = inputData.getString(EXTRA_TRANSITION_TITLE) ?: "Vedic Transition"
            val message = inputData.getString(EXTRA_TRANSITION_MESSAGE) ?: "A new cosmic period begins."

            sendNotification(title, message)

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending Vedic transition notification", e)
            return Result.failure()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "vedic_transitions_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Vedic Transitions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for timely Vedic period transitions like Sunrise and Sunset"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Add WearableExtender so it's optimized for the watch
        val wearableExtender = NotificationCompat.WearableExtender()

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .extend(wearableExtender)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
