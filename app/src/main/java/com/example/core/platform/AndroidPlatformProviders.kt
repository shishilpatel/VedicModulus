package com.example.core.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.widget.Toast

class AndroidPlatformProvider(private val context: Context) {

    val platformDetails: PlatformDetails = PlatformDetails(
        type = PlatformType.ANDROID,
        osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
        supportsSensors = true,
        supportsHaptics = true,
        supportsNativeAlarms = true,
        supportsWearCompanion = true
    )

    val compassProvider: ICompassSensorProvider = object : ICompassSensorProvider, SensorEventListener {
        private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        private var rotationSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        private var headingCallback: ((Float, Int) -> Unit)? = null
        private val rotationMatrix = FloatArray(9)
        private val orientationAngles = FloatArray(3)

        override val isAvailable: Boolean
            get() = rotationSensor != null

        override fun startListening(onHeadingChanged: (azimuthDegrees: Float, accuracy: Int) -> Unit) {
            headingCallback = onHeadingChanged
            rotationSensor?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }

        override fun stopListening() {
            headingCallback = null
            sensorManager?.unregisterListener(this)
        }

        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null || event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            var azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            if (azimuthDegrees < 0) azimuthDegrees += 360f
            headingCallback?.invoke(azimuthDegrees, event.accuracy)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    val calendarReminderProvider: ICalendarReminderProvider = object : ICalendarReminderProvider {
        override fun setMuhurtaAlarm(title: String, epochMillis: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
            try {
                val calendar = java.util.Calendar.getInstance().apply {
                    timeInMillis = epochMillis
                }
                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_MESSAGE, title)
                    putExtra(AlarmClock.EXTRA_HOUR, calendar.get(java.util.Calendar.HOUR_OF_DAY))
                    putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(java.util.Calendar.MINUTE))
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to set alarm")
            }
        }

        override fun addEventToCalendar(
            title: String,
            description: String,
            startEpochMillis: Long,
            endEpochMillis: Long,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ) {
            try {
                val intent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    putExtra(CalendarContract.Events.TITLE, title)
                    putExtra(CalendarContract.Events.DESCRIPTION, description)
                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startEpochMillis)
                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endEpochMillis)
                    putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to add calendar event")
            }
        }

        override fun sendLocalNotification(title: String, message: String) {
            // Android local notification trigger
        }
    }

    val audioHapticsProvider: IAudioHapticsProvider = object : IAudioHapticsProvider {
        override fun triggerHapticFeedback(patternType: HapticType) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    val vibrator = vm?.defaultVibrator
                    val effect = when (patternType) {
                        HapticType.CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                        HapticType.HEAVY -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                        HapticType.SUCCESS -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                        else -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    }
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            } catch (_: Exception) {}
        }

        override fun playSingingBowlSound(frequencyHz: Float) {
            triggerHapticFeedback(HapticType.CLICK)
        }

        override fun playTempleBell() {
            triggerHapticFeedback(HapticType.MEDIUM)
        }
    }

    val shareProvider: INativeShareProvider = object : INativeShareProvider {
        override fun shareText(title: String, text: String) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_TITLE, title)
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareIntent = Intent.createChooser(sendIntent, title).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareIntent)
        }

        override fun copyToClipboard(label: String, text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}
