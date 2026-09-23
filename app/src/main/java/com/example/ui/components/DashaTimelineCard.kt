package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engines.reports.AntardashaPeriod
import com.example.engines.reports.DashaEngine
import com.example.engines.reports.DashaPeriod
import com.example.ui.theme.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun DashaTimelineCard(
    dashaPeriods: List<DashaPeriod>,
    modifier: Modifier = Modifier,
    currentTime: ZonedDateTime = remember { ZonedDateTime.now() }
) {
    val dashaEngine = remember { DashaEngine() }
    val dtf = remember { DateTimeFormatter.ofPattern("MMM yyyy") }
    val dtfDay = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    // State for which Mahadasha is expanded to show Antardashas
    var expandedMahadashaIndex by remember {
        val currentIdx = dashaPeriods.indexOfFirst { it.isCurrent }
        mutableStateOf(if (currentIdx >= 0) currentIdx else 0)
    }

    // State for which Antardasha is expanded to show Pratyantardashas
    var expandedAntardashaKey by remember { mutableStateOf<String?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vimshottari Dasha & Antardasha Timeline",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "120-Year Nakshatra Planetary Cycles • विंशोत्तरी दशा",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary, fontSize = 10.sp)
                    )
                }

                Surface(
                    color = GoldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Vedic Jyotish",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaroonPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mahadasha List
            dashaPeriods.forEachIndexed { index, period ->
                val isCurrent = period.isCurrent
                val isExpanded = expandedMahadashaIndex == index

                val totalDurationMillis = ChronoUnit.MILLIS.between(period.startDate, period.endDate).toFloat().coerceAtLeast(1f)
                val elapsedMillis = ChronoUnit.MILLIS.between(period.startDate, currentTime).coerceIn(0, totalDurationMillis.toLong()).toFloat()
                val progressFraction = (elapsedMillis / totalDurationMillis).coerceIn(0f, 1f)

                Surface(
                    color = if (isCurrent) MaroonPrimary.copy(alpha = 0.07f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(
                        width = if (isCurrent) 1.5.dp else 1.dp,
                        color = if (isCurrent) MaroonPrimary else LightBorder
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable {
                            expandedMahadashaIndex = if (isExpanded) -1 else index
                        }
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = period.lord.symbol,
                                    fontSize = 18.sp,
                                    color = if (isCurrent) MaroonPrimary else DarkTextSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${period.lord.displayName} Mahadasha",
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCurrent) MaroonPrimary else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = AuspiciousGreen,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${period.startDate.format(dtf)} – ${period.endDate.format(dtf)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isCurrent) DarkTextSecondary else GrayText,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isCurrent) {
                                    Text(
                                        text = "${(progressFraction * 100).toInt()}%",
                                        fontWeight = FontWeight.Bold,
                                        color = MaroonPrimary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand Antardashas",
                                    tint = if (isCurrent) MaroonPrimary else DarkTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Progress Bar for Current Mahadasha
                        if (isCurrent) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaroonPrimary,
                                trackColor = MaroonPrimary.copy(alpha = 0.2f)
                            )
                        }

                        // Antardashas (Bhukti) Sub-Timeline when Expanded
                        AnimatedVisibility(visible = isExpanded) {
                            val antardashas = remember(period) {
                                dashaEngine.calculateAntardashas(period, currentTime)
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                HorizontalDivider(color = LightBorder)
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Antardashas (Bhuktis) of ${period.lord.displayName}:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaroonPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                antardashas.forEach { sub ->
                                    val isSubCurrent = sub.isCurrent
                                    val subKey = "${sub.mahadashaLord.name}_${sub.antardashaLord.name}"
                                    val isPratyantardashaExpanded = expandedAntardashaKey == subKey
                                    val subBg = if (isSubCurrent) SaffronAccent.copy(alpha = 0.18f) else Color.Transparent

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(subBg, shape = RoundedCornerShape(4.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    expandedAntardashaKey = if (isPratyantardashaExpanded) null else subKey
                                                }
                                                .padding(vertical = 5.dp, horizontal = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${sub.mahadashaLord.symbol}-${sub.antardashaLord.symbol}",
                                                    fontSize = 12.sp,
                                                    color = if (isSubCurrent) MaroonPrimary else DarkTextSecondary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${sub.mahadashaLord.displayName} / ${sub.antardashaLord.displayName}",
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSubCurrent) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSubCurrent) MaroonPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (isSubCurrent) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = OrangeAccent,
                                                        shape = RoundedCornerShape(3.dp)
                                                    ) {
                                                        Text(
                                                            text = "NOW",
                                                            color = Color.White,
                                                            fontSize = 7.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${sub.startDate.format(dtfDay)} – ${sub.endDate.format(dtfDay)}",
                                                    fontSize = 10.sp,
                                                    color = if (isSubCurrent) MaroonPrimary else GrayText,
                                                    fontWeight = if (isSubCurrent) FontWeight.Bold else FontWeight.Normal,
                                                    textAlign = TextAlign.End
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = if (isPratyantardashaExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Toggle Pratyantardashas",
                                                    tint = GrayText,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        // Pratyantardasha (Sub-sub-period) Level 3
                                        AnimatedVisibility(visible = isPratyantardashaExpanded) {
                                            val pratyantardashas = remember(sub, currentTime) {
                                                dashaEngine.calculatePratyantardashas(sub, currentTime)
                                            }
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp, end = 6.dp, top = 2.dp, bottom = 6.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .border(0.5.dp, LightBorder, RoundedCornerShape(6.dp))
                                                    .padding(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    text = "PRATYANTARDASHA (प्रत्यन्तर्दशा)",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaroonPrimary
                                                )
                                                pratyantardashas.forEach { prat ->
                                                    val isPratActive = prat.isCurrent
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(
                                                                if (isPratActive) GoldPrimary.copy(alpha = 0.22f) else Color.Transparent,
                                                                shape = RoundedCornerShape(3.dp)
                                                            )
                                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = "${prat.pratyantardashaLord.symbol} ${prat.pratyantardashaLord.displayName}",
                                                                fontSize = 10.sp,
                                                                fontWeight = if (isPratActive) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isPratActive) MaroonPrimary else MaterialTheme.colorScheme.onSurface
                                                            )
                                                            if (isPratActive) {
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = "• ACTIVE",
                                                                    fontSize = 8.sp,
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    color = OrangeAccent
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = "${prat.startDate.format(dtfDay)} – ${prat.endDate.format(dtfDay)}",
                                                            fontSize = 9.sp,
                                                            color = if (isPratActive) MaroonPrimary else GrayText,
                                                            fontWeight = if (isPratActive) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
