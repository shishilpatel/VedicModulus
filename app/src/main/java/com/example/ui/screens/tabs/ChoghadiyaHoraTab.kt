package com.example.ui.screens.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.core.model.*
import com.example.data.CityRepository
import com.example.engines.astronomy.AstronomicalEngineProvider
import com.example.engines.choghadiya.ChoghadiyaEngine
import com.example.engines.hora.HoraEngine
import com.example.engines.panchanga.MuhurtaReminderHelper
import com.example.ui.theme.*
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ChoghadiyaHoraTab(
    dailyProfile: DailyVedicProfile,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }
    val astroEngine = AstronomicalEngineProvider.instance
    val choghadiyaEngine = remember { ChoghadiyaEngine() }
    val horaEngine = remember { HoraEngine() }

    val zone = remember(dailyProfile) {
        try {
            dailyProfile.currentZonedDateTime.zone
        } catch (e: Exception) {
            java.time.ZoneId.systemDefault()
        }
    }
    var now by remember { mutableStateOf(ZonedDateTime.now(zone)) }
    LaunchedEffect(zone) {
        while (true) {
            now = ZonedDateTime.now(zone)
            val nowMs = System.currentTimeMillis()
            val remaining = 1000L - (nowMs % 1000L)
            kotlinx.coroutines.delay(if (remaining <= 50L) 1000L else remaining)
        }
    }

    val solarTimes = remember(dailyProfile) {
        val date = dailyProfile.currentZonedDateTime
        val city = CityRepository.getDefaultCity()
        astroEngine.calculateSolarTimes(date, city.latitude, city.longitude)
    }

    val nextSolarTimes = remember(dailyProfile) {
        val nextDate = dailyProfile.currentZonedDateTime.plusDays(1)
        val city = CityRepository.getDefaultCity()
        astroEngine.calculateSolarTimes(nextDate, city.latitude, city.longitude)
    }

    val allChoghadiyaSlots = remember(solarTimes, nextSolarTimes) {
        choghadiyaEngine.calculateDailyChoghadiya(
            solarTimes = solarTimes,
            nextDaySunrise = nextSolarTimes.sunrise,
            dayOfWeek = dailyProfile.currentZonedDateTime.dayOfWeek.value
        )
    }

    val active = remember(allChoghadiyaSlots, now) {
        choghadiyaEngine.findActiveChoghadiya(allChoghadiyaSlots, now)
    }
    val activeType = active.type
    val minsLeft = Duration.between(now, active.endTime).toMinutes().coerceAtLeast(0)

    val allHoras = remember(solarTimes, nextSolarTimes) {
        horaEngine.calculateDailyHoras(
            solarTimes = solarTimes,
            nextDaySunrise = nextSolarTimes.sunrise,
            dayOfWeek = dailyProfile.currentZonedDateTime.dayOfWeek.value
        )
    }

    var selectedViewMode by remember { mutableStateOf(0) } // 0: Day Choghadiya, 1: Night Choghadiya, 2: 24 Horas

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Active Choghadiya Status Banner
        val bannerBg = if (activeType.isAuspicious) AuspiciousGreen.copy(alpha = 0.15f) else InauspiciousRed.copy(alpha = 0.15f)
        val bannerBorder = if (activeType.isAuspicious) AuspiciousGreen else InauspiciousRed

        Card(
            colors = CardDefaults.cardColors(containerColor = bannerBg),
            border = BorderStroke(1.dp, bannerBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE CHOGHADIYA",
                        style = MaterialTheme.typography.labelSmall.copy(color = bannerBorder, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (activeType.isAuspicious) "AUSPICIOUS" else "INAUSPICIOUS",
                        style = MaterialTheme.typography.labelSmall.copy(color = bannerBorder, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${activeType.displayName} (${activeType.devanagari})",
                    style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${activeType.quality} • Ruled by ${activeType.rulingGraha.displayName}",
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "${active.startTime.format(timeFormatter)} – ${active.endTime.format(timeFormatter)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaroonPrimary, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "$minsLeft mins left",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active Hora Status Card
        val hora = dailyProfile.currentHora
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.active_planetary_hora),
                        style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = stringResource(R.string.hora_of_fmt, hora.rulingGraha.displayName, hora.rulingGraha.sanskritName),
                        style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${hora.startTime.format(timeFormatter)} – ${hora.endTime.format(timeFormatter)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = GrayText)
                    )
                }
                Text(
                    text = hora.rulingGraha.symbol,
                    fontSize = 32.sp,
                    color = MaroonPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // View Mode Switcher: Day Choghadiya, Night Choghadiya, 24 Horas
        TabRow(
            selectedTabIndex = selectedViewMode,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaroonPrimary
        ) {
            Tab(selected = selectedViewMode == 0, onClick = { selectedViewMode = 0 }, text = { Text(stringResource(R.string.day_choghadiya), fontSize = 11.sp) })
            Tab(selected = selectedViewMode == 1, onClick = { selectedViewMode = 1 }, text = { Text(stringResource(R.string.night_choghadiya), fontSize = 11.sp) })
            Tab(selected = selectedViewMode == 2, onClick = { selectedViewMode = 2 }, text = { Text(stringResource(R.string.twenty_four_horas), fontSize = 11.sp) })
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedViewMode) {
            0 -> {
                // Day slots (first 8)
                val daySlots = allChoghadiyaSlots.filter { !it.isNight }
                daySlots.forEach { slot ->
                    ChoghadiyaSlotRow(slot, timeFormatter, slot == active)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            1 -> {
                // Night slots (last 8)
                val nightSlots = allChoghadiyaSlots.filter { it.isNight }
                nightSlots.forEach { slot ->
                    ChoghadiyaSlotRow(slot, timeFormatter, slot == active)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            2 -> {
                allHoras.forEach { h ->
                    HoraSlotRow(h, timeFormatter, h.horaNumber == hora.horaNumber)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun ChoghadiyaSlotRow(slot: ChoghadiyaSlot, formatter: DateTimeFormatter, isActive: Boolean) {
    val context = LocalContext.current
    val type = slot.type
    val accent = if (type.isAuspicious) AuspiciousGreen else InauspiciousRed

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (isActive) MaroonPrimary else LightBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${type.displayName} (${type.devanagari})",
                            color = if (isActive) MaroonPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${type.quality} • Lord: ${type.rulingGraha.displayName}",
                            color = GrayText,
                            fontSize = 11.sp
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${slot.startTime.format(formatter)} – ${slot.endTime.format(formatter)}",
                        color = if (isActive) MaterialTheme.colorScheme.onSurface else DarkTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    if (isActive) {
                        Text(stringResource(R.string.active_now), color = MaroonPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Affordances: Calendar Sync & 15m Reminder
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        MuhurtaReminderHelper.scheduleAdvanceReminder(
                            context = context,
                            title = "${type.displayName} Choghadiya",
                            startZoned = slot.startTime,
                            leadMinutes = 15
                        )
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Set Reminder",
                        tint = if (type.isAuspicious) AuspiciousGreen else DarkTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        MuhurtaReminderHelper.addToSystemCalendar(
                            context = context,
                            title = "${type.displayName} (${type.devanagari}) Choghadiya",
                            description = "${type.quality}. Ruled by ${type.rulingGraha.displayName}.",
                            startZoned = slot.startTime,
                            endZoned = slot.endTime
                        )
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Add to Calendar",
                        tint = MaroonPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HoraSlotRow(hora: HoraSlot, formatter: DateTimeFormatter, isActive: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (isActive) OrangeAccent else LightBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(hora.rulingGraha.symbol, color = MaroonPrimary, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.hora_num_fmt, hora.horaNumber, hora.rulingGraha.displayName),
                        color = if (isActive) OrangeAccent else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(hora.rulingGraha.sanskritName, color = GrayText, fontSize = 11.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${hora.startTime.format(formatter)} – ${hora.endTime.format(formatter)}",
                    color = if (isActive) MaterialTheme.colorScheme.onSurface else DarkTextSecondary,
                    fontSize = 12.sp
                )
                if (isActive) {
                    Text(stringResource(R.string.active_hora), color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
