package com.example.ui.screens.tabs

import android.content.Context
import kotlinx.coroutines.launch
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.VastuCompassViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.PersonalizedProfile
import com.example.core.model.VastuDirectionInfo
import com.example.data.UserPreferencesRepository
import com.example.engines.ai.AstrologicalCalculationAiBridge
import com.example.engines.vastu.DirectionSuitability
import com.example.engines.vastu.RoomTypeInfo
import com.example.engines.vastu.VastuEngine
import com.example.engines.vastu.VastuSpaceMode
import com.example.engines.vastu.VastuSpaceOptimizationEngine
import com.example.ui.components.VastuCalibrationDialog
import com.example.ui.components.VastuCompassCanvas
import com.example.ui.components.VastuFloorPlanAuditor
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun VastuCompassTab(
    personalizedProfile: PersonalizedProfile,
    showOnlyShastra: Boolean = false,
    viewModel: VastuCompassViewModel = viewModel(),
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val vmSensorActive by viewModel.isLiveSensorActive.collectAsStateWithLifecycle()

    // Process-wide & storage-backed local state to ensure 100% immediate persistence across page changes
    var isLiveSensorActive by rememberSaveable {
        mutableStateOf(UserPreferencesRepository.isVastuSpaceOptimizationEnabledCached(context))
    }

    var showCalibrationDialog by remember { mutableStateOf(false) }

    // Space Optimization Mode: Home vs Office (persisted in SharedPreferences & rememberSaveable)
    var spaceMode by rememberSaveable {
        mutableStateOf<VastuSpaceMode>(UserPreferencesRepository.getVastuSpaceModeCached(context))
    }
    val rooms = remember(spaceMode) { VastuSpaceOptimizationEngine.getRoomsForMode(spaceMode) }
    var selectedRoom by remember(spaceMode) {
        val storedRoomName = UserPreferencesRepository.getVastuSelectedRoomNameCached(context)
        mutableStateOf(rooms.firstOrNull { it.name == storedRoomName } ?: rooms.first())
    }

    // Explicit handler to persist ON/OFF status everywhere synchronously and asynchronously
    val onToggleStatus: (Boolean) -> Unit = { newStatus ->
        isLiveSensorActive = newStatus
        UserPreferencesRepository.setVastuSpaceOptimizationEnabledCached(context, newStatus)
        viewModel.setLiveSensorActive(newStatus)
    }

    // Keep local toggle in lockstep with ViewModel flow
    LaunchedEffect(vmSensorActive, showOnlyShastra) {
        if (!showOnlyShastra) {
            isLiveSensorActive = vmSensorActive
        }
    }

    // Always connect and disconnect sensors strictly according to screen visibility and lifecycle
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(showOnlyShastra, lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    if (!showOnlyShastra) {
                        viewModel.onScreenVisible()
                    }
                }
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE,
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    viewModel.onScreenHidden()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        if (!showOnlyShastra) {
            val storedStatus = UserPreferencesRepository.isVastuSpaceOptimizationEnabledCached(context)
            isLiveSensorActive = storedStatus
            viewModel.onScreenVisible()
        } else {
            viewModel.onScreenHidden()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.onScreenHidden()
        }
    }

    // Ambient Soundscape
    var isPlayingSound by remember { mutableStateOf(false) }
    var audioTrack by remember { mutableStateOf<AudioTrack?>(null) }

    if (showCalibrationDialog) {
        VastuCalibrationDialog(onDismiss = { showCalibrationDialog = false })
    }

    // Soundscape effect based on directional elemental frequencies
    LaunchedEffect(isPlayingSound, uiState.selectedDirection) {
        // Always stop and release previous track first to avoid audio track leakage & audio thread hang
        audioTrack?.let { oldTrack ->
            try {
                oldTrack.stop()
                oldTrack.release()
            } catch (_: Exception) {}
        }
        audioTrack = null

        if (isPlayingSound) {
            val track = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val frequency = when (uiState.selectedDirection.directionName) {
                    "North" -> 108.0    // Kuber / Mercury harmonic
                    "East" -> 432.0     // Surya solar harmonic
                    "South-East" -> 528.0 // Agni transformation
                    "South" -> 285.0    // Yama / Prithvi grounding
                    "South-West" -> 396.0 // Nairutya stability
                    "West" -> 639.0     // Varuna flow & gains
                    "North-West" -> 741.0 // Vayu air circulation
                    else -> 136.1       // Ishanya cosmic Om frequency
                }
                val sampleRate = 44100
                val numSamples = sampleRate * 2
                val samples = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / frequency)
                    samples[i] = (Math.sin(angle) * 16384.0).toInt().toShort()
                }
                val newTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(numSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                newTrack.apply {
                    write(samples, 0, numSamples)
                    setLoopPoints(0, numSamples, -1)
                }
            }
            try {
                track.play()
                audioTrack = track
            } catch (_: Exception) {
                try { track.release() } catch (_: Exception) {}
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (_: Exception) {}
            audioTrack = null
        }
    }

    // Connect sensors lifecycle to screen visibility and live active state (only in Vastu Compass)
    DisposableEffect(viewModel, isLiveSensorActive, showOnlyShastra) {
        if (!showOnlyShastra && isLiveSensorActive) {
            viewModel.startSensors()
        } else {
            viewModel.stopSensors()
        }
        onDispose {
            viewModel.stopSensors()
        }
    }

    // Room Evaluation for the current facing direction
    val roomEvaluation = remember(selectedRoom, uiState.selectedDirection) {
        VastuSpaceOptimizationEngine.evaluateRoomDirection(selectedRoom, uiState.selectedDirection.directionName)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!showOnlyShastra) {
            // Mode Switcher Header: Home vs Office with Vastu Space Optimization Toggle (Vastu Compass only)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleStatus(!isLiveSensorActive) }
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Vastu Space Optimization",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary)
                            )
                            Text(
                                text = if (isLiveSensorActive) "Status: ON • Live Space Optimization ACTIVE" else "Status: OFF • Space Optimization Paused",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isLiveSensorActive) AuspiciousGreen else DarkTextSecondary,
                                    fontWeight = if (isLiveSensorActive) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                        Switch(
                            checked = isLiveSensorActive,
                            onCheckedChange = onToggleStatus,
                            modifier = Modifier.testTag("vastu_live_sensor_switch")
                        )
                    }

                    if (isLiveSensorActive) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Home vs Office Selector (visible when Space Optimization is ON)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            VastuSpaceMode.values().forEach { mode ->
                                val isSel = spaceMode == mode
                                Button(
                                    onClick = {
                                        spaceMode = mode
                                        val newRoom = VastuSpaceOptimizationEngine.getRoomsForMode(mode).first()
                                        selectedRoom = newRoom
                                        UserPreferencesRepository.setVastuSpaceModeCached(context, mode)
                                        UserPreferencesRepository.setVastuSelectedRoomNameCached(context, newRoom.name)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSel) MaroonPrimary else Color.Transparent,
                                        contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = mode.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Digital Bubble Level & Sensor Status Indicator
            DigitalSpiritLevelBanner(
                tiltDeg = uiState.deviceTilt,
                pitchDeg = uiState.pitchAngle,
                rollDeg = uiState.rollAngle,
                isLiveActive = isLiveSensorActive,
                hasHardware = uiState.hasMagneticSensor,
                onCalibrateClick = { showCalibrationDialog = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Live Compass Canvas
            VastuCompassCanvas(
                rotationAngle = uiState.rotationAngle,
                selectedDirection = uiState.selectedDirection,
                onDirectionSelected = { viewModel.selectDirection(it) },
                onRotate = { viewModel.updateManualRotation(it) },
                modifier = Modifier
                    .size(260.dp)
                    .testTag("vastu_compass_canvas")
            )

            Spacer(modifier = Modifier.height(8.dp))
        } else {
            // Vastu Shastra Header Banner (clean Vedic Shastra wisdom without space optimization)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, LightBorder),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Vastu Shastra Principles & Remedies",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Classical Vedic directional guidelines governing cosmic elements, ruling deities, positive vibrations, and traditional architectural remedies for all 8 zones.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Current Facing Angle & Direction Badge
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = MaroonPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (showOnlyShastra) "SELECTED ZONE: ${uiState.selectedDirection.directionName.uppercase()}" else "FACING: ${uiState.selectedDirection.directionName.uppercase()}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary)
                        )
                        Text(
                            text = "${uiState.selectedDirection.sanskritName} • ${uiState.selectedDirection.degrees.toInt()}°",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                        )
                    }
                }

                IconButton(
                    onClick = { isPlayingSound = !isPlayingSound },
                    modifier = Modifier
                        .background(if (isPlayingSound) InauspiciousRed else OrangeAccent.copy(alpha = 0.15f), CircleShape)
                        .size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isPlayingSound) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Toggle Ambient Frequency Soundscape",
                        tint = if (isPlayingSound) Color.White else MaroonPrimary
                    )
                }
            }
        }

        if (showOnlyShastra) {
            Spacer(modifier = Modifier.height(8.dp))
            // Quick 8-directional selector chips for Shastra analysis
            Text(
                text = "SELECT DIRECTION TO VIEW SHASTRA RULES & REMEDIES:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GrayText),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VastuEngine.ASHTA_DIK.forEach { dir ->
                    val isSel = dir.directionName == uiState.selectedDirection.directionName
                    FilterChip(
                        selected = isSel,
                        onClick = { viewModel.selectDirection(dir) },
                        label = { Text(dir.directionName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaroonPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Direction Shastra Comprehensive Detail Card (always visible for selected direction)
        DirectionShastraCard(
            direction = uiState.selectedDirection,
            spaceMode = spaceMode
        )

        // Space Optimization features (Room Audit & Floor Plan Auditor)
        // EXCLUSIVE to Vastu Compass tab and toggled by Vastu Space Optimization switch
        if (!showOnlyShastra) {
            if (isLiveSensorActive) {
                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Room Auditor Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUDIT SPECIFIC SPACE / ROOM",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GrayText)
                    )
                    Text(
                        text = "${rooms.size} Available",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaroonPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Horizontal Room Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rooms.forEach { room ->
                        val isSel = room.id == selectedRoom.id
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedRoom = room
                                UserPreferencesRepository.setVastuSelectedRoomNameCached(context, room.name)
                            },
                            label = { Text("${room.iconEmoji} ${room.name}", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaroonPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dynamic Room Evaluation Card
                RoomAuditResultCard(
                    evaluation = roomEvaluation,
                    onAlternativeClick = { dirName ->
                        viewModel.selectDirectionByName(dirName)
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // AI Translated Vastu Consultation Card
                var aiVastuConsultation by remember { mutableStateOf<String?>(null) }
                var isConsultingVastuAi by remember { mutableStateOf(false) }
                val vastuCoroutineScope = rememberCoroutineScope()

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.ai_vastu_title),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = MaroonPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Button(
                                onClick = {
                                    isConsultingVastuAi = true
                                    vastuCoroutineScope.launch {
                                        try {
                                            aiVastuConsultation = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                AstrologicalCalculationAiBridge.interpretVastu(
                                                    roomName = roomEvaluation.room.name,
                                                    direction = uiState.selectedDirection.directionName,
                                                    suitability = roomEvaluation.suitability.name,
                                                    explanation = roomEvaluation.explanation,
                                                    remedy = roomEvaluation.recommendedRemedy,
                                                    bestAlternatives = roomEvaluation.bestAlternativeDirections,
                                                    currentLanguage = currentLanguage
                                                )
                                            }
                                        } catch (e: Exception) {
                                            aiVastuConsultation = "Error: ${e.message}"
                                        } finally {
                                            isConsultingVastuAi = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                enabled = !isConsultingVastuAi
                            ) {
                                if (isConsultingVastuAi) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (isConsultingVastuAi) stringResource(R.string.calculating_stars) else stringResource(R.string.generate_ai_vastu),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (isConsultingVastuAi) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val langPromptName = com.example.core.language.LanguageManager.getLanguagePromptName(currentLanguage)
                            Text(
                                text = stringResource(R.string.translating_vastu, langPromptName),
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                            )
                        } else if (aiVastuConsultation != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = LightBorder, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = aiVastuConsultation!!,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive 9-Grid Vastu Purusha Mandala Floor Plan Auditor
                VastuFloorPlanAuditor()
            } else {
                Spacer(modifier = Modifier.height(8.dp))

                // Space Optimization Paused Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, LightBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaroonPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Vastu Space Optimization Paused",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Switch ON the toggle above to enable room audits, directional suitability analysis, and the 9-grid floor plan auditor.",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun DigitalSpiritLevelBanner(
    tiltDeg: Float,
    pitchDeg: Float,
    rollDeg: Float,
    isLiveActive: Boolean,
    hasHardware: Boolean,
    onCalibrateClick: () -> Unit
) {
    val isFlat = tiltDeg < 9f
    val isModerate = tiltDeg in 9f..16f

    val badgeColor = when {
        !isLiveActive -> GrayText
        isFlat -> AuspiciousGreen
        isModerate -> OrangeAccent
        else -> InauspiciousRed
    }

    val statusText = when {
        !hasHardware -> "Magnetometer sensor absent; manual dial enabled"
        !isLiveActive -> "Manual dial active (tap or drag to rotate)"
        isFlat -> "Device Flat & Level (Optimal Compass Accuracy)"
        isModerate -> "Slight Tilt (${tiltDeg.toInt()}°) — hold horizontal for best accuracy"
        else -> "Tilted (${tiltDeg.toInt()}°) — hold phone flat on palm"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Miniature Spirit Bubble Level Canvas
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.dp, badgeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val maxBubbleOffset = size.width * 0.35f
                        val bx = (rollDeg / 25f).coerceIn(-1f, 1f) * maxBubbleOffset
                        val by = (pitchDeg / 25f).coerceIn(-1f, 1f) * maxBubbleOffset

                        // Target crosshairs
                        drawLine(Color.Gray.copy(alpha = 0.4f), Offset(cx - 8f, cy), Offset(cx + 8f, cy), 1f)
                        drawLine(Color.Gray.copy(alpha = 0.4f), Offset(cx, cy - 8f), Offset(cx, cy + 8f), 1f)

                        // Bubble dot
                        drawCircle(
                            color = badgeColor,
                            radius = 4.dp.toPx(),
                            center = Offset(cx + bx, cy + by)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = badgeColor)
                    )
                    Text(
                        text = "Pitch: ${pitchDeg.toInt()}° • Roll: ${rollDeg.toInt()}° • Tilt: ${tiltDeg.toInt()}°",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                    )
                }
            }

            TextButton(
                onClick = onCalibrateClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Calibrate", fontSize = 11.sp, color = MaroonPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RoomAuditResultCard(
    evaluation: com.example.engines.vastu.VastuEvaluationResult,
    onAlternativeClick: (String) -> Unit
) {
    val statusColor = when (evaluation.suitability) {
        DirectionSuitability.EXCELLENT -> AuspiciousGreen
        DirectionSuitability.ACCEPTABLE -> OrangeAccent
        DirectionSuitability.DOSHA -> InauspiciousRed
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(evaluation.room.iconEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = evaluation.room.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            text = evaluation.room.hindiName,
                            style = MaterialTheme.typography.bodySmall.copy(color = GrayText)
                        )
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = evaluation.suitability.ratingStars,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = evaluation.suitability.label,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = evaluation.explanation,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
            )

            if (evaluation.recommendedRemedy != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = OrangeAccent.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "VEDIC SHASTRA REMEDY FOR THIS POSITION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = OrangeAccent)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = evaluation.recommendedRemedy,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Best Alternative Directions
            Text(
                text = "Ideal Directions for ${evaluation.room.name}:",
                style = MaterialTheme.typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                evaluation.bestAlternativeDirections.forEach { dir ->
                    val isCurrent = dir == evaluation.direction
                    SuggestionChip(
                        onClick = { onAlternativeClick(dir) },
                        label = {
                            Text(
                                text = if (isCurrent) "✓ $dir (Current)" else "👉 $dir",
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isCurrent) AuspiciousGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectionShastraCard(
    direction: VastuDirectionInfo,
    spaceMode: VastuSpaceMode
) {
    val roomGuide = remember(direction) {
        when (direction.directionName) {
            "North-East" -> DirectionGuidance(
                energyNature = "Ishanya; divine consciousness, spiritual influx, mental clarity",
                homeIdeal = "Puja Room (Mandir), Underground Water, Meditation Study, Open Balcony",
                homeForbidden = "Toilet, Kitchen, Heavy Storage, Master Bedroom, Overhead Tank",
                homeRemedy = "Keep perfectly clean and clutter-free. Place a Sphatik Shree Yantra or indoor water cascade.",
                officeIdeal = "Chairman / Founder Meditation Cabin, Visionary R&D Lab, Sacred Altar",
                officeForbidden = "Heavy Server Racks, Chemical Dumps, Restrooms, Manufacturing Machinery",
                officeRemedy = "Keep open and illuminated. Avoid storing scrap or unorganized archives here."
            )
            "East" -> DirectionGuidance(
                energyNature = "Indra & Surya; cosmic solar prana, vitality, fame and executive leadership",
                homeIdeal = "Main Entrance Door, Living Room, Children's Study Desk, Verandah",
                homeForbidden = "Heavy Master Bedroom, Staircase blocking sunrise, Septic Tank",
                homeRemedy = "Install an engraved copper Surya Yantra on East wall; ensure early morning sun penetrates.",
                officeIdeal = "Main Reception, Customer Welcome Desk, PR & Communications Office",
                officeForbidden = "Dark Archives, Heavy Scrap Store",
                officeRemedy = "Hang a copper Sun emblem; keep windows wide and unshielded by heavy opaque blinds."
            )
            "South-East" -> DirectionGuidance(
                energyNature = "Agneya; sacred fire element governing metabolism, energy, and financial ignition",
                homeIdeal = "Kitchen, Gas Stove, Electrical Inverter, Water Heater/Geyser",
                homeForbidden = "Pooja Room, Master Bedroom, Underground Water Sump, Borewell",
                homeRemedy = "If kitchen is elsewhere, install an Agni Gayatri pyramid or warm crimson accent here.",
                officeIdeal = "IT Server Room, Power Transformers, Electrical Generators, Pantry/Cafeteria",
                officeForbidden = "CEO / MD Cabin (causes irritability & friction), Washroom",
                officeRemedy = "Place a copper pyramid on electrical breaker panels; maintain fire safety equipment."
            )
            "South" -> DirectionGuidance(
                energyNature = "Dakshina; Yama & Mangala; stability, fame, courage and legal protection",
                homeIdeal = "Heavy Bedroom, Staircase, Machinery, Safe Vaults",
                homeForbidden = "Main Entrance Door (Yama Dwar), Low Sunken Courtyard, Borewell",
                homeRemedy = "Keep boundary wall higher than North; install a Panchmukhi Hanuman tile.",
                officeIdeal = "Heavy Storage, Conference Room, Senior Partner Offices",
                officeForbidden = "Main Glass Entrance without protective threshold",
                officeRemedy = "Use solid wall surfaces with warm terracotta or deep earth tones."
            )
            "South-West" -> DirectionGuidance(
                energyNature = "Nairutya; Earth element anchor of household authority, marriage stability and savings",
                homeIdeal = "Master Bedroom (Head of Family), Heavy Cash Locker, Overhead Water Tank",
                homeForbidden = "Main Entrance, Toilet, Septic Tank, Open Kitchen, Underground Tank",
                homeRemedy = "Never dig or build sunken tanks here. Use heavy brass strips or lead helix if defect exists.",
                officeIdeal = "MD / CEO / Managing Director Cabin, Safe Vault, Key Documents Repository",
                officeForbidden = "Customer Reception, Calling Staff, Water Coolers",
                officeRemedy = "CEO should sit in SW facing North or East with a solid wall behind their chair."
            )
            "West" -> DirectionGuidance(
                energyNature = "Pashchima; Varuna & Shani; cosmic order, commercial profits, karma fulfillment",
                homeIdeal = "Dining Room, Children's Study, Overhead Water Tank, Guest Room",
                homeForbidden = "Underground Water Well, Main Porch",
                homeRemedy = "Place a Varuna Yantra or blue crystal sphere for balanced profits and harmonious partnerships.",
                officeIdeal = "Conference Room, Legal & Compliance Dept, Senior Partner Cabins",
                officeForbidden = "Garbage Staging, Damp Leaking Restrooms",
                officeRemedy = "Display corporate awards, patents, and framed achievements on the West wall."
            )
            "North-West" -> DirectionGuidance(
                energyNature = "Vayavya; Vayu & Chandra; movement, social networking, circulation and velocity",
                homeIdeal = "Guest Bedroom, Unmarried Daughter's Room, Vehicle Garage, Finished Goods",
                homeForbidden = "Master Bedroom (creates restlessness/wanderlust), Main Cash Locker",
                homeRemedy = "Hang a 5-rod metal wind chime or crystal globe to harmonize air currents.",
                officeIdeal = "Sales & Marketing Team, Dispatch Warehouse, Client Calling Hub",
                officeForbidden = "MD / CEO Desk (creates indecisiveness), Main Accounts Safe",
                officeRemedy = "Store ready-for-dispatch goods in NW to accelerate sales turnover and client shipping."
            )
            else -> DirectionGuidance(
                energyNature = "Uttara; Kubera & Budha; treasury of cosmic wealth, commercial brilliance and liquid cash",
                homeIdeal = "Treasury, Cash Locker, Living Room, Study Desk, Open Green Garden",
                homeForbidden = "Kitchen (burns wealth), Dark Storage, Heavy Concrete Staircase",
                homeRemedy = "Place indoor money plants in water, brass Kuber statue, or silver bowl on North wall.",
                officeIdeal = "Accounts & Finance Department, Cash Counter, Main Reception, Desk facing North",
                officeForbidden = "Heavy Scrap Store, Pantry, High Unventilated Partitions",
                officeRemedy = "Keep North open, brightly lit, and free of heavy furniture to attract financial growth."
            )
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${direction.directionName.uppercase()} ZONE • ${direction.degrees.toInt()}°",
                        style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = direction.sanskritName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                }

                Surface(
                    color = MaroonPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaroonPrimary)
                ) {
                    Text(
                        text = spaceMode.displayName,
                        color = MaroonPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Presiding Deity & Cosmic Element:", color = GrayText, fontSize = 11.sp)
            Text(
                text = "${direction.presidingDeity} • Element: ${direction.mahabhutaElement}",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Energy Characteristic:", color = GrayText, fontSize = 11.sp)
            Text(text = roomGuide.energyNature, color = MaroonPrimary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (spaceMode == VastuSpaceMode.HOME) "Recommended Residential Rooms:" else "Recommended Workplace Placements:",
                color = AuspiciousGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (spaceMode == VastuSpaceMode.HOME) roomGuide.homeIdeal else roomGuide.officeIdeal,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (spaceMode == VastuSpaceMode.HOME) "Incompatible / Avoid Here:" else "Avoid in this Commercial Zone:",
                color = InauspiciousRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (spaceMode == VastuSpaceMode.HOME) roomGuide.homeForbidden else roomGuide.officeForbidden,
                color = DarkTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("Vedic Shastra Remedy:", color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                text = if (spaceMode == VastuSpaceMode.HOME) roomGuide.homeRemedy else roomGuide.officeRemedy,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
        }
    }
}

private data class DirectionGuidance(
    val energyNature: String,
    val homeIdeal: String,
    val homeForbidden: String,
    val homeRemedy: String,
    val officeIdeal: String,
    val officeForbidden: String,
    val officeRemedy: String
)
