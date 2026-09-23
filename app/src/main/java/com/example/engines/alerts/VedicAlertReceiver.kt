package com.example.engines.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.R

enum class VedicAlertType {
    BRAHMA_MUHURTA,
    ABHIJIT_MUHURTA,
    RAHU_KAAL,
    SANDHYA,
    GENERIC
}

class VedicAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("ALERT_TITLE") ?: "Vedic Cosmic Ghadi"
        val message = intent.getStringExtra("ALERT_MESSAGE") ?: "Sacred Vedic timing begins."
        val typeStr = intent.getStringExtra("ALERT_TYPE") ?: VedicAlertType.GENERIC.name
        val alertType = try {
            VedicAlertType.valueOf(typeStr)
        } catch (_: Exception) {
            VedicAlertType.GENERIC
        }

        // 1. Trigger distinctive tactile haptic feedback
        triggerHapticAlert(context, alertType)

        // 2. Display Notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "vedic_alerts_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Vedic Sacred Alerts & Muhurtas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications and haptic cues for Rahu Kaal, Sandhya, and Brahma Muhurta."
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun triggerHapticAlert(context: Context, type: VedicAlertType) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (!vibrator.hasVibrator()) return

            when (type) {
                VedicAlertType.RAHU_KAAL -> {
                    // Rahu Kaal: Warning pattern (Two pronounced pulses: wait 0ms, 400ms on, 200ms pause, 400ms on)
                    val timings = longArrayOf(0, 400, 200, 400)
                    val amplitudes = intArrayOf(0, 255, 0, 255)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(timings, -1)
                    }
                }
                VedicAlertType.SANDHYA, VedicAlertType.BRAHMA_MUHURTA -> {
                    // Sandhya & Brahma Muhurta: Gentle triple harmonic pulse (150ms on, 150ms pause, 150ms on, 150ms pause, 250ms on)
                    val timings = longArrayOf(0, 150, 150, 150, 150, 250)
                    val amplitudes = intArrayOf(0, 140, 0, 180, 0, 220)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(timings, -1)
                    }
                }
                else -> {
                    // Standard gentle pulse
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(250)
                    }
                }
            }
        } catch (_: Exception) {
            // Gracefully ignore on devices without vibration hardware/permission
        }
    }
}

