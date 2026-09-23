package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.BirthProfile
import com.example.core.model.Graha
import com.example.engines.astronomy.SwissEphAdvancedFeatures
import com.example.engines.astronomy.SwissEphGrahaSpashtaHelper
import com.example.engines.astronomy.TransitEvent
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TransitCalendarCard(
    currentZonedDateTime: ZonedDateTime,
    userBirthProfile: BirthProfile? = null,
    modifier: Modifier = Modifier
) {
    val engine = remember { SwissEphAdvancedFeatures.getInstance() }
    val spashtaHelper = remember { SwissEphGrahaSpashtaHelper() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }

    var transits by remember { mutableStateOf<List<TransitEvent>>(emptyList()) }
    var natalMoonRashiIndex by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val dateKey = currentZonedDateTime.toLocalDate()
    LaunchedEffect(dateKey, userBirthProfile) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val list = mutableListOf<TransitEvent>()
            listOf(Graha.GURU, Graha.SHANI, Graha.RAHU, Graha.KETU).forEach { graha ->
                val event = engine.findNextRashiTransit(graha, currentZonedDateTime)
                if (event != null && event.transitTime.isBefore(currentZonedDateTime.plusYears(2))) {
                    list.add(event)
                }
            }
            val sortedList = list.sortedBy { it.transitTime }

            val moonRashi = userBirthProfile?.let { profile ->
                try {
                    val zdt = ZonedDateTime.of(profile.birthDate, profile.birthTime, java.time.ZoneId.of(profile.birthLocation.timezoneId))
                    val spashtaList = spashtaHelper.calculateRealTimeGrahaSpashta(
                        latitude = profile.birthLocation.latitude,
                        longitude = profile.birthLocation.longitude,
                        altitudeMeters = 0.0,
                        dateTime = zdt
                    )
                    spashtaList.grahas.find { it.graha == Graha.CHANDRA }?.rashi?.number
                } catch (e: Exception) {
                    null
                }
            }

            withContext(Dispatchers.Main) {
                transits = sortedList
                natalMoonRashiIndex = moonRashi
                isLoading = false
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "UPCOMING PLANETARY TRANSITS (GOCHAR)",
                    style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = OrangeAccent
                    )
                }
            } else if (transits.isEmpty()) {
                Text(
                    text = "No major transits found in the near future.",
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                )
            } else {
                transits.forEachIndexed { index, transit ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${transit.graha.displayName} -> ${transit.targetRashi}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonPrimary
                                )
                            )
                            if (natalMoonRashiIndex != null) {
                                val houseFromMoon = (((com.example.core.model.Rashi.values().find { it.sanskritName == transit.targetRashi }?.number ?: 1) - natalMoonRashiIndex!! + 12) % 12) + 1
                                val suffix = if (houseFromMoon in 11..13) "th" else when (houseFromMoon % 10) { 1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th" }
                                Text(
                                    text = "Transit in ${houseFromMoon}${suffix} House from Natal Moon",
                                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 10.sp)
                                )
                            }
                        }
                        Text(
                            text = transit.transitTime.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    if (index < transits.lastIndex) {
                        HorizontalDivider(thickness = 0.5.dp, color = LightBorder.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}
