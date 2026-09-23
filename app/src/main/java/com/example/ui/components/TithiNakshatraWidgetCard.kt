package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun TithiNakshatraWidgetCard(
    dailyProfile: DailyVedicProfile,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Snapshot Widget",
                    style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AuspiciousGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "LIVE", color = AuspiciousGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tithi Snapshot Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(BorderStroke(1.dp, LightBorder), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(text = "Current Tithi", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dailyProfile.panchanga.tithi.name,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = dailyProfile.panchanga.tithi.paksha.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(color = OrangeAccent)
                        )
                    }
                }

                // Nakshatra Snapshot Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(BorderStroke(1.dp, LightBorder), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(text = "Current Nakshatra", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dailyProfile.panchanga.nakshatra.sanskritName,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Lord: ${dailyProfile.panchanga.nakshatra.rulingGraha.displayName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                        )
                    }
                }
            }
        }
    }
}
