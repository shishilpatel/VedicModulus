package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.DailyVedicProfile
import com.example.ui.theme.*
import java.time.LocalTime

@Composable
fun VedicTimelineCard(
    dailyProfile: DailyVedicProfile,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Daily Vedic Hour Timeline",
                style = MaterialTheme.typography.titleMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Color-coded hours (Auspicious Shubh, Neutral, Challenging Rahu Kaal)",
                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 24 Hour blocks representation (6 AM through next 5 AM)
            val hours = (6..29).map { it % 24 }
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Group into rows of 6 hours
                hours.chunked(6).forEach { rowHours ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowHours.forEach { hour ->
                            val hourTime = LocalTime.of(hour, 0)
                            val isRahu = isTimeInRange(hourTime, dailyProfile.rahuKalam.startTime.toLocalTime(), dailyProfile.rahuKalam.endTime.toLocalTime())
                            val isYamaganda = isTimeInRange(hourTime, dailyProfile.yamaganda.startTime.toLocalTime(), dailyProfile.yamaganda.endTime.toLocalTime())
                            val isBrahma = isTimeInRange(hourTime, dailyProfile.brahmaMuhurta.startTime.toLocalTime(), dailyProfile.brahmaMuhurta.endTime.toLocalTime())
                            val isAbhijit = dailyProfile.abhijitMuhurta?.let { isTimeInRange(hourTime, it.startTime.toLocalTime(), it.endTime.toLocalTime()) } == true

                            val (bgColor, label) = when {
                                isRahu || isYamaganda -> InauspiciousRed.copy(alpha = 0.8f) to "Challenging"
                                isBrahma || isAbhijit -> AuspiciousGreen.copy(alpha = 0.8f) to "Auspicious"
                                else -> MaroonPrimary.copy(alpha = 0.2f) to "Neutral"
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bgColor)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format("%02d:00", hour),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (bgColor == MaroonPrimary.copy(alpha = 0.2f)) MaroonPrimary else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = label.take(4),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (bgColor == MaroonPrimary.copy(alpha = 0.2f)) DarkTextSecondary else Color.White.copy(alpha = 0.9f),
                                            fontSize = 8.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem(color = AuspiciousGreen, label = "Auspicious (Shubh)")
                LegendItem(color = MaroonPrimary.copy(alpha = 0.3f), label = "Neutral")
                LegendItem(color = InauspiciousRed, label = "Challenging / Rahu")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = DarkTextSecondary))
    }
}

private fun isTimeInRange(time: LocalTime, start: LocalTime, end: LocalTime): Boolean {
    return if (start.isBefore(end)) {
        !time.isBefore(start) && !time.isAfter(end)
    } else {
        !time.isBefore(start) || !time.isAfter(end)
    }
}
