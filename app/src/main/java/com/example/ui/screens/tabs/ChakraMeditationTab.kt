package com.example.ui.screens.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.BirthProfile
import com.example.core.model.DailyVedicProfile
import com.example.core.model.PersonalizedProfile
import com.example.core.model.chakra.*
import com.example.data.ChakraProgressRepository
import com.example.engines.chakra.*
import com.example.ui.components.AuspiciousYogaTimerCard
import com.example.ui.components.BreathingGuideVisualizer
import com.example.ui.components.ChakraMandalaVisualizer
import com.example.ui.components.JapaMalaCard
import com.example.ui.components.SynchronizedChantingGuide
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class SpiritualPracticeView(val title: String, val sanskrit: String) {
    CHAKRA_DHYANA("Chakra Journey", "चक्र ध्यान"),
    JAPA_MALA("108 Japa Mala", "जप माला"),
    CHANTING_PRANAYAMA("Karaoke Guide", "मन्त्र एवं प्राणायाम")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChakraMeditationTab(
    profile: BirthProfile?,
    dailyProfile: DailyVedicProfile,
    personalizedProfile: PersonalizedProfile?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Engines & Repositories
    val meditationEngine = remember { ChakraMeditationEngine() }
    val progressRepository = remember { ChakraProgressRepository.getInstance(context) }
    val player = remember { MeditationPlayer(context) }
    val breathingEngine = remember {
        BreathingExerciseEngine(onPhaseChanged = { phase ->
            when (phase) {
                BreathingPhase.INHALE -> player.playCueBell("audio/breathing/breathing_bell_inhale.mp3")
                BreathingPhase.HOLD_IN -> player.playCueBell("audio/breathing/breathing_bell_hold.mp3")
                BreathingPhase.EXHALE -> player.playCueBell("audio/breathing/breathing_bell_exhale.mp3")
                BreathingPhase.HOLD_OUT -> player.playCueBell("audio/breathing/breathing_bell_hold.mp3")
            }
        })
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
            breathingEngine.release()
            meditationEngine.release()
        }
    }

    val playerState by player.playerState.collectAsState()
    val breathingState by breathingEngine.state.collectAsState()
    val journeyState by meditationEngine.journeyState.collectAsState()
    val selectedTradition by meditationEngine.selectedTradition.collectAsState()
    val userStats by progressRepository.getStats().collectAsState(initial = com.example.data.MeditationStats())

    var selectedChakra by remember { mutableStateOf(ChakraType.MULADHARA) }
    var selectedMode by remember { mutableStateOf(MeditationMode.CHAKRA_FOCUS) }
    var showTraditionDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var isFavoriteSession by remember { mutableStateOf(false) }
    var volumeSliderOpen by remember { mutableStateOf(false) }
    var activePracticeView by remember { mutableStateOf(SpiritualPracticeView.CHAKRA_DHYANA) }

    // Personalized suggestion
    val suggestion = remember(personalizedProfile) {
        meditationEngine.getPersonalizedSuggestion(profile, personalizedProfile)
    }

    // Save session on completion
    LaunchedEffect(playerState.isSessionComplete) {
        if (playerState.isSessionComplete && playerState.elapsedSeconds > 30) {
            progressRepository.recordSession(
                chakraType = selectedChakra,
                mode = selectedMode,
                durationMinutes = playerState.targetDurationMinutes,
                completedSeconds = playerState.elapsedSeconds,
                ambientTrack = playerState.selectedAmbient.id
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. Header & Tradition Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Chakra Meditation & Mantra",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = "चक्र ध्यानम् एवं मन्त्राः • 7 Centers of Dhyana",
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                )
            }

            IconButton(onClick = { showDisclaimerDialog = true }) {
                Icon(Icons.Default.Info, contentDescription = "Spiritual Practice Notice", tint = GoldPrimary)
            }
            IconButton(onClick = { showTraditionDialog = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Configure Tradition", tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tradition Chip
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tradition: ${selectedTradition.title}",
                    style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = selectedTradition.scriptureReference,
                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Spiritual Practice Focus Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpiritualPracticeView.values().forEach { viewOption ->
                val isSel = activePracticeView == viewOption
                FilterChip(
                    selected = isSel,
                    onClick = { activePracticeView = viewOption },
                    label = {
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = viewOption.title,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = viewOption.sanskrit,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = if (isSel) MaroonPrimary else DarkTextSecondary
                                )
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary.copy(alpha = 0.25f),
                        selectedLabelColor = MaroonPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSel,
                        borderColor = if (isSel) MaroonPrimary else LightBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activePracticeView == SpiritualPracticeView.JAPA_MALA) {
            // Dedicated 108-Bead Japa Mala View
            JapaMalaCard()
            Spacer(modifier = Modifier.height(16.dp))
            SynchronizedChantingGuide(isAudioPlaying = playerState.isPlaying)
        } else if (activePracticeView == SpiritualPracticeView.CHANTING_PRANAYAMA) {
            // Dedicated Synchronized Chanting & Pranayama View
            SynchronizedChantingGuide(isAudioPlaying = playerState.isPlaying)
            Spacer(modifier = Modifier.height(16.dp))
            JapaMalaCard()
        } else {
            // 2. Mode Selector Bar
        Text(
            text = "Select Practice Mode:",
            style = MaterialTheme.typography.labelMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MeditationMode.values().forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = {
                        selectedMode = mode
                        player.setMode(mode)
                        if (mode == MeditationMode.BREATH_MEDITATION) {
                            breathingEngine.start()
                        } else {
                            breathingEngine.pause()
                        }
                        if (mode == MeditationMode.FULL_JOURNEY) {
                            meditationEngine.startSevenChakraJourney(
                                totalDurationMinutes = playerState.targetDurationMinutes,
                                onChakraTransition = { newChakra ->
                                    selectedChakra = newChakra
                                    player.playChakra(newChakra, mode)
                                }
                            )
                        } else {
                            meditationEngine.stopSevenChakraJourney()
                        }
                    },
                    label = { Text(mode.displayName, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. 7-Chakra Carousel Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChakraType.values().forEach { ch ->
                val isSelected = selectedChakra == ch
                val chColor = ch.composeColor

                Surface(
                    color = if (isSelected) chColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) chColor else LightBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .clickable {
                            selectedChakra = ch
                            if (playerState.isPlaying) {
                                player.playChakra(ch, selectedMode)
                            }
                        }
                        .padding(vertical = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = ch.bijaMantra,
                            color = chColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = ch.sanskritName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        Text(
                            text = "${ch.order}. ${ch.bijaTransliteration}",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 10.sp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Hero Visualizer Card (Mandala or Breath)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D12)), // Cosmic AMOLED Canvas
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedMode == MeditationMode.BREATH_MEDITATION) {
                    BreathingGuideVisualizer(breathingState = breathingState)

                    Spacer(modifier = Modifier.height(8.dp))
                    // Breathing Pattern selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BreathingPattern.values().forEach { pattern ->
                            FilterChip(
                                selected = breathingState.pattern == pattern,
                                onClick = { breathingEngine.setPattern(pattern) },
                                label = { Text(pattern.title, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                } else {
                    ChakraMandalaVisualizer(
                        chakra = selectedChakra,
                        isPlaying = playerState.isPlaying
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chakra Title & Element Tag
                Text(
                    text = "${selectedChakra.order}. ${selectedChakra.sanskritName} (${selectedChakra.englishName})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Element: ${selectedChakra.traditionalElement} • Bija: ${selectedChakra.bijaMantra} (${selectedChakra.bijaTransliteration})",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = selectedChakra.composeColor,
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )

                if (selectedMode == MeditationMode.FULL_JOURNEY && journeyState.isActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { journeyState.totalProgressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GoldPrimary,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ascending Journey: ${journeyState.currentChakra.englishName} (${journeyState.secondsInCurrentChakra}/${journeyState.secondsPerChakra}s)",
                        style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Media Player & Duration Controls Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Duration Chips: 5, 10, 15, 20, 30, 45, 60 minutes
                Text(
                    text = "Meditation Duration:",
                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MeditationRepository.DURATION_PRESETS_MINUTES.forEach { mins ->
                        FilterChip(
                            selected = playerState.targetDurationMinutes == mins,
                            onClick = { player.setTargetDuration(mins) },
                            label = { Text("$mins min", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaroonPrimary,
                                selectedLabelColor = OnMaroon
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Timer Progress & Interactive Seekbar
                val elapsedMinutes = playerState.elapsedSeconds / 60
                val elapsedSec = playerState.elapsedSeconds % 60
                val targetMinutes = playerState.targetDurationMinutes
                val totalTargetSec = (targetMinutes * 60).toFloat()
                val currentProgress = (playerState.elapsedSeconds / totalTargetSec).coerceIn(0f, 1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%02d:%02d", elapsedMinutes, elapsedSec),
                        style = MaterialTheme.typography.labelMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Drag slider to seek",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 10.sp)
                    )
                    Text(
                        text = "$targetMinutes:00 min",
                        style = MaterialTheme.typography.labelMedium.copy(color = DarkTextSecondary)
                    )
                }

                Slider(
                    value = currentProgress,
                    onValueChange = { fraction ->
                        val targetSeconds = (fraction * totalTargetSec).toInt()
                        player.seekSessionSeconds(targetSeconds)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaroonPrimary,
                        activeTrackColor = MaroonPrimary,
                        inactiveTrackColor = LightBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Play / Pause / Reset Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        isFavoriteSession = !isFavoriteSession
                        scope.launch {
                            progressRepository.recordSession(
                                chakraType = selectedChakra,
                                mode = selectedMode,
                                durationMinutes = playerState.targetDurationMinutes,
                                completedSeconds = playerState.elapsedSeconds,
                                ambientTrack = playerState.selectedAmbient.id
                            )
                        }
                    }) {
                        Icon(
                            if (isFavoriteSession) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Save Favorite",
                            tint = if (isFavoriteSession) Color.Red else DarkTextSecondary
                        )
                    }

                    // Reset / Stop
                    IconButton(onClick = {
                        player.stop()
                        breathingEngine.stop()
                        meditationEngine.stopSevenChakraJourney()
                    }) {
                        Icon(Icons.Default.Replay, contentDescription = "Reset", tint = DarkTextSecondary)
                    }

                    // Main Play / Pause Hero Button
                    FilledIconButton(
                        onClick = {
                            if (playerState.isPlaying) {
                                player.pause()
                                breathingEngine.pause()
                            } else {
                                player.playChakra(selectedChakra, selectedMode)
                                if (selectedMode == MeditationMode.BREATH_MEDITATION) {
                                    breathingEngine.start()
                                }
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = GoldPrimary),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Volume dialog toggle
                    IconButton(onClick = { volumeSliderOpen = !volumeSliderOpen }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Volume", tint = DarkTextSecondary)
                    }

                    // Sleep Timer Quick Cycle (0 -> 15 -> 30 -> 45 -> 60)
                    IconButton(onClick = {
                        val next = when (playerState.sleepTimerRemainingMinutes) {
                            0 -> 15
                            15 -> 30
                            30 -> 45
                            45 -> 60
                            else -> 0
                        }
                        player.setSleepTimer(next)
                    }) {
                        Badge(
                            containerColor = if (playerState.sleepTimerRemainingMinutes > 0) MaroonPrimary else Color.Transparent
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "Sleep Timer",
                                tint = if (playerState.sleepTimerRemainingMinutes > 0) MaroonPrimary else DarkTextSecondary
                            )
                        }
                    }
                }

                if (volumeSliderOpen) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = null, tint = DarkTextSecondary)
                        Slider(
                            value = playerState.volume,
                            onValueChange = { player.setVolume(it) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = DarkTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ambient Soundscape Selector
                Text(
                    text = "Ambient Soundscape:",
                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AmbientSoundTrack.values().forEach { track ->
                        FilterChip(
                            selected = playerState.selectedAmbient == track,
                            onClick = { player.setAmbientTrack(track) },
                            label = { Text(track.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary.copy(alpha = 0.8f),
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Classical Chakra Mantra & Pronunciation Guide Card
        val chakraMantraData = remember(selectedChakra) {
            MantraRepository.getChakraMantraData(selectedChakra)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Authentic Chakra Mantra",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Surface(
                        color = GoldPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = chakraMantraData.bijaTransliteration,
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sanskrit Verse Box
                Surface(
                    color = Color(0xFF14131A),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = chakraMantraData.sanskritText,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = chakraMantraData.transliteration,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Meaning
                Text(
                    text = "Meaning & Tattva:",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = chakraMantraData.englishMeaning,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Pronunciation Guide
                Text(
                    text = "Pronunciation Guide (Uchcharana):",
                    style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.Bold)
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chakraMantraData.pronunciationGuide,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Traditional source & deities
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Presiding Deity:",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = chakraMantraData.presidingDeity,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Scriptural Source:",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = chakraMantraData.traditionalSource,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Educational & Yogic Asanas Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Traditional Yogic Associations (${selectedChakra.sanskritName})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaroonPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = selectedChakra.educationalExplanation,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sanskrit / Hindi / Gujarati names
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Hindi: ${selectedChakra.hindiName}", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary))
                    Text("Gujarati: ${selectedChakra.gujaratiName}", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pronunciation Guide:",
                    style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = selectedChakra.pronunciationGuide,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Recommended Asanas
                Text(
                    text = "Related Yoga Asanas (Classical):",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                )
                selectedChakra.recommendedAsanas.forEach { asana ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("• ", color = MaroonPrimary, fontWeight = FontWeight.Bold)
                        Text(asana, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pranayama:", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontWeight = FontWeight.Bold))
                        Text(selectedChakra.recommendedPranayama, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mudra:", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontWeight = FontWeight.Bold))
                        Text(selectedChakra.recommendedMudra, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notice: Classical asanas are spiritual postures from classical Hatha texts; they are not medical interventions.",
                    style = MaterialTheme.typography.labelSmall.copy(color = GrayText, fontSize = 10.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Guided Contemplation Steps Card
        MeditationRepository.GUIDED_MEDITATIONS[selectedChakra]?.let { guide ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Guided Contemplation: ${guide.title}",
                        style = MaterialTheme.typography.titleSmall.copy(color = GoldPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = guide.focusTheme,
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Visualization: ${guide.visualizationPrompt}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    guide.steps.forEachIndexed { idx, step ->
                        Row(modifier = Modifier.padding(vertical = 3.dp)) {
                            Text("${idx + 1}. ", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(step, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Affirmation: ${guide.closingAffirmation}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 8. Astrological & Personalized Suggestion Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Jyotish & Element Harmony Suggestion",
                    style = MaterialTheme.typography.titleSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Recommended Focus: ${suggestion.recommendedChakra.englishName} (${suggestion.recommendedChakra.sanskritName})",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = suggestion.astrologicalReason,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = suggestion.traditionalPracticeNote,
                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 10.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 9. Auspicious Yoga & Meditation Timer (MOVED HERE FROM PANCHANGA AS REQUESTED)
        AuspiciousYogaTimerCard(dailyProfile = dailyProfile)

        Spacer(modifier = Modifier.height(16.dp))

        // 10. Meditation Streak & Offline Statistics
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your Meditation Journey & Offline Stats",
                    style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${userStats.totalMinutes}m", style = MaterialTheme.typography.titleLarge.copy(color = GoldPrimary, fontWeight = FontWeight.Bold))
                        Text("Total Meditated", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${userStats.totalSessions}", style = MaterialTheme.typography.titleLarge.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold))
                        Text("Sessions", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${userStats.currentStreakDays}d", style = MaterialTheme.typography.titleLarge.copy(color = AuspiciousGreen, fontWeight = FontWeight.Bold))
                        Text("Streak", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 11. Integrated 108-Bead Japa Mala Quick Access
        JapaMalaCard()

        Spacer(modifier = Modifier.height(16.dp))

        // 12. Integrated Synchronized Sanskrit Chanting & Pranayama
        SynchronizedChantingGuide(isAudioPlaying = playerState.isPlaying)
        }
    }

    // Tradition Dialog
    if (showTraditionDialog) {
        AlertDialog(
            onDismissRequest = { showTraditionDialog = false },
            title = { Text("Chakra Tradition & Lineage") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChakraTradition.values().forEach { trad ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    meditationEngine.setTradition(trad)
                                    showTraditionDialog = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTradition == trad,
                                onClick = {
                                    meditationEngine.setTradition(trad)
                                    showTraditionDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(trad.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(trad.scriptureReference, color = GoldPrimary, fontSize = 11.sp)
                                Text(trad.description, color = DarkTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTraditionDialog = false }) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        )
    }

    // Spiritual Practice Notice Dialog
    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            title = { Text("Traditional Spiritual Practice Notice") },
            text = {
                Text(
                    text = MeditationRepository.SPIRITUAL_DISCLAIMER,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            },
            confirmButton = {
                TextButton(onClick = { showDisclaimerDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }
}
