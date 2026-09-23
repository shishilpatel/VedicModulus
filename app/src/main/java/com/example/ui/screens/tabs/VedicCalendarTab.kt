package com.example.ui.screens.tabs

import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import com.example.engines.export.PanchangCalendarExporter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.BirthProfile
import com.example.core.model.DailyVedicProfile
import com.example.core.model.PersonalizedProfile
import com.example.core.model.VedicAssetRegistry
import com.example.engines.astronomy.MoonPhaseCalculator
import com.example.engines.personalization.VedicPersonalizationEngine
import com.example.ui.components.VedicCurrentDateCalendarComponent
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun VedicCalendarTab(
    profile: BirthProfile,
    personalizedProfile: PersonalizedProfile,
    personalizationEngine: VedicPersonalizationEngine,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var monthProfiles by remember { mutableStateOf<Map<LocalDate, DailyVedicProfile>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    LaunchedEffect(currentMonth, profile) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val profiles = mutableMapOf<LocalDate, DailyVedicProfile>()
            val daysInMonth = currentMonth.lengthOfMonth()
            val zoneId = try {
                java.time.ZoneId.of(profile.birthLocation.timezoneId)
            } catch (e: Exception) {
                java.time.ZoneId.systemDefault()
            }

            for (day in 1..daysInMonth) {
                val date = currentMonth.atDay(day)
                val zdt = ZonedDateTime.now(zoneId)
                    .withYear(date.year)
                    .withMonth(date.monthValue)
                    .withDayOfMonth(date.dayOfMonth)
                profiles[date] = personalizationEngine.createDailyVedicProfile(
                    now = zdt,
                    location = profile.birthLocation,
                    ayanamsha = profile.ayanamsha,
                    tradition = profile.calendarTradition,
                    personalizedProfile = personalizedProfile
                )
            }
            monthProfiles = profiles
        }
        isLoading = false
    }

    val selectedProfile = monthProfiles[selectedDate]

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 16.dp)
    ) {
        // Current Date Vedic Tithi and Nakshatra Calculator
        VedicCurrentDateCalendarComponent(
            location = profile.birthLocation,
            ayanamsha = profile.ayanamsha,
            tradition = profile.calendarTradition,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Selected Date Header Panel
        if (selectedProfile != null) {
            SelectedDatePanel(selectedDate = selectedDate, profile = selectedProfile)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangeAccent, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Month Navigation Header
        MonthNavigation(
            currentMonth = currentMonth,
            onPrevious = {
                currentMonth = currentMonth.minusMonths(1)
                selectedDate = currentMonth.atDay(1)
            },
            onNext = {
                currentMonth = currentMonth.plusMonths(1)
                selectedDate = currentMonth.atDay(1)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid or Loading Spinner
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = OrangeAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.calculating_ephemeris),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                }
            }
        } else {
            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                monthProfiles = monthProfiles,
                onDateSelected = { selectedDate = it }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Date Details Card
        if (selectedProfile != null) {
            SelectedDateDetailsSection(
                selectedDate = selectedDate,
                profile = selectedProfile
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // All Festivals & Vrats for this month
        MonthEventsSection(
            currentMonth = currentMonth,
            monthProfiles = monthProfiles,
            selectedDate = selectedDate,
            onSelectDate = { selectedDate = it }
        )
    }
}

@Composable
fun SelectedDatePanel(selectedDate: LocalDate, profile: DailyVedicProfile) {
    val phaseAngle = MoonPhaseCalculator.calculateLunarPhaseAngle(profile.currentZonedDateTime)
    val moonAssetId = MoonPhaseCalculator.getMoonPhaseAssetId(phaseAngle)
    val moonAsset = VedicAssetRegistry.getAsset(moonAssetId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, LightBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (moonAsset != null) {
                Image(
                    painter = painterResource(id = moonAsset.drawableResId),
                    contentDescription = moonAsset.title,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (moonAsset != null) {
                    Text(
                        text = moonAsset.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OrangeAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE")),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaroonPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                val tithi = profile.panchanga.tithi
                Text(
                    text = "${tithi.name} (${tithi.devanagari}) • ${tithi.paksha.displayName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.nakshatra_pada_fmt, profile.panchanga.nakshatra.name, profile.panchanga.nakshatraPada),
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                )

                val activeEvents = (profile.festivals.map { it.name } + profile.vrats.map { it.name }).distinct()
                if (activeEvents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = activeEvents.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OrangeAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MonthNavigation(currentMonth: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, LightBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = MaroonPrimary
                )
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                color = MaroonPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNext) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = MaroonPrimary
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    monthProfiles: Map<LocalDate, DailyVedicProfile>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value // 1 (Mon) to 7 (Sun)
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, LightBorder)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header Row of Day Names
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        color = DarkTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            HorizontalDivider(color = LightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

            var dayOfMonth = 1
            for (week in 0 until 6) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (dayOfWeek in 1..7) {
                        if (week == 0 && dayOfWeek < firstDayOfWeek || dayOfMonth > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val date = currentMonth.atDay(dayOfMonth)
                            val profile = monthProfiles[date]
                            CalendarCell(
                                date = date,
                                profile = profile,
                                isSelected = date == selectedDate,
                                isToday = date == LocalDate.now(),
                                onClick = { onDateSelected(date) },
                                modifier = Modifier.weight(1f)
                            )
                            dayOfMonth++
                        }
                    }
                }
                if (dayOfMonth > daysInMonth) break
            }
        }
    }
}

@Composable
fun CalendarCell(
    date: LocalDate,
    profile: DailyVedicProfile?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> OrangeAccent
        isToday -> OrangeAccent.copy(alpha = 0.18f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val tithiColor = if (isSelected) Color.White else OrangeAccent
    val iconTint = if (isSelected) Color.White else MaroonPrimary
    val borderStroke = if (isToday && !isSelected) BorderStroke(1.5.dp, OrangeAccent) else null

    // Calculate Moon Phase for this exact date (whether profile is loaded or still computing)
    val phaseAngle = if (profile != null) {
        MoonPhaseCalculator.calculateLunarPhaseAngle(profile.currentZonedDateTime)
    } else {
        val zdt = date.atTime(12, 0).atZone(java.time.ZoneId.systemDefault())
        MoonPhaseCalculator.calculateLunarPhaseAngle(zdt)
    }
    val moonAssetId = MoonPhaseCalculator.getMoonPhaseAssetId(phaseAngle)
    val moonAsset = VedicAssetRegistry.getAsset(moonAssetId)

    val hasFestivalOrVrat = profile != null && (profile.festivals.isNotEmpty() || profile.vrats.isNotEmpty())

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(8.dp)) else Modifier)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 3.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Moon Phase image on the left, Gregorian Day number on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (moonAsset != null) {
                    Image(
                        painter = painterResource(id = moonAsset.drawableResId),
                        contentDescription = moonAsset.title,
                        modifier = Modifier.size(19.dp)
                    )
                } else {
                    Box(modifier = Modifier.size(19.dp))
                }

                Text(
                    text = date.dayOfMonth.toString(),
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.SemiBold
                )
            }

            // Bottom Row: Festival / Vrat Indicator on the left, Tithi number on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (hasFestivalOrVrat) {
                    Icon(
                        imageVector = Icons.Filled.Event,
                        contentDescription = "Festival or Vrat",
                        tint = iconTint,
                        modifier = Modifier.size(13.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.size(13.dp))
                }

                if (profile != null) {
                    Text(
                        text = profile.panchanga.tithi.number.toString(),
                        color = tithiColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedDateDetailsSection(
    selectedDate: LocalDate,
    profile: DailyVedicProfile
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = "Details for ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}",
            style = MaterialTheme.typography.titleMedium,
            color = MaroonPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, LightBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailColumn(
                        title = "Tithi",
                        value = "${profile.panchanga.tithi.name} (${profile.panchanga.tithi.devanagari})",
                        subtitle = profile.panchanga.tithi.paksha.displayName,
                        modifier = Modifier.weight(1f)
                    )
                    DetailColumn(
                        title = "Nakshatra",
                        value = profile.panchanga.nakshatra.name,
                        subtitle = "Pada ${profile.panchanga.nakshatraPada}",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = LightBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailColumn(
                        title = "Yoga",
                        value = profile.panchanga.yoga,
                        subtitle = "Auspicious alignment",
                        modifier = Modifier.weight(1f)
                    )
                    DetailColumn(
                        title = "Karana",
                        value = profile.panchanga.karana,
                        subtitle = "Half lunar day",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Calendar Integration Action Card
        val context = LocalContext.current
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Sync to Calendar",
                        tint = OrangeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.sync_panchang_calendar),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.sync_panchang_desc),
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            PanchangCalendarExporter.exportDayPanchangToIcs(context, profile)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.export_ics), fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            PanchangCalendarExporter.addEventToCalendar(
                                context = context,
                                title = "Brahma Muhurta Meditation",
                                description = "Sacred pre-dawn window for meditation and mantra japa. Tithi: ${profile.panchanga.tithi.name}.",
                                location = "Sacred Space",
                                startTime = profile.brahmaMuhurta.startTime,
                                endTime = profile.brahmaMuhurta.endTime
                            )
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.add_brahma_muhurta), fontSize = 12.sp)
                    }
                }
            }
        }

        // Specific Festivals & Vrats for this selected date
        val festivals = profile.festivals
        val vrats = profile.vrats

        if (festivals.isNotEmpty() || vrats.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.festivals_observances_day),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = OrangeAccent,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            festivals.forEach { festival ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = OrangeAccent.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(OrangeAccent.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = festival.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = MaroonPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            if (festival.description.isNotBlank()) {
                                Text(
                                    text = festival.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                                )
                            }
                        }
                    }
                }
            }

            vrats.forEach { vrat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LightBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaroonPrimary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Event,
                                contentDescription = null,
                                tint = MaroonPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.vrat_fmt, vrat.name),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = MaroonPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = vrat.recommendedPractice,
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailColumn(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaroonPrimary,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
        )
    }
}

@Composable
fun MonthEventsSection(
    currentMonth: YearMonth,
    monthProfiles: Map<LocalDate, DailyVedicProfile>,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit
) {
    val monthEvents = monthProfiles.values.flatMap { profile ->
        val date = profile.currentZonedDateTime.toLocalDate()
        val fList = profile.festivals.map { Triple(date, it.name, "Festival") }
        val vList = profile.vrats.map { Triple(date, it.name, "Vrat") }
        fList + vList
    }.sortedBy { it.first }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = "Festivals & Events in ${currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}",
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaroonPrimary,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (monthEvents.isEmpty()) {
            Text(
                text = stringResource(R.string.no_festivals_month),
                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, LightBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    monthEvents.forEachIndexed { index, (date, name, type) ->
                        val isDateSelected = date == selectedDate
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isDateSelected) OrangeAccent.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .clickable { onSelectDate(date) }
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Date Pill Badge
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isDateSelected) OrangeAccent
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = if (isDateSelected) Color.White else MaroonPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = date.format(DateTimeFormatter.ofPattern("MMM")),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isDateSelected) Color.White.copy(alpha = 0.9f) else DarkTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isDateSelected) OrangeAccent else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${date.format(DateTimeFormatter.ofPattern("EEEE"))} • $type",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                                )
                            }

                            Icon(
                                imageVector = Icons.Filled.Event,
                                contentDescription = "View date",
                                tint = if (isDateSelected) OrangeAccent else DarkTextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (index < monthEvents.lastIndex) {
                            HorizontalDivider(
                                color = LightBorder,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
