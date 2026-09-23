package com.example.workers

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.R
import com.example.core.model.Graha
import com.example.engines.astronomy.SwissEphAdvancedFeatures
import java.time.ZonedDateTime

class TransitNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure()
        }

        try {
            val engine = SwissEphAdvancedFeatures.getInstance()
            val now = ZonedDateTime.now()
            
            // Check for major transits happening exactly today (within 24 hours)
            val grahasToCheck = listOf(Graha.GURU, Graha.SHANI, Graha.RAHU, Graha.KETU)
            val transitsToday = mutableListOf<String>()

            for (graha in grahasToCheck) {
                val transit = engine.findNextRashiTransit(graha, now)
                if (transit != null) {
                    val durationHours = java.time.Duration.between(now, transit.transitTime).toHours()
                    if (durationHours in 0..24) {
                        transitsToday.add("${graha.displayName} is entering ${transit.targetRashi} today.")
                    }
                }
            }

            if (transitsToday.isNotEmpty()) {
                val message = transitsToday.joinToString("\n")
                sendNotification("Planetary Transit Alert (Gochar)", message)
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "transit_alerts"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Transit Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for planetary transits"
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        @SuppressLint("MissingPermission")
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(1001, builder.build())
        }
    }
}
