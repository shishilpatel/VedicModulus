package com.example.core.platform

/**
 * Supported runtime targets for Kotlin Multiplatform (KMP).
 */
enum class PlatformType(val displayName: String, val isMobile: Boolean, val isWeb: Boolean) {
    ANDROID("Android", isMobile = true, isWeb = false),
    IOS("iOS", isMobile = true, isWeb = false),
    WEB_WASM("WebAssembly (Wasm)", isMobile = false, isWeb = true),
    DESKTOP("Desktop (JVM)", isMobile = false, isWeb = false)
}

/**
 * Common platform information interface.
 */
data class PlatformDetails(
    val type: PlatformType,
    val osVersion: String,
    val deviceModel: String,
    val supportsSensors: Boolean,
    val supportsHaptics: Boolean,
    val supportsNativeAlarms: Boolean,
    val supportsWearCompanion: Boolean
)

/**
 * Common interface for compass & orientation hardware across Android, iOS, and Web.
 */
interface ICompassSensorProvider {
    val isAvailable: Boolean
    fun startListening(onHeadingChanged: (azimuthDegrees: Float, accuracy: Int) -> Unit)
    fun stopListening()
}

/**
 * Common interface for calendar, alarms, and notifications.
 */
interface ICalendarReminderProvider {
    fun setMuhurtaAlarm(title: String, epochMillis: Long, onSuccess: () -> Unit, onError: (String) -> Unit)
    fun addEventToCalendar(
        title: String,
        description: String,
        startEpochMillis: Long,
        endEpochMillis: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    fun sendLocalNotification(title: String, message: String)
}

/**
 * Common interface for haptics & audio feedback (Temple Bells, Singing Bowls).
 */
interface IAudioHapticsProvider {
    fun triggerHapticFeedback(patternType: HapticType = HapticType.CLICK)
    fun playSingingBowlSound(frequencyHz: Float = 432f)
    fun playTempleBell()
}

enum class HapticType {
    LIGHT,
    MEDIUM,
    HEAVY,
    SUCCESS,
    WARNING,
    CLICK
}

/**
 * Common interface for sharing daily Panchang, Kundali, or Dincharya reports.
 */
interface INativeShareProvider {
    fun shareText(title: String, text: String)
    fun copyToClipboard(label: String, text: String)
}
