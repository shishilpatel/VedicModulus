package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.DailyVedicProfile
import com.example.engines.alerts.VedicAlarmStatus
import com.example.engines.alerts.VedicSilentAlarmManager
import com.example.engines.alerts.VedicTimeMarker
import com.example.ui.theme.*

@Composable
fun VedicSilentAlarmsCard(
    dailyProfile: DailyVedicProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var alarmStatuses by remember(dailyProfile) {
        mutableStateOf(VedicSilentAlarmManager.getAlarmStatuses(context, dailyProfile))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("vedic_silent_alarms_card")
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
                            .background(OrangeAccent.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "VEDIC SILENT VIBRATION ALARMS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Text(
                            text = "Discreet Haptics for Sacred Celestial Transitions",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    color = MaroonPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Phone & Watch",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "Receive whisper-quiet haptic pulses on your mobile phone and connected Wear OS smartwatch at exact astronomical moments without playing audible rings during meditation or meetings.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            )

            // Alarm Toggles List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                alarmStatuses.forEach { status ->
                    VedicAlarmToggleRow(
                        status = status,
                        onToggle = { isChecked ->
                            VedicSilentAlarmManager.setAlarmEnabled(context, status.marker, isChecked, dailyProfile)
                            alarmStatuses = VedicSilentAlarmManager.getAlarmStatuses(context, dailyProfile)
                        },
                        onTestHaptic = {
                            VedicSilentAlarmManager.testSilentVibration(context, status.marker)
                            Toast.makeText(context, "Haptic wave sent for ${status.marker.title}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VedicAlarmToggleRow(
    status: VedicAlarmStatus,
    onToggle: (Boolean) -> Unit,
    onTestHaptic: () -> Unit
) {
    Surface(
        color = if (status.isEnabled) MaroonPrimary.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(
            0.5.dp,
            if (status.isEnabled) MaroonPrimary.copy(alpha = 0.25f) else LightBorder
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = status.marker.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )
                    Text(
                        text = status.marker.devanagari,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OrangeAccent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = OrangeAccent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = status.targetTimeFormatted,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = status.marker.description,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DarkTextSecondary,
                            fontSize = 9.5.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onTestHaptic,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Test Haptic Vibration",
                        tint = OrangeAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Switch(
                    checked = status.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaroonPrimary,
                        uncheckedTrackColor = LightBorder
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}
