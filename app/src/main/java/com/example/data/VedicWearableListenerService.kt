package com.example.data

import android.util.Log
import com.example.data.vitality.VedicVitalityService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class VedicWearableListenerService : WearableListenerService() {
    companion object {
        private const val TAG = "VedicWearableListener"
        const val PATH_VITALITY_TELEMETRY = "/vedic_vitality_telemetry"
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val uri = event.dataItem.uri
                if (uri.path == PATH_VITALITY_TELEMETRY) {
                    try {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        val steps = dataMap.getInt("step_count", 0)
                        val hr = dataMap.getInt("heart_rate_bpm", 0)
                        val timestamp = dataMap.getLong("timestamp", System.currentTimeMillis())
                        Log.d(TAG, "Received health telemetry from Wear OS: Steps=$steps, HR=$hr")
                        VedicVitalityService.getInstance(applicationContext)
                            .ingestWearHealthTelemetry(steps = steps, heartRate = hr, timestamp = timestamp)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing telemetry DataMap: ${e.message}")
                    }
                } else if (uri.path == VedicWearableSyncManager.PATH_VEDIC_SYNC) {
                    Log.d(TAG, "Data Client event received from Galaxy Watch: $uri")
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        if (messageEvent.path == PATH_VITALITY_TELEMETRY) {
            try {
                val dataStr = String(messageEvent.data, Charsets.UTF_8)
                val parts = dataStr.split(":")
                val steps = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val hr = parts.getOrNull(1)?.toIntOrNull() ?: 0
                VedicVitalityService.getInstance(applicationContext)
                    .ingestWearHealthTelemetry(steps = steps, heartRate = hr)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling message telemetry: ${e.message}")
            }
        }
    }
}

