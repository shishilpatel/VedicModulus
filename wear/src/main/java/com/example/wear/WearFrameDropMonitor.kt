package com.example.wear

import android.os.SystemClock
import android.util.Log
import android.view.Choreographer

/**
 * High-precision frame timing and drop monitor using Android's Choreographer.FrameCallback.
 *
 * Measures delta frame times, detects skipped VSYNC frames, calculates real-time rolling FPS,
 * and logs warning diagnostics to Logcat to pinpoint exactly which watch face rendering functions
 * or calculations cause UI thread drops or main thread freezes.
 */
object WearFrameDropMonitor : Choreographer.FrameCallback {
    private const val TAG = "WearFrameMonitor"
    
    // Constants for 60Hz display refresh interval
    private const val VSYNC_NANOS = 16_666_666L // 16.67ms standard frame budget
    private const val MIN_JANK_NANOS = 33_333_333L // > 2 frames threshold (33.3ms)
    private const val SEVERE_FREEZE_NANOS = 100_000_000L // 100ms severe freeze threshold

    @Volatile
    private var isRunning = false
    private var lastFrameTimeNanos: Long = 0L
    private var totalFramesRendered: Long = 0L
    private var totalDroppedFrames: Long = 0L
    private var windowStartTimeMs: Long = 0L
    private var windowFrames: Int = 0
    private var windowDroppedFrames: Long = 0L

    @Volatile
    var currentRenderPhase: String = "IDLE"
        private set

    /**
     * Start monitoring frame drops on the main UI thread.
     */
    fun start() {
        if (isRunning) return
        isRunning = true
        lastFrameTimeNanos = 0L
        windowStartTimeMs = SystemClock.elapsedRealtime()
        windowFrames = 0
        windowDroppedFrames = 0L
        Log.i(TAG, "🟢 WearFrameDropMonitor started: Listening for Choreographer frame intervals")
        try {
            Choreographer.getInstance().postFrameCallback(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register Choreographer.FrameCallback: ${e.message}")
        }
    }

    /**
     * Stop monitoring to conserve battery and CPU when watch goes to sleep or background.
     */
    fun stop() {
        if (!isRunning) return
        isRunning = false
        Log.i(TAG, "🔴 WearFrameDropMonitor stopped (Total frames: $totalFramesRendered, Total dropped: $totalDroppedFrames)")
        try {
            Choreographer.getInstance().removeFrameCallback(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister Choreographer.FrameCallback: ${e.message}")
        }
    }

    /**
     * Tag active rendering or calculation section to correlate slow frames with exact functions.
     */
    fun markPhase(phase: String) {
        currentRenderPhase = phase
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!isRunning) return

        if (lastFrameTimeNanos > 0L) {
            val frameDeltaNanos = frameTimeNanos - lastFrameTimeNanos
            val frameDurationMs = frameDeltaNanos / 1_000_000f

            if (frameDeltaNanos > VSYNC_NANOS) {
                val dropped = ((frameDeltaNanos - VSYNC_NANOS) / VSYNC_NANOS).coerceAtLeast(1)
                totalDroppedFrames += dropped
                windowDroppedFrames += dropped

                if (frameDeltaNanos >= SEVERE_FREEZE_NANOS) {
                    Log.e(
                        TAG,
                        "🚨 SEVERE MAIN-THREAD FREEZE: Frame duration = ${"%.1f".format(frameDurationMs)}ms (~$dropped dropped frames). Active Phase: [$currentRenderPhase]. Thread was blocked!"
                    )
                } else if (frameDeltaNanos >= MIN_JANK_NANOS) {
                    Log.w(
                        TAG,
                        "⚠️ JANK / FRAME DROP: Frame duration = ${"%.1f".format(frameDurationMs)}ms ($dropped dropped frames). Active Phase: [$currentRenderPhase]"
                    )
                }
            }
        }

        lastFrameTimeNanos = frameTimeNanos
        totalFramesRendered++
        windowFrames++

        // Periodic 4-second throughput summary
        val nowMs = SystemClock.elapsedRealtime()
        if (nowMs - windowStartTimeMs >= 4000L) {
            val elapsedSec = (nowMs - windowStartTimeMs) / 1000f
            val instantaneousFps = windowFrames / elapsedSec
            Log.d(
                TAG,
                "📊 Wear UI Frame Rate: FPS=${"%.1f".format(instantaneousFps)} | Dropped in window: $windowDroppedFrames | Total Dropped: $totalDroppedFrames | Phase: [$currentRenderPhase]"
            )
            windowStartTimeMs = nowMs
            windowFrames = 0
            windowDroppedFrames = 0L
        }

        if (isRunning) {
            Choreographer.getInstance().postFrameCallback(this)
        }
    }
}
