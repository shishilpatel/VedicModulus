package com.example.wear

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Dedicated background computation dispatcher and HandlerThread for Vedic astronomical,
 * astrological, and procedural watch face geometry calculations.
 *
 * Runs heavy mathematical, trigonometry, and planetary coordinate calculations
 * completely off the Android Main/UI thread to guarantee 60 FPS UI responsiveness.
 */
object VedicCalculationDispatcher {
    private const val TAG = "VedicCalcDispatcher"

    // Dedicated HandlerThread with background priority
    private val calculationHandlerThread = HandlerThread("VedicCalculationThread", Process.THREAD_PRIORITY_BACKGROUND).apply {
        start()
    }

    val handler: Handler = Handler(calculationHandlerThread.looper)

    // Dedicated single-thread ExecutorService for deterministic FIFO task execution
    val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "VedicCalculationExecutor").apply {
            priority = Thread.NORM_PRIORITY - 1
            isDaemon = true
        }
    }

    // Coroutine dispatcher backed by the dedicated executor
    val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

    fun post(task: Runnable) {
        handler.post(task)
    }

    fun <T> submit(callable: Callable<T>): Future<T> {
        return executor.submit(callable)
    }

    suspend fun <T> executeAsync(block: suspend () -> T): T {
        return withContext(dispatcher) {
            try {
                block()
            } catch (e: Exception) {
                Log.e(TAG, "Error executing Vedic calculation in background: ${e.message}", e)
                throw e
            }
        }
    }
}
