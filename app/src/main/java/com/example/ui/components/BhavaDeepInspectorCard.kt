package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.KundaliChartData
import com.example.engines.jyotisha.BhavaDeepAnalysis
import com.example.engines.jyotisha.KundaliAnalysisEngine
import com.example.engines.jyotisha.PlanetaryDignity
import com.example.ui.theme.*

@Composable
fun BhavaDeepInspectorCard(
    selectedHouseNum: Int,
    chartData: KundaliChartData,
    transitPlanets: Map<com.example.core.model.Graha, Double> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val analysis = remember(selectedHouseNum, chartData, transitPlanets) {
        KundaliAnalysisEngine.analyzeBhava(selectedHouseNum, chartData, transitPlanets)
    }
    val bhava = analysis.bhavaDetail

    var showAspectsDetails by remember { mutableStateOf(true) }
    var showDignityDetails by remember { mutableStateOf(true) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            // House Title & Sign Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BHAVA $selectedHouseNum: ${bhava.traditionalName}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${bhava.devanagariName} • ${bhava.significations}",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 10.sp)
                    )
                }

                Surface(
                    color = MaroonPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.4f))
                ) {
                    val rashiElement = when (bhava.rashi) {
                        com.example.core.model.Rashi.MESHA, com.example.core.model.Rashi.SIMHA, com.example.core.model.Rashi.DHANUS -> "Agni (Fire)"
                        com.example.core.model.Rashi.VRISHABHA, com.example.core.model.Rashi.KANYA, com.example.core.model.Rashi.MAKARA -> "Prithvi (Earth)"
                        com.example.core.model.Rashi.MITHUNA, com.example.core.model.Rashi.TULA, com.example.core.model.Rashi.KUMBHA -> "Vayu (Air)"
                        com.example.core.model.Rashi.KARKATA, com.example.core.model.Rashi.VRISHCHIKA, com.example.core.model.Rashi.MEENA -> "Jala (Water)"
                    }
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${bhava.rashi.sanskritName} (${bhava.rashi.number})",
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                        Text(
                            text = rashiElement,
                            color = OrangeAccent,
                            fontSize = 8.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 1. House Lord (Bhavesha) Placement
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HOUSE LORD (भावेश):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GrayText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp
                            )
                        )
                        Text(
                            text = "${analysis.houseLord.displayName} (${analysis.houseLord.symbol})",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "SEATED IN:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GrayText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp
                            )
                        )
                        Text(
                            text = "House ${analysis.lordPlacedInHouse} (${analysis.lordPlacementSign.sanskritName})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Occupants & Planetary Dignity (Uchcha / Neecha / Sva-kshetra)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Occupying Planets & Dignity:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaroonPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                )
                IconButton(
                    onClick = { showDignityDetails = !showDignityDetails },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (showDignityDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Dignity Details",
                        tint = DarkTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showDignityDetails) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (analysis.occupants.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Empty House (शुद्ध भाव) — Influenced primarily by Lord ${analysis.houseLord.displayName} and receiving planetary aspects.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GrayText,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontSize = 10.5.sp
                                ),
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    } else {
                        analysis.occupants.forEach { occ ->
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when (occ.dignity) {
                                        PlanetaryDignity.EXALTED -> AuspiciousGreen
                                        PlanetaryDignity.DEBILITATED -> InauspiciousRed
                                        PlanetaryDignity.OWN_SIGN -> GoldPrimary
                                        else -> LightBorder
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${occ.graha.symbol} ${occ.graha.displayName}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaroonPrimary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${String.format(java.util.Locale.US, "%.1f°", occ.degreeInRashi)}",
                                                color = GrayText,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                        Text(
                                            text = occ.note,
                                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 9.5.sp)
                                        )
                                    }

                                    Surface(
                                        color = when (occ.dignity) {
                                            PlanetaryDignity.EXALTED -> AuspiciousGreen.copy(alpha = 0.15f)
                                            PlanetaryDignity.DEBILITATED -> InauspiciousRed.copy(alpha = 0.15f)
                                            PlanetaryDignity.OWN_SIGN -> GoldPrimary.copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = occ.dignity.displayName,
                                            color = when (occ.dignity) {
                                                PlanetaryDignity.EXALTED -> AuspiciousGreen
                                                PlanetaryDignity.DEBILITATED -> InauspiciousRed
                                                PlanetaryDignity.OWN_SIGN -> MaroonPrimary
                                                else -> DarkTextSecondary
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Planetary Aspects (Drishti) Received
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aspects Received (दृष्टि):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaroonPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                )
                IconButton(
                    onClick = { showAspectsDetails = !showAspectsDetails },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (showAspectsDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Aspects Details",
                        tint = DarkTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showAspectsDetails) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (analysis.aspectsReceived.isEmpty()) {
                        Text(
                            text = "No direct major planetary Drishti on this Bhava.",
                            style = MaterialTheme.typography.labelSmall.copy(color = GrayText, fontSize = 10.sp)
                        )
                    } else {
                        analysis.aspectsReceived.forEach { asp ->
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, LightBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${asp.castingGraha.symbol} ${asp.castingGraha.displayName} from House ${asp.fromHouse}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = asp.aspectType,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = GoldPrimary,
                                                fontSize = 9.5.sp
                                            )
                                        )
                                    }
                                    Text(
                                        text = asp.influenceQuality,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (asp.influenceQuality.contains("Grace") || asp.influenceQuality.contains("Benefic")) AuspiciousGreen else OrangeAccent,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 9.5.sp,
                                            textAlign = TextAlign.End
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Current Transit (Gochar) Influences
            Surface(
                color = GoldPrimary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TODAY'S TRANSIT (गोचर):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp
                            )
                        )
                        val transitText = if (analysis.transitOccupants.isEmpty()) {
                            "No major grahas currently transiting ${bhava.rashi.sanskritName}"
                        } else {
                            analysis.transitOccupants.joinToString(", ") { "${it.displayName} ${it.symbol}" } + " transiting here today"
                        }
                        Text(
                            text = transitText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
