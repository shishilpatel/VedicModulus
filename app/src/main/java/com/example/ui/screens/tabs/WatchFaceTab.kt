package com.example.ui.screens.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import com.example.api.generateHoroscope
import androidx.compose.ui.text.style.TextAlign
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.*
import com.example.data.VedicWearableSyncManager
import com.example.data.sync.WearWatchFacePushManager
import com.example.data.sync.WearWatchInfo
import com.example.data.sync.WatchFacePushState
import com.example.data.sync.WearWatchFaceSyncManager
import com.example.generator.watchface.AIWatchFaceImageProcessor
import com.example.generator.watchface.WatchFaceBitmapRenderer
import com.example.generator.watchface.WatchFaceFormatBuilder
import com.example.ui.components.GalaxyWatchFaceCanvas
import com.example.ui.components.VedicTimeReadout
import com.example.ui.components.WearSyncAnimationCard
import com.example.ui.components.VedicArtisticThemesCard
import com.example.ui.components.VedicVitalityTrendsCard
import com.example.ui.components.VedicSilentAlarmsCard
import com.example.data.UserPreferencesRepository
import android.widget.Toast
import com.example.generator.watchface.CelestialClockEngine
import com.example.generator.watchface.CelestialClockState
import com.example.ui.watch.VedicComplicationDataHelper
import kotlin.math.roundToInt
import com.example.ui.theme.*
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime

@Composable
fun WatchFaceTab(
    profile: BirthProfile,
    config: WatchFaceConfiguration,
    personalizedProfile: PersonalizedProfile,
    dailyProfile: DailyVedicProfile,

    currentTimeProvider: () -> LocalTime,
    isAmbientMode: Boolean,
    onAmbientToggle: () -> Unit,
    onStyleSelect: (WatchFaceStyle) -> Unit,
    onSyncToWatch: () -> Unit,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    var complications by remember { mutableStateOf(ComplicationCustomization()) }
    var currentConfig by remember(config) { mutableStateOf(config) }
    var showWatchSettingsDialog by remember { mutableStateOf(false) }
    var showTithiIndicator by remember { mutableStateOf(true) }
    var showNakshatraIndicator by remember { mutableStateOf(true) }
    var showYogaIndicator by remember { mutableStateOf(true) }
    var showKaranaIndicator by remember { mutableStateOf(true) }
    var showVaraIndicator by remember { mutableStateOf(true) }
    var showRahuKaalIndicator by remember { mutableStateOf(true) }
    var showHoraIndicator by remember { mutableStateOf(true) }
    var showDashaRing by remember { mutableStateOf(true) }
    var showGocharTransits by remember { mutableStateOf(true) }
    var showChoghadiyaSubdial by remember { mutableStateOf(true) }
    var isApplyingWatchFace by remember { mutableStateOf(false) }
    var applyWatchFaceFeedback by remember { mutableStateOf<String?>(null) }
    var isProcessingImageWithAi by remember { mutableStateOf(false) }
    var aiImageProcessingSummary by remember { mutableStateOf<String?>(null) }
    var rawSelectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val preferencesRepository = remember { UserPreferencesRepository(context) }
    val wearBackgroundSyncEnabled by preferencesRepository.wearBackgroundSyncEnabledFlow.collectAsState(initial = true)

    var connectedWatches by remember { mutableStateOf<List<WearWatchInfo>>(emptyList()) }
    var showWffXmlDialog by remember { mutableStateOf(false) }
    var currentWffXml by remember { mutableStateOf("") }
    var showHowToSetActiveDialog by remember { mutableStateOf(false) }
    var showInstallCompanionAppDialog by remember { mutableStateOf(false) }
    var pushProgressStep by remember { mutableStateOf("") }

    val zoneId = remember(dailyProfile) {
        try {
            dailyProfile.currentZonedDateTime.zone
        } catch (e: Exception) {
            java.time.ZoneId.systemDefault()
        }
    }

    var liveZonedDateTime by remember { mutableStateOf(ZonedDateTime.now(zoneId)) }
    var liveLocalTime by remember { mutableStateOf(currentTimeProvider()) }

    val celestialEngine = remember { CelestialClockEngine() }
    val loc = personalizedProfile.userBirthProfile.birthLocation
    val celestialState by produceState<CelestialClockState?>(
        initialValue = null,
        key1 = liveZonedDateTime.minute,
        key2 = loc.latitude,
        key3 = loc.longitude
    ) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            celestialEngine.calculateClockState(
                dateTime = liveZonedDateTime,
                latitude = loc.latitude,
                longitude = loc.longitude
            )
        }
    }

    LaunchedEffect(isAmbientMode, zoneId) {
        while (true) {
            val now = ZonedDateTime.now(zoneId)
            liveZonedDateTime = now
            liveLocalTime = now.toLocalTime()
            if (isAmbientMode) {
                val nowMs = System.currentTimeMillis()
                val remainingInMin = 60000L - (nowMs % 60000L)
                kotlinx.coroutines.delay(if (remainingInMin <= 50L) 60000L else remainingInMin)
            } else {
                val nowMs = System.currentTimeMillis()
                val remainingInSec = 1000L - (nowMs % 1000L)
                kotlinx.coroutines.delay(if (remainingInSec <= 20L) 1000L else remainingInSec)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (profile.hasSmartWatch && profile.watchModel.isNotBlank() && profile.watchModel != "None") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Watch, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Connected Wear OS Watch",
                            style = MaterialTheme.typography.labelMedium.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = profile.watchModel,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
        // Mode switchers: Active vs Ambient Always-On Display (AOD)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = !isAmbientMode,
                onClick = { if (isAmbientMode) onAmbientToggle() },
                label = { Text(stringResource(R.string.active_galaxy_display)) },
                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaroonPrimary,
                    selectedLabelColor = OnMaroon
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            FilterChip(
                selected = isAmbientMode,
                onClick = { if (!isAmbientMode) onAmbientToggle() },
                label = { Text(stringResource(R.string.aod_ambient_mode)) },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LightBorder,
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Real-Time Live Clock Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaroonPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("live_watch_time_badge")
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isAmbientMode) DarkTextSecondary else AuspiciousGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val timePattern = if (isAmbientMode) "hh:mm a (z)" else "hh:mm:ss a (z)"
            val formattedLiveTime = liveZonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern(timePattern))
            Text(
                text = if (isAmbientMode) "AOD • $formattedLiveTime" else "LIVE • $formattedLiveTime",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaroonPrimary,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Galaxy Watch Canvas
        GalaxyWatchFaceCanvas(
            config = currentConfig,
            personalizedProfile = personalizedProfile,
            dailyProfile = dailyProfile,
            currentTimeProvider = { liveLocalTime },
            isAmbientMode = isAmbientMode,
            complications = complications,
            externalCelestialClockState = celestialState,
            modifier = Modifier.size(260.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val processImageWithAi: (android.net.Uri) -> Unit = { uri ->
            rawSelectedImageUri = uri
            isProcessingImageWithAi = true
            aiImageProcessingSummary = null
            coroutineScope.launch {
                try {
                    val result = AIWatchFaceImageProcessor.processAndFitImage(
                        context = context,
                        imageUri = uri,
                        currentConfig = currentConfig
                    )
                    currentConfig = currentConfig.copy(
                        customBackgroundUri = result.uriString,
                        primaryColorHex = result.suggestedPrimaryColor ?: currentConfig.primaryColorHex,
                        accentColorHex = result.suggestedAccentColor ?: currentConfig.accentColorHex
                    )
                    aiImageProcessingSummary = result.aiSummary
                    onStyleSelect(currentConfig.style)
                } catch (e: Exception) {
                    currentConfig = currentConfig.copy(customBackgroundUri = uri.toString())
                    aiImageProcessingSummary = "Image applied with standard aspect-fill center crop."
                } finally {
                    isProcessingImageWithAi = false
                }
            }
        }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                processImageWithAi(uri)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isProcessingImageWithAi,
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("select_chatgpt_bg_btn")
                ) {
                    if (isProcessingImageWithAi) {
                        CircularProgressIndicator(
                            color = OnMaroon,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("✨ AI Processing & Fitting Image...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (currentConfig.customBackgroundUri.isNotBlank()) "✨ Upload & AI-Fit New Background" else "✨ Upload & AI-Fit Watch Face Background",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isProcessingImageWithAi) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Analyzing focal subject, auto-centering for circular smartwatch dial & balancing dial contrast...",
                        style = MaterialTheme.typography.bodySmall.copy(color = OrangeAccent, fontSize = 11.sp),
                        textAlign = TextAlign.Center
                    )
                } else if (currentConfig.customBackgroundUri.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AuspiciousGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AuspiciousGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = aiImageProcessingSummary ?: "✨ AI Smart-Fitted for Circular Smartwatch Dial",
                                color = AuspiciousGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (rawSelectedImageUri != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { rawSelectedImageUri?.let { processImageWithAi(it) } },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Re-fit with AI", tint = AuspiciousGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = {
                            currentConfig = currentConfig.copy(customBackgroundUri = "")
                            aiImageProcessingSummary = null
                            rawSelectedImageUri = null
                            onStyleSelect(currentConfig.style)
                        },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        border = BorderStroke(1.dp, GrayText.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = GrayText)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Sacred Procedural Dial", fontSize = 11.sp, color = GrayText)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Google Wear OS Watch Face Push Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.5.dp, OrangeAccent),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("apply_watchface_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Watch,
                        contentDescription = null,
                        tint = MaroonPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google Wear OS Watch Face Push",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Real-time Wear OS Watch Status Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (connectedWatches.isNotEmpty()) AuspiciousGreen.copy(alpha = 0.15f) else OrangeAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (connectedWatches.isNotEmpty()) AuspiciousGreen else OrangeAccent)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (connectedWatches.isNotEmpty()) AuspiciousGreen else OrangeAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (connectedWatches.isNotEmpty()) {
                                "Connected: ${connectedWatches.first().name}"
                            } else if (profile.watchModel.isNotBlank() && profile.watchModel != "None") {
                                "Target: ${profile.watchModel}"
                            } else {
                                "Galaxy Watch / Pixel Watch Ready"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Builds official Google Watch Face Format (WFF v2) declarative XML & pushes the design directly to your smartwatch via Google Wearable APIs.",
                    style = MaterialTheme.typography.bodySmall.copy(color = GrayText, fontSize = 11.sp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Primary Push Action Button
                Button(
        onClick = {
            isApplyingWatchFace = true
            pushProgressStep = "Detecting connected Wear OS smartwatch..."
            applyWatchFaceFeedback = null
            coroutineScope.launch {
                val watches = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    WearWatchFacePushManager.getConnectedWatches(context)
                }
                connectedWatches = watches
                if (watches.isEmpty()) {
                    isApplyingWatchFace = false
                    showInstallCompanionAppDialog = true
                } else {
                    pushProgressStep = "Compiling Watch Face Format (WFF v2)..."
                    val pushResult = WearWatchFacePushManager.createAndPushWatchFace(
                        context = context,
                        profile = profile,
                        personalizedProfile = personalizedProfile,
                        dailyProfile = dailyProfile,
                        config = currentConfig
                    )
                    isApplyingWatchFace = false
                    currentWffXml = pushResult.wffXml
                    applyWatchFaceFeedback = pushResult.userMessage

                    if (pushResult.isSuccess) {
                        onSyncToWatch()
                        if (pushResult.state == WatchFacePushState.PUSHED_PENDING_SELECTION) {
                            showHowToSetActiveDialog = true
                        }
                    }
                }
            }
        },
                    enabled = !isApplyingWatchFace,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaroonPrimary,
                        contentColor = OnMaroon
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("apply_design_to_watch_button")
                ) {
                    if (isApplyingWatchFace) {
                        CircularProgressIndicator(
                            color = OnMaroon,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = pushProgressStep.ifEmpty { "Pushing Watch Face..." },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create & Push Watch Face to Watch", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Links: Inspect WFF XML, Activation Guide, Complications Refresh (Horizontally scrollable with no text splitting)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentWffXml.isEmpty()) {
                                currentWffXml = WatchFaceFormatBuilder.buildWffXml(
                                    config = currentConfig,
                                    personalizedProfile = personalizedProfile,
                                    dailyProfile = dailyProfile
                                )
                            }
                            showWffXmlDialog = true
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("inspect_wff_xml_button"),
                        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Inspect WFF XML",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    OutlinedButton(
                        onClick = { showHowToSetActiveDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("activation_guide_button"),
                        border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Activation Help",
                            style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                VedicComplicationDataHelper.requestComplicationUpdates(context)
                            }
                            applyWatchFaceFeedback = "✓ Watch complications (Tithi, Choghadiya, Hora) refreshed!"
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("refresh_complications_button"),
                        border = BorderStroke(1.dp, AuspiciousGreen.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = AuspiciousGreen, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sync Complications",
                            style = MaterialTheme.typography.labelSmall.copy(color = AuspiciousGreen, fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                if (applyWatchFaceFeedback != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = applyWatchFaceFeedback!!,
                        color = if (applyWatchFaceFeedback!!.startsWith("✓")) AuspiciousGreen else MaroonPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Live Vedic Time Readout (Ghati/Pala)
        VedicTimeReadout(
            dailyProfile = dailyProfile,
            currentTime = liveLocalTime,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Real-Time Swiss Ephemeris Celestial Clock Telemetry & Harmonic Escapement Controls

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("celestial_clock_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Vedic Celestial Clock",
                                style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Nirayana (${celestialState?.ayanamshaName ?: "Lahiri"})",
                                style = MaterialTheme.typography.bodySmall.copy(color = GrayText, fontSize = 10.sp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AuspiciousGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AuspiciousGreen))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "REALTIME LIVE",
                            color = AuspiciousGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Live planetary longitudes and horizon ascendant dynamically drive the rotating watch face zodiac chapter ring and navagraha gears.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time Planetary Telemetry Ribbon
                if (celestialState != null) {
                    val state = celestialState!!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CelestialTelemetryPill("Asc / Lagna", "★", "${state.lagnaRashi.sanskritName} ${(state.lagnaLongitudeDeg % 30.0).roundToInt()}°", MaroonPrimary)
                        CelestialTelemetryPill("Surya (Sun)", "☉", "${state.surya.rashi.sanskritName} ${(state.surya.degreesInRashi).roundToInt()}°", Color(0xFFFFD700))
                        CelestialTelemetryPill("Chandra (Moon)", "☽", "${state.chandra.rashi.sanskritName} ${(state.chandra.degreesInRashi).roundToInt()}°", Color(0xFFBAE6FD))
                        state.getPlanet(Graha.GURU)?.let { guru ->
                            CelestialTelemetryPill("Guru (Jupiter)", "♃", "${guru.rashi.sanskritName} ${(guru.degreesInRashi).roundToInt()}°${if (guru.isRetrograde) " ℞" else ""}", Color(0xFFF59E0B))
                        }
                        state.getPlanet(Graha.SHANI)?.let { shani ->
                            CelestialTelemetryPill("Shani (Saturn)", "♄", "${shani.rashi.sanskritName} ${(shani.degreesInRashi).roundToInt()}°${if (shani.isRetrograde) " ℞" else ""}", Color(0xFF3B82F6))
                        }
                        state.getPlanet(Graha.MANGALA)?.let { mangal ->
                            CelestialTelemetryPill("Mangala (Mars)", "♂", "${mangal.rashi.sanskritName} ${(mangal.degreesInRashi).roundToInt()}°${if (mangal.isRetrograde) " ℞" else ""}", Color(0xFFEF4444))
                        }
                        state.getPlanet(Graha.BUDHA)?.let { budh ->
                            CelestialTelemetryPill("Budha (Mercury)", "☿", "${budh.rashi.sanskritName} ${(budh.degreesInRashi).roundToInt()}°${if (budh.isRetrograde) " ℞" else ""}", Color(0xFF10B981))
                        }
                        state.getPlanet(Graha.SHUKRA)?.let { shukra ->
                            CelestialTelemetryPill("Shukra (Venus)", "♀", "${shukra.rashi.sanskritName} ${(shukra.degreesInRashi).roundToInt()}°${if (shukra.isRetrograde) " ℞" else ""}", Color(0xFFF472B6))
                        }
                        CelestialTelemetryPill("Rahu-Ketu Axis", "☊☋", "180° Cosmic Node", Color(0xFF8B5CF6))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Motion & Escapement Speed Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Escapement Animation:",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val speeds = listOf(
                            Pair("Subtle (1x)", 1.0f),
                            Pair("Dynamic (2.5x)", 2.5f),
                            Pair("Locked (0x)", 0.0f)
                        )
                        speeds.forEach { (label, speedVal) ->
                            val isSel = (currentConfig.celestialRotationSpeed == speedVal)
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    currentConfig = currentConfig.copy(
                                        celestialRotationSpeed = speedVal,
                                        showCelestialClock = true
                                    )
                                },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaroonPrimary,
                                    selectedLabelColor = OnMaroon
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real-Time Vedic Status Bar with dynamic Choghadiya tracking
        val choghadiyaEngine = remember { com.example.engines.choghadiya.ChoghadiyaEngine() }
        val choghadiyaSlots = remember(dailyProfile) {
            val sunrise = dailyProfile.panchanga.sunrise
            val sunset = dailyProfile.panchanga.sunset
            val solarTimes = com.example.engines.astronomy.SolarTimes(
                sunrise = sunrise,
                sunset = sunset,
                solarNoon = sunrise.plusSeconds(Duration.between(sunrise, sunset).seconds / 2)
            )
            choghadiyaEngine.calculateDailyChoghadiya(
                solarTimes = solarTimes,
                nextDaySunrise = sunrise.plusDays(1),
                dayOfWeek = dailyProfile.currentZonedDateTime.dayOfWeek.value
            )
        }
        val liveActiveChoghadiya = remember(liveZonedDateTime.minute, choghadiyaSlots) {
            choghadiyaEngine.findActiveChoghadiya(choghadiyaSlots, liveZonedDateTime)
        }
        val choghadiyaEnd = liveActiveChoghadiya.endTime
        val now = liveZonedDateTime
        val minutesRemaining = Duration.between(now, choghadiyaEnd).toMinutes().coerceAtLeast(0)
        val rahuStart = dailyProfile.rahuKalam.startTime
        val minutesToRahu = Duration.between(now, rahuStart).toMinutes()

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.active_choghadiya), style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent, fontSize = 9.sp))
                    Text(
                        text = "${liveActiveChoghadiya.type.displayName} (${liveActiveChoghadiya.type.devanagari})",
                        style = MaterialTheme.typography.bodyMedium.copy(color = if (liveActiveChoghadiya.type.isAuspicious) AuspiciousGreen else InauspiciousRed, fontWeight = FontWeight.Bold)
                    )
                    Text("${minutesRemaining}m remaining", style = MaterialTheme.typography.labelSmall.copy(color = GrayText))
                }

                Divider(modifier = Modifier.height(32.dp).width(1.dp), color = LightBorder)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.rahu_kalam), style = MaterialTheme.typography.labelSmall.copy(color = InauspiciousRed, fontSize = 9.sp))
                    Text(
                        text = if (now.isAfter(dailyProfile.rahuKalam.startTime) && now.isBefore(dailyProfile.rahuKalam.endTime)) "ACTIVE DOSHA" else if (minutesToRahu > 0) "in ${minutesToRahu}m" else "Passed today",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${dailyProfile.rahuKalam.startTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}",
                        style = MaterialTheme.typography.labelSmall.copy(color = GrayText)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Watch Face Style Selector
        // AI Watch Face Horoscope
        var aiHoroscope by remember { mutableStateOf<String?>(null) }
        var isGenerating by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.ai_horoscope),
                        style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(16.dp))
                }

                if (aiHoroscope != null) {
                    Text(
                        text = aiHoroscope!!,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        isGenerating = true
                        coroutineScope.launch {
                            val celestialData = "Lagna: ${personalizedProfile.natalLagnaRashi.sanskritName}, Moon: ${personalizedProfile.natalChandraRashi.sanskritName}, Nakshatra: ${personalizedProfile.natalNakshatra.sanskritName}, Current Choghadiya: ${dailyProfile.currentChoghadiya.type.displayName}, Day: ${dailyProfile.panchanga.vara}"
                            aiHoroscope = generateHoroscope(celestialData, currentLanguage)
                            isGenerating = false
                        }
                    },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = OnMaroon),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = OnMaroon, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.generating_horoscope), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.generate_ai_horoscope), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vedic Artistic Themes Selection Panel
        VedicArtisticThemesCard(
            currentConfig = currentConfig,
            onConfigChange = { updatedConfig ->
                currentConfig = updatedConfig
                onStyleSelect(updatedConfig.style)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Wear OS Vitality Trends (Prana, Tejas, Ojas score aggregation from steps & heart rate)
        VedicVitalityTrendsCard()

        Spacer(modifier = Modifier.height(16.dp))

        // Silent Vibration Alarms for Vedic Time Markers (Brahma Muhurta, Sunrise, Sunset, Abhijit, Rahu Kalam)
        VedicSilentAlarmsCard(dailyProfile = dailyProfile)

        Spacer(modifier = Modifier.height(16.dp))

        // Watch Face Astrology Indicators Settings Button Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("open_watch_settings_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Watch Face Astrology Indicators",
                            style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Toggle subdials, Nakshatra, Dasha & Transits",
                            style = MaterialTheme.typography.bodySmall.copy(color = GrayText, fontSize = 11.sp)
                        )
                    }
                }
                Button(
                    onClick = { showWatchSettingsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("configure_watch_indicators_button")
                ) {
                    Text("Settings", fontSize = 12.sp, color = OnMaroon, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Complications Slot Customizer Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Wear OS Complication Subdials",
                    style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ComplicationSlotRow("Top Subdial", complications.topSlot) {
                    complications = complications.copy(topSlot = it)
                }
                ComplicationSlotRow("Bottom Subdial", complications.bottomSlot) {
                    complications = complications.copy(bottomSlot = it)
                }
                ComplicationSlotRow("Left Subdial", complications.leftSlot) {
                    complications = complications.copy(leftSlot = it)
                }
                ComplicationSlotRow("Right Subdial", complications.rightSlot) {
                    complications = complications.copy(rightSlot = it)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Direct Sync Complications Button with no text splitting
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = VedicWearableSyncManager.syncProfileAndHoroscopeToWatch(
                                context = context,
                                profile = profile,
                                personalizedProfile = personalizedProfile,
                                dailyProfile = dailyProfile,
                                watchConfig = currentConfig
                            )
                            if (success) {
                                onSyncToWatch()
                                android.widget.Toast.makeText(context, "Complications synced to smartwatch!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("btn_sync_complications_direct")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = OnMaroon
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sync Complications to Watch",
                        fontSize = 12.sp,
                        color = OnMaroon,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        WearSyncAnimationCard(
            onPerformSync = {
                val success = VedicWearableSyncManager.syncProfileAndHoroscopeToWatch(
                    context = context,
                    profile = profile,
                    personalizedProfile = personalizedProfile,
                    dailyProfile = dailyProfile,
                    watchConfig = currentConfig
                )
                if (success) {
                    onSyncToWatch()
                }
                success
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Background Sync & Battery Optimization Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (!wearBackgroundSyncEnabled)
                    Color(0xFF2E7D32).copy(alpha = 0.06f)
                else
                    MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                if (!wearBackgroundSyncEnabled) Color(0xFF2E7D32).copy(alpha = 0.3f) else LightBorder
            ),
            modifier = Modifier.fillMaxWidth().testTag("watch_background_sync_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (!wearBackgroundSyncEnabled) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaroonPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (!wearBackgroundSyncEnabled) Icons.Default.BatteryChargingFull else Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = if (!wearBackgroundSyncEnabled) Color(0xFF2E7D32) else MaroonPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Background Watch Sync",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (!wearBackgroundSyncEnabled) Color(0xFF2E7D32).copy(alpha = 0.15f) else MaroonPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (!wearBackgroundSyncEnabled) "🔋 Battery Saver Active" else "⚡ Auto-Sync On",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!wearBackgroundSyncEnabled) Color(0xFF2E7D32) else MaroonPrimary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (!wearBackgroundSyncEnabled)
                                    "Background syncing is disabled to preserve phone & watch battery. Manual sync above remains active."
                                else
                                    "Smart background sync is running (15 min on charger, 2 hours on battery) to update Hora & Muhurtas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = wearBackgroundSyncEnabled,
                        onCheckedChange = { isEnabled ->
                            coroutineScope.launch {
                                preferencesRepository.updateWearBackgroundSyncEnabled(isEnabled)
                                if (!isEnabled) {
                                    androidx.work.WorkManager.getInstance(context).cancelUniqueWork("VedicWearSyncWork")
                                    Toast.makeText(context, "Background watch sync disabled (Battery Saver Active)", Toast.LENGTH_SHORT).show()
                                } else {
                                    val constraints = androidx.work.Constraints.Builder()
                                        .setRequiresBatteryNotLow(true)
                                        .build()
                                    val syncWorkRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.workers.VedicSyncWorker>(
                                        120, java.util.concurrent.TimeUnit.MINUTES
                                    )
                                        .setConstraints(constraints)
                                        .build()
                                    androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                                        "VedicWearSyncWork",
                                        androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                                        syncWorkRequest
                                    )
                                    Toast.makeText(context, "Background watch sync enabled", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.testTag("toggle_watch_tab_background_sync")
                    )
                }
            }
        }
    }

    if (showWatchSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showWatchSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Watch, contentDescription = null, tint = MaroonPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Watch Face Indicators", color = MaroonPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Toggle specific Vedic astrology indicators and Panchang elements rendered on your smart watch face display.",
                        style = MaterialTheme.typography.bodySmall.copy(color = GrayText),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "5 Limbs of Panchang Display Elements:",
                        style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    IndicatorToggleRow("Lunar Tithi & Paksha Phase", showTithiIndicator) { showTithiIndicator = it }
                    IndicatorToggleRow("Nakshatra & Pada Indicator", showNakshatraIndicator) { showNakshatraIndicator = it }
                    IndicatorToggleRow("Panchang Yoga Element", showYogaIndicator) { showYogaIndicator = it }
                    IndicatorToggleRow("Panchang Karana Element", showKaranaIndicator) { showKaranaIndicator = it }
                    IndicatorToggleRow("Vara (Solar Day) & Sunrise/Sunset", showVaraIndicator) { showVaraIndicator = it }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = LightBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Vedic Astrological Telemetry:",
                        style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    IndicatorToggleRow("Rahu Kaal & Yamaganda Safety Arc", showRahuKaalIndicator) { showRahuKaalIndicator = it }
                    IndicatorToggleRow("Planetary Hora Lord", showHoraIndicator) { showHoraIndicator = it }
                    IndicatorToggleRow("Vimshottari Dasha Ring", showDashaRing) { showDashaRing = it }
                    IndicatorToggleRow("Gochar Planetary Transits", showGocharTransits) { showGocharTransits = it }
                    IndicatorToggleRow("Choghadiya Muhurta Subdial", showChoghadiyaSubdial) { showChoghadiyaSubdial = it }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = LightBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Complication Subdials Customization",
                        style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quick Presets:",
                        fontSize = 11.sp,
                        color = DarkTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset chips for instant configuration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    complications = ComplicationCustomization(
                                        topSlot = ComplicationType.CHOGHADIYA,
                                        bottomSlot = ComplicationType.RAHU_COUNTDOWN,
                                        leftSlot = ComplicationType.BRAHMA_MUHURTA,
                                        rightSlot = ComplicationType.ABHIJIT_MUHURTA
                                    )
                                }
                        ) {
                            Text(
                                "🕉 Muhurta",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    complications = ComplicationCustomization(
                                        topSlot = ComplicationType.DASHA_PERIOD,
                                        bottomSlot = ComplicationType.TARA_BALA,
                                        leftSlot = ComplicationType.MOON_RASHI,
                                        rightSlot = ComplicationType.HORA_GRAHA
                                    )
                                }
                        ) {
                            Text(
                                "⭐ Jyotish",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    complications = ComplicationCustomization(
                                        topSlot = ComplicationType.MANTRA_JAPA,
                                        bottomSlot = ComplicationType.MINDFULNESS_RING,
                                        leftSlot = ComplicationType.BRAHMA_MUHURTA,
                                        rightSlot = ComplicationType.FESTIVAL_COUNTDOWN
                                    )
                                }
                        ) {
                            Text(
                                "📿 Sadhana",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    complications = ComplicationCustomization(
                                        topSlot = ComplicationType.GHATI_PALA,
                                        bottomSlot = ComplicationType.COSMIC_SCORE,
                                        leftSlot = ComplicationType.YOGA_KARANA,
                                        rightSlot = ComplicationType.TITHI_PHASE
                                    )
                                }
                        ) {
                            Text(
                                "⏳ Vedic",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    ComplicationSlotRow("Top Subdial", complications.topSlot) {
                        complications = complications.copy(topSlot = it)
                    }
                    ComplicationSlotRow("Bottom Subdial", complications.bottomSlot) {
                        complications = complications.copy(bottomSlot = it)
                    }
                    ComplicationSlotRow("Left Subdial", complications.leftSlot) {
                        complications = complications.copy(leftSlot = it)
                    }
                    ComplicationSlotRow("Right Subdial", complications.rightSlot) {
                        complications = complications.copy(rightSlot = it)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showWatchSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
                ) {
                    Text("Save & Sync", color = OnMaroon, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWatchSettingsDialog = false }) {
                    Text("Cancel", color = GrayText)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Google Watch Face Format (WFF v2) XML Inspector Dialog
    if (showWffXmlDialog) {
        AlertDialog(
            onDismissRequest = { showWffXmlDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = MaroonPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Watch Face Format (WFF v2)", color = MaroonPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Official declarative XML conforming to Google Wear OS Watch Face Format standards:",
                        style = MaterialTheme.typography.bodySmall.copy(color = GrayText, fontSize = 11.sp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        Text(
                            text = currentWffXml.ifEmpty {
                                WatchFaceFormatBuilder.buildWffXml(currentConfig, personalizedProfile, dailyProfile)
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showWffXmlDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
                ) {
                    Text("Done", color = OnMaroon, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Wear OS Activation Guide Dialog
    if (showHowToSetActiveDialog) {
        AlertDialog(
            onDismissRequest = { showHowToSetActiveDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Watch, contentDescription = null, tint = OrangeAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Activating on Smartwatch", color = MaroonPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Google Wear OS Watch Face Activation Steps:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. The watch face has been pushed directly via Google Wearable Data Layer to your smartwatch.", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("2. On your watch: Touch and hold your current watch face for 2 seconds to enter the Watch Face Picker.", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("3. Swipe across to select your Vedic Astrological Watch Face (${currentConfig.style.displayName}).", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("4. Alternatively, open the Galaxy Wearable or Google Pixel Watch app on your phone -> Watch Faces -> Downloaded to activate it with one tap.", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = AuspiciousGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Note: Wear OS 4+ & 5+ support the SET_PUSHED_WATCH_FACE_AS_ACTIVE permission for instant direct activation.",
                            fontSize = 11.sp,
                            color = AuspiciousGreen,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHowToSetActiveDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
                ) {
                    Text("Understood", color = OnMaroon, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Companion App Installation Required Prompt
    if (showInstallCompanionAppDialog) {
        AlertDialog(
            onDismissRequest = { showInstallCompanionAppDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Watch, contentDescription = null, tint = OrangeAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Install Wear OS Companion App", color = MaroonPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No Wear OS smartwatch with the Jyotish Companion app was detected.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To apply customized watch faces, background images, and real-time Panchang data directly to your watch:",
                        style = MaterialTheme.typography.bodySmall.copy(color = GrayText)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "1. Open Google Play Store on your Wear OS watch.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "2. Search for 'Vedic Jyotish Watch' & install.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "3. Grant notification and complication permissions when prompted.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "4. Click 'Create & Push Watch Face' again.", fontSize = 12.sp, color = MaroonPrimary, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val playStoreIntent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("market://details?id=${context.packageName}")
                            ).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
                            context.startActivity(playStoreIntent)
                        } catch (e: Exception) {
                            val webIntent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                            ).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
                            context.startActivity(webIntent)
                        }
                        showInstallCompanionAppDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
                ) {
                    Text("Install via Play Store", color = OnMaroon, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstallCompanionAppDialog = false }) {
                    Text("Cancel", color = GrayText)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun ComplicationSlotRow(
    slotTitle: String,
    selected: ComplicationType,
    onSelect: (ComplicationType) -> Unit
) {
    var showPickerDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(slotTitle, color = DarkTextSecondary, fontSize = 12.sp)
        OutlinedButton(
            onClick = { showPickerDialog = true },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier
                .height(34.dp)
                .widthIn(max = 200.dp),
            border = BorderStroke(1.dp, LightBorder)
        ) {
            Text(
                text = selected.displayName,
                fontSize = 11.sp,
                color = MaroonPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                softWrap = false
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaroonPrimary)
        }
    }

    if (showPickerDialog) {
        AlertDialog(
            onDismissRequest = { showPickerDialog = false },
            title = {
                Text("Select for $slotTitle", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaroonPrimary)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ComplicationType.values().forEach { type ->
                        val isSelected = type == selected
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaroonPrimary.copy(alpha = 0.12f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) MaroonPrimary else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(type)
                                    showPickerDialog = false
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = type.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaroonPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = type.subtitle,
                                        fontSize = 11.sp,
                                        color = GrayText
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaroonPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPickerDialog = false }) {
                    Text("Close", color = MaroonPrimary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun CelestialTelemetryPill(
    label: String,
    symbol: String,
    value: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = symbol, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = label, color = GrayText, fontSize = 9.sp)
                Text(text = value, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun IndicatorToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnMaroon,
                checkedTrackColor = MaroonPrimary
            )
        )
    }
}

