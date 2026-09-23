package com.example.wear

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
import java.io.InputStream

class VedicWearableListenerService : WearableListenerService(), androidx.lifecycle.LifecycleOwner {

    private val lifecycleRegistry = androidx.lifecycle.LifecycleRegistry(this)
    override val lifecycle: androidx.lifecycle.Lifecycle get() = lifecycleRegistry

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(VedicCalculationDispatcher.dispatcher + serviceJob)

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
        serviceJob.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "WearSyncListener"
        const val PATH_VEDIC_SYNC = "/vedic_watch_sync"
        const val PATH_AI_PREDICTION_SYNC = "/vedic_prediction_sync"
        const val PATH_DESIGN_UPDATE = "/watchface/design_update"
        const val PATH_INSTALL_WATCHFACE = "/watchface/install_watch_face"
        const val PATH_APPLY_ACTIVE_WATCHFACE = "/watchface/set_active_watchface"
        const val PATH_QUERY_STATUS = "/watchface/query_status"
        const val PATH_STATUS_RESPONSE = "/watchface/status_response"
        const val PATH_WEAR_SILENT_ALARM = "/vedic_silent_alarm"

        const val KEY_BG_ASSET = "bg_image_asset"
        const val KEY_BG_STYLE_ID = "bg_style_id"
        const val KEY_ACCENT_COLOR = "accent_color_hex"
        const val KEY_PRIMARY_COLOR = "primary_color_hex"
        const val KEY_WATCH_STYLE = "watch_style"
        const val KEY_APPLY_IMMEDIATELY = "apply_immediately"
        const val KEY_WFF_XML = "wff_xml_content"
        const val CUSTOM_BG_FILENAME = "watchface_custom_bg.png"
        const val WFF_FILENAME = "current_watchface.xml"
        const val ACTION_WATCHFACE_UPDATED = "com.example.watchface.ACTION_DESIGN_UPDATED"
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        for (event in dataEvents) {
            val uri = event.dataItem.uri
            Log.d(TAG, "Data changed event: $uri")

            if (event.type == DataEvent.TYPE_CHANGED) {
                when {
                    uri.path == PATH_VEDIC_SYNC -> {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        processVedicProfileSync(dataMap)
                    }
                    uri.path == PATH_AI_PREDICTION_SYNC -> {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        processAiPredictionSync(dataMap)
                    }
                    uri.path == PATH_DESIGN_UPDATE -> {
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        serviceScope.launch {
                            processIncomingWatchFaceDesign(dataMap)
                        }
                    }
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Message received: path=${messageEvent.path} from node=${messageEvent.sourceNodeId}")

        when (messageEvent.path) {
            PATH_APPLY_ACTIVE_WATCHFACE -> {
                val styleName = String(messageEvent.data, Charsets.UTF_8).ifEmpty { "COSMIC" }
                Log.d(TAG, "Received active watch face switch request: $styleName")
                serviceScope.launch(VedicCalculationDispatcher.dispatcher) {
                    applyWatchFaceLocally(styleName)
                    try {
                        Wearable.getMessageClient(applicationContext).sendMessage(
                            messageEvent.sourceNodeId,
                            PATH_STATUS_RESPONSE,
                            "ACTIVE_OK:$styleName".toByteArray(Charsets.UTF_8)
                        ).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not send status response back to phone: ${e.message}")
                    }
                }
            }
            PATH_INSTALL_WATCHFACE -> {
                val payload = String(messageEvent.data, Charsets.UTF_8)
                Log.d(TAG, "Received install watch face payload (${payload.length} chars)")
                serviceScope.launch(VedicCalculationDispatcher.dispatcher) {
                    try {
                        saveWffToLocalStorage(payload)
                        applyWatchFaceLocally("WFF_CUSTOM")
                        Wearable.getMessageClient(applicationContext).sendMessage(
                            messageEvent.sourceNodeId,
                            PATH_STATUS_RESPONSE,
                            "INSTALL_OK".toByteArray(Charsets.UTF_8)
                        ).await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed installing WFF watch face", e)
                    }
                }
            }
            PATH_QUERY_STATUS -> {
                serviceScope.launch(VedicCalculationDispatcher.dispatcher) {
                    val prefs = applicationContext.getSharedPreferences("wff_styles", MODE_PRIVATE)
                    val activeStyle = prefs.getString("current_watch_style", "COSMIC") ?: "COSMIC"
                    val isActive = prefs.getBoolean("is_custom_active", true)
                    val response = "STATUS:style=$activeStyle,active=$isActive"
                    try {
                        Wearable.getMessageClient(applicationContext).sendMessage(
                            messageEvent.sourceNodeId,
                            PATH_STATUS_RESPONSE,
                            response.toByteArray(Charsets.UTF_8)
                        ).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to respond to query status: ${e.message}")
                    }
                }
            }
            PATH_WEAR_SILENT_ALARM -> {
                val markerName = String(messageEvent.data, Charsets.UTF_8)
                Log.d(TAG, "Received silent vibration alarm trigger for marker: $markerName")
                triggerWearHapticVibration(markerName)
            }
        }
    }

    private fun triggerWearHapticVibration(markerName: String) {
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val mgr = getSystemService(VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                mgr?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as? android.os.Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                val pattern = when {
                    markerName.contains("RAHU") -> longArrayOf(0, 350, 150, 350)
                    markerName.contains("BRAHMA") -> longArrayOf(0, 200, 150, 200, 150, 400)
                    else -> longArrayOf(0, 250, 100, 250, 100, 250)
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val amps = IntArray(pattern.size) { if (it % 2 == 1) 220 else 0 }
                    vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, amps, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Wear haptic vibration failed: ${e.message}")
        }
    }

    private fun processVedicProfileSync(dataMap: com.google.android.gms.wearable.DataMap) {
        val fullName = dataMap.getString("full_name", "Vedic Seeker")
        val birthDate = dataMap.getString("birth_date", "")
        val birthTime = dataMap.getString("birth_time", "")
        val cityName = dataMap.getString("city_name", "Varanasi")
        val natalNakshatra = dataMap.getString("natal_nakshatra", "Rohini")
        val natalNakshatraPada = dataMap.getInt("natal_nakshatra_pada", 1)
        val natalLagna = dataMap.getString("natal_lagna", "Mesha")
        val natalChandra = dataMap.getString("natal_chandra", "Vrishabha")
        val birthNumber = dataMap.getInt("birth_number", 7)
        val destinyNumber = dataMap.getInt("destiny_number", 3)
        val latestPredictionTitle = dataMap.getString("latest_prediction_title", "Vedic Life Insights")
        val latestPredictionText = dataMap.getString("latest_prediction_text", "")
        val latestPredictionTimestamp = dataMap.getLong("latest_prediction_timestamp", 0L)

        val watchStyle = dataMap.getString("watch_style", "COSMIC")
        val primaryColorHex = dataMap.getLong("primary_color", 0xFFFFD166)
        val accentColorHex = dataMap.getLong("accent_color", 0xFFF43F5E)
        val currentHora = dataMap.getString("current_hora", "Surya")
        val horaSymbol = dataMap.getString("hora_symbol", "☉")
        val choghadiya = dataMap.getString("choghadiya", "Amrit")
        val choghadiyaDevanagari = dataMap.getString("choghadiya_devanagari", "अमृत")
        val isAuspicious = dataMap.getBoolean("is_auspicious", true)
        val choghadiyaEnd = dataMap.getString("choghadiya_end", "")
        val sunrise = dataMap.getString("sunrise", "06:00 AM")
        val sunset = dataMap.getString("sunset", "06:00 PM")
        val tithi = dataMap.getString("tithi", "Shukla Dashami")
        val tithiDevanagari = dataMap.getString("tithi_devanagari", "शुक्ल दशमी")
        val nakshatra = dataMap.getString("nakshatra", "Rohini")
        val rahuStart = dataMap.getString("rahu_start", "04:30 PM")
        val rahuEnd = dataMap.getString("rahu_end", "06:00 PM")
        val isRahuActive = dataMap.getBoolean("is_rahu_active", false)
        val isSafeMode = dataMap.getBoolean("is_safe_mode", false)
        val topComp = dataMap.getString("top_comp", AstroDataStore.syncedData.value.topComplicationType)
        val bottomComp = dataMap.getString("bottom_comp", AstroDataStore.syncedData.value.bottomComplicationType)
        val leftComp = dataMap.getString("left_comp", AstroDataStore.syncedData.value.leftComplicationType)
        val rightComp = dataMap.getString("right_comp", AstroDataStore.syncedData.value.rightComplicationType)
        val stepCount = dataMap.getInt("step_count", AstroDataStore.syncedData.value.stepCount)
        val stepGoal = dataMap.getInt("step_goal", AstroDataStore.syncedData.value.stepGoal)
        val weatherTemp = dataMap.getString("weather_temp", AstroDataStore.syncedData.value.weatherTemp)
        val weatherCond = dataMap.getString("weather_cond", AstroDataStore.syncedData.value.weatherCondition)
        val weatherIcon = dataMap.getString("weather_icon", AstroDataStore.syncedData.value.weatherIcon)
        val batteryPct = dataMap.getInt("battery_percent", AstroDataStore.syncedData.value.batteryPercent)
        val heartRate = dataMap.getInt("heart_rate_bpm", AstroDataStore.syncedData.value.heartRateBpm)
        val moonPhaseAssetId = dataMap.getString("moon_phase_asset_id")
        val timestamp = dataMap.getLong("timestamp", System.currentTimeMillis())

        serviceScope.launch(VedicCalculationDispatcher.dispatcher) {
            val synced = SyncData(
                watchStyle = watchStyle,
                primaryColorHex = primaryColorHex,
                accentColorHex = accentColorHex,
                fullName = fullName,
                birthDate = birthDate,
                birthTime = birthTime,
                cityName = cityName,
                natalNakshatra = natalNakshatra,
                natalNakshatraPada = natalNakshatraPada,
                natalLagna = natalLagna,
                natalChandra = natalChandra,
                birthNumber = birthNumber,
                destinyNumber = destinyNumber,
                latestPredictionTitle = latestPredictionTitle,
                latestPredictionText = latestPredictionText,
                latestPredictionTimestamp = latestPredictionTimestamp,
                currentHora = currentHora,
                horaSymbol = horaSymbol,
                currentMuhurat = choghadiya,
                currentMuhuratDevanagari = choghadiyaDevanagari,
                isAuspicious = isAuspicious,
                muhuratEndTime = choghadiyaEnd,
                sunrise = sunrise,
                sunset = sunset,
                tithi = tithi,
                tithiDevanagari = tithiDevanagari,
                nakshatra = nakshatra,
                rahuStart = rahuStart,
                rahuEnd = rahuEnd,
                isRahuActive = isRahuActive,
                isSafeModeEnabled = isSafeMode,
                topComplicationType = topComp,
                bottomComplicationType = bottomComp,
                leftComplicationType = leftComp,
                rightComplicationType = rightComp,
                stepCount = stepCount,
                stepGoal = stepGoal,
                weatherTemp = weatherTemp,
                weatherCondition = weatherCond,
                weatherIcon = weatherIcon,
                batteryPercent = batteryPct,
                heartRateBpm = heartRate,
                moonPhaseAssetId = moonPhaseAssetId,
                isSyncedFromPhone = true,
                lastSyncTimestamp = timestamp
            )
            AstroDataStore.updateSyncData(applicationContext, synced)
            updateComplications()
        }

        Log.d(TAG, "Successfully updated watch UI with Profile: $fullName, Lagna: $natalLagna and AI Prediction: $latestPredictionTitle")
    }

    private fun processAiPredictionSync(dataMap: com.google.android.gms.wearable.DataMap) {
        val predTitle = dataMap.getString("latest_prediction_title", "Vedic Life Insights")
        val predText = dataMap.getString("latest_prediction_text", "")
        val predTimestamp = dataMap.getLong("latest_prediction_timestamp", System.currentTimeMillis())

        serviceScope.launch(VedicCalculationDispatcher.dispatcher) {
            val updated = AstroDataStore.syncedData.value.copy(
                latestPredictionTitle = predTitle,
                latestPredictionText = predText,
                latestPredictionTimestamp = predTimestamp,
                isSyncedFromPhone = true,
                lastSyncTimestamp = System.currentTimeMillis()
            )
            AstroDataStore.updateSyncData(applicationContext, updated)
        }
        Log.d(TAG, "Successfully synced AI Prediction update: $predTitle")
    }

    private suspend fun processIncomingWatchFaceDesign(dataMap: com.google.android.gms.wearable.DataMap) {
        try {
            val bgStyleId = dataMap.getString(KEY_BG_STYLE_ID, "bg_custom")
            val accentColor = dataMap.getString(KEY_ACCENT_COLOR, "#FFE8B05C")
            val primaryColor = dataMap.getString(KEY_PRIMARY_COLOR, "#FFD4AF37")
            val watchStyle = dataMap.getString(KEY_WATCH_STYLE, "COSMIC")
            val applyImmediately = dataMap.getBoolean(KEY_APPLY_IMMEDIATELY, true)
            val bgAsset = dataMap.getAsset(KEY_BG_ASSET)
            val wffXml = dataMap.getString(KEY_WFF_XML)

            // 1. Extract Asset InputStream and Save to Watch Internal Storage
            if (bgAsset != null) {
                try {
                    val response = Wearable.getDataClient(applicationContext).getFdForAsset(bgAsset).await()
                    val stream: InputStream? = response?.inputStream
                    stream?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        if (bitmap != null) {
                            saveBitmapToLocalStorage(bitmap, CUSTOM_BG_FILENAME)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed reading background asset: ${e.message}")
                }
            }

            // 2. Save WFF XML if provided
            if (!wffXml.isNullOrEmpty()) {
                saveWffToLocalStorage(wffXml)
            }

            // 3. Persist Style Preferences locally on the watch
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
        try {
            val intent = Intent(ACTION_WATCHFACE_UPDATED).apply {
                putExtra("watch_style", styleName)
                putExtra("is_active", true)
                setPackage(packageName)
            }
            applicationContext.sendBroadcast(intent)

            // Update in AstroDataStore so the UI immediately switches to this style
            val current = AstroDataStore.syncedData.value
            AstroDataStore.syncedData.value = current.copy(watchStyle = styleName)

            updateComplications()
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

    private fun saveWffToLocalStorage(wffXml: String) {
        val file = File(applicationContext.filesDir, WFF_FILENAME)
        file.writeText(wffXml, Charsets.UTF_8)
    }

    private fun updateComplications() {
        try {
            val complications = listOf(
                VedicComplicationDataSourceService::class.java,
                VedicSunriseSunsetComplicationService::class.java,
                VedicChoghadiyaComplicationService::class.java,
                VedicHoraComplicationService::class.java
            )
            for (serviceClass in complications) {
                try {
                    val component = android.content.ComponentName(applicationContext, serviceClass)
                    val requester = androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester.create(
                        applicationContext,
                        component
                    )
                    requester.requestUpdateAll()
                } catch (e: Exception) {
                    Log.d(TAG, "Could not update complication ${serviceClass.simpleName}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "updateComplications error: ${e.message}")
        }
    }
}
