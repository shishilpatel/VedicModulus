package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.Graha
import com.example.core.model.Rashi
import com.example.engines.jyotisha.AshtakavargaEngine
import com.example.ui.theme.*

@Composable
fun AshtakavargaHeatmapCard(
    planetaryPositions: Map<Graha, Double>,
    lagnaDegrees: Double,
    modifier: Modifier = Modifier
) {
    val engine = remember { AshtakavargaEngine() }
    val report = remember(planetaryPositions, lagnaDegrees) {
        engine.calculateAshtakavarga(planetaryPositions, lagnaDegrees)
    }

    var selectedViewMode by remember { mutableStateOf(0) } // 0: Sarvashtakavarga (SAV), 1: Bhinna (BAV)
    var selectedBavGraha by remember { mutableStateOf(Graha.GURU) }
    var selectedHouse by remember { mutableStateOf(1) }

    val allRashis = Rashi.values().toList()
    val lagnaRashiIdx = (lagnaDegrees / 30.0).toInt().coerceIn(0, 11)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ashtakavarga_heatmap_card")
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
                        text = stringResource(R.string.ashtakavarga_title),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = stringResource(R.string.ashtakavarga_subtitle),
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 10.sp)
                    )
                }

                Surface(
                    color = AuspiciousGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, AuspiciousGreen)
                ) {
                    Text(
                        text = "Total 337 Bindus",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuspiciousGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Guidance banner
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = report.astrologicalGuidance,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // View Mode Selector (SAV vs BAV)
            TabRow(
                selectedTabIndex = selectedViewMode,
                containerColor = Color.Transparent,
                contentColor = MaroonPrimary,
                divider = { HorizontalDivider(color = LightBorder) }
            ) {
                Tab(
                    selected = selectedViewMode == 0,
                    onClick = { selectedViewMode = 0 },
                    text = { Text(stringResource(R.string.sav_total_matrix), fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedViewMode == 1,
                    onClick = { selectedViewMode = 1 },
                    text = { Text(stringResource(R.string.bav_planet_charts), fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedViewMode == 0) {
                // SAV 12-House Heatmap Grid (4 rows x 3 cols or 3 rows x 4 cols)
                Text(
                    text = "Tap any house to inspect Sarvashtakavarga potency:",
                    fontSize = 11.sp,
                    color = DarkTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (col in 0..3) {
                                val houseNum = row * 4 + col + 1
                                val rashiIdx = (lagnaRashiIdx + (houseNum - 1)) % 12
                                val rashi = allRashis[rashiIdx]
                                val bindus = report.sarvashtakavarga.bindusPerHouse[houseNum] ?: 28
                                val isSelected = selectedHouse == houseNum

                                val bgColor = when {
                                    bindus >= 32 -> AuspiciousGreen.copy(alpha = 0.25f)
                                    bindus >= 28 -> GoldPrimary.copy(alpha = 0.22f)
                                    bindus >= 25 -> OrangeAccent.copy(alpha = 0.18f)
                                    else -> InauspiciousRed.copy(alpha = 0.18f)
                                }

                                val borderColor = if (isSelected) MaroonPrimary else when {
                                    bindus >= 30 -> AuspiciousGreen
                                    bindus >= 28 -> GoldPrimary
                                    bindus >= 25 -> OrangeAccent
                                    else -> InauspiciousRed
                                }

                                Surface(
                                    color = bgColor,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedHouse = houseNum }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "H$houseNum",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaroonPrimary
                                        )
                                        Text(
                                            text = rashi.sanskritName.take(3),
                                            fontSize = 9.sp,
                                            color = DarkTextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "$bindus",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = if (bindus >= 28) AuspiciousGreen else if (bindus >= 25) OrangeAccent else InauspiciousRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(AuspiciousGreen, ">=30 (Exalted)")
                    LegendItem(GoldPrimary, "28-29 (Auspicious)")
                    LegendItem(OrangeAccent, "25-27 (Average)")
                    LegendItem(InauspiciousRed, "<25 (Weak)")
                }

                // Selected House Deep Detail
                val curBindus = report.sarvashtakavarga.bindusPerHouse[selectedHouse] ?: 28
                val curRashiIdx = (lagnaRashiIdx + (selectedHouse - 1)) % 12
                val curRashi = allRashis[curRashiIdx]

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "House $selectedHouse (${curRashi.sanskritName}) Analysis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaroonPrimary
                            )
                            Text(
                                text = "$curBindus Bindus",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (curBindus >= 28) AuspiciousGreen else OrangeAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (curBindus >= 30) {
                                "Supreme cosmic strength. Transits through House $selectedHouse deliver extraordinary fruition, success, and protection against challenges."
                            } else if (curBindus >= 28) {
                                "Auspicious strength. Yields favorable results during benefic transits and supports constructive life initiatives."
                            } else if (curBindus >= 25) {
                                "Moderate vitality. Neutral stability requiring disciplined effort to yield consistent rewards."
                            } else {
                                "Challenging Bindu score. Inauspicious or malefic planetary transits here may require mindful remedies and patience."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

            } else {
                // Bhinna Ashtakavarga (BAV) Planet Selector & Matrix
                val sevenGrahas = listOf(
                    Graha.SURYA, Graha.CHANDRA, Graha.MANGALA,
                    Graha.BUDHA, Graha.GURU, Graha.SHUKRA, Graha.SHANI
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sevenGrahas.forEach { graha ->
                        val isSelected = graha == selectedBavGraha
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedBavGraha = graha },
                            label = { Text("${graha.symbol} ${graha.displayName}", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaroonPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = MaroonPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MaroonPrimary else LightBorder
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val bav = report.bhinnaAshtakavargas[selectedBavGraha]
                if (bav != null) {
                    Text(
                        text = "${selectedBavGraha.displayName} Bhinnashtakavarga (Total: ${bav.totalBindus} Bindus / Max 64)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaroonPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (row in 0..2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (col in 0..3) {
                                    val rashiIdx = row * 4 + col
                                    val rashi = allRashis[rashiIdx]
                                    val pts = bav.bindusPerRashi[rashi] ?: 0

                                    Surface(
                                        color = if (pts >= 5) AuspiciousGreen.copy(alpha = 0.15f) else if (pts >= 4) MaterialTheme.colorScheme.surfaceVariant else InauspiciousRed.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(0.5.dp, if (pts >= 5) AuspiciousGreen else LightBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(rashi.sanskritName.take(3), fontSize = 10.sp, color = DarkTextSecondary)
                                            Text(
                                                text = "$pts / 8",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (pts >= 5) AuspiciousGreen else if (pts >= 4) MaterialTheme.colorScheme.onSurface else InauspiciousRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = color, shape = CircleShape, modifier = Modifier.size(8.dp)) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, color = DarkTextSecondary)
    }
}
