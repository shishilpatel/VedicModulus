package com.example.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import android.net.Uri
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AstroDataStore.init(applicationContext)
        WearPowerManager.init(applicationContext)
        fetchLatestDataClientItem()
        setContent {
            WearApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        WearPowerManager.destroy(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        WearFrameDropMonitor.start()
        WearPowerManager.refreshPowerState(applicationContext)
        try {
            Wearable.getDataClient(this).addListener(this)
            fetchLatestDataClientItem()
        } catch (e: Exception) {
            android.util.Log.e("WearMainActivity", "Failed to add DataClient listener: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        WearFrameDropMonitor.stop()
        try {
            Wearable.getDataClient(this).removeListener(this)
        } catch (e: Exception) {
            android.util.Log.e("WearMainActivity", "Failed to remove DataClient listener: ${e.message}")
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        VedicCalculationDispatcher.post {
            for (event in dataEvents) {
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val uri = event.dataItem.uri
                    if (uri.path == "/vedic_watch_sync") {
                        parseAndApplySyncDataItem(event.dataItem)
                    }
                }
            }
        }
    }

    private fun fetchLatestDataClientItem() {
        try {
            Wearable.getDataClient(this).dataItems.addOnSuccessListener { dataItemBuffer ->
                VedicCalculationDispatcher.post {
                    for (item in dataItemBuffer) {
                        if (item.uri.path == "/vedic_watch_sync") {
                            parseAndApplySyncDataItem(item)
                        }
                    }
                    dataItemBuffer.release()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WearMainActivity", "Failed to fetch data items: ${e.message}")
        }
    }

    private fun parseAndApplySyncDataItem(dataItem: com.google.android.gms.wearable.DataItem) {
        try {
            val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
            val synced = SyncData(
                watchStyle = dataMap.getString("watch_style", "COSMIC"),
                primaryColorHex = dataMap.getLong("primary_color", 0xFFFFD166),
                accentColorHex = dataMap.getLong("accent_color", 0xFFF43F5E),
                fullName = dataMap.getString("full_name", "Vedic Seeker"),
                birthDate = dataMap.getString("birth_date", ""),
                birthTime = dataMap.getString("birth_time", ""),
                cityName = dataMap.getString("city_name", "Varanasi"),
                natalNakshatra = dataMap.getString("natal_nakshatra", "Rohini"),
                natalNakshatraPada = dataMap.getInt("natal_nakshatra_pada", 1),
                natalLagna = dataMap.getString("natal_lagna", "Mesha"),
                natalChandra = dataMap.getString("natal_chandra", "Vrishabha"),
                birthNumber = dataMap.getInt("birth_number", 7),
                destinyNumber = dataMap.getInt("destiny_number", 3),
                latestPredictionTitle = dataMap.getString("latest_prediction_title", "Vedic Life Insights"),
                latestPredictionText = dataMap.getString("latest_prediction_text", ""),
                latestPredictionTimestamp = dataMap.getLong("latest_prediction_timestamp", 0L),
                currentHora = dataMap.getString("current_hora", "Surya"),
                horaSymbol = dataMap.getString("hora_symbol", "☉"),
                currentMuhurat = dataMap.getString("choghadiya", "Amrit"),
                currentMuhuratDevanagari = dataMap.getString("choghadiya_devanagari", "अमृत"),
                isAuspicious = dataMap.getBoolean("is_auspicious", true),
                muhuratEndTime = dataMap.getString("choghadiya_end", ""),
                sunrise = dataMap.getString("sunrise", "06:00 AM"),
                sunset = dataMap.getString("sunset", "06:00 PM"),
                tithi = dataMap.getString("tithi", "Shukla Dashami"),
                tithiDevanagari = dataMap.getString("tithi_devanagari", "शुक्ल दशमी"),
                nakshatra = dataMap.getString("nakshatra", "Rohini"),
                rahuStart = dataMap.getString("rahu_start", "04:30 PM"),
                rahuEnd = dataMap.getString("rahu_end", "06:00 PM"),
                isRahuActive = dataMap.getBoolean("is_rahu_active", false),
                isSafeModeEnabled = dataMap.getBoolean("is_safe_mode", false),
                topComplicationType = dataMap.getString("top_comp", AstroDataStore.syncedData.value.topComplicationType),
                bottomComplicationType = dataMap.getString("bottom_comp", AstroDataStore.syncedData.value.bottomComplicationType),
                leftComplicationType = dataMap.getString("left_comp", AstroDataStore.syncedData.value.leftComplicationType),
                rightComplicationType = dataMap.getString("right_comp", AstroDataStore.syncedData.value.rightComplicationType),
                stepCount = dataMap.getInt("step_count", AstroDataStore.syncedData.value.stepCount),
                stepGoal = dataMap.getInt("step_goal", AstroDataStore.syncedData.value.stepGoal),
                weatherTemp = dataMap.getString("weather_temp", AstroDataStore.syncedData.value.weatherTemp),
                weatherCondition = dataMap.getString("weather_cond", AstroDataStore.syncedData.value.weatherCondition),
                weatherIcon = dataMap.getString("weather_icon", AstroDataStore.syncedData.value.weatherIcon),
                batteryPercent = dataMap.getInt("battery_percent", AstroDataStore.syncedData.value.batteryPercent),
                heartRateBpm = dataMap.getInt("heart_rate_bpm", AstroDataStore.syncedData.value.heartRateBpm),
                moonPhaseAssetId = dataMap.getString("moon_phase_asset_id"),
                isSyncedFromPhone = true,
                lastSyncTimestamp = dataMap.getLong("timestamp", System.currentTimeMillis())
            )
            AstroDataStore.updateSyncData(applicationContext, synced)
        } catch (e: Exception) {
            android.util.Log.e("WearMainActivity", "Failed to parse dataItem: ${e.message}")
        }
    }
}

@Composable
fun WearApp() {
    val syncData by AstroDataStore.syncedData.collectAsState()
    val powerState by WearPowerManager.powerState.collectAsState()
    var showDetails by remember { mutableStateOf(false) }

    // Lifecycle-aware time ticker: switches dynamically between 500ms (high-fidelity) and minute alignment (low-power)
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner, powerState.isLowPowerModeActive) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                currentTime = LocalTime.now()
                if (powerState.isLowPowerModeActive) {
                    val delayMs = WearPowerManager.calculateDelayToNextMinuteMs()
                    delay(delayMs)
                } else {
                    delay(500)
                }
            }
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Main Watch Face Canvas (Hardware Canvas optimized)
            VedicWatchFaceCanvas(
                syncData = syncData,
                currentTime = currentTime,
                isLowPowerMode = powerState.isLowPowerModeActive,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showDetails = !showDetails }
            )

            // Low-Power & Thermal Throttling Badge (Top)
            if (powerState.isLowPowerModeActive && !showDetails) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (powerState.isThermalThrottled) Color(0xDD7F1D1D) else Color(0xDD065F46))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = powerState.powerModeBadgeText,
                        color = if (powerState.isThermalThrottled) Color(0xFFFCA5A5) else Color(0xFFA7F3D0),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (syncData.isSafeModeEnabled && !showDetails) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xDD1E3A8A))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🛡️ SAFE MODE",
                        color = Color(0xFF93C5FD),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Small bottom tap affordance pill to invite opening the details
            if (!showDetails) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x881E293B))
                        .clickable { showDetails = true }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "▲ DETAILS",
                        color = Color(0xFFFFD54F),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Overlay sheet with comprehensive Muhurt, Hora, Panchanga, Style picker and Safe Mode toggle
            AnimatedVisibility(
                visible = showDetails,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                VedicDetailsOverlay(
                    syncData = syncData,
                    currentTime = currentTime,
                    onClose = { showDetails = false },
                    onSelectStyle = { newStyle ->
                        AstroDataStore.syncedData.value = syncData.copy(watchStyle = newStyle.name)
                    }
                )
            }
        }
    }
}

@Composable
fun SunTimesRow(syncData: SyncData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SUNRISE", color = Color(0xFFFFD54F), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(syncData.sunrise, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SUNSET", color = Color(0xFFF97316), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(syncData.sunset, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun VedicDetailsOverlay(
    syncData: SyncData,
    currentTime: LocalTime,
    onClose: () -> Unit,
    onSelectStyle: (WatchFaceStyle) -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm:ss a") }
    val auspiciousColor = if (syncData.isAuspicious) Color(0xFF10B981) else Color(0xFFEF4444)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF0000000))
            .clickable(enabled = false) {}
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Dismiss Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF334155))
                    .clickable { onClose() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("▼ CLOSE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Live Time
            Text(
                text = currentTime.format(timeFormatter),
                color = Color(0xFFFFD54F),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Active Muhurat Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(auspiciousColor.copy(alpha = 0.2f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ACTIVE MUHURAT",
                        color = auspiciousColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${syncData.currentMuhurat} (${syncData.currentMuhuratDevanagari})",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (syncData.muhuratEndTime.isNotEmpty()) {
                        Text(
                            text = "Until ${syncData.muhuratEndTime}",
                            color = Color.LightGray,
                            fontSize = 9.sp
                        )
                    }
                    Text(
                        text = if (syncData.isAuspicious) "✓ Auspicious (Shubh)" else "⚠ Inauspicious (Ashubh)",
                        color = auspiciousColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Hora & Rahu Kalam
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("HORA", color = Color(0xFF67E8F9), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("${syncData.horaSymbol} ${syncData.currentHora}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (syncData.isRahuActive) Color(0x44EF4444) else Color(0xFF1E293B))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RAHU KALAM", color = Color(0xFFEF4444), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(syncData.rahuStart, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sunrise & Sunset

            SunTimesRow(syncData = syncData)

            Spacer(modifier = Modifier.height(6.dp))

            // Panchanga (Tithi & Nakshatra)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PANCHANGA", color = Color(0xFFFFD54F), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${syncData.tithiDevanagari.ifEmpty { syncData.tithi }} • ${syncData.nakshatra}",
                        color = Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // User Vedic Natal Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👤 VEDIC NATAL PROFILE", color = Color(0xFF67E8F9), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = syncData.fullName.ifEmpty { "Vedic Seeker" },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Lagna: ${syncData.natalLagna} • Rashi: ${syncData.natalChandra}",
                        color = Color(0xFFFFD54F),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Nakshatra: ${syncData.natalNakshatra} (Pada ${syncData.natalNakshatraPada})",
                        color = Color.LightGray,
                        fontSize = 8.5.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Mulank: ${syncData.birthNumber} • Bhagyank: ${syncData.destinyNumber}",
                        color = Color(0xFFA78BFA),
                        fontSize = 8.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Latest AI Prediction Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x33A855F7))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✨ LATEST AI PREDICTION", color = Color(0xFFC084FC), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = syncData.latestPredictionTitle.ifEmpty { "Vedic Life Insights" },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = syncData.latestPredictionText.ifEmpty { "Open Life Predictions in the phone app to generate your reading." },
                        color = Color(0xFFF1F5F9),
                        fontSize = 8.5.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 6
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Complication Slots Customizer
            var activeSlotToConfigure by remember { mutableStateOf<ComplicationSlotPosition?>(null) }
            val context = androidx.compose.ui.platform.LocalContext.current

            Text("⚙️ COMPLICATION SLOTS", color = Color(0xFFFFD54F), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                val slots = listOf(
                    Triple(ComplicationSlotPosition.TOP, "TOP SLOT", syncData.topComplicationType),
                    Triple(ComplicationSlotPosition.BOTTOM, "BOTTOM SLOT", syncData.bottomComplicationType),
                    Triple(ComplicationSlotPosition.LEFT, "LEFT SLOT", syncData.leftComplicationType),
                    Triple(ComplicationSlotPosition.RIGHT, "RIGHT SLOT", syncData.rightComplicationType)
                )

                slots.chunked(2).forEach { rowSlots ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        rowSlots.forEach { (slotPos, label, currentType) ->
                            val opt = ComplicationTypeOption.values().find { it.id == currentType } ?: ComplicationTypeOption.VEDIC_MUHURAT
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .clickable { activeSlotToConfigure = slotPos }
                                    .padding(horizontal = 6.dp, vertical = 5.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text(label, color = Color.Gray, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${opt.icon} ${opt.title.take(10)}", color = Color(0xFF67E8F9), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            if (activeSlotToConfigure != null) {
                val currentSlot = activeSlotToConfigure!!
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SELECT FOR ${currentSlot.name}", color = Color(0xFFFFD54F), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF334155))
                                    .clickable { activeSlotToConfigure = null }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("✕", color = Color.White, fontSize = 8.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        ComplicationTypeOption.values().forEach { option ->
                            val isSelected = when (currentSlot) {
                                ComplicationSlotPosition.TOP -> syncData.topComplicationType == option.id
                                ComplicationSlotPosition.BOTTOM -> syncData.bottomComplicationType == option.id
                                ComplicationSlotPosition.LEFT -> syncData.leftComplicationType == option.id
                                ComplicationSlotPosition.RIGHT -> syncData.rightComplicationType == option.id
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0x4438BDF8) else Color(0xFF1E293B))
                                    .clickable {
                                        AstroDataStore.setComplicationSlot(context, currentSlot, option.id)
                                        activeSlotToConfigure = null
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${option.icon} ${option.title}", color = if (isSelected) Color(0xFF38BDF8) else Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold)
                                    if (isSelected) {
                                        Text("✓", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Watch Face Style Switcher directly on Watch
            Text("SWITCH STYLE", color = Color.LightGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                WatchFaceStyle.values().take(4).forEach { style ->
                    val isSel = syncData.watchStyle == style.name
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) Color(0xFFFFD54F) else Color(0xFF334155))
                            .clickable { onSelectStyle(style) }
                            .padding(horizontal = 5.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = style.displayName.take(5),
                            color = if (isSel) Color.Black else Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                WatchFaceStyle.values().drop(4).forEach { style ->
                    val isSel = syncData.watchStyle == style.name
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) Color(0xFFFFD54F) else Color(0xFF334155))
                            .clickable { onSelectStyle(style) }
                            .padding(horizontal = 5.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = style.displayName.take(5),
                            color = if (isSel) Color.Black else Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Diagnostic Safe Mode Toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (syncData.isSafeModeEnabled) Color(0xFF1E3A8A) else Color(0xFF1E293B))
                    .clickable {
                        AstroDataStore.toggleSafeMode(context)
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🛡️ SAFE MODE",
                            color = if (syncData.isSafeModeEnabled) Color(0xFF93C5FD) else Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (syncData.isSafeModeEnabled) "Active: Low-power flat dial" else "Disabled: Full cosmic shaders",
                            color = Color.LightGray,
                            fontSize = 7.5.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (syncData.isSafeModeEnabled) Color(0xFF3B82F6) else Color(0xFF475569))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (syncData.isSafeModeEnabled) "ON" else "OFF",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Low-Power Mode & Thermal Throttling Card
            val powerState by WearPowerManager.powerState.collectAsState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (powerState.isLowPowerModeActive) Color(0xFF064E3B) else Color(0xFF1E293B))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "⚡ LOW-POWER & THERMAL",
                                color = if (powerState.isLowPowerModeActive) Color(0xFFA7F3D0) else Color(0xFFFFD54F),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Batt: ${powerState.batteryPercent}% • ${powerState.thermalStatusName}",
                                color = Color.LightGray,
                                fontSize = 7.5.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (powerState.isLowPowerModeActive) Color(0xFF10B981) else Color(0xFF475569))
                                .clickable {
                                    WearPowerManager.toggleForceLowPower(context)
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (powerState.isLowPowerModeActive) "FORCE ON" else "AUTO",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (powerState.isLowPowerModeActive)
                            "✓ 60s Ticks Active (99.2% CPU wake reduction, second hand off)"
                        else
                            "⚡ High-Fidelity 500ms updates (Auto eco below ${powerState.lowPowerThresholdPercent}%)",
                        color = if (powerState.isLowPowerModeActive) Color(0xFF6EE7B7) else Color.LightGray,
                        fontSize = 7.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sync Status
            Text(
                text = if (syncData.isSyncedFromPhone) "● Synced with Phone App" else "● Local Vedic Calculation",
                color = if (syncData.isSyncedFromPhone) Color(0xFF10B981) else Color(0xFF67E8F9),
                fontSize = 8.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
