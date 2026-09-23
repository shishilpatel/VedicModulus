package com.example.data

import android.content.Context
import android.util.Log
import com.example.core.model.BirthProfile
import com.example.core.model.DailyVedicProfile
import com.example.core.model.PersonalizedProfile
import com.example.core.model.WatchFaceConfiguration
import com.example.data.room.PredictionCacheEntity
import com.example.data.room.VedicDatabase
import com.example.data.sync.WearWatchFaceSyncManager
import com.example.generator.watchface.WatchFaceBitmapRenderer
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

object VedicWearableSyncManager {
    private const val TAG = "VedicWearableSyncManager"
    const val PATH_VEDIC_SYNC = "/vedic_watch_sync"
    const val PATH_AI_PREDICTION_SYNC = "/vedic_prediction_sync"

    /**
     * Synchronizes full Vedic profile, current Panchanga data, AI predictions, and
     * simultaneously renders and pushes the custom visual design to be applied
     * directly as the smartwatch watch face.
     */
    suspend fun syncProfileAndHoroscopeToWatch(
        context: Context,
        profile: BirthProfile,
        personalizedProfile: PersonalizedProfile,
        dailyProfile: DailyVedicProfile,
        watchConfig: WatchFaceConfiguration? = null,
        latestPrediction: PredictionCacheEntity? = null
    ): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        return@withContext try {
            val availability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context)
            if (availability != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                return@withContext true
            }

            val prediction = latestPrediction ?: try {
                VedicDatabase.getDatabase(context).predictionCacheDao().getLatestPredictionDirect()
            } catch (e: Exception) {
                null
            }

            val finalWatchConfig = watchConfig ?: com.example.engines.personalization.VedicPersonalizationEngine().generateWatchFaceConfig(personalizedProfile)

            // 1. Render custom visual watch face design bitmap
            val watchFaceBitmap = try {
                WatchFaceBitmapRenderer.renderWatchFaceBitmap(
                    config = finalWatchConfig,
                    personalizedProfile = personalizedProfile,
                    dailyProfile = dailyProfile,
                    size = 450
                )
            } catch (e: Exception) {
                Log.w(TAG, "Could not render watch face bitmap: ${e.message}")
                null
            }

            val dataClient = Wearable.getDataClient(context)
            val putDataMapReq = PutDataMapRequest.create(PATH_VEDIC_SYNC).apply {
                // User Vedic Profile Details
                dataMap.putString("full_name", profile.fullName)
                dataMap.putString("birth_date", profile.birthDate.toString())
                dataMap.putString("birth_time", profile.birthTime.toString())
                dataMap.putString("city_name", profile.birthLocation.cityName)
                dataMap.putDouble("latitude", profile.birthLocation.latitude)
                dataMap.putDouble("longitude", profile.birthLocation.longitude)
                dataMap.putString("ayanamsha", profile.ayanamsha.name)
                dataMap.putString("tradition", profile.calendarTradition.name)
                dataMap.putString("natal_nakshatra", personalizedProfile.natalNakshatra.sanskritName)
                dataMap.putInt("natal_nakshatra_pada", personalizedProfile.natalNakshatraPada)
                dataMap.putString("natal_lagna", personalizedProfile.natalLagnaRashi.sanskritName)
                dataMap.putString("natal_chandra", personalizedProfile.natalChandraRashi.sanskritName)
                dataMap.putInt("birth_number", personalizedProfile.numerology.birthNumber)
                dataMap.putInt("destiny_number", personalizedProfile.numerology.lifePathNumber)

                // Latest AI Prediction
                if (prediction != null) {
                    dataMap.putLong("latest_prediction_id", prediction.id)
                    dataMap.putString("latest_prediction_title", prediction.title)
                    dataMap.putString("latest_prediction_text", prediction.predictionData)
                    dataMap.putLong("latest_prediction_timestamp", prediction.timestamp)
                } else {
                    dataMap.putLong("latest_prediction_id", 0L)
                    dataMap.putString("latest_prediction_title", "Vedic Life Insights")
                    dataMap.putString("latest_prediction_text", "Open Life Predictions in the mobile app to receive your personalized astrological reading.")
                    dataMap.putLong("latest_prediction_timestamp", 0L)
                }

                // Watch Configuration & Style Settings
                dataMap.putString("watch_style", finalWatchConfig.style.name)
                dataMap.putLong("primary_color", finalWatchConfig.primaryColorHex)
                dataMap.putLong("accent_color", finalWatchConfig.accentColorHex)
                dataMap.putBoolean("show_devanagari", finalWatchConfig.showDevanagari)
                dataMap.putBoolean("apply_as_watchface", true)

                // Attach rendered design bitmap asset if available
                if (watchFaceBitmap != null) {
                    val asset = WearWatchFaceSyncManager.createAssetFromBitmap(watchFaceBitmap)
                    dataMap.putAsset("bg_image_asset", asset)
                }

                // Daily Ephemeris & Panchanga
                dataMap.putString("tithi", dailyProfile.panchanga.tithi.name)
                dataMap.putString("tithi_devanagari", dailyProfile.panchanga.tithi.devanagari)
                dataMap.putString("nakshatra", dailyProfile.panchanga.nakshatra.sanskritName)
                dataMap.putString("yoga", dailyProfile.panchanga.yoga)
                dataMap.putString("choghadiya", dailyProfile.currentChoghadiya.type.displayName)
                dataMap.putString("choghadiya_devanagari", dailyProfile.currentChoghadiya.type.devanagari)
                dataMap.putBoolean("is_auspicious", dailyProfile.currentChoghadiya.type.isAuspicious)
                dataMap.putString("choghadiya_end", dailyProfile.currentChoghadiya.endTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")))
                
                dataMap.putString("sunrise", dailyProfile.panchanga.sunrise.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")))
                dataMap.putString("sunset", dailyProfile.panchanga.sunset.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")))

                dataMap.putString("current_hora", dailyProfile.currentHora.rulingGraha.name)
                dataMap.putString("hora_symbol", dailyProfile.currentHora.rulingGraha.symbol)
                dataMap.putString("rahu_start", dailyProfile.rahuKalam.startTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")))
                dataMap.putString("rahu_end", dailyProfile.rahuKalam.endTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")))
                val isRahuActive = !dailyProfile.currentZonedDateTime.isBefore(dailyProfile.rahuKalam.startTime) && dailyProfile.currentZonedDateTime.isBefore(dailyProfile.rahuKalam.endTime)
                dataMap.putBoolean("is_rahu_active", isRahuActive)
                
                // Calculate precise lunar phase and map to asset index for watch face
                val phaseAngle = com.example.engines.astronomy.MoonPhaseCalculator.calculateLunarPhaseAngle(dailyProfile.currentZonedDateTime)
                val moonAssetId = com.example.engines.astronomy.MoonPhaseCalculator.getMoonPhaseAssetId(phaseAngle)
                dataMap.putString("moon_phase_asset_id", moonAssetId)
                
                // Add timestamp to force DataItem change event and ensure the watch receives the sync reliably
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }
            val putDataReq = putDataMapReq.asPutDataRequest().setUrgent()
            val result = kotlinx.coroutines.withTimeoutOrNull(3000L) {
                dataClient.putDataItem(putDataReq).await()
            }

            // Also invoke dedicated Watch Face design sync for Wear OS 7 WFF5 layer
            if (watchFaceBitmap != null) {
                WearWatchFaceSyncManager.applyDesignAsActiveWatchFace(
                    context = context,
                    config = finalWatchConfig,
                    personalizedProfile = personalizedProfile,
                    dailyProfile = dailyProfile,
                    existingBitmap = watchFaceBitmap
                )
            }

            if (result != null) {
                Log.d(TAG, "Successfully synced Vedic profile and watch face design to Wear OS Data Layer.")
                com.example.ui.watch.VedicComplicationDataHelper.requestComplicationUpdates(context)
                true
            } else {
                Log.i(TAG, "Wear OS DataClient timed out or not available.")
                com.example.ui.watch.VedicComplicationDataHelper.requestComplicationUpdates(context)
                true // Allow UI to proceed with optimistic success if rendered locally
            }
        } catch (e: Exception) {
            Log.i(TAG, "Wear OS DataClient not available on this device: ${e.message}")
            false
        }
    }

    /**
     * Dedicated action to apply the customized design created on mobile directly as the active
     * Watch Face on the connected Galaxy Watch / Wear OS device.
     */
    suspend fun applyWatchFaceDesignDirectly(
        context: Context,
        profile: BirthProfile,
        personalizedProfile: PersonalizedProfile,
        dailyProfile: DailyVedicProfile,
        watchConfig: WatchFaceConfiguration
    ): Result<String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        // Run full sync and active watch face application
        syncProfileAndHoroscopeToWatch(
            context = context,
            profile = profile,
            personalizedProfile = personalizedProfile,
            dailyProfile = dailyProfile,
            watchConfig = watchConfig
        )

        return@withContext WearWatchFaceSyncManager.applyDesignAsActiveWatchFace(
            context = context,
            config = watchConfig,
            personalizedProfile = personalizedProfile,
            dailyProfile = dailyProfile
        )
    }

    suspend fun syncLatestPredictionOnly(
        context: Context,
        prediction: PredictionCacheEntity
    ): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        return@withContext try {
            val dataClient = Wearable.getDataClient(context)
            val putDataMapReq = PutDataMapRequest.create(PATH_AI_PREDICTION_SYNC).apply {
                dataMap.putLong("latest_prediction_id", prediction.id)
                dataMap.putString("latest_prediction_title", prediction.title)
                dataMap.putString("latest_prediction_text", prediction.predictionData)
                dataMap.putLong("latest_prediction_timestamp", prediction.timestamp)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }
            val putDataReq = putDataMapReq.asPutDataRequest().setUrgent()
            val result = kotlinx.coroutines.withTimeoutOrNull(2000L) {
                dataClient.putDataItem(putDataReq).await()
            }
            if (result != null) {
                Log.d(TAG, "Successfully synced AI prediction directly to Wear OS Data Layer.")
                true
            } else {
                Log.i(TAG, "Wear OS DataClient timed out or not available.")
                false
            }
        } catch (e: Exception) {
            Log.i(TAG, "Wear OS sync error: ${e.message}")
            false
        }
    }
}
