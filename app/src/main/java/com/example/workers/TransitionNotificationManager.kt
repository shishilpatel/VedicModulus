package com.example.workers

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object TransitionNotificationManager {
    private const val TAG = "TransitionNotifMgr"
    
    /**
     * Enqueues a periodic worker that runs roughly every 24 hours to schedule exact 
     * one-time notifications for Sunrise and Sunset transitions.
     */
    fun scheduleDailyTransitions(context: Context) {
        try {
            val workRequest = PeriodicWorkRequestBuilder<VedicTransitionSchedulerWorker>(24, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                VedicTransitionSchedulerWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
            Log.d(TAG, "Successfully scheduled daily Vedic Transition Scheduler.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule daily transitions: ${e.message}", e)
        }
    }
}
