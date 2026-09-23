package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engines.alerts.VedicAlertScheduler
import com.example.ui.theme.*

@Composable
fun VedicAlertsPreferenceCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var prefs by remember { mutableStateOf(VedicAlertScheduler.getPreferences(context)) }

    fun updatePrefs(newPrefs: VedicAlertScheduler.VedicAlertPreferences) {
        prefs = newPrefs
        VedicAlertScheduler.savePreferences(context, newPrefs)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("vedic_alerts_preference_card")
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaroonPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.vedic_alarms_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = stringResource(R.string.vedic_alarms_subtitle),
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 10.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Switches
            AlertToggleRow(
                title = stringResource(R.string.alert_brahma_muhurta),
                subtitle = "Pre-dawn alert 4:00 AM - 5:30 AM for Dhyana & Sadhana",
                checked = prefs.enableBrahmaMuhurta,
                onCheckedChange = { updatePrefs(prefs.copy(enableBrahmaMuhurta = it)) }
            )

            AlertToggleRow(
                title = stringResource(R.string.alert_rahu_kaal),
                subtitle = "Advance warning before daily inauspicious window begins",
                checked = prefs.enableRahuKaalWarning,
                onCheckedChange = { updatePrefs(prefs.copy(enableRahuKaalWarning = it)) }
            )

            AlertToggleRow(
                title = stringResource(R.string.alert_abhijit_muhurta),
                subtitle = "Midday victorious auspicious window around Solar Noon",
                checked = prefs.enableAbhijitMuhurta,
                onCheckedChange = { updatePrefs(prefs.copy(enableAbhijitMuhurta = it)) }
            )

            AlertToggleRow(
                title = stringResource(R.string.alert_ekadashi_vrats),
                subtitle = "Sacred Ekadashi, Purnima, Amavasya and Fasting Reminders",
                checked = prefs.enableEkadashiVrats,
                onCheckedChange = { updatePrefs(prefs.copy(enableEkadashiVrats = it)) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Lead Time Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alert Lead Time:",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(5, 15, 30).forEach { mins ->
                        val isSelected = prefs.leadTimeMinutes == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = { updatePrefs(prefs.copy(leadTimeMinutes = mins)) },
                            label = { Text("${mins}m prior", fontSize = 9.5.sp) },
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
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Instant Test Button
            OutlinedButton(
                onClick = {
                    VedicAlertScheduler.triggerInstantTestAlert(
                        context = context,
                        title = "🌅 Vedic Muhurta Test Alert",
                        message = "Your sacred notifications are active with high-priority channel alerts and vibration!"
                    )
                    Toast.makeText(context, "Test notification triggered!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaroonPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaroonPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Send Test Alert Notification",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaroonPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun AlertToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                color = DarkTextSecondary
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaroonPrimary,
                uncheckedTrackColor = LightBorder
            )
        )
    }
}
