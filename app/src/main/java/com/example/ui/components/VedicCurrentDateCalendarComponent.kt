package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.*
import com.example.engines.astronomy.AstronomicalEngine
import com.example.engines.astronomy.AstronomicalEngineProvider
import com.example.engines.panchanga.PanchangaEngine
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Interactive Vedic Calendar component that calculates and displays the
 * Vedic Tithi (lunar day) and Nakshatra (lunar mansion) for the current date.
 */
@Composable
fun VedicCurrentDateCalendarComponent(
    location: LocationData,
    ayanamsha: AyanamshaSystem = AyanamshaSystem.LAHIRI,
    tradition: CalendarTradition = CalendarTradition.AMANTA,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }

    val astronomicalEngine: AstronomicalEngine = AstronomicalEngineProvider.instance
    val panchangaEngine = remember(astronomicalEngine) { PanchangaEngine(astronomicalEngine) }

    // Calculate Vedic Panchanga (Tithi & Nakshatra) for selected date at current/noon time
    val panchangaDataState = remember { mutableStateOf<com.example.core.model.PanchangaData?>(null) }
    val panchangaData = panchangaDataState.value
    LaunchedEffect(selectedDate, location, ayanamsha, tradition) {
        val zoneId = try {
            ZoneId.of(location.timezoneId)
        } catch (e: Exception) {
            ZoneId.systemDefault()
        }
        val targetZdt = selectedDate.atTime(12, 0).atZone(zoneId)
        panchangaDataState.value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            panchangaEngine.calculatePanchanga(
                dateTime = targetZdt,
                latitude = location.latitude,
                longitude = location.longitude,
                ayanamsha = ayanamsha,
                tradition = tradition
            )
        }
    }

    if (panchangaData == null) {
        Card(
            modifier = modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.25f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaroonPrimary)
            }
        }
        return
    }

    val isCurrentDate = selectedDate == today
    val tithi = panchangaData.tithi
    val nakshatra = panchangaData.nakshatra
    val pada = panchangaData.nakshatraPada

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vedic_calendar_component"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Component Title and "Current Date" Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaroonPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaroonPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Vedic Calendar & Panchang",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaroonPrimary
                            )
                        )
                        Text(
                            text = "Tithi & Nakshatra Calculator",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                        )
                    }
                }

                // If not today, show "Jump to Today" action
                if (!isCurrentDate) {
                    OutlinedButton(
                        onClick = { selectedDate = today },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, OrangeAccent),
                        modifier = Modifier.testTag("action_jump_to_today")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = OrangeAccent.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(OrangeAccent, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CURRENT DATE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OrangeAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            // Interactive Date Selector Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedDate = selectedDate.minusDays(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("action_prev_day")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Day",
                            tint = MaroonPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.US)),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = if (isCurrentDate) "Today's Celestial Alignment" else "Calculated Alignment",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isCurrentDate) OrangeAccent else DarkTextSecondary,
                                fontWeight = if (isCurrentDate) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }

                    IconButton(
                        onClick = { selectedDate = selectedDate.plusDays(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("action_next_day")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Day",
                            tint = MaroonPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Quick Day Switcher Chips (Yesterday, Today, Tomorrow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Yesterday" to today.minusDays(1),
                    "Today (Current)" to today,
                    "Tomorrow" to today.plusDays(1)
                ).forEach { (label, date) ->
                    val isSelected = selectedDate == date
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDate = date },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(color = LightBorder)

            // CALCULATION 1: VEDIC TITHI (LUNAR DAY)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "☽",
                                fontSize = 22.sp,
                                color = MaroonPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "VEDIC TITHI (LUNAR DAY)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = OrangeAccent,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = "${tithi.name} (${tithi.devanagari})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaroonPrimary
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (tithi.paksha == Paksha.SHUKLA) OrangeAccent.copy(alpha = 0.15f) else MaroonPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = tithi.paksha.displayName.split(" ").first() + " Paksha",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (tithi.paksha == Paksha.SHUKLA) OrangeAccent else MaroonPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tithi Details row
                    val tithiNature = getTithiNature(tithi.number)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Lunar Cycle Position",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                            )
                            Text(
                                text = "Tithi #${tithi.number} of 30",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Tithi Classification",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                            )
                            Text(
                                text = tithiNature,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaroonPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = getTithiSignificance(tithi.number),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DarkTextSecondary,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            // CALCULATION 2: VEDIC NAKSHATRA (LUNAR MANSION)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✨",
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "VEDIC NAKSHATRA (LUNAR MANSION)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = OrangeAccent,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = "${nakshatra.sanskritName} (${nakshatra.devanagari})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaroonPrimary
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaroonPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Nakshatra #${nakshatra.number}/27",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaroonPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pada Step Indicator (1 to 4)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nakshatra Pada (Quarter):",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                            )
                            Text(
                                text = "Pada $pada of 4 (${pada * 3}°20' arc)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OrangeAccent
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (p in 1..4) {
                                val isCovered = p <= pada
                                val isCurrent = p == pada
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (isCurrent) OrangeAccent
                                            else if (isCovered) MaroonPrimary
                                            else LightBorder
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Nakshatra Attributes: Ruling Graha & Deity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Ruling Planet (Lord)",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                            )
                            Text(
                                text = "${nakshatra.rulingGraha.sanskritName} ${nakshatra.rulingGraha.symbol}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Presiding Deity / Symbol",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                            )
                            Text(
                                text = getNakshatraDeity(nakshatra),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaroonPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Calculation Metadata Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ayanamsha: ${ayanamsha.name} • ${location.cityName}",
                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                )
                Text(
                    text = "High Astronomical Precision",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OrangeAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

/**
 * Traditional classification of Tithis into 5 groups.
 */
private fun getTithiNature(tithiNumber: Int): String {
    val mod = ((tithiNumber - 1) % 5) + 1
    return when (mod) {
        1 -> "Nanda (Delightful / Joyful)"
        2 -> "Bhadra (Auspicious / Stable)"
        3 -> "Jaya (Victorious / Triumphant)"
        4 -> "Rikta (Empty / Purifying)"
        5 -> "Poorna (Complete / Fulfilling)"
        else -> "Nanda"
    }
}

/**
 * Traditional significance for each lunar day.
 */
private fun getTithiSignificance(tithiNumber: Int): String {
    return when (tithiNumber) {
        1, 16 -> "Pratipada: Ideal for commencing meditation, planting, and cultivating new intentions."
        2, 17 -> "Dwitiya: Auspicious for social agreements, creative crafts, and laying spiritual foundations."
        3, 18 -> "Tritiya: Highly propitious for ceremonies, trade, learning, and starting enduring enterprises."
        4, 19 -> "Chaturthi: Sacred to Lord Ganesha; ideal for overcoming obstacles, inner sadhana, and fasting."
        5, 20 -> "Panchami: Governed by the divine Serpents (Nagas); beneficial for medicinal remedies and study."
        6, 21 -> "Shashthi: Dedicated to Kartikeya; favorable for courage, discipline, and confronting difficulties."
        7, 22 -> "Saptami: Governed by Surya (Sun God); excellent for health rejuvenation, journeys, and vows."
        8, 23 -> "Ashtami: Powerful for worship of Durga & Shiva; ideal for spiritual discipline and quiet reflection."
        9, 24 -> "Navami: Governed by Goddess Saraswati; auspicious for spiritual inquiry and renouncing negative habits."
        10, 25 -> "Dashami: Excellent for righteous actions, community welfare, ceremonies, and pilgrimages."
        11, 26 -> "Ekadashi: Sacred fasting day dedicated to Lord Vishnu; supreme for physical cleansing and mantra chanting."
        12, 27 -> "Dwadashi: Favorable for charity, ending fasts with sattvic meals, and spiritual study."
        13, 28 -> "Trayodashi: Pradosha vrata day; potent for honoring Lord Shiva and resolving past karmic weight."
        14, 29 -> "Chaturdashi: Deep spiritual austerity and inner stillness; recommended to avoid materialistic starts."
        15 -> "Purnima (Full Moon): Peak cosmic vitality; supreme for puja, blessings, meditation, and fulfillment."
        30 -> "Amavasya (New Moon): Ideal for Pitru Tarpana, inner stillness, quiet meditation, and ancestors' remembrance."
        else -> "Auspicious for contemplation and conscious action."
    }
}

/**
 * Traditional Presiding Deity for the 27 Nakshatras.
 */
private fun getNakshatraDeity(nakshatra: Nakshatra): String {
    return when (nakshatra) {
        Nakshatra.ASHWINI -> "Ashwini Kumaras"
        Nakshatra.BHARANI -> "Yama (Justice)"
        Nakshatra.KRITTIKA -> "Agni (Fire)"
        Nakshatra.ROHINI -> "Brahma / Prajapati"
        Nakshatra.MRIGASHIRA -> "Soma (Moon)"
        Nakshatra.ARDRA -> "Rudra (Storm)"
        Nakshatra.PUNARVASU -> "Aditi (Abundance)"
        Nakshatra.PUSHYA -> "Brihaspati (Wisdom)"
        Nakshatra.ASHLESHA -> "Sarpas (Nagas)"
        Nakshatra.MAGHA -> "Pitris (Ancestors)"
        Nakshatra.PURVA_PHALGUNI -> "Bhaga (Prosperity)"
        Nakshatra.UTTARA_PHALGUNI -> "Aryaman (Dharma)"
        Nakshatra.HASTA -> "Savitr (Sun God)"
        Nakshatra.CHITRA -> "Vishvakarma (Artisan)"
        Nakshatra.SWATI -> "Vayu (Wind)"
        Nakshatra.VISHAKHA -> "Indragni (Power)"
        Nakshatra.ANURADHA -> "Mitra (Friendship)"
        Nakshatra.JYESHTHA -> "Indra (Supreme King)"
        Nakshatra.MULA -> "Nirriti (Dissolution)"
        Nakshatra.PURVA_ASHADHA -> "Apas (Sacred Waters)"
        Nakshatra.UTTARA_ASHADHA -> "Vishwadevas (Cosmic Virtues)"
        Nakshatra.SHRAVANA -> "Vishnu (Preserver)"
        Nakshatra.DHANISHTHA -> "Ashta Vasus (Light)"
        Nakshatra.SHATABHISHA -> "Varuna (Cosmic Ocean)"
        Nakshatra.PURVA_BHADRAPADA -> "Aja Ekapada (Fire Serpent)"
        Nakshatra.UTTARA_BHADRAPADA -> "Ahir Budhnya (Deep Ocean)"
        Nakshatra.REVATI -> "Pushan (Nourisher)"
    }
}
