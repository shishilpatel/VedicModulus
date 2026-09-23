package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class JapaMantraOption(
    val title: String,
    val devanagari: String,
    val transliteration: String,
    val meaning: String
)

val DEFAULT_JAPA_MANTRAS = listOf(
    JapaMantraOption(
        title = "Gayatri Mantra",
        devanagari = "ॐ भूर्भुवः स्वः तत्सवितुर्वरेण्यं भर्गो देवस्य धीमहि धियो यो नः प्रचोदयात्",
        transliteration = "Om Bhūr Bhuvaḥ Svaḥ Tat Savitur Vareṇyaṁ Bhargo Devasya Dhīmahi Dhiyo Yo Naḥ Pracodayāt",
        meaning = "Illumination of intellect by the supreme solar consciousness."
    ),
    JapaMantraOption(
        title = "Mahamrityunjaya",
        devanagari = "ॐ त्र्यम्बकं यजामहे सुगन्धिं पुष्टिवर्धनम् उर्वारुकमिव बन्धनान् मृत्योर्मुक्षीय मामृतात्",
        transliteration = "Om Tryambakaṁ Yajāmahe Sugandhiṁ Puṣṭivardhanam Urvārukamiva Bandhanān Mṛtyormukṣīya Māmṛtāt",
        meaning = "Liberation from mortality and spiritual nourishment."
    ),
    JapaMantraOption(
        title = "Shiva Panchakshari",
        devanagari = "ॐ नमः शिवाय",
        transliteration = "Om Namaḥ Śivāya",
        meaning = "Salutations to Shiva, the auspicious inner consciousness."
    ),
    JapaMantraOption(
        title = "Maha Mantra",
        devanagari = "हरे कृष्ण हरे कृष्ण कृष्ण कृष्ण हरे हरे । हरे राम हरे राम राम राम हरे हरे ॥",
        transliteration = "Hare Kṛṣṇa Hare Kṛṣṇa Kṛṣṇa Kṛṣṇa Hare Hare | Hare Rāma Hare Rāma Rāma Rāma Hare Hare",
        meaning = "Awakening divine love and transcendental peace."
    ),
    JapaMantraOption(
        title = "Om Primordial",
        devanagari = "ॐ ॐ ॐ",
        transliteration = "Om Om Om",
        meaning = "The primal cosmic vibration of creation."
    ),
    JapaMantraOption(
        title = "Ganesh Mool",
        devanagari = "ॐ गं गणपतये नमः",
        transliteration = "Om Gaṁ Gaṇapataye Namaḥ",
        meaning = "Removal of obstacles and auspicious beginnings."
    )
)

@Composable
fun JapaMalaCard(
    modifier: Modifier = Modifier,
    initialMantra: JapaMantraOption = DEFAULT_JAPA_MANTRAS[0]
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var currentBead by remember { mutableStateOf(0) }
    var completedRounds by remember { mutableStateOf(0) }
    var selectedMantra by remember { mutableStateOf(initialMantra) }
    var autoPaceActive by remember { mutableStateOf(false) }
    var autoPaceIntervalSec by remember { mutableStateOf(4) }
    var milestoneMessage by remember { mutableStateOf<String?>(null) }
    var soundEnabled by remember { mutableStateOf(true) }

    val toneGenerator = remember {
        try {
            android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 65)
        } catch (_: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator?.release()
        }
    }

    fun triggerVibration(milestone: Boolean = false, roundComplete: Boolean = false) {
        try {
            if (soundEnabled && toneGenerator != null) {
                if (roundComplete) {
                    toneGenerator.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 350)
                } else if (milestone) {
                    toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_BEEP2, 180)
                } else {
                    toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 30)
                }
            }

            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    when {
                        roundComplete -> {
                            // Sustained celebratory 3-beat rhythm
                            val pattern = longArrayOf(0, 150, 100, 150, 100, 300)
                            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                        }
                        milestone -> {
                            // Double tap
                            val pattern = longArrayOf(0, 80, 80, 120)
                            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                        }
                        else -> {
                            vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (roundComplete) 400 else 40)
                }
            }
        } catch (_: Exception) {}
    }

    fun incrementBead() {
        if (currentBead < 108) {
            currentBead++
            when (currentBead) {
                27 -> {
                    triggerVibration(milestone = true)
                    milestoneMessage = "Quarter Mala Completed (27 / 108 beads) • 1/4 Sankalpa"
                }
                54 -> {
                    triggerVibration(milestone = true)
                    milestoneMessage = "Half Mala Reached (54 / 108 beads) • Midpoint Harmony"
                }
                81 -> {
                    triggerVibration(milestone = true)
                    milestoneMessage = "Three-Quarters Done (81 / 108 beads) • Steadfast Focus"
                }
                108 -> {
                    triggerVibration(roundComplete = true)
                    completedRounds++
                    milestoneMessage = "Full 108-Bead Mala Completed! • 1 Round Finished 🕉"
                }
                else -> {
                    triggerVibration(milestone = false)
                    if (currentBead % 10 == 0) milestoneMessage = null
                }
            }
        } else {
            // Roll over to new mala from Sumeru bead
            currentBead = 1
            triggerVibration(milestone = false)
            milestoneMessage = "Starting Round ${completedRounds + 1} from Sumeru Bead"
        }
    }

    // Auto pacing loop
    LaunchedEffect(autoPaceActive, currentBead) {
        if (autoPaceActive) {
            delay(autoPaceIntervalSec * 1000L)
            incrementBead()
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "108-Bead Japa Mala / जप माला",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Tactile Haptic Digital Prayer Beads",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { soundEnabled = !soundEnabled },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = if (soundEnabled) "🔔" else "🔕",
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        color = GoldPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Rounds: $completedRounds",
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mantra Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DEFAULT_JAPA_MANTRAS.forEach { mantra ->
                    val isSel = mantra.title == selectedMantra.title
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedMantra = mantra },
                        label = { Text(mantra.title, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
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

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Mantra Display
            Surface(
                color = Color(0xFF14131A),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedMantra.devanagari,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedMantra.transliteration,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Central Interactive Rosary Wheel & Counter
            Box(
                modifier = Modifier
                    .size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circular 108 Beads Canvas Visualizer
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f - 18f

                    // Rosary thread ring
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.25f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw representative 54 or 108 beads around circumference
                    val totalDisplayBeads = 54
                    for (i in 0 until totalDisplayBeads) {
                        val angle = Math.toRadians((i * (360.0 / totalDisplayBeads)) - 90.0)
                        val beadX = center.x + (radius * cos(angle)).toFloat()
                        val beadY = center.y + (radius * sin(angle)).toFloat()

                        val beadIndex = (i * 2) + 1
                        val isCounted = beadIndex <= currentBead

                        val beadColor = when {
                            i == 0 -> SaffronAccent // Sumeru / Guru bead
                            isCounted -> MaroonPrimary
                            else -> LightBorder
                        }

                        val beadRadius = if (i == 0) 6.dp.toPx() else if (isCounted) 4.5.dp.toPx() else 3.dp.toPx()

                        drawCircle(
                            color = beadColor,
                            radius = beadRadius,
                            center = Offset(beadX, beadY)
                        )
                    }
                }

                // Center Tap Button with Animated Scale
                val interactionSource = remember { MutableInteractionSource() }
                val scaleAnim by animateFloatAsState(
                    targetValue = if (currentBead > 0) 1.02f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "bead_pulse"
                )

                Surface(
                    shape = CircleShape,
                    color = MaroonPrimary,
                    border = BorderStroke(3.dp, Brush.radialGradient(listOf(GoldPrimary, SaffronAccent))),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scaleAnim)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            incrementBead()
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$currentBead",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "/ 108 BEADS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "TAP TO CHANT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 8.sp
                            )
                        )
                    }
                }
            }

            // Milestone Banner
            milestoneMessage?.let { msg ->
                Spacer(modifier = Modifier.height(10.dp))
                AnimatedVisibility(visible = true, enter = fadeIn() + expandVertically()) {
                    Surface(
                        color = AuspiciousGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AuspiciousGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = AuspiciousGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AuspiciousGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar with Milestone Markers
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { (currentBead / 108f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaroonPrimary,
                    trackColor = LightBorder
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0", fontSize = 10.sp, color = DarkTextSecondary)
                    Text("27 (1/4)", fontSize = 10.sp, color = if (currentBead >= 27) MaroonPrimary else DarkTextSecondary, fontWeight = FontWeight.Bold)
                    Text("54 (1/2)", fontSize = 10.sp, color = if (currentBead >= 54) MaroonPrimary else DarkTextSecondary, fontWeight = FontWeight.Bold)
                    Text("81 (3/4)", fontSize = 10.sp, color = if (currentBead >= 81) MaroonPrimary else DarkTextSecondary, fontWeight = FontWeight.Bold)
                    Text("108", fontSize = 10.sp, color = if (currentBead >= 108) MaroonPrimary else DarkTextSecondary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Controls: Auto-Pacing & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Auto-Pacing Toggle
                Button(
                    onClick = { autoPaceActive = !autoPaceActive },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (autoPaceActive) OrangeAccent else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (autoPaceActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (autoPaceActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (autoPaceActive) "Auto-Pacing On (${autoPaceIntervalSec}s)" else "Auto-Pacing",
                        fontSize = 11.sp,
                        color = if (autoPaceActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Reset Button
                OutlinedButton(
                    onClick = {
                        currentBead = 0
                        autoPaceActive = false
                        milestoneMessage = null
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, LightBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        modifier = Modifier.size(15.dp),
                        tint = DarkTextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Mala", fontSize = 11.sp, color = DarkTextSecondary)
                }
            }
        }
    }
}
