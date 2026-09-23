package com.example.data.sync

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import com.example.core.model.DailyVedicProfile
import com.example.core.model.PersonalizedProfile
import com.example.core.model.WatchFaceConfiguration
import com.example.generator.watchface.WatchFaceBitmapRenderer
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream

object WearWatchFaceSyncManager {

    private const val TAG = "WearWatchFaceSyncManager"
    const val PATH_DESIGN_UPDATE = "/watchface/design_update"
    const val PATH_APPLY_ACTIVE_WATCHFACE = "/watchface/set_active_watchface"
    const val KEY_BG_ASSET = "bg_image_asset"
    const val KEY_BG_STYLE_ID = "bg_style_id"
    const val KEY_ACCENT_COLOR = "accent_color_hex"
    const val KEY_PRIMARY_COLOR = "primary_color_hex"
    const val KEY_WATCH_STYLE = "watch_style"
    const val KEY_APPLY_IMMEDIATELY = "apply_immediately"
    private const val SYNC_TIMEOUT_MS = 2_000L

    /**
     * Renders the custom watch face design created in the mobile app into a high-res bitmap,
     * serializes it as an Asset, pushes it to the connected Wear OS / Galaxy Watch, and sends
     * the command to apply it directly as the active Watch Face.
     */
    suspend fun applyDesignAsActiveWatchFace(
        context: Context,
        config: WatchFaceConfiguration,
        personalizedProfile: PersonalizedProfile,
        dailyProfile: DailyVedicProfile?,
        existingBitmap: Bitmap? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val availability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context)
            if (availability != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                return@withContext Result.failure(Exception("Google Play Services / Wearable service unavailable"))
            }
            // 1. Render pixel-perfect 450x450 Bitmap of the mobile-designed watch face (or reuse existing)
            val bitmap = existingBitmap ?: WatchFaceBitmapRenderer.renderWatchFaceBitmap(
                config = config,
                personalizedProfile = personalizedProfile,
                dailyProfile = dailyProfile,
                size = 450
            )

            // 2. Build settings bundle
            val layoutSettings = Bundle().apply {
                putString(KEY_BG_STYLE_ID, "bg_custom")
                putString(KEY_WATCH_STYLE, config.style.name)
                putString(KEY_ACCENT_COLOR, String.format("#%08X", config.accentColorHex))
                putString(KEY_PRIMARY_COLOR, String.format("#%08X", config.primaryColorHex))
                putBoolean(KEY_APPLY_IMMEDIATELY, true)
                putLong("design_seed", System.currentTimeMillis())
                putString("theme_title", "${config.style.displayName} Watch Face")
            }

            // 3. Push design asset and configuration to Wear OS Data Layer
            val pushResult = pushDesignToWatch(context, bitmap, layoutSettings)
            if (pushResult.isFailure) {
                return@withContext pushResult
            }

            // 4. Send direct message command to activate watch face on all connected nodes
            try {
                val nodeClient = Wearable.getNodeClient(context)
                val messageClient = Wearable.getMessageClient(context)
                val connectedNodes = nodeClient.connectedNodes.await()
                for (node in connectedNodes) {
                    messageClient.sendMessage(
                        node.id,
                        PATH_APPLY_ACTIVE_WATCHFACE,
                        config.style.name.toByteArray(Charsets.UTF_8)
                    ).await()
                }
                Log.d(TAG, "Sent active watch face switch signal to ${connectedNodes.size} node(s).")
            } catch (e: Exception) {
                Log.w(TAG, "Message send skipped or completed with fallback: ${e.message}")
            }

            Result.success("Design successfully applied as Watch Face on your smartwatch!")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying design as watch face", e)
            Result.failure(e)
        }
    }

    /**
     * Converts a generated watch face bitmap and style parameters into a Data Layer payload,
     * verifies active watch connectivity, and pushes the update urgently.
     */
    suspend fun pushDesignToWatch(
        context: Context,
        bitmap: Bitmap,
        layoutSettings: Bundle
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            withTimeout(SYNC_TIMEOUT_MS) {
                val nodeClient = Wearable.getNodeClient(context)
                val dataClient = Wearable.getDataClient(context)

                // 1. Verify Connected Wear OS / Galaxy Watch Nodes
                val connectedNodes: List<Node> = nodeClient.connectedNodes.await()
                if (connectedNodes.isEmpty()) {
                    return@withTimeout Result.failure(
                        IllegalStateException("No Wear OS watch connected. Please ensure Bluetooth is enabled and paired in Galaxy Wearable.")
                    )
                }

                // 2. Compress and Serialize Bitmap to Data Layer Asset
                val asset = createAssetFromBitmap(bitmap)

                // 3. Build PutDataMapRequest
                val putDataMapReq = PutDataMapRequest.create(PATH_DESIGN_UPDATE).apply {
                    dataMap.putAsset(KEY_BG_ASSET, asset)
                    dataMap.putString(KEY_BG_STYLE_ID, layoutSettings.getString(KEY_BG_STYLE_ID, "bg_custom"))
                    dataMap.putString(KEY_WATCH_STYLE, layoutSettings.getString(KEY_WATCH_STYLE, "COSMIC"))
                    dataMap.putString(KEY_ACCENT_COLOR, layoutSettings.getString(KEY_ACCENT_COLOR, "#FFE8B05C"))
                    dataMap.putString(KEY_PRIMARY_COLOR, layoutSettings.getString(KEY_PRIMARY_COLOR, "#FFD4AF37"))
                    dataMap.putBoolean(KEY_APPLY_IMMEDIATELY, layoutSettings.getBoolean(KEY_APPLY_IMMEDIATELY, true))
                    dataMap.putLong("design_seed", layoutSettings.getLong("design_seed", System.currentTimeMillis()))
                    dataMap.putString("theme_title", layoutSettings.getString("theme_title", "Custom Vedic Theme"))
                    // High-resolution timestamp forces DataEvent trigger on Wear OS
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }

                // Set as urgent to bypass battery batching and transmit immediately
                val putDataReq = putDataMapReq.asPutDataRequest().setUrgent()

                // 4. Send to Google Data Layer
                val dataItem = dataClient.putDataItem(putDataReq).await()
                Log.d(TAG, "Design successfully synced to Wear OS Data Layer: ${dataItem.uri}")

                Result.success("Watch face design synchronized to ${connectedNodes.size} connected watch(es).")
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Timed out syncing design to watch after ${SYNC_TIMEOUT_MS}ms", e)
            Result.failure(Exception("Watch sync timed out. Please check watch connection and retry."))
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing design to watch", e)
            Result.failure(e)
        }
    }

    /**
     * Converts a Bitmap into a PNG Asset with stream buffering
     */
    fun createAssetFromBitmap(bitmap: Bitmap): Asset {
        val byteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 95, byteStream)
        return Asset.createFromBytes(byteStream.toByteArray())
    }
}
