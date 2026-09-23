package com.example.ui.screens.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engines.alerts.VedicAlertEngine
import androidx.compose.material.icons.filled.Vibration
import com.example.core.model.MuhurtaDeity
import com.example.core.model.DailyVedicProfile
import com.example.core.model.TimingWindow
import com.example.engines.panchanga.MuhurtaReminderHelper
import com.example.ui.components.PujaScheduleSection
import com.example.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun MuhurtasTab(
    dailyProfile: DailyVedicProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }
    val alertEngine = remember { VedicAlertEngine(context) }
    var enableRahuKaalAlert by remember { mutableStateOf(alertEngine.enableRahuKaalAlert) }
    var enableSandhyaAlert by remember { mutableStateOf(alertEngine.enableSandhyaAlert) }
    var enableBrahmaAlert by remember { mutableStateOf(alertEngine.enableBrahmaMuhurtaAlert) }
    var notificationScheduled by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Sacred & Auspicious Muhurtas",
            style = MaterialTheme.typography.titleMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Haptic Alerts Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Vibration, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rahu Kaal & Sandhya Haptic Alerts",
                        style = MaterialTheme.typography.titleSmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Receive background alarms with custom tactile haptic patterns (warning pulses for Rahu Kaal; gentle meditative triple-pulse for Sandhya).",
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Toggle 1: Rahu Kaal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rahu Kaal Warning", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = InauspiciousRed))
                        Text("Vibrates 5 min before start (${dailyProfile.rahuKalam.startTime.format(timeFormatter)})", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                    }
                    Switch(
                        checked = enableRahuKaalAlert,
                        onCheckedChange = {
                            enableRahuKaalAlert = it
                            alertEngine.enableRahuKaalAlert = it
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Toggle 2: Sandhya (Prataha & Sayam)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sandhya Twilight Alerts", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = OrangeAccent))
                        Text("Dawn & Dusk meditative harmonic pulse", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                    }
                    Switch(
                        checked = enableSandhyaAlert,
                        onCheckedChange = {
                            enableSandhyaAlert = it
                            alertEngine.enableSandhyaAlert = it
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Toggle 3: Brahma Muhurta
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Brahma Muhurta (04:24 AM)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = AuspiciousGreen))
                        Text("Pre-dawn sacred meditation alarm", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                    }
                    Switch(
                        checked = enableBrahmaAlert,
                        onCheckedChange = {
                            enableBrahmaAlert = it
                            alertEngine.enableBrahmaMuhurtaAlert = it
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        alertEngine.scheduleAlertsForDay(dailyProfile)
                        notificationScheduled = true
                        snackbarMessage = "✓ Successfully armed Haptic Vedic Alarms: Rahu Kaal ($enableRahuKaalAlert), Sandhya ($enableSandhyaAlert), Brahma Muhurta ($enableBrahmaAlert)."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (notificationScheduled) AuspiciousGreen else MaroonPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (notificationScheduled) "Haptic Alerts Armed ✓" else "Arm Selected Haptic Alarms")
                }
            }
        }

        snackbarMessage?.let { msg ->
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = AuspiciousGreen.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, AuspiciousGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    color = AuspiciousGreen,
                    modifier = Modifier.padding(10.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Brahma Muhurta
        TimingCard(
            window = dailyProfile.brahmaMuhurta,
            timeFormatter = timeFormatter,
            description = "Peak sattva hour (approx. 1h 36m prior to sunrise). Favorable for meditation, japa, deep creative insight and pranayama."
        )

        // Abhijit Muhurta
        dailyProfile.abhijitMuhurta?.let { abhijit ->
            Spacer(modifier = Modifier.height(6.dp))
            TimingCard(
                window = abhijit,
                timeFormatter = timeFormatter,
                description = "Midday solar alignment that dissolves afflictions for new beginnings, business negotiations and journeys."
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Inauspicious Daily Intervals (Doshas)",
            style = MaterialTheme.typography.titleMedium.copy(color = InauspiciousRed, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))

        TimingCard(
            window = dailyProfile.rahuKalam,
            timeFormatter = timeFormatter,
            description = "Ruled by Rahu. Inauspicious for initiating auspicious ventures, contracts, financial investments or travel."
        )

        Spacer(modifier = Modifier.height(6.dp))

        TimingCard(
            window = dailyProfile.yamaganda,
            timeFormatter = timeFormatter,
            description = "Ruled by Yamaraj / Ketu. Favorable only for spiritual detachment and ending lingering commitments."
        )

        Spacer(modifier = Modifier.height(6.dp))

        TimingCard(
            window = dailyProfile.gulikaKalam,
            timeFormatter = timeFormatter,
            description = "Ruled by Gulika (son of Saturn). Any action started in this period repeats indefinitely."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Muhurta Wisdom Guidelines
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Vedic Muhurta Principles",
                    style = MaterialTheme.typography.titleSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Rahu Kalam occurs daily for 1/8th of daytime, its position rotating systematically through the 7 days of the week.\n• Abhijit Muhurta is the 8th Muhurta of the daytime, centered exactly at Solar Noon.\n• While Rahu Kalam demands caution for worldly initiatives, it remains suitable for Rahu Shanti pooja and internal reflection.",
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Puja Schedule & Vrata Manager
        PujaScheduleSection(referenceDate = dailyProfile.currentZonedDateTime)
    }
}

@Composable
private fun TimingCard(
    window: TimingWindow,
    timeFormatter: DateTimeFormatter,
    description: String
) {
    val context = LocalContext.current
    val accent = if (window.isAuspicious) AuspiciousGreen else InauspiciousRed
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(window.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Text(window.devanagari, color = MaroonPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${window.startTime.format(timeFormatter)} – ${window.endTime.format(timeFormatter)}",
                color = accent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = GrayText,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Affordances: Calendar Sync & 15-Min Advance Reminder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        MuhurtaReminderHelper.scheduleAdvanceReminder(
                            context = context,
                            title = window.name,
                            startZoned = window.startTime,
                            leadMinutes = 15
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (window.isAuspicious) AuspiciousGreen else InauspiciousRed
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (window.isAuspicious) "Remind (15m)" else "Warning (15m)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        MuhurtaReminderHelper.addToSystemCalendar(
                            context = context,
                            title = "${window.name} (${window.devanagari})",
                            description = description,
                            startZoned = window.startTime,
                            endZoned = window.endTime
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Calendar", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }
}
