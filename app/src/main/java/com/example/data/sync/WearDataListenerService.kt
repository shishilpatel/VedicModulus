package com.example.data.sync

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

class WearDataListenerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "WearDataListener"
        const val PATH_DESIGN_UPDATE = "/watchface/design_update"
        const val PATH_APPLY_ACTIVE_WATCHFACE = "/watchface/set_active_watchface"
        const val KEY_BG_ASSET = "bg_image_asset"
        const val KEY_BG_STYLE_ID = "bg_style_id"
        const val KEY_ACCENT_COLOR = "accent_color_hex"
        const val KEY_PRIMARY_COLOR = "primary_color_hex"
        const val KEY_WATCH_STYLE = "watch_style"
        const val KEY_APPLY_IMMEDIATELY = "apply_immediately"
        const val CUSTOM_BG_FILENAME = "watchface_custom_bg.png"
        const val ACTION_WATCHFACE_UPDATED = "com.example.watchface.ACTION_DESIGN_UPDATED"
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val uri = event.dataItem.uri
                if (uri.path == PATH_DESIGN_UPDATE) {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    serviceScope.launch {
                        processIncomingDesign(dataMap)
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        if (messageEvent.path == PATH_APPLY_ACTIVE_WATCHFACE) {
            val styleName = String(messageEvent.data, Charsets.UTF_8)
            Log.d(TAG, "Received active watch face switch request: $styleName")
            applyWatchFaceLocally(styleName)
        }
    }

    private suspend fun processIncomingDesign(dataMap: com.google.android.gms.wearable.DataMap) {
        try {
            val bgStyleId = dataMap.getString(KEY_BG_STYLE_ID, "bg_custom")
            val accentColor = dataMap.getString(KEY_ACCENT_COLOR, "#FFE8B05C")
            val primaryColor = dataMap.getString(KEY_PRIMARY_COLOR, "#FFD4AF37")
            val watchStyle = dataMap.getString(KEY_WATCH_STYLE, "COSMIC")
            val applyImmediately = dataMap.getBoolean(KEY_APPLY_IMMEDIATELY, true)
            val bgAsset = dataMap.getAsset(KEY_BG_ASSET)

            // 1. Extract Asset InputStream and Save to Watch Internal Storage
            if (bgAsset != null) {
                val fdForAsset = Wearable.getDataClient(applicationContext)
                    .getFdForAsset(bgAsset)
                    .await()

                fdForAsset?.inputStream?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        saveBitmapToLocalStorage(bitmap, CUSTOM_BG_FILENAME)
                    }
                }
            }

            // 2. Persist Style Preferences locally on the watch
            val prefs = applicationContext.getSharedPreferences("wff_styles", MODE_PRIVATE)
            prefs.edit()
                .putString("current_bg_style", bgStyleId)
                .putString("current_accent_color", accentColor)
                .putString("current_primary_color", primaryColor)
                .putString("current_watch_style", watchStyle)
                .putBoolean("is_custom_active", true)
                .putLong("last_synced_time", System.currentTimeMillis())
                .apply()

            if (applyImmediately) {
                applyWatchFaceLocally(watchStyle)
            }

            Log.d(TAG, "Watch Face design update processed successfully: style=$watchStyle, color=$accentColor")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process incoming watch face design", e)
        }
    }

    private fun applyWatchFaceLocally(styleName: String) {
        // Broadcast change so watch face components, complications, and WFF configuration reload instantly
        try {
            val intent = Intent(ACTION_WATCHFACE_UPDATED).apply {
                putExtra("watch_style", styleName)
                putExtra("is_active", true)
                setPackage(packageName)
            }
            applicationContext.sendBroadcast(intent)
            com.example.ui.watch.VedicComplicationDataHelper.requestComplicationUpdates(applicationContext)
        } catch (e: Exception) {
            Log.w(TAG, "Watch face broadcast update notice: ${e.message}")
        }
    }

    private fun saveBitmapToLocalStorage(bitmap: Bitmap, filename: String) {
        val file = File(applicationContext.filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
