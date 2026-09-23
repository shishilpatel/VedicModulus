package com.example.engines.chakra

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.core.model.chakra.AmbientSoundTrack
import com.example.core.model.chakra.ChakraType
import com.example.core.model.chakra.MeditationMode
import com.example.core.model.chakra.MeditationPlayerState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

class MeditationPlayer(
    private val context: Context,
    private val onSessionCompleted: (() -> Unit)? = null
) {
    companion object {
        private const val TAG = "MeditationPlayer"
    }

    private var primaryPlayer: MediaPlayer? = null
    private var ambientPlayer: MediaPlayer? = null
    private var cuePlayer: MediaPlayer? = null

    private val _playerState = MutableStateFlow(MeditationPlayerState())
    val playerState: StateFlow<MeditationPlayerState> = _playerState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickerJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        startProgressTicker()
    }

    fun playChakra(
        chakra: ChakraType,
        mode: MeditationMode = _playerState.value.currentMode,
        ambient: AmbientSoundTrack = _playerState.value.selectedAmbient,
        durationMinutes: Int = _playerState.value.targetDurationMinutes
    ) {
        _playerState.value = _playerState.value.copy(
            currentChakra = chakra,
            currentMode = mode,
            selectedAmbient = ambient,
            targetDurationMinutes = durationMinutes,
            elapsedSeconds = 0,
            isSessionComplete = false
        )

        loadAndPlayPrimary(chakra.audioFileName, loop = true)
        loadAndPlayAmbient(ambient.audioPath)
        _playerState.value = _playerState.value.copy(isPlaying = true)
    }

    fun playAudioPath(
        audioPath: String,
        ambient: AmbientSoundTrack = _playerState.value.selectedAmbient,
        loop: Boolean = true
    ) {
        loadAndPlayPrimary(audioPath, loop = loop)
        loadAndPlayAmbient(ambient.audioPath)
        _playerState.value = _playerState.value.copy(isPlaying = true)
    }

    fun setAmbientTrack(ambient: AmbientSoundTrack) {
        _playerState.value = _playerState.value.copy(selectedAmbient = ambient)
        if (_playerState.value.isPlaying) {
            loadAndPlayAmbient(ambient.audioPath)
        }
    }

    fun setMode(mode: MeditationMode) {
        _playerState.value = _playerState.value.copy(currentMode = mode)
    }

    fun setTargetDuration(minutes: Int) {
        _playerState.value = _playerState.value.copy(targetDurationMinutes = minutes)
    }

    fun togglePlayPause() {
        if (_playerState.value.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun resume() {
        try {
            primaryPlayer?.start()
            ambientPlayer?.start()
            _playerState.value = _playerState.value.copy(isPlaying = true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume playback", e)
        }
    }

    fun pause() {
        try {
            primaryPlayer?.pause()
            ambientPlayer?.pause()
            _playerState.value = _playerState.value.copy(isPlaying = false)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause playback", e)
        }
    }

    fun stop() {
        try {
            primaryPlayer?.stop()
            ambientPlayer?.stop()
            primaryPlayer?.reset()
            ambientPlayer?.reset()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping players", e)
        }
        _playerState.value = _playerState.value.copy(
            isPlaying = false,
            currentPositionMs = 0L,
            elapsedSeconds = 0
        )
    }

    fun seekTo(positionMs: Long) {
        try {
            primaryPlayer?.seekTo(positionMs.toInt())
            _playerState.value = _playerState.value.copy(currentPositionMs = positionMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking player", e)
        }
    }

    /**
     * Seeks the overall meditation session elapsed time and audio playback position.
     */
    fun seekSessionSeconds(targetSeconds: Int) {
        val totalSec = _playerState.value.targetDurationMinutes * 60
        val clampedSec = targetSeconds.coerceIn(0, totalSec)
        _playerState.value = _playerState.value.copy(
            elapsedSeconds = clampedSec,
            isSessionComplete = clampedSec >= totalSec
        )
        // Also seek primary audio if valid duration is known
        val audioDuration = primaryPlayer?.duration ?: 0
        if (audioDuration > 0) {
            val audioPos = (clampedSec * 1000L) % audioDuration
            try {
                primaryPlayer?.seekTo(audioPos.toInt())
            } catch (e: Exception) {
                Log.e(TAG, "Error seeking audio track", e)
            }
        }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _playerState.value = _playerState.value.copy(volume = clamped)
        primaryPlayer?.setVolume(clamped, clamped)
        ambientPlayer?.setVolume(clamped * 0.75f, clamped * 0.75f)
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _playerState.value = _playerState.value.copy(sleepTimerRemainingMinutes = minutes)
        if (minutes <= 0) return

        sleepTimerJob = scope.launch {
            var rem = minutes
            while (rem > 0 && isActive) {
                delay(60_000L)
                rem--
                _playerState.value = _playerState.value.copy(sleepTimerRemainingMinutes = rem)
            }
            if (isActive) {
                pause()
                _playerState.value = _playerState.value.copy(sleepTimerRemainingMinutes = 0)
            }
        }
    }

    fun playCueBell(cuePath: String) {
        try {
            cuePlayer?.release()
            cuePlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                val afd = context.assets.openFd(cuePath)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                setVolume(0.8f, 0.8f)
                start()
                setOnCompletionListener {
                    it.release()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing cue bell: $cuePath", e)
        }
    }

    private fun loadAndPlayPrimary(assetPath: String, loop: Boolean) {
        try {
            primaryPlayer?.stop()
            primaryPlayer?.release()
            primaryPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                val afd = context.assets.openFd(assetPath)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = loop
                prepare()
                val vol = _playerState.value.volume
                setVolume(vol, vol)
                start()
            }
            _playerState.value = _playerState.value.copy(
                durationMs = primaryPlayer?.duration?.toLong() ?: 0L,
                currentPositionMs = 0L
            )
        } catch (e: IOException) {
            Log.e(TAG, "Could not open primary audio: $assetPath", e)
        }
    }

    private fun loadAndPlayAmbient(assetPath: String) {
        try {
            ambientPlayer?.stop()
            ambientPlayer?.release()
            ambientPlayer = null

            if (assetPath.isBlank()) return

            ambientPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                val afd = context.assets.openFd(assetPath)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                prepare()
                val vol = _playerState.value.volume * 0.75f
                setVolume(vol, vol)
                start()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Could not open ambient audio: $assetPath", e)
        }
    }

    private fun startProgressTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                if (_playerState.value.isPlaying) {
                    val pos = primaryPlayer?.currentPosition?.toLong() ?: 0L
                    val dur = primaryPlayer?.duration?.toLong() ?: _playerState.value.durationMs
                    val newElapsed = _playerState.value.elapsedSeconds + 1
                    val targetSeconds = _playerState.value.targetDurationMinutes * 60

                    val isComplete = newElapsed >= targetSeconds
                    _playerState.value = _playerState.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur,
                        elapsedSeconds = newElapsed,
                        isSessionComplete = isComplete
                    )

                    if (isComplete && !_playerState.value.isLooping) {
                        pause()
                        onSessionCompleted?.invoke()
                    }
                }
            }
        }
    }

    fun release() {
        tickerJob?.cancel()
        sleepTimerJob?.cancel()
        try {
            primaryPlayer?.release()
            ambientPlayer?.release()
            cuePlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing players", e)
        }
        primaryPlayer = null
        ambientPlayer = null
        cuePlayer = null
        scope.cancel()
    }
}
