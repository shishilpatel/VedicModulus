package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.VastuDirectionInfo
import com.example.data.UserPreferencesRepository
import com.example.engines.vastu.VastuEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * UI State for the Vastu Compass screen.
 */
data class VastuCompassUiState(
    val isLiveSensorActive: Boolean = true,
    val selectedDirection: VastuDirectionInfo = VastuEngine.ASHTA_DIK[0],
    val currentAzimuthDeg: Float = 0f,
    val rotationAngle: Float = 0f,
    val pitchAngle: Float = 0f,
    val rollAngle: Float = 0f,
    val deviceTilt: Float = 0f,
    val hasMagneticSensor: Boolean = true
)

/**
 * ViewModel responsible for maintaining Vastu compass live active/inactive state,
 * sensor subscriptions, and directional azimuth calculations across recompositions.
 */
class VastuCompassViewModel(
    application: Application
) : AndroidViewModel(application), SensorEventListener {

    companion object {
        // In-memory single source of truth shared across all tab switches and page navigations
        private var _sharedLiveSensorFlow: MutableStateFlow<Boolean>? = null

        fun getSharedSensorFlow(context: Context): MutableStateFlow<Boolean> {
            val current = _sharedLiveSensorFlow
            if (current != null) return current
            val cached = UserPreferencesRepository.isVastuLiveSensorEnabledCached(context)
            val flow = MutableStateFlow(cached)
            _sharedLiveSensorFlow = flow
            return flow
        }
    }

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private val vastuEngine = VastuEngine()
    private val preferencesRepository = UserPreferencesRepository(application)

    private val hasHardwareSensor = (sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) ||
            (sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null)

    private val _sharedIsLiveSensorActive = getSharedSensorFlow(application)

    // Dedicated StateFlow for live sensor active/inactive state to guarantee true state across recompositions
    val isLiveSensorActive: StateFlow<Boolean> = _sharedIsLiveSensorActive.asStateFlow()

    private val _uiState = MutableStateFlow(
        VastuCompassUiState(
            isLiveSensorActive = _sharedIsLiveSensorActive.value,
            hasMagneticSensor = hasHardwareSensor,
            selectedDirection = VastuEngine.ASHTA_DIK[0],
            rotationAngle = -VastuEngine.ASHTA_DIK[0].degrees.toFloat(),
            currentAzimuthDeg = VastuEngine.ASHTA_DIK[0].degrees.toFloat()
        )
    )
    val uiState: StateFlow<VastuCompassUiState> = _uiState.asStateFlow()

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var lastDirectionIndex = -1
    private var lastEmitTimestampMs = 0L
    private var lastEmittedDeg = -1f
    private var isSensorRegistered = false
    private var isScreenVisible = false

    init {
        // Read cached user preference without starting sensors in background
        val cached = UserPreferencesRepository.isVastuLiveSensorEnabledCached(getApplication())
        _sharedIsLiveSensorActive.value = cached
        _uiState.update { it.copy(isLiveSensorActive = cached) }
    }

    /**
     * Called when the Vastu Compass screen is actively visible and focused.
     * Starts sensors ONLY if the screen is visible AND the feature is enabled.
     */
    fun onScreenVisible() {
        isScreenVisible = true
        refreshFromStorage()
    }

    /**
     * Called when the user navigates away from the Vastu Compass or app is backgrounded.
     * Stops all hardware sensors immediately to eliminate battery drain and background haptic feedback.
     */
    fun onScreenHidden() {
        isScreenVisible = false
        lastDirectionIndex = -1
        stopSensors()
    }

    /**
     * Re-reads the on/off status from persistent storage and updates state.
     * Only activates hardware sensors if the screen is currently visible.
     */
    fun refreshFromStorage() {
        val cached = UserPreferencesRepository.isVastuLiveSensorEnabledCached(getApplication())
        _sharedIsLiveSensorActive.value = cached
        _uiState.update { it.copy(isLiveSensorActive = cached) }

        if (cached && isScreenVisible) {
            startSensors()
        } else {
            stopSensors()
        }
    }

    /**
     * Toggles or sets the active/inactive state of the live sensor.
     * Persists synchronously to SharedPreferences and asynchronously to DataStore.
     */
    fun setLiveSensorActive(enabled: Boolean) {
        // Synchronous write guarantees persistence even if page/tab changes immediately
        UserPreferencesRepository.setVastuLiveSensorEnabledCached(getApplication(), enabled)

        _sharedIsLiveSensorActive.value = enabled
        _uiState.update { it.copy(isLiveSensorActive = enabled) }

        if (enabled && isScreenVisible) {
            startSensors()
        } else {
            stopSensors()
        }

        viewModelScope.launch {
            preferencesRepository.saveVastuLiveSensorEnabled(enabled)
        }
    }

    fun toggleLiveSensor() {
        setLiveSensorActive(!_sharedIsLiveSensorActive.value)
    }

    /**
     * Selects a direction manually for architectural audit and points
     * the compass directly to that cardinal zone.
     * Note: Does NOT disable the user's live sensor / space optimization switch.
     */
    fun selectDirection(direction: VastuDirectionInfo) {
        _uiState.update { current ->
            current.copy(
                selectedDirection = direction,
                rotationAngle = -direction.degrees.toFloat(),
                currentAzimuthDeg = direction.degrees.toFloat()
            )
        }
    }

    fun selectDirectionByName(directionName: String) {
        val target = VastuEngine.ASHTA_DIK.find { it.directionName.equals(directionName, ignoreCase = true) }
            ?: VastuEngine.ASHTA_DIK[0]
        selectDirection(target)
    }

    /**
     * Updates rotation from user touch/drag interaction on the compass wheel.
     * Note: Does NOT disable the user's live sensor / space optimization switch.
     */
    fun updateManualRotation(rotationAngle: Float) {
        val norm = ((-rotationAngle % 360f) + 360f) % 360f
        val dir = vastuEngine.getDirectionByAngle(norm.toDouble())

        _uiState.update { current ->
            current.copy(
                rotationAngle = rotationAngle % 360f,
                currentAzimuthDeg = norm,
                selectedDirection = dir
            )
        }
    }

    /**
     * Starts listening to device orientation sensors if live sensor is enabled and screen is visible.
     * Uses SENSOR_DELAY_UI to avoid flooding the UI thread and freezing Compose rendering.
     */
    fun startSensors() {
        if (!isScreenVisible) return
        if (isSensorRegistered) return
        if (!_sharedIsLiveSensorActive.value) return
        val sm = sensorManager ?: return

        val rotationVectorSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor != null) {
            sm.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
            isSensorRegistered = true
        } else {
            val accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magnetometer = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            if (accelerometer != null && magnetometer != null) {
                sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
                sm.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
                isSensorRegistered = true
            }
        }
    }

    /**
     * Stops listening to device sensors to conserve battery and avoid background haptic feedback.
     */
    fun stopSensors() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {}
        isSensorRegistered = false
    }

    private fun lowPass(input: FloatArray, output: FloatArray?): FloatArray {
        if (output == null) return input.clone()
        val alpha = 0.35f
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
        return output
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!isScreenVisible || !_sharedIsLiveSensorActive.value) {
            stopSensors()
            return
        }

        val rMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var hasOrientation = false

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMatrix, event.values)
            SensorManager.getOrientation(rMatrix, orientation)
            hasOrientation = true
        } else {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity = lowPass(event.values, gravity)
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = lowPass(event.values, geomagnetic)
            }

            val currentGravity = gravity
            val currentGeo = geomagnetic

            if (currentGravity != null && currentGeo != null) {
                val iMatrix = FloatArray(9)
                if (SensorManager.getRotationMatrix(rMatrix, iMatrix, currentGravity, currentGeo)) {
                    SensorManager.getOrientation(rMatrix, orientation)
                    hasOrientation = true
                }
            }
        }

        if (hasOrientation) {
            val azimuthRad = orientation[0]
            var deg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            if (deg < 0) deg += 360f

            val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
            val tilt = sqrt(pitch * pitch + roll * roll)

            val newDir = vastuEngine.getDirectionByAngle(deg.toDouble())
            val newIndex = VastuEngine.ASHTA_DIK.indexOfFirst { it.directionName == newDir.directionName }

            val now = System.currentTimeMillis()
            val isDirectionChanged = (newIndex != -1 && newIndex != lastDirectionIndex)
            val isAngleSignificant = kotlin.math.abs(deg - lastEmittedDeg) >= 0.5f
            val isTimeElapsed = (now - lastEmitTimestampMs) >= 40L

            if (isDirectionChanged) {
                lastDirectionIndex = newIndex
                triggerHapticFeedback()
            }

            // Rate-limit state updates to avoid starving Main Looper and freezing UI
            if (isDirectionChanged || (isTimeElapsed && isAngleSignificant)) {
                lastEmitTimestampMs = now
                lastEmittedDeg = deg
                _uiState.update { current ->
                    current.copy(
                        currentAzimuthDeg = deg,
                        rotationAngle = -deg,
                        pitchAngle = pitch,
                        rollAngle = roll,
                        deviceTilt = tilt,
                        selectedDirection = newDir
                    )
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun triggerHapticFeedback() {
        if (!isScreenVisible || !_sharedIsLiveSensorActive.value) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (e: Exception) {
            // Ignore if vibration fails or unsupported on device
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSensors()
    }
}
