package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.R
import com.example.core.model.DailyVedicProfile
import com.example.core.model.Graha
import com.example.core.model.Rashi
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Enhanced, celestial Zodiac & Moon Phase Transit Overlay visualization.
 * Displays interactive 12-Rashi Astrolabe, dynamic real-time Moon illumination orb,
 * element markers, transit telemetry, and Vedic sadhana recommendations.
 */
@Composable
fun ZodiacMoonPhaseOverlayCard(
    dailyProfile: DailyVedicProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dimens = LocalAppDimens.current
    val p = dailyProfile.panchanga

    // Calculate transit Moon Rashi based on Nakshatra and Pada
    val moonSignIdx = remember(p.nakshatra, p.nakshatraPada) {
        ((p.nakshatra.number - 1) * 4 + p.nakshatraPada - 1) / 9
    }
    val currentRashi = remember(moonSignIdx) {
        Rashi.values()[moonSignIdx.coerceIn(0, 11)]
    }

    var selectedRashi by remember { mutableStateOf(currentRashi) }

    val progress = dailyProfile.moonPhaseProgress
    val phaseName = when {
        progress < 0.04f || progress > 0.96f -> "New Moon (Amavasya)"
        progress < 0.24f -> "Waxing Crescent (Shukla Pratipada-Shasthi)"
        progress < 0.28f -> "First Quarter (Shukla Ashtami)"
        progress < 0.48f -> "Waxing Gibbous (Shukla Ekadashi)"
        progress < 0.52f -> "Full Moon (Purnima)"
        progress < 0.74f -> "Waning Gibbous (Krishna Tritiya)"
        progress < 0.78f -> "Last Quarter (Krishna Ashtami)"
        else -> "Waning Crescent (Krishna Dwadashi)"
    }

    val pakshaName = if (progress in 0.0f..0.50f) "Shukla Paksha (Waxing)" else "Krishna Paksha (Waning)"
    val illumination = remember(progress) {
        (sin((progress - 0.25f) * 2 * Math.PI) * 50 + 50).toInt().coerceIn(0, 100)
    }

    // Infinite breathing animation for active Moon transit halo
    val infiniteTransition = rememberInfiniteTransition(label = "ChandraAura")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Card(
        shape = RoundedCornerShape(dimens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder.copy(alpha = 0.8f)),
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(dimens.cardCornerRadius))
    ) {
        Column(modifier = Modifier.padding(dimens.paddingHorizontal)) {
            // Header with Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFD54F), Color(0xFFE65100))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = "Moon Transit",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Zodiac & Moon Transit Wheel",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "चन्द्र गोचर एवं राशि मण्डल",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaffronAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Surface(
                    color = AuspiciousGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AuspiciousGreen.copy(alpha = 0.35f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AuspiciousGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "ACTIVE TRANSIT",
                            color = AuspiciousGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-header summary pill row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.05f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Transit Sign: ${currentRashi.westernName} (${currentRashi.sanskritName} ${currentRashi.symbol})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    )
                    Text(
                        text = "$pakshaName • $illumination% Lunar Illumination",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                    )
                }

                Surface(
                    color = GoldPrimary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Pada ${p.nakshatraPada}",
                        color = MaroonPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Celestial Astrolabe & Chandra Mandala
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A),
                                Color(0xFF030712)
                            )
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val rashis = remember { Rashi.values() }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = minOf(size.width, size.height) * 0.44f
                    val innerRadius = maxRadius * 0.68f

                    // 1. Draw mystical background stars
                    drawStarField(center, maxRadius)

                    // 2. Outer Ecliptic Golden Ring
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFFF59E0B),
                                Color(0xFFFDE68A),
                                Color(0xFFD97706),
                                Color(0xFFF59E0B)
                            ),
                            center = center
                        ),
                        radius = maxRadius,
                        center = center,
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Outer micro degree ticks (72 ticks)
                    for (i in 0 until 72) {
                        val angleRad = Math.toRadians((i * 5.0 - 90.0))
                        val isMajor = i % 6 == 0
                        val tickLen = if (isMajor) 8.dp.toPx() else 4.dp.toPx()
                        val tickColor = if (isMajor) Color(0xFFFDE68A) else Color(0x66FDE68A)

                        val p1 = Offset(
                            x = (center.x + (maxRadius * cos(angleRad))).toFloat(),
                            y = (center.y + (maxRadius * sin(angleRad))).toFloat()
                        )
                        val p2 = Offset(
                            x = (center.x + ((maxRadius - tickLen) * cos(angleRad))).toFloat(),
                            y = (center.y + ((maxRadius - tickLen) * sin(angleRad))).toFloat()
                        )
                        drawLine(
                            color = tickColor,
                            start = p1,
                            end = p2,
                            strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
                        )
                    }

                    // 3. Middle Division Ring
                    drawCircle(
                        color = Color(0x33F59E0B),
                        radius = innerRadius,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // 4. 12 Zodiac Houses Radial Spokes & Nodes
                    rashis.forEachIndexed { index, rashi ->
                        val angleDeg = index * 30f - 90f
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val edgePos = Offset(
                            x = (center.x + (maxRadius * cos(angleRad))).toFloat(),
                            y = (center.y + (maxRadius * sin(angleRad))).toFloat()
                        )
                        val innerPos = Offset(
                            x = (center.x + (innerRadius * cos(angleRad))).toFloat(),
                            y = (center.y + (innerRadius * sin(angleRad))).toFloat()
                        )

                        // Spoke line
                        drawLine(
                            color = Color(0x22FDE68A),
                            start = innerPos,
                            end = edgePos,
                            strokeWidth = 1.dp.toPx()
                        )

                        // Midpoint for Rashi Glyph Node
                        val midAngleRad = Math.toRadians((angleDeg + 15f).toDouble())
                        val nodeRadius = (maxRadius + innerRadius) / 2f
                        val nodePos = Offset(
                            x = (center.x + (nodeRadius * cos(midAngleRad))).toFloat(),
                            y = (center.y + (nodeRadius * sin(midAngleRad))).toFloat()
                        )

                        val isCurrent = rashi == currentRashi
                        val isSelected = rashi == selectedRashi

                        // If active transit sign, draw radiant pulsing aura
                        if (isCurrent) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF38BDF8).copy(alpha = glowAlpha),
                                        Color(0xFF0284C7).copy(alpha = 0.2f),
                                        Color.Transparent
                                    ),
                                    center = nodePos,
                                    radius = 28.dp.toPx() * pulseScale
                                ),
                                center = nodePos,
                                radius = 28.dp.toPx() * pulseScale
                            )

                            // Light beam to central Moon
                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF38BDF8).copy(alpha = 0.8f),
                                        Color(0xFFFEF08A).copy(alpha = 0.3f)
                                    ),
                                    start = nodePos,
                                    end = center
                                ),
                                start = nodePos,
                                end = center,
                                strokeWidth = 1.8.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        }

                        // Node Circle
                        val elemColor = getElementColor(rashi)
                        val badgeRadius = if (isCurrent) 14.dp.toPx() else 10.dp.toPx()

                        drawCircle(
                            color = if (isSelected) Color(0xFFF59E0B) else elemColor.copy(alpha = if (isCurrent) 0.9f else 0.4f),
                            radius = badgeRadius,
                            center = nodePos
                        )
                        drawCircle(
                            color = if (isCurrent) Color(0xFF38BDF8) else Color(0x66FFFFFF),
                            radius = badgeRadius,
                            center = nodePos,
                            style = Stroke(width = if (isCurrent || isSelected) 2.dp.toPx() else 1.dp.toPx())
                        )
                    }

                    // 5. Central Chandra Mandala (Realistic Illuminated Lunar Orb)
                    val moonRadius = 38.dp.toPx()

                    // Lunar Corona Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFEF08A).copy(alpha = 0.35f * glowAlpha),
                                Color(0xFF38BDF8).copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = moonRadius * 1.8f
                        ),
                        radius = moonRadius * 1.8f,
                        center = center
                    )

                    // Dark lunar hemisphere base
                    drawCircle(
                        color = Color(0xFF1E293B),
                        radius = moonRadius,
                        center = center
                    )

                    // Illuminated lunar phase crescent
                    drawLunarPhase(center, moonRadius, progress)

                    // Moon disk outer rim
                    drawCircle(
                        color = Color(0xFFFDE68A).copy(alpha = 0.8f),
                        radius = moonRadius,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // Overlay interactive 12 Rashi Glyph buttons
                val rashisList = Rashi.values()
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val w = maxWidth.value
                    val h = maxHeight.value
                    val centerX = w / 2f
                    val centerY = h / 2f
                    val maxR = minOf(w, h) * 0.44f
                    val innerR = maxR * 0.68f
                    val nodeR = (maxR + innerR) / 2f

                    rashisList.forEachIndexed { index, rashi ->
                        val angleDeg = index * 30f - 90f + 15f
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val posX = centerX + (nodeR * cos(angleRad)).toFloat()
                        val posY = centerY + (nodeR * sin(angleRad)).toFloat()

                        val isCurrent = rashi == currentRashi
                        val isSelected = rashi == selectedRashi

                        Box(
                            modifier = Modifier
                                .offset(x = (posX - 16).dp, y = (posY - 16).dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { selectedRashi = rashi },
                            contentAlignment = Alignment.Center
                        ) {
                            com.example.ui.components.icons.ModernZodiacIcon(
                                rashi = rashi,
                                size = if (isCurrent || isSelected) 20.dp else 16.dp,
                                tint = if (isCurrent) Color(0xFF38BDF8) else if (isSelected) Color(0xFFFDE68A) else Color.White.copy(alpha = 0.85f),
                                isRealistic = isCurrent || isSelected
                            )
                        }
                    }
                }

                // Center Moon text readout
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.clickable { selectedRashi = currentRashi }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        com.example.ui.components.icons.ModernZodiacIcon(
                            rashi = currentRashi,
                            size = 14.dp,
                            tint = Color(0xFFFEF08A)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = currentRashi.sanskritName.take(3),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFEF08A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                    Text(
                        text = "$illumination%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = if (progress <= 0.5f) "शुक्ल" else "कृष्ण",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF38BDF8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rashi Selector Horizontal Carousel Chips
            Text(
                text = "Explore 12 Zodiac Houses (द्वादश राशि):",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextSecondary
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Rashi.values().forEach { rashi ->
                    val isCurrent = rashi == currentRashi
                    val isSelected = rashi == selectedRashi

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedRashi = rashi },
                        label = {
                            Text(
                                text = rashi.sanskritName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected || isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            com.example.ui.components.icons.ModernZodiacIcon(
                                rashi = rashi,
                                size = 16.dp,
                                tint = if (isSelected) MaroonPrimary else Color.Unspecified,
                                isRealistic = !isSelected
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaroonPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = MaroonPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isCurrent) AuspiciousGreen else LightBorder,
                            selectedBorderColor = MaroonPrimary,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Rashi Detailed Telemetry Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                border = BorderStroke(1.dp, LightBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.18f))
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                com.example.ui.components.icons.ModernZodiacIcon(
                                    rashi = selectedRashi,
                                    size = 32.dp,
                                    isRealistic = true
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${selectedRashi.westernName} (${selectedRashi.sanskritName})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = MaroonPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Sign #${selectedRashi.number} • Lord: ${selectedRashi.rulingGraha.displayName} (${selectedRashi.rulingGraha.sanskritName})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                                )
                            }
                        }

                        if (selectedRashi == currentRashi) {
                            Surface(
                                color = AuspiciousGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "MOON HERE",
                                    color = AuspiciousGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = LightBorder.copy(alpha = 0.6f)
                    )

                    // Rashi Attribute Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RashiAttributeItem(
                            label = "Element",
                            value = getElementTitle(selectedRashi),
                            icon = Icons.Default.Terrain
                        )
                        RashiAttributeItem(
                            label = "Modality",
                            value = getModalityTitle(selectedRashi),
                            icon = Icons.Default.AllInclusive
                        )
                        RashiAttributeItem(
                            label = "Quality",
                            value = getQualityTitle(selectedRashi),
                            icon = Icons.Default.AutoAwesome
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Transit Astrological & Spiritual Guidance
                    Text(
                        text = if (selectedRashi == currentRashi) {
                            "The Moon (Chandra) is directly magnifying intuitive power, emotional clarity, and spiritual resonance through ${selectedRashi.sanskritName} Rashi in ${p.nakshatra.sanskritName} Nakshatra. Auspicious for peaceful meditation, creative devotion, and satvik endeavors."
                        } else {
                            "When the Moon transits ${selectedRashi.westernName} (${selectedRashi.sanskritName}), energies governed by ${selectedRashi.rulingGraha.displayName} become prominent, emphasizing ${getElementTitle(selectedRashi)} elemental harmony."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DarkTextSecondary,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Chandra Mantra Action
                    Surface(
                        color = GoldPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast
                                    .makeText(
                                        context,
                                        "Chandra Bija Mantra: ॐ सों सोमाय नमः (Om Som Somaya Namah)",
                                        Toast.LENGTH_LONG
                                    )
                                    .show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.SelfImprovement,
                                    contentDescription = "Mantra",
                                    tint = MaroonPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Lunar Mantra: ॐ सों सोमाय नमः",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonPrimary
                                )
                            }
                            Text(
                                text = "Tap to view",
                                fontSize = 10.sp,
                                color = SaffronAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RashiAttributeItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = SaffronAccent,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = GrayText
                )
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontSize = 11.sp
            )
        )
    }
}

/**
 * Draws soft realistic lunar phase illumination on canvas.
 */
private fun DrawScope.drawLunarPhase(center: Offset, radius: Float, progress: Float) {
    val phaseRatio = progress.coerceIn(0f, 1f)
    val moonGlowColor = Color(0xFFFEF08A)

    if (phaseRatio in 0.48f..0.52f) {
        // Full Moon: Entire disk illuminated with golden glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF08A), Color(0xFFF59E0B)),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    } else if (phaseRatio < 0.03f || phaseRatio > 0.97f) {
        // New Moon: Dark disk with subtle rim aura
        drawCircle(
            color = Color(0xFF0F172A),
            radius = radius,
            center = center
        )
    } else {
        // Crescent / Gibbous illumination
        val isWaxing = phaseRatio <= 0.5f
        val illuminationFactor = if (isWaxing) phaseRatio * 2f else (1f - phaseRatio) * 2f

        // Base glow arc
        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFFFBEB), moonGlowColor),
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y)
            ),
            startAngle = if (isWaxing) -90f else 90f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )

        // Elliptical terminator overlay
        val terminatorWidth = radius * (1f - illuminationFactor * 2f)
        drawOval(
            color = if (illuminationFactor < 0.5f) Color(0xFF1E293B) else moonGlowColor,
            topLeft = Offset(center.x - kotlin.math.abs(terminatorWidth), center.y - radius),
            size = Size(kotlin.math.abs(terminatorWidth) * 2, radius * 2)
        )
    }
}

/**
 * Draws background star field with varying depths.
 */
private fun DrawScope.drawStarField(center: Offset, maxRadius: Float) {
    val starCoords = listOf(
        Pair(0.25f, 0.20f), Pair(0.75f, 0.15f), Pair(0.18f, 0.70f), Pair(0.82f, 0.80f),
        Pair(0.35f, 0.85f), Pair(0.65f, 0.25f), Pair(0.12f, 0.40f), Pair(0.88f, 0.45f),
        Pair(0.40f, 0.12f), Pair(0.60f, 0.88f), Pair(0.28f, 0.55f), Pair(0.72f, 0.60f)
    )

    starCoords.forEachIndexed { i, (rx, ry) ->
        val x = (center.x - maxRadius) + rx * (maxRadius * 2)
        val y = (center.y - maxRadius) + ry * (maxRadius * 2)
        val starRadius = if (i % 3 == 0) 1.5.dp.toPx() else 1.0.dp.toPx()
        val starAlpha = if (i % 2 == 0) 0.6f else 0.35f

        drawCircle(
            color = Color(0xFFFEF08A).copy(alpha = starAlpha),
            radius = starRadius,
            center = Offset(x, y)
        )
    }
}

private fun getElementColor(rashi: Rashi): Color {
    return when (rashi.number) {
        1, 5, 9 -> Color(0xFFEF4444) // Fire (Mesha, Simha, Dhanu)
        2, 6, 10 -> Color(0xFF10B981) // Earth (Vrishabha, Kanya, Makara)
        3, 7, 11 -> Color(0xFF06B6D4) // Air (Mithuna, Tula, Kumbha)
        else -> Color(0xFF3B82F6) // Water (Karkata, Vrishchika, Meena)
    }
}

private fun getElementTitle(rashi: Rashi): String {
    return when (rashi.number) {
        1, 5, 9 -> "Fire (अग्नि)"
        2, 6, 10 -> "Earth (पृथ्वी)"
        3, 7, 11 -> "Air (वायु)"
        else -> "Water (जल)"
    }
}

private fun getModalityTitle(rashi: Rashi): String {
    return when (rashi.number % 3) {
        1 -> "Movable (चर)"
        2 -> "Fixed (स्थिर)"
        else -> "Dual (द्विस्वभाव)"
    }
}

private fun getQualityTitle(rashi: Rashi): String {
    return when (rashi.number) {
        1, 5, 9 -> "Dynamic & Vital"
        2, 6, 10 -> "Grounded & Material"
        3, 7, 11 -> "Intellectual & Social"
        else -> "Intuitive & Spiritual"
    }
}
