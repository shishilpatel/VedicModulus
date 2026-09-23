package com.example.wear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime

/**
 * Wear OS Low-Power and Thermal Throttling Protection Manager.
 *
 * Prevents thermal throttling, CPU wake lock drain, and screen freezing by:
 * 1. Detecting low battery conditions (<= threshold, default 20%).
 * 2. Monitoring system Power Save Mode and Android OS Thermal Status (API 29+).
 * 3. Dynamically dropping canvas tick frequency from 500ms (2Hz) down to 60,000ms (1 min tick)
 *    when in Low-Power Mode — yielding a 99.2% reduction in wakeups.
 * 4. Eliminating second hand drawing and disabling continuous animations/shaders.
 */
data class WearPowerState(
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false,
    val isThermalThrottled: Boolean = false,
    val thermalStatus: Int = 0,
    val thermalStatusName: String = "NOMINAL",
    val forceLowPowerMode: Boolean = false,
    val isAutoLowPowerEnabled: Boolean = true,
    val lowPowerThresholdPercent: Int = 20
) {
    /**
     * Active if manually forced OR if auto mode is on and conditions trigger it
     * (low battery, power save mode, or thermal throttling).
     */
    val isLowPowerModeActive: Boolean
        get() = forceLowPowerMode || (isAutoLowPowerEnabled && (
            batteryPercent <= lowPowerThresholdPercent || isPowerSaveMode || isThermalThrottled
        ))

    /**
     * Dynamic update interval:
     * - Low-Power Mode: 60,000ms (1 minute interval with exact minute-boundary alignment)
     * - Normal High-Fidelity Active Mode: 500ms (smooth 2Hz animation)
     */
    val updateIntervalMs: Long
        get() = if (isLowPowerModeActive) 60_000L else 500L

    val shouldRenderSecondHand: Boolean
        get() = !isLowPowerModeActive

    val powerModeBadgeText: String
        get() = when {
            isThermalThrottled -> "🔥 THERMAL ECO (${batteryPercent}%)"
            forceLowPowerMode -> "⚡ LOW POWER (${batteryPercent}%)"
            batteryPercent <= lowPowerThresholdPercent -> "⚡ ECO (${batteryPercent}%)"
            isPowerSaveMode -> "🔋 BATTERY SAVER"
            else -> "NOMINAL (${batteryPercent}%)"
        }
}

object WearPowerManager {
    private const val TAG = "WearPowerManager"
    private const val PREFS_NAME = "wear_power_prefs"
    private const val KEY_FORCE_LOW_POWER = "force_low_power"
    private const val KEY_AUTO_LOW_POWER = "auto_low_power_enabled"
    private const val KEY_THRESHOLD = "low_power_threshold"

    private val _powerState = MutableStateFlow(WearPowerState())
    val powerState: StateFlow<WearPowerState> = _powerState.asStateFlow()

    private var isInitialized = false
    private var batteryReceiver: BroadcastReceiver? = null
    private var thermalListener: Any? = null // Store reference for API 29+

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val appContext = context.applicationContext
        loadPersistedSettings(appContext)
        registerBatteryAndPowerReceivers(appContext)
        registerThermalListener(appContext)
        refreshPowerState(appContext)

        Log.d(TAG, "WearPowerManager initialized. Low power active: ${_powerState.value.isLowPowerModeActive}")
    }

    fun destroy(context: Context) {
        if (!isInitialized) return
        val appContext = context.applicationContext

        batteryReceiver?.let {
            try {
                appContext.unregisterReceiver(it)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering battery receiver: ${e.message}")
            }
            batteryReceiver = null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && thermalListener != null) {
            try {
                val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
                val listener = thermalListener as? PowerManager.OnThermalStatusChangedListener
                if (powerManager != null && listener != null) {
                    powerManager.removeThermalStatusListener(listener)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error removing thermal listener: ${e.message}")
            }
            thermalListener = null
        }

        isInitialized = false
    }

    private fun loadPersistedSettings(context: Context) {
        try {
            val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val forceLowPower = sp.getBoolean(KEY_FORCE_LOW_POWER, false)
            val autoEnabled = sp.getBoolean(KEY_AUTO_LOW_POWER, true)
            val threshold = sp.getInt(KEY_THRESHOLD, 20)

            _powerState.value = _powerState.value.copy(
                forceLowPowerMode = forceLowPower,
                isAutoLowPowerEnabled = autoEnabled,
                lowPowerThresholdPercent = threshold
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading power settings: ${e.message}")
        }
    }

    private fun registerBatteryAndPowerReceivers(context: Context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent == null || c == null) return
                when (intent.action) {
                    Intent.ACTION_BATTERY_CHANGED,
                    Intent.ACTION_POWER_CONNECTED,
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                status == BatteryManager.BATTERY_STATUS_FULL

                        if (level >= 0 && scale > 0) {
                            val batteryPct = (level * 100 / scale.toFloat()).toInt()
                            updateBatteryLevel(c, batteryPct, isCharging)
                        }
                    }
                    PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                        refreshPowerState(c)
                    }
                }
            }
        }
        batteryReceiver = receiver

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        }
        try {
            context.registerReceiver(receiver, filter)
        } catch (e: Exception) {
            Log.e(TAG, "Failed registering battery receiver: ${e.message}")
        }
    }

    private fun registerThermalListener(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (powerManager != null) {
                    val listener = PowerManager.OnThermalStatusChangedListener { status ->
                        val (isThrottled, statusName) = parseThermalStatus(status)
                        Log.i(TAG, "Thermal status changed: $statusName ($status) -> Throttled=$isThrottled")
                        _powerState.value = _powerState.value.copy(
                            thermalStatus = status,
                            thermalStatusName = statusName,
                            isThermalThrottled = isThrottled
                        )
                        AstroDataStore.setLowPowerState(
                            context,
                            _powerState.value.isLowPowerModeActive,
                            isThrottled,
                            statusName
                        )
                    }
                    powerManager.addThermalStatusListener(listener)
                    thermalListener = listener

                    // Query initial thermal status
                    val currentThermal = powerManager.currentThermalStatus
                    val (isThrottled, statusName) = parseThermalStatus(currentThermal)
                    _powerState.value = _powerState.value.copy(
                        thermalStatus = currentThermal,
                        thermalStatusName = statusName,
                        isThermalThrottled = isThrottled
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Thermal listener not supported or error: ${e.message}")
            }
        }
    }

    private fun parseThermalStatus(status: Int): Pair<Boolean, String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (status) {
                PowerManager.THERMAL_STATUS_NONE -> false to "NOMINAL"
                PowerManager.THERMAL_STATUS_LIGHT -> false to "LIGHT"
                PowerManager.THERMAL_STATUS_MODERATE -> true to "MODERATE (THROTTLED)"
                PowerManager.THERMAL_STATUS_SEVERE -> true to "SEVERE (THROTTLED)"
                PowerManager.THERMAL_STATUS_CRITICAL -> true to "CRITICAL (THROTTLED)"
                PowerManager.THERMAL_STATUS_EMERGENCY -> true to "EMERGENCY (THROTTLED)"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> true to "SHUTDOWN"
                else -> false to "UNKNOWN"
            }
        } else {
            false to "NOMINAL"
        }
    }

    fun refreshPowerState(context: Context) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isPowerSave = powerManager?.isPowerSaveMode ?: false

        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else _powerState.value.batteryPercent

        _powerState.value = _powerState.value.copy(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            isPowerSaveMode = isPowerSave
        )

        AstroDataStore.setLowPowerState(
            context,
            _powerState.value.isLowPowerModeActive,
            _powerState.value.isThermalThrottled,
            _powerState.value.thermalStatusName
        )
    }

    private fun updateBatteryLevel(context: Context, batteryPct: Int, isCharging: Boolean) {
        val oldState = _powerState.value.isLowPowerModeActive
        _powerState.value = _powerState.value.copy(
            batteryPercent = batteryPct,
            isCharging = isCharging
        )
        val newState = _powerState.value.isLowPowerModeActive
        if (oldState != newState) {
            Log.d(TAG, "Low Power Mode transition: old=$oldState, new=$newState (battery=$batteryPct%, charging=$isCharging)")
        }
        AstroDataStore.setLowPowerState(
            context,
            newState,
            _powerState.value.isThermalThrottled,
            _powerState.value.thermalStatusName
        )
        // Also update battery percentage in data store for complications
        if (AstroDataStore.syncedData.value.batteryPercent != batteryPct) {
            AstroDataStore.syncedData.value = AstroDataStore.syncedData.value.copy(batteryPercent = batteryPct)
        }
    }

    fun toggleForceLowPower(context: Context): Boolean {
        val nextValue = !_powerState.value.forceLowPowerMode
        setForceLowPower(context, nextValue)
        return nextValue
    }

    fun setForceLowPower(context: Context, force: Boolean) {
        _powerState.value = _powerState.value.copy(forceLowPowerMode = force)
        persistSetting(context, KEY_FORCE_LOW_POWER, force)
        AstroDataStore.setLowPowerState(
            context,
            _powerState.value.isLowPowerModeActive,
            _powerState.value.isThermalThrottled,
            _powerState.value.thermalStatusName
        )
    }

    fun setAutoLowPowerEnabled(context: Context, enabled: Boolean) {
        _powerState.value = _powerState.value.copy(isAutoLowPowerEnabled = enabled)
        persistSetting(context, KEY_AUTO_LOW_POWER, enabled)
        AstroDataStore.setLowPowerState(
            context,
            _powerState.value.isLowPowerModeActive,
            _powerState.value.isThermalThrottled,
            _powerState.value.thermalStatusName
        )
    }

    fun setLowPowerThreshold(context: Context, thresholdPercent: Int) {
        val clamped = thresholdPercent.coerceIn(5, 50)
        _powerState.value = _powerState.value.copy(lowPowerThresholdPercent = clamped)
        persistSetting(context, KEY_THRESHOLD, clamped)
        AstroDataStore.setLowPowerState(
            context,
            _powerState.value.isLowPowerModeActive,
            _powerState.value.isThermalThrottled,
            _powerState.value.thermalStatusName
        )
    }

    private fun persistSetting(context: Context, key: String, value: Any) {
        try {
            val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = sp.edit()
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is String -> editor.putString(key, value)
            }
            editor.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving power pref $key: ${e.message}")
        }
    }

    /**
     * Calculates the exact delay (in milliseconds) to sleep until the start of the next minute.
     * Used in Low-Power Mode to ensure the watch face updates precisely on :00 seconds without drift.
     */
    fun calculateDelayToNextMinuteMs(): Long {
        val now = LocalTime.now()
        val secondsUntilNextMinute = 60 - now.second
        val millisInCurrentSecond = now.nano / 1_000_000L
        val delayMs = (secondsUntilNextMinute * 1000L) - millisInCurrentSecond
        return delayMs.coerceIn(1000L, 60000L)
    }
}
