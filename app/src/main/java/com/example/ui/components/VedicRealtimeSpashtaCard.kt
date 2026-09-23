package com.example.ui.components
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.getValue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Graha
import com.example.core.model.LocationData
import com.example.engines.astronomy.AyanamsaType
import com.example.engines.astronomy.GrahaSpashta
import com.example.engines.astronomy.PlanetDignity
import com.example.engines.astronomy.RealTimeGrahaSpashtaReport
import com.example.engines.astronomy.SwissEphGrahaSpashtaHelper
import com.example.ui.theme.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Material 3 Card displaying real-time planetary positions (Graha Spashta)
 * calculated using the Swiss Ephemeris (swisseph) library for the user's current location.
 */
@Composable
fun VedicRealtimeSpashtaCard(
    currentLocation: LocationData,
    currentZonedDateTime: ZonedDateTime,
    modifier: Modifier = Modifier
) {
    val helper = remember { SwissEphGrahaSpashtaHelper.getInstance() }

    var isExpanded by remember { mutableStateOf(false) }
    var selectedAyanamsa by remember { mutableStateOf(AyanamsaType.LAHIRI) }
    var showAllDetails by remember { mutableStateOf(false) }
    var showBhavaChalit by remember { mutableStateOf(false) }
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    val reportState = remember { mutableStateOf<RealTimeGrahaSpashtaReport?>(null) }
    val report = reportState.value
    LaunchedEffect(currentLocation, currentZonedDateTime.minute, selectedAyanamsa) {
        reportState.value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            helper.calculateRealTimeGrahaSpashta(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                altitudeMeters = 0.0,
                dateTime = currentZonedDateTime,
                ayanamshaMode = selectedAyanamsa.sweConst
            )
        }
    }
    
    if (report == null) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp),
            modifier = modifier.fillMaxWidth().height(200.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaroonPrimary)
            }
        }
        return
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title & Engine Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = MaroonPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "REAL-TIME GRAHA SPASHTA",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaroonPrimary
                            )
                        )
                        Text(
                            text = "Vedic Planetary Longitudes (ग्रह स्पष्ट)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Surface(
                    color = AuspiciousGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, AuspiciousGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "LIVE ALIGNMENT",
                        color = AuspiciousGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ayanamsha: ${report.ayanamshaDms} (${selectedAyanamsa.displayName})",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextSecondary,
                        fontSize = 11.sp
                    )
                )
                
                TextButton(onClick = { 
                    selectedAyanamsa = if (selectedAyanamsa == AyanamsaType.LAHIRI) AyanamsaType.RAMAN else if (selectedAyanamsa == AyanamsaType.RAMAN) AyanamsaType.KP else AyanamsaType.LAHIRI 
                }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                    Text("Change", style = MaterialTheme.typography.labelSmall)
                }
            }

            Text(
                text = "Topocentric Calculations • ${currentLocation.cityName}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkTextSecondary,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Lagna (Ascendant) Banner
            Surface(
                color = MaroonPrimary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CURRENT LAGNA (लग्न)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = "${report.lagna.rashi.westernName} (${report.lagna.rashi.sanskritName})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaroonPrimary
                            )
                        )
                        Text(
                            text = "${report.lagna.nakshatra.sanskritName} • Pada ${report.lagna.nakshatraPada}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = report.lagna.dmsFormatted,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaroonPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = "Ascendant Cusp",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlight Sun & Moon in Quick View
            val surya = report.getGraha(Graha.SURYA)
            val chandra = report.getGraha(Graha.CHANDRA)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                surya?.let {
                    CompactGrahaCard(
                        spashta = it,
                        title = "SURYA (SUN)",
                        modifier = Modifier.weight(1f)
                    )
                }
                chandra?.let {
                    CompactGrahaCard(
                        spashta = it,
                        title = "CHANDRA (MOON)",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expand / Collapse All 9 Navagrahas
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isExpanded) "Hide All 9 Grahas" else "View All 9 Grahas (Spashta Degrees, Dignity & Bhavas)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaroonPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    report.grahas.forEach { grahaSpashta ->
                        GrahaSpashtaRow(spashta = grahaSpashta)
                        HorizontalDivider(thickness = 0.5.dp, color = LightBorder.copy(alpha = 0.6f))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(
                        onClick = { showBhavaChalit = !showBhavaChalit },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (showBhavaChalit) "Hide Bhava Chalit (Shripati Cusps)" else "Show Bhava Chalit (Shripati Cusps)",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaroonPrimary)
                        )
                    }
                    
                    AnimatedVisibility(visible = showBhavaChalit) {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            report.bhavaCusps.forEachIndexed { index, cuspLon ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Bhava ${index + 1}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        text = "${SwissEphGrahaSpashtaHelper.rashiFromDegrees(cuspLon).sanskritName} ${SwissEphGrahaSpashtaHelper.formatDms(cuspLon % 30.0)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                    )
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = LightBorder.copy(alpha = 0.6f))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "℞: Vakri (Retrograde) • 🔥: Asta (Combust by Sun)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = DarkTextSecondary
                            )
                        )
                        Text(
                            text = "Time: ${report.dateTime.format(timeFormatter)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = DarkTextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactGrahaCard(
    spashta: GrahaSpashta,
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, LightBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier
                        .size(6.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (spashta.isRetrograde) InauspiciousRed else OrangeAccent)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary,
                            fontSize = 10.sp
                        )
                    )
                }
                if (spashta.isRetrograde) {
                    Text(
                        text = "VAKRI ℞",
                        color = InauspiciousRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${spashta.rashi.sanskritName} ${spashta.dmsFormatted}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaroonPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            )
            Text(
                text = "${spashta.nakshatra.sanskritName} (Pada ${spashta.nakshatraPada}) • Bhava ${spashta.bhava}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkTextSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun GrahaSpashtaRow(spashta: GrahaSpashta) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Graha Name & Sanskrit
        Column(modifier = Modifier.weight(1.2f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Indicator Dot
                Box(modifier = Modifier
                    .size(6.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (spashta.isRetrograde) InauspiciousRed else OrangeAccent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                
                Text(
                    text = spashta.graha.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                )
                if (spashta.isRetrograde) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "℞",
                        color = InauspiciousRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                if (spashta.isCombust) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "🔥",
                        fontSize = 11.sp
                    )
                }
            }
            Text(
                text = "${spashta.nakshatra.sanskritName} • Pada ${spashta.nakshatraPada}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkTextSecondary,
                    fontSize = 10.sp
                )
            )
        }

        // Rashi & Degrees
        Column(modifier = Modifier.weight(1.4f)) {
            Text(
                text = "${spashta.rashi.sanskritName} ${spashta.dmsFormatted}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaroonPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            )
            Text(
                text = "Motion: ${if (spashta.isRetrograde) "Retrograde" else "Direct"} (${String.format(Locale.US, "%.2f°/d", spashta.speedDegPerDay)})",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkTextSecondary,
                    fontSize = 9.sp
                )
            )
        }

        // Bhava & Dignity
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1.2f)
        ) {
            Surface(
                color = when (spashta.dignity) {
                    PlanetDignity.UCHHA, PlanetDignity.MOOLATRIKONA -> AuspiciousGreen.copy(alpha = 0.15f)
                    PlanetDignity.NEECHA -> InauspiciousRed.copy(alpha = 0.15f)
                    PlanetDignity.SWAKSHETRA -> OrangeAccent.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = spashta.dignity.devanagari,
                    color = when (spashta.dignity) {
                        PlanetDignity.UCHHA, PlanetDignity.MOOLATRIKONA -> AuspiciousGreen
                        PlanetDignity.NEECHA -> InauspiciousRed
                        PlanetDignity.SWAKSHETRA -> MaroonPrimary
                        else -> DarkTextSecondary
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Bhava ${spashta.bhava} (House)",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaroonPrimary,
                    fontSize = 10.sp
                )
            )
        }
    }
}
