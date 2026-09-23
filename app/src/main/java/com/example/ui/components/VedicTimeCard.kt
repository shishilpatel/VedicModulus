package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sync
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
import com.example.core.model.DailyVedicProfile
import com.example.core.model.LocationData
import com.example.engines.astronomy.VedicDateData
import com.example.engines.astronomy.VedicTimeCalculator
import com.example.engines.astronomy.VedicTimeData
import com.example.ui.theme.*
import java.time.format.DateTimeFormatter

/**
 * Polished Material 3 card showing real-time Gregorian time and traditional Vedic Time (Ghadi, Pala, Prahara)
 * alongside comprehensive Vedic calendar date information.
 */
@Composable
fun VedicTimeCard(
    dailyProfile: DailyVedicProfile,
    currentLocation: LocationData,
    isGpsSynced: Boolean,
    onSyncLocationClick: () -> Unit,
    isSyncing: Boolean,
    currentTimeProvider: (() -> java.time.LocalTime)? = null,
    modifier: Modifier = Modifier
) {
    val zone = remember(currentLocation) { java.time.ZoneId.of(currentLocation.timezoneId) }
    
    val zdt = dailyProfile.currentZonedDateTime

    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
    val sunriseFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    val vedicTime: VedicTimeData = remember(zdt, dailyProfile.panchanga.sunrise, dailyProfile.panchanga.sunset) {
        VedicTimeCalculator.calculateVedicTime(zdt, dailyProfile.panchanga.sunrise, dailyProfile.panchanga.sunset)
    }

    val vedicDate: VedicDateData = remember(dailyProfile) {
        VedicTimeCalculator.getVedicDateData(dailyProfile)
    }

    var showCalendarDetails by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Location & Sync Badge
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
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = OrangeAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "${currentLocation.cityName}, ${currentLocation.state}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaroonPrimary
                            )
                        )
                        Text(
                            text = "Timezone: ${currentLocation.timezoneId} (${if (isGpsSynced) "GPS Live" else "Profile"})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Sync button
                OutlinedButton(
                    onClick = onSyncLocationClick,
                    enabled = !isSyncing,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isGpsSynced) AuspiciousGreen else OrangeAccent)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = OrangeAccent
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = if (isGpsSynced) AuspiciousGreen else OrangeAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGpsSynced) "Synced" else "Sync Time",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isGpsSynced) AuspiciousGreen else OrangeAccent
                            )
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.8.dp,
                color = LightBorder
            )

            // Dual Clock Section: Gregorian & Vedic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Standard Gregorian Time Box
                Surface(
                    color = MaroonPrimary.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaroonPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STANDARD TIME",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaroonPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        TickingDigitalClock(currentTimeProvider, zone)
                        Text(
                            text = vedicDate.gregorianDateFormatted,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                }

                // 2. Vedic Time Box (Ghadi & Pala)
                Surface(
                    color = OrangeAccent.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Brightness5,
                                    contentDescription = null,
                                    tint = OrangeAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VEDIC TIME (घटी/पल)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = OrangeAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = vedicTime.devanagariGhadiPala,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaroonPrimary
                            )
                        )
                        Text(
                            text = "${vedicTime.ghadi} Ghadi • ${vedicTime.pala} Pala",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vedic Day Progress & Prahara Division
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Prahara: ${vedicTime.prahara.devanagariName}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaroonPrimary
                            )
                        )
                        Text(
                            text = "${(vedicTime.dayProgressPercent * 60).toInt()} / 60 Ghadis",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedSunPathArc(
                        vedicTime = vedicTime,
                        currentTime = dailyProfile.currentZonedDateTime,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Brightness5,
                                contentDescription = "Sunrise",
                                tint = OrangeAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Suryodaya: ${dailyProfile.panchanga.sunrise.format(sunriseFormatter)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = DarkTextSecondary
                                )
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Brightness6,
                                contentDescription = "Sunset",
                                tint = MaroonPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Suryast: ${dailyProfile.panchanga.sunset.format(sunriseFormatter)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = DarkTextSecondary
                                )
                            )
                        }
                    }
                    Text(
                        text = "Significance: ${vedicTime.prahara.significance}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = DarkTextSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vedic Calendar Date Summary
            Surface(
                color = MaroonPrimary.copy(alpha = 0.04f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaroonPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VEDIC DATE (पञ्चाङ्ग तिथि)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaroonPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                        TextButton(
                            onClick = { showCalendarDetails = !showCalendarDetails },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (showCalendarDetails) "Hide" else "Details",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OrangeAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${vedicDate.tithiDevanagari} (${vedicDate.tithiName}) • ${vedicDate.paksha}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonPrimary
                        )
                    )
                    Text(
                        text = "Vikram Samvat ${vedicDate.vikramSamvat} • Shaka ${vedicDate.shakaSamvat} • Masa: ${vedicDate.masa}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DarkTextSecondary,
                            fontSize = 11.sp
                        )
                    )

                    if (showCalendarDetails) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), thickness = 0.5.dp, color = LightBorder)

                        VedicDetailItem("Vara (Vasara)", "${vedicDate.varaDevanagari} (${vedicDate.varaName})")
                        VedicDetailItem("Nakshatra & Pada", "${vedicDate.nakshatraName} • Pada ${vedicDate.nakshatraPada}")
                        VedicDetailItem("Ritu & Ayana", "${vedicDate.ritu} Ritu • ${vedicDate.ayana}")
                        VedicDetailItem("Ayanamsha", "Chitrapaksha Lahiri")
                    }
                }
            }
        }
    }
}

@Composable
private fun VedicDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary, fontSize = 11.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaroonPrimary,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun AnimatedSunPathArc(
    vedicTime: VedicTimeData,
    currentTime: java.time.ZonedDateTime,
    modifier: Modifier = Modifier
) {
    var tickingTime by remember(currentTime) { mutableStateOf(currentTime) }
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(60000L) // Update every minute
            tickingTime = java.time.ZonedDateTime.now(currentTime.zone)
        }
    }

    val dayDuration = java.time.Duration.between(vedicTime.sunrise, vedicTime.sunset).seconds.toFloat().coerceAtLeast(1f)
    val elapsedDay = java.time.Duration.between(vedicTime.sunrise, tickingTime).seconds.toFloat()
    val progress = (elapsedDay / dayDuration).coerceIn(0f, 1f)
    
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = progress,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 2000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "sunPathArc"
    )
    
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxWidth().aspectRatio(2f)) {
        val canvasWidth = size.width
        val radius = canvasWidth / 2f
        
        // Dashed Background Arc (Full Sunrise to Sunset)
        drawArc(
            color = MaroonPrimary.copy(alpha = 0.2f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            size = androidx.compose.ui.geometry.Size(canvasWidth, canvasWidth),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 4.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
            )
        )
        
        // Active Progress Arc
        if (animatedProgress > 0f) {
            drawArc(
                color = OrangeAccent,
                startAngle = 180f,
                sweepAngle = 180f * animatedProgress,
                useCenter = false,
                size = androidx.compose.ui.geometry.Size(canvasWidth, canvasWidth),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 6.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
        
        // Calculate Sun position
        val cx = radius
        val cy = radius
        val angleInDegrees = 180f + (180f * animatedProgress)
        val angleInRadians = Math.toRadians(angleInDegrees.toDouble())
        
        val sunX = cx + radius * kotlin.math.cos(angleInRadians).toFloat()
        val sunY = cy + radius * kotlin.math.sin(angleInRadians).toFloat()
        val sunOffset = androidx.compose.ui.geometry.Offset(sunX, sunY)
        
        // Sun Glow
        drawCircle(
            color = OrangeAccent.copy(alpha = 0.4f),
            radius = 16.dp.toPx(),
            center = sunOffset
        )
        // Sun Core
        drawCircle(
            color = OrangeAccent,
            radius = 8.dp.toPx(),
            center = sunOffset
        )
        // Sun Inner
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = sunOffset
        )
    }
}

@Composable
fun TickingDigitalClock(currentTimeProvider: (() -> java.time.LocalTime)? = null, zone: java.time.ZoneId) {
    var currentTimeValue by remember { mutableStateOf(currentTimeProvider?.invoke() ?: java.time.LocalTime.now(zone)) }
    LaunchedEffect(zone) {
        while (true) {
            kotlinx.coroutines.delay(1000L)
            currentTimeValue = currentTimeProvider?.invoke() ?: java.time.LocalTime.now(zone)
        }
    }
    val zdt = remember(currentTimeValue) { java.time.ZonedDateTime.of(java.time.LocalDate.now(zone), currentTimeValue, zone) }
    val timeFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a") }
    Text(
        text = zdt.format(timeFormatter),
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaroonPrimary,
            fontFamily = FontFamily.Monospace
        )
    )
}
