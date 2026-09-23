package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.Graha
import com.example.engines.jyotisha.ShadbalaEngine
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun ShadbalaBreakdownCard(
    planetaryPositions: Map<Graha, Double>,
    lagnaDegrees: Double,
    isDayBirth: Boolean = true,
    modifier: Modifier = Modifier
) {
    val engine = remember { ShadbalaEngine() }
    val report = remember(planetaryPositions, lagnaDegrees, isDayBirth) {
        engine.calculateShadbala(
            planetaryPositions = planetaryPositions,
            lagnaDegrees = lagnaDegrees,
            isDayBirth = isDayBirth,
            moonPhaseWaxing = true
        )
    }

    var expandedGraha by remember { mutableStateOf<Graha?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Rankings & Rupa, 1: Six-Fold Sub-Balas

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("shadbala_breakdown_card")
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = GoldPrimary.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaroonPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.shadbala_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = stringResource(R.string.shadbala_subtitle),
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 10.sp)
                        )
                    }
                }

                Surface(
                    color = MaroonPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Parashari BPHS",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Summary box
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
                        text = report.astrologicalSummary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaroonPrimary,
                divider = { HorizontalDivider(color = LightBorder) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.shadbala_tab_rankings), fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.shadbala_tab_subbalas), fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                // Planetary Rankings and Progress bars
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    report.planets.forEach { item ->
                        val isExpanded = expandedGraha == item.graha
                        Surface(
                            color = if (item.isAuspicious) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (item.rank == 1) GoldPrimary else LightBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedGraha = if (isExpanded) null else item.graha
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Rank badge
                                        Surface(
                                            color = when (item.rank) {
                                                1 -> GoldPrimary
                                                2, 3 -> OrangeAccent
                                                else -> GrayText.copy(alpha = 0.2f)
                                            },
                                            shape = CircleShape,
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "#${item.rank}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (item.rank <= 3) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "${item.graha.symbol} ${item.graha.displayName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaroonPrimary
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${String.format(Locale.US, "%.2f", item.strength.totalRupas)} Rupas",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (item.isAuspicious) AuspiciousGreen else InauspiciousRed
                                            )
                                            Text(
                                                text = "${String.format(Locale.US, "%.0f", item.strength.totalVirupas)} / ${item.requiredVirupas.toInt()} req",
                                                fontSize = 10.sp,
                                                color = DarkTextSecondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = DarkTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Visual Progress Bar
                                LinearProgressIndicator(
                                    progress = { (item.strengthRatio / 1.5).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (item.strengthRatio >= 1.2) GoldPrimary else if (item.strengthRatio >= 1.0) AuspiciousGreen else OrangeAccent,
                                    trackColor = LightBorder
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.statusText,
                                        fontSize = 10.sp,
                                        color = if (item.isAuspicious) AuspiciousGreen else OrangeAccent,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.0f%%", item.strengthRatio * 100)} of Req. Virupas",
                                        fontSize = 10.sp,
                                        color = DarkTextSecondary
                                    )
                                }

                                // Expanded sub-details
                                AnimatedVisibility(visible = isExpanded) {
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        HorizontalDivider(color = LightBorder, thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "6-Fold Virupa Breakdown:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaroonPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        SubBalaRow("Sthanabala (Positional)", item.strength.sthanabala)
                                        SubBalaRow("Digbala (Directional)", item.strength.digbala)
                                        SubBalaRow("Kaalabala (Temporal)", item.strength.kaalabala)
                                        SubBalaRow("Cheshtabala (Motional)", item.strength.cheshtabala)
                                        SubBalaRow("Naisargikabala (Natural)", item.strength.naisargikabala)
                                        SubBalaRow("Drikbala (Aspectual)", item.strength.drikbala)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Sub-Balas matrix table
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    report.planets.forEach { item ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "${item.graha.symbol} ${item.graha.displayName} — Total: ${String.format(Locale.US, "%.1f", item.strength.totalVirupas)} Virupas (${String.format(Locale.US, "%.2f", item.strength.totalRupas)} Rupas)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaroonPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SubBalaMiniBadge("Sthana", item.strength.sthanabala)
                                    SubBalaMiniBadge("Dig", item.strength.digbala)
                                    SubBalaMiniBadge("Kaala", item.strength.kaalabala)
                                    SubBalaMiniBadge("Cheshta", item.strength.cheshtabala)
                                    SubBalaMiniBadge("Naisarg", item.strength.naisargikabala)
                                    SubBalaMiniBadge("Drik", item.strength.drikbala)
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
private fun SubBalaRow(name: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name, fontSize = 10.sp, color = DarkTextSecondary)
        Text("${String.format(Locale.US, "%.1f", value)} V", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SubBalaMiniBadge(label: String, value: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = DarkTextSecondary)
        Text(
            text = String.format(Locale.US, "%.0f", value),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaroonPrimary
        )
    }
}
