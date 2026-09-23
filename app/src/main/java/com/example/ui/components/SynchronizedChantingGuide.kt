package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

data class ChantingWord(
    val devanagari: String,
    val transliteration: String,
    val meaning: String
)

data class GuidedMantraTrack(
    val title: String,
    val source: String,
    val words: List<ChantingWord>
)

val GUIDED_MANTRA_TRACKS = listOf(
    GuidedMantraTrack(
        title = "Gayatri Mantra",
        source = "Rigveda 3.62.10",
        words = listOf(
            ChantingWord("ॐ", "OM", "The Primordial Sound of Cosmic Creation"),
            ChantingWord("भूर्", "BHŪḤ", "The Physical Realm & Earth Plane"),
            ChantingWord("भुवः", "BHUVAḤ", "The Subtle Atmosphere & Vital Prana"),
            ChantingWord("स्वः", "SVAḤ", "The Celestial Light & Divine Mind"),
            ChantingWord("तत्", "TAT", "That Ultimate Transcendent Reality"),
            ChantingWord("सवितुर्", "SAVITUR", "The Radiance of the Supreme Divine Sun"),
            ChantingWord("वरेण्यं", "VAREṆYAṀ", "Most Adorable, Supremely Desirable"),
            ChantingWord("भर्गो", "BHARGO", "The Luminous Grace that Destroys Ignorance"),
            ChantingWord("देवस्य", "DEVASYA", "Of the Radiant, Supreme Divine Being"),
            ChantingWord("धीमहि", "DHĪMAHI", "We Meditate & Merge Our Heart In"),
            ChantingWord("धियो", "DHIYO", "Our Awakened Intellect & Higher Vision"),
            ChantingWord("यो", "YO", "May That Very Solar Consciousness"),
            ChantingWord("नः", "NAḤ", "For All of Us Unceasingly"),
            ChantingWord("प्रचोदयात्", "PRACODAYĀT", "Enlighten, Awaken & Inspire")
        )
    ),
    GuidedMantraTrack(
        title = "Mahamrityunjaya Mantra",
        source = "Rigveda 7.59.12",
        words = listOf(
            ChantingWord("ॐ", "OM", "The Primordial Cosmic Vibration"),
            ChantingWord("त्र्यम्बकं", "TRYAMBAKAṀ", "The Three-Eyed Lord of Omni-Vision"),
            ChantingWord("यजामहे", "YAJĀMAHE", "We Worship and Adore Devotedly"),
            ChantingWord("सुगन्धिं", "SUGANDHIṀ", "The Fragrant, Auspicious Essence of Life"),
            ChantingWord("पुष्टिवर्धनम्", "PUṢṬIVARDHANAM", "The Nurturer of Spiritual Vitality"),
            ChantingWord("उर्वारुकमिव", "URVĀRUKAMIVA", "As a Ripe Cucumber Freely Detaches"),
            ChantingWord("बन्धनान्", "BANDHANĀN", "From the Binding Stem of Illusion"),
            ChantingWord("मृत्योर्मुक्षीय", "MṚTYORMUKṢĪYA", "May We Be Liberated From Mortality"),
            ChantingWord("मामृतात्", "MĀ'MṚTĀT", "And Never Part From Boundless Immortality")
        )
    ),
    GuidedMantraTrack(
        title = "7 Chakra Kundalini Bija Chain",
        source = "Shat-Chakra-Nirupana",
        words = listOf(
            ChantingWord("लं", "LAM", "Muladhara • Earth Element • Root Center"),
            ChantingWord("वं", "VAM", "Svadhisthana • Water Element • Sacral Center"),
            ChantingWord("रं", "RAM", "Manipura • Fire Element • Solar Plexus Center"),
            ChantingWord("यं", "YAM", "Anahata • Air Element • Heart Center"),
            ChantingWord("हं", "HAM", "Vishuddha • Ether Element • Throat Center"),
            ChantingWord("ॐ", "AUM", "Ajna • Mind & Intuition • Third Eye"),
            ChantingWord("शान्तिः", "SHĀNTIḤ", "Sahasrara • Supreme Silence • Crown Union")
        )
    )
)

enum class BreathPhase(val title: String, val sanskrit: String, val color: Color) {
    INHALE("Inhale Divine Light", "पूरक (Pūraka)", Color(0xFF4DD0E1)),
    HOLD_IN("Hold & Absorb Prana", "अन्तः कुम्भक (Antar Kumbhaka)", Color(0xFFFFD54F)),
    EXHALE("Exhale & Chant Mantra", "रेचक (Recaka)", SaffronAccent),
    HOLD_OUT("Rest in Silent Stillness", "बाह्य कुम्भक (Bāhya Kumbhaka)", MaroonPrimary)
}

enum class PranayamaRatio(val label: String, val inhale: Int, val holdIn: Int, val exhale: Int, val holdOut: Int) {
    EQUAL("Box Breathing 4-4-4-4", 4, 4, 4, 4),
    DEEP("Calming 4-7-8", 4, 7, 8, 0),
    YOGIC("Classical 4-8-8-2", 4, 8, 8, 2)
}

@Composable
fun SynchronizedChantingGuide(
    modifier: Modifier = Modifier,
    isAudioPlaying: Boolean = false
) {
    var selectedTrackIndex by remember { mutableStateOf(0) }
    val track = GUIDED_MANTRA_TRACKS[selectedTrackIndex]

    var activeWordIndex by remember { mutableStateOf(0) }
    var isGuideActive by remember { mutableStateOf(true) }
    var tempoSecondsPerWord by remember { mutableStateOf(2.5f) }

    // Pranayama State
    var selectedRatio by remember { mutableStateOf(PranayamaRatio.EQUAL) }
    var currentPhase by remember { mutableStateOf(BreathPhase.INHALE) }
    var phaseSecondsRemaining by remember { mutableStateOf(selectedRatio.inhale) }

    // Step through synchronized words
    LaunchedEffect(isGuideActive, track, activeWordIndex) {
        if (isGuideActive) {
            delay((tempoSecondsPerWord * 1000L).toLong())
            activeWordIndex = (activeWordIndex + 1) % track.words.size
        }
    }

    // Pranayama breathing loop
    LaunchedEffect(selectedRatio, currentPhase, phaseSecondsRemaining) {
        delay(1000L)
        if (phaseSecondsRemaining > 1) {
            phaseSecondsRemaining--
        } else {
            // Transition to next breath phase
            when (currentPhase) {
                BreathPhase.INHALE -> {
                    if (selectedRatio.holdIn > 0) {
                        currentPhase = BreathPhase.HOLD_IN
                        phaseSecondsRemaining = selectedRatio.holdIn
                    } else {
                        currentPhase = BreathPhase.EXHALE
                        phaseSecondsRemaining = selectedRatio.exhale
                    }
                }
                BreathPhase.HOLD_IN -> {
                    currentPhase = BreathPhase.EXHALE
                    phaseSecondsRemaining = selectedRatio.exhale
                }
                BreathPhase.EXHALE -> {
                    if (selectedRatio.holdOut > 0) {
                        currentPhase = BreathPhase.HOLD_OUT
                        phaseSecondsRemaining = selectedRatio.holdOut
                    } else {
                        currentPhase = BreathPhase.INHALE
                        phaseSecondsRemaining = selectedRatio.inhale
                    }
                }
                BreathPhase.HOLD_OUT -> {
                    currentPhase = BreathPhase.INHALE
                    phaseSecondsRemaining = selectedRatio.inhale
                }
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Synchronized Chanting & Pranayama",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Syllable-by-syllable lyric resonance & breath guide",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                    )
                }

                Surface(
                    color = GoldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = track.source,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Track Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GUIDED_MANTRA_TRACKS.forEachIndexed { index, t ->
                    val isSel = index == selectedTrackIndex
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            selectedTrackIndex = index
                            activeWordIndex = 0
                        },
                        label = { Text(t.title, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaroonPrimary.copy(alpha = 0.12f),
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

            // Pranayama Breathing Circle Visualizer
            val targetScale = when (currentPhase) {
                BreathPhase.INHALE -> 1.18f
                BreathPhase.HOLD_IN -> 1.22f
                BreathPhase.EXHALE -> 0.88f
                BreathPhase.HOLD_OUT -> 0.82f
            }
            val breathAnimScale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing),
                label = "breath_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(breathAnimScale)
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 8f

                    // Pulsing outer aura
                    drawCircle(
                        color = currentPhase.color.copy(alpha = 0.2f),
                        radius = radius + 6.dp.toPx(),
                        center = center
                    )

                    // Main breathing circle
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(currentPhase.color.copy(alpha = 0.85f), currentPhase.color.copy(alpha = 0.35f))
                        ),
                        radius = radius,
                        center = center
                    )

                    // Golden contour
                    drawCircle(
                        color = GoldPrimary,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${phaseSecondsRemaining}s",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = currentPhase.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = currentPhase.sanskrit,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldPrimary,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Syllable-by-Syllable Karaoke Highlight Area
            Surface(
                color = Color(0xFF14131A),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Synchronized Sanskrit Verses (Touch word to seek):",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 10.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Flow of Words
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val activeWord = track.words.getOrNull(activeWordIndex)

                        // Highlighted Active Word Spotlight
                        activeWord?.let { word ->
                            Surface(
                                color = MaroonPrimary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, GoldPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = word.devanagari,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            color = GoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 26.sp
                                        )
                                    )
                                    Text(
                                        text = word.transliteration,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = word.meaning,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SaffronAccent,
                                            textAlign = TextAlign.Center,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Full Verses Row with Highlighted Pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            track.words.forEachIndexed { idx, w ->
                                val isCurrent = idx == activeWordIndex
                                val pillBg by animateColorAsState(
                                    targetValue = if (isCurrent) SaffronAccent else Color(0xFF22202C),
                                    label = "pill_color"
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = pillBg,
                                    modifier = Modifier.clickable { activeWordIndex = idx }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = w.devanagari,
                                            color = if (isCurrent) Color.Black else Color.White,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = w.transliteration,
                                            color = if (isCurrent) Color.Black.copy(alpha = 0.8f) else Color.Gray,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Breath Ratio Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pranayama Rhythm:",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PranayamaRatio.values().forEach { ratio ->
                        val isSel = ratio == selectedRatio
                        AssistChip(
                            onClick = {
                                selectedRatio = ratio
                                currentPhase = BreathPhase.INHALE
                                phaseSecondsRemaining = ratio.inhale
                            },
                            label = { Text(ratio.label.substringBefore(" "), fontSize = 10.sp, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSel) MaroonPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
