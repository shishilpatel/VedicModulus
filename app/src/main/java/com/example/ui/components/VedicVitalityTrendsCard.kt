package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.vitality.DailyVitalityScoreSummary
import com.example.data.vitality.VedicVitalityService
import com.example.data.vitality.VitalityTrendPoint
import com.example.ui.theme.*

@Composable
fun VedicVitalityTrendsCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vitalityService = remember { VedicVitalityService.getInstance(context) }
    val summary by vitalityService.currentVitalitySummary.collectAsState()
    val weeklyTrends by vitalityService.weeklyTrends.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("vedic_vitality_trends_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaroonPrimary.copy(alpha = 0.12f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = MaroonPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "VEDIC VITALITY & BIOMETRICS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Text(
                            text = "Wear OS Prana & Heart Telemetry",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Text(
                            text = "Wear Synced",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Main Vitality Meter Score Box
            Surface(
                color = MaroonPrimary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Overall Prana Vitality",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = DarkTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${summary.overallVitalityScore}/100",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = summary.vedicHealthGuidance,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Circular Score Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(OrangeAccent.copy(alpha = 0.25f), MaroonPrimary.copy(alpha = 0.05f))
                                ),
                                shape = CircleShape
                            )
                            .border(3.dp, OrangeAccent, CircleShape)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = InauspiciousRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${summary.averageHeartRate} bpm",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaroonPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Tri-Dosha Biometric Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Steps / Prana Box
                VitalityMetricBox(
                    label = "PRANA (Steps)",
                    value = "${summary.totalSteps}",
                    subtext = "Goal: 8.5k (${summary.pranaScore}%)",
                    icon = Icons.Default.DirectionsWalk,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )

                // Tejas (Heart Rate) Box
                VitalityMetricBox(
                    label = "TEJAS (Heart)",
                    value = "${summary.restingHeartRate} bpm",
                    subtext = "Resting: Optimal",
                    icon = Icons.Default.FavoriteBorder,
                    color = InauspiciousRed,
                    modifier = Modifier.weight(1f)
                )

                // Ojas (Harmony) Box
                VitalityMetricBox(
                    label = "OJAS (Sattva)",
                    value = "${summary.circadianHarmonyPercent}%",
                    subtext = "Circadian Rhythm",
                    icon = Icons.Default.Spa,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }

            // Weekly Trend Bar Chart
            if (weeklyTrends.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "7-Day Periodic Vitality Trends",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OrangeAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyTrends.forEach { trend ->
                            val animHeight by animateFloatAsState(
                                targetValue = (trend.vitalityScore / 100f).coerceIn(0.15f, 1f),
                                animationSpec = tween(600),
                                label = "bar_anim"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${trend.vitalityScore}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaroonPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .fillMaxHeight(animHeight * 0.68f)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (trend.dayLabel == "Today") OrangeAccent else MaroonPrimary.copy(alpha = 0.7f)
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = trend.dayLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = if (trend.dayLabel == "Today") OrangeAccent else DarkTextSecondary,
                                        fontWeight = if (trend.dayLabel == "Today") FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VitalityMetricBox(
    label: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaroonPrimary
                ),
                maxLines = 1
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 7.5.sp,
                    color = DarkTextSecondary
                ),
                maxLines = 1
            )
        }
    }
}
