package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Graha
import com.example.core.model.LocationData
import com.example.engines.astronomy.EclipseEvent
import com.example.engines.astronomy.SwissEphAdvancedFeatures
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
fun AdvancedFeaturesCard(
    currentZonedDateTime: ZonedDateTime,
    currentLocation: LocationData,
    modifier: Modifier = Modifier
) {
    val engine = remember { SwissEphAdvancedFeatures.getInstance() }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm") }

    var shaniTransit by remember { mutableStateOf<TransitEvent?>(null) }
    var guruTransit by remember { mutableStateOf<TransitEvent?>(null) }
    var solarEclipse by remember { mutableStateOf<EclipseEvent?>(null) }
    var lunarEclipse by remember { mutableStateOf<EclipseEvent?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val dateKey = currentZonedDateTime.toLocalDate()
    LaunchedEffect(dateKey, currentLocation) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val shani = engine.findNextRashiTransit(Graha.SHANI, currentZonedDateTime)
            val guru = engine.findNextRashiTransit(Graha.GURU, currentZonedDateTime)
            val sol = engine.findNextEclipse(currentLocation.latitude, currentLocation.longitude, 0.0, currentZonedDateTime, isLunar = false)
            val lun = engine.findNextEclipse(currentLocation.latitude, currentLocation.longitude, 0.0, currentZonedDateTime, isLunar = true)
            withContext(Dispatchers.Main) {
                shaniTransit = shani
                guruTransit = guru
                solarEclipse = sol
                lunarEclipse = lun
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
            Text(
                text = "ADVANCED ASTRONOMY (TRANSITS & ECLIPSES)",
                style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
            )
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
            } else {
                // Eclipses
                solarEclipse?.let { eclipse ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Next ${eclipse.eclipseType}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary))
                            Text(text = "Peak: ${eclipse.peakTime.format(timeFormatter)}", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                        }
                    }
                }

                lunarEclipse?.let { eclipse ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaroonPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Next ${eclipse.eclipseType}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary))
                            Text(text = "Peak: ${eclipse.peakTime.format(timeFormatter)}", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                        }
                    }
                }

                if (solarEclipse != null || lunarEclipse != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LightBorder)
                }

                // Transits
                shaniTransit?.let { transit ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(imageVector = Icons.Default.Explore, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Shani (Saturn) enters ${transit.targetRashi}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary))
                            Text(text = transit.transitTime.format(timeFormatter), style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                        }
                    }
                }

                guruTransit?.let { transit ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(imageVector = Icons.Default.Explore, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Guru (Jupiter) enters ${transit.targetRashi}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary))
                            Text(text = transit.transitTime.format(timeFormatter), style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                        }
                    }
                }
            }
        }
    }
}
