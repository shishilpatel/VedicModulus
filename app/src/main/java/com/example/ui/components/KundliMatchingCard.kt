package com.example.ui.components

import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.BirthProfile
import com.example.core.model.Nakshatra
import com.example.core.model.Rashi
import com.example.engines.jyotisha.AshtakootaMatchingResult
import com.example.engines.jyotisha.KootaScore
import com.example.engines.jyotisha.KundliMatchingEngine
import com.example.ui.components.icons.ModernZodiacIcon
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KundliMatchingCard(
    userProfile: BirthProfile?,
    modifier: Modifier = Modifier
) {
    // Default Groom and Bride selections
    var boyRashi by remember { mutableStateOf(Rashi.MESHA) }
    var boyNakshatra by remember { mutableStateOf(Nakshatra.ASHWINI) }
    var boyPada by remember { mutableStateOf(1) }

    var girlRashi by remember { mutableStateOf(Rashi.VRISHABHA) }
    var girlNakshatra by remember { mutableStateOf(Nakshatra.ROHINI) }
    var girlPada by remember { mutableStateOf(2) }

    var expandedKootaName by remember { mutableStateOf<String?>(null) }
    var showBoySelector by remember { mutableStateOf(false) }
    var showGirlSelector by remember { mutableStateOf(false) }

    val matchResult: AshtakootaMatchingResult = remember(
        boyRashi, boyNakshatra, boyPada,
        girlRashi, girlNakshatra, girlPada
    ) {
        KundliMatchingEngine.matchKundlis(
            boyRashi = boyRashi,
            boyNak = boyNakshatra,
            boyPada = boyPada,
            girlRashi = girlRashi,
            girlNak = girlNakshatra,
            girlPada = girlPada
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vedic Ashtakoota Milan (36 Gunas)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Authentic Parashara Kundli Compatibility Engine",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 10.sp)
                    )
                }

                Surface(
                    color = when {
                        matchResult.totalPoints >= 28.0 -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                        matchResult.totalPoints >= 18.0 -> OrangeAccent.copy(alpha = 0.15f)
                        else -> CrimsonError.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f / 36", matchResult.totalPoints),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = when {
                                matchResult.totalPoints >= 28.0 -> Color(0xFF2E7D32)
                                matchResult.totalPoints >= 18.0 -> Color(0xFFD97706)
                                else -> CrimsonError
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Partner Profile Selectors Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Groom Box
                PartnerSummaryBox(
                    label = "Groom (वर)",
                    rashi = boyRashi,
                    nakshatra = boyNakshatra,
                    pada = boyPada,
                    onClick = { showBoySelector = true },
                    modifier = Modifier.weight(1f)
                )

                // Bride Box
                PartnerSummaryBox(
                    label = "Bride (वधू)",
                    rashi = girlRashi,
                    nakshatra = girlNakshatra,
                    pada = girlPada,
                    onClick = { showGirlSelector = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Overall Score Bar & Verdict
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = matchResult.ratingCategory,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaroonPrimary
                            )
                        )
                        Text(
                            text = "${matchResult.compatibilityPercentage.toInt()}% Match",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OrangeAccent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (matchResult.totalPoints / 36.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            matchResult.totalPoints >= 28.0 -> Color(0xFF2E7D32)
                            matchResult.totalPoints >= 18.0 -> OrangeAccent
                            else -> CrimsonError
                        },
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = matchResult.overallInterpretation,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8-Kootas Breakdown Title
            Text(
                text = "ASHTAKOOTA BREAKDOWN (अष्टकूट गुण विवरण)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaroonPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 8 Kootas list
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                matchResult.scores.forEach { koota ->
                    val isExpanded = expandedKootaName == koota.kootaName
                    KootaRowItem(
                        koota = koota,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedKootaName = if (isExpanded) null else koota.kootaName
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Manglik Dosha Analysis Card
            Surface(
                color = MaroonPrimary.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("♂️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Manglik Dosha (Kuja) Compatibility",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = matchResult.manglikCompatibilityVerdict,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Groom: ${matchResult.boyManglik.effectiveDoshaLevel}",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                        )
                        Text(
                            text = "Bride: ${matchResult.girlManglik.effectiveDoshaLevel}",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Non-deterministic Vedic Notice
            Surface(
                color = OrangeAccent.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Authentic Jyotish recognizes that mutual emotional maturity, shared dharmic values, and dedication outweigh pure numerical scores. Consult experienced astrologers for nuanced D9 Navamsa assessment.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = DarkTextSecondary,
                            lineHeight = 15.sp
                        )
                    )
                }
            }
        }
    }

    // Groom Quick Selector Dialog
    if (showBoySelector) {
        PartnerSelectorDialog(
            title = "Select Groom's Rashi & Nakshatra",
            selectedRashi = boyRashi,
            selectedNakshatra = boyNakshatra,
            selectedPada = boyPada,
            onDismiss = { showBoySelector = false },
            onConfirm = { r, n, p ->
                boyRashi = r
                boyNakshatra = n
                boyPada = p
                showBoySelector = false
            }
        )
    }

    // Bride Quick Selector Dialog
    if (showGirlSelector) {
        PartnerSelectorDialog(
            title = "Select Bride's Rashi & Nakshatra",
            selectedRashi = girlRashi,
            selectedNakshatra = girlNakshatra,
            selectedPada = girlPada,
            onDismiss = { showGirlSelector = false },
            onConfirm = { r, n, p ->
                girlRashi = r
                girlNakshatra = n
                girlPada = p
                showGirlSelector = false
            }
        )
    }
}

@Composable
private fun PartnerSummaryBox(
    label: String,
    rashi: Rashi,
    nakshatra: Nakshatra,
    pada: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = OrangeAccent,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ModernZodiacIcon(rashi = rashi, size = 18.dp)
                Text(
                    text = "${rashi.sanskritName} (${rashi.westernName})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = "${nakshatra.sanskritName} (Pada $pada)",
                style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
            )
        }
    }
}

@Composable
private fun KootaRowItem(
    koota: KootaScore,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Surface(
        onClick = onToggleExpand,
        color = if (koota.isAuspicious) Color(0xFF2E7D32).copy(alpha = 0.04f) else CrimsonError.copy(alpha = 0.04f),
        border = BorderStroke(
            1.dp,
            if (koota.isAuspicious) Color(0xFF2E7D32).copy(alpha = 0.3f) else CrimsonError.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = koota.sanskritName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = koota.details,
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", koota.pointsObtained)} / ${koota.maxPoints.toInt()}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (koota.isAuspicious) Color(0xFF2E7D32) else CrimsonError
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = DarkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(color = LightBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = koota.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartnerSelectorDialog(
    title: String,
    selectedRashi: Rashi,
    selectedNakshatra: Nakshatra,
    selectedPada: Int,
    onDismiss: () -> Unit,
    onConfirm: (Rashi, Nakshatra, Int) -> Unit
) {
    var tempRashi by remember { mutableStateOf(selectedRashi) }
    var tempNakshatra by remember { mutableStateOf(selectedNakshatra) }
    var tempPada by remember { mutableStateOf(selectedPada) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaroonPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Moon Sign (Rashi):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Rashi.values().forEach { rashi ->
                        FilterChip(
                            selected = tempRashi == rashi,
                            onClick = { tempRashi = rashi },
                            leadingIcon = { ModernZodiacIcon(rashi = rashi, size = 16.dp) },
                            label = { Text("${rashi.sanskritName} (${rashi.westernName})") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Moon Nakshatra:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Nakshatra.values().forEach { nak ->
                        FilterChip(
                            selected = tempNakshatra == nak,
                            onClick = { tempNakshatra = nak },
                            label = { Text(nak.sanskritName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Nakshatra Pada (Quarter):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..4).forEach { pada ->
                        FilterChip(
                            selected = tempPada == pada,
                            onClick = { tempPada = pada },
                            label = { Text("Pada $pada") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempRashi, tempNakshatra, tempPada) },
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White)
            ) {
                Text("Apply Match", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.past_readings_cancel))
            }
        }
    )
}
