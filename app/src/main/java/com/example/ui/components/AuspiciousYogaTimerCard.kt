package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.DailyVedicProfile
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

@Composable
fun AuspiciousYogaTimerCard(
    dailyProfile: DailyVedicProfile,
    modifier: Modifier = Modifier
) {
    var selectedDurationMinutes by remember { mutableStateOf(15) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var timeRemainingSeconds by remember { mutableStateOf(15 * 60) }
    var sessionCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning, timeRemainingSeconds) {
        if (isTimerRunning && timeRemainingSeconds > 0) {
            delay(1000L)
            timeRemainingSeconds--
        } else if (isTimerRunning && timeRemainingSeconds <= 0) {
            isTimerRunning = false
            sessionCompleted = true
        }
    }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, AuspiciousGreen),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Auspicious Yoga & Meditation Timer",
                    style = MaterialTheme.typography.titleMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AuspiciousGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "SHUBH MUHURAT", color = AuspiciousGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Optimal Window: ${dailyProfile.brahmaMuhurta.name} (${dailyProfile.brahmaMuhurta.startTime.format(timeFormatter)} - ${dailyProfile.brahmaMuhurta.endTime.format(timeFormatter)})",
                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!isTimerRunning && !sessionCompleted) {
                Text(
                    text = "Select Session Duration:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        FilterChip(
                            selected = selectedDurationMinutes == mins,
                            onClick = {
                                selectedDurationMinutes = mins
                                timeRemainingSeconds = mins * 60
                            },
                            label = { Text("$mins min") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaroonPrimary,
                                selectedLabelColor = OnMaroon
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaroonPrimary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val minutes = timeRemainingSeconds / 60
                    val seconds = timeRemainingSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = if (sessionCompleted) AuspiciousGreen else MaroonPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            sessionCompleted -> "Session Complete! Har Om Shanti ॐ"
                            isTimerRunning -> "Breathe in harmony with Brahma Muhurta..."
                            else -> "Ready to begin spiritual practice"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (sessionCompleted) AuspiciousGreen else DarkTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isTimerRunning) {
                    Button(
                        onClick = {
                            sessionCompleted = false
                            isTimerRunning = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AuspiciousGreen, contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (sessionCompleted) "Restart Session" else "Start Yoga / Meditation", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { isTimerRunning = false },
                        colors = ButtonDefaults.buttonColors(containerColor = InauspiciousRed, contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pause Session", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = {
                        isTimerRunning = false
                        sessionCompleted = false
                        timeRemainingSeconds = selectedDurationMinutes * 60
                    },
                    border = BorderStroke(1.dp, MaroonPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset", color = MaroonPrimary)
                }
            }
        }
    }
}
