package com.example.engines.astronomy

import com.example.core.model.DailyVedicProfile
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Prahara (3-hour Vedic time division).
 */
data class VedicPraharaInfo(
    val praharaNumber: Int, // 1 to 8
    val englishName: String,
    val devanagariName: String,
    val isDaytime: Boolean,
    val significance: String
)

/**
 * Exact Vedic Time representation:
 * - 60 Ghadis (Ghatis) in a full solar day (Ahoratra)
 * - 1 Ghadi = 24 minutes = 1440 seconds
 * - 1 Pala (Vighati) = 24 seconds
 * - 1 Vipala = 0.4 seconds
 */
data class VedicTimeData(
    val ghadi: Int,
    val pala: Int,
    val vipala: Int,
    val formattedGhadiPala: String,
    val devanagariGhadiPala: String,
    val ishtaKaalaDuration: Duration,
    val prahara: VedicPraharaInfo,
    val dayProgressPercent: Float,
    val sunrise: ZonedDateTime,
    val sunset: ZonedDateTime,
    val nextSunrise: ZonedDateTime,
    val isDayTime: Boolean
)

/**
 * Dynamic Vedic greeting based on current local time.
 */
data class VedicGreeting(
    val englishTitle: String,
    val vedicTitle: String,
    val subtitle: String,
    val iconName: String
)

/**
 * Complete Vedic date representation alongside regular Gregorian date.
 */
data class VedicDateData(
    val gregorianDateFormatted: String,
    val gregorianTimeFormatted: String,
    val timezoneDisplay: String,
    val vikramSamvat: Int,
    val shakaSamvat: Int,
    val masa: String,
    val paksha: String,
    val tithiName: String,
    val tithiDevanagari: String,
    val varaName: String,
    val varaDevanagari: String,
    val nakshatraName: String,
    val nakshatraPada: Int,
    val ritu: String,
    val ayana: String,
    val suryaRashi: String,
    val chandraRashi: String
)

/**
 * Calculator for real-time Vedic Time (Ghadi, Pala, Prahara), Vedic Calendar date,
 * and contextual Vedic greetings.
 */
object VedicTimeCalculator {

    /**
     * Calculates Vedic Time (Ghadi, Pala, Vipala) elapsed from local sunrise (Ishta Kaala).
     */
    fun calculateVedicTime(
        currentTime: ZonedDateTime,
        sunrise: ZonedDateTime,
        sunset: ZonedDateTime
    ): VedicTimeData {
        // Find the applicable sunrise for the current Vedic day:
        // A Vedic day begins at sunrise. If current time is before today's sunrise,
        // it belongs to the previous day's cycle.
        val effectiveSunrise = if (currentTime.isBefore(sunrise)) {
            sunrise.minusDays(1)
        } else {
            sunrise
        }

        val effectiveNextSunrise = effectiveSunrise.plusDays(1)
        val elapsedDuration = Duration.between(effectiveSunrise, currentTime)
        val elapsedSeconds = elapsedDuration.seconds.coerceAtLeast(0L)
        val totalDaySeconds = Duration.between(effectiveSunrise, effectiveNextSunrise).seconds.coerceAtLeast(86400L)

        // 60 Ghadis in totalDaySeconds
        // Average 1 Ghadi = 1440 seconds
        val ghadiExact = (elapsedSeconds.toDouble() / totalDaySeconds.toDouble()) * 60.0
        val ghadi = (ghadiExact.toInt()).coerceIn(0, 59)
        val palaExact = (ghadiExact - ghadi) * 60.0
        val pala = (palaExact.toInt()).coerceIn(0, 59)
        val vipalaExact = (palaExact - pala) * 60.0
        val vipala = (vipalaExact.toInt()).coerceIn(0, 59)

        val isDay = !currentTime.isBefore(sunrise) && currentTime.isBefore(sunset)

        // Determine Prahara (8 Praharas in a day, each ~7.5 Ghadis / ~3 hours)
        val praharaIndex = (ghadiExact / 7.5).toInt().coerceIn(0, 7)
        val prahara = getPraharaInfo(praharaIndex + 1, isDay)

        val dayProgress = (elapsedSeconds.toFloat() / totalDaySeconds.toFloat()).coerceIn(0f, 1f)

        val devanagariGhadi = toDevanagariNumerals(ghadi)
        val devanagariPala = toDevanagariNumerals(pala)
        val devanagariVipala = toDevanagariNumerals(vipala)

        return VedicTimeData(
            ghadi = ghadi,
            pala = pala,
            vipala = vipala,
            formattedGhadiPala = "$ghadi Ghadi, $pala Pala, $vipala Vipala",
            devanagariGhadiPala = "$devanagariGhadi घटी, $devanagariPala पल, $devanagariVipala विपल",
            ishtaKaalaDuration = elapsedDuration,
            prahara = prahara,
            dayProgressPercent = dayProgress,
            sunrise = effectiveSunrise,
            sunset = sunset,
            nextSunrise = effectiveNextSunrise,
            isDayTime = isDay
        )
    }

    /**
     * Generates a context-aware Vedic greeting based on the current local time.
     */
    fun getVedicGreeting(time: LocalTime): VedicGreeting {
        val hour = time.hour
        val minute = time.minute
        val timeInMinutes = hour * 60 + minute

        return when {
            // Brahma Muhurta (03:30 to 05:30)
            timeInMinutes in 210 until 330 -> VedicGreeting(
                englishTitle = "BRAHMA MUHURTA",
                vedicTitle = "शुभ ब्राह्म मुहूर्त • Divine Dawn",
                subtitle = "Sacred hours of spiritual alignment, heightened prana, and serene meditation.",
                iconName = "lotus"
            )
            // Morning / Pratah (05:30 to 12:00)
            timeInMinutes in 330 until 720 -> VedicGreeting(
                englishTitle = "GOOD MORNING",
                vedicTitle = "शुभ प्रभात • Shubh Prabhat",
                subtitle = "May the radiant Surya Deva bestow vitality, wisdom, and auspicious success.",
                iconName = "sun"
            )
            // Afternoon / Madhyahan (12:00 to 16:30)
            timeInMinutes in 720 until 990 -> VedicGreeting(
                englishTitle = "GOOD AFTERNOON",
                vedicTitle = "शुभ मध्याह्न • Shubh Madhyahan",
                subtitle = "Peak solar energy for focused dharmic actions and purposeful execution.",
                iconName = "solar"
            )
            // Dusk / Sayankal / Godhuli (16:30 to 19:30)
            timeInMinutes in 990 until 1170 -> VedicGreeting(
                englishTitle = "GOOD EVENING",
                vedicTitle = "शुभ संध्या • Shubh Sandhya",
                subtitle = "Godhuli twilight transition — ideal for evening aarti, gratitude, and quiet mind.",
                iconName = "sunset"
            )
            // Night / Ratri (19:30 to 23:30)
            timeInMinutes in 1170 until 1410 -> VedicGreeting(
                englishTitle = "PEACEFUL NIGHT",
                vedicTitle = "शुभ रात्रि • Shubh Ratri",
                subtitle = "Restful lunar energy for cellular rejuvenation, mantra reflection, and peace.",
                iconName = "moon"
            )
            // Deep Midnight / Nishita Kaal (23:30 to 03:30)
            else -> VedicGreeting(
                englishTitle = "SACRED MIDNIGHT",
                vedicTitle = "निशीथ काल • Nishita Kaal",
                subtitle = "Deep cosmic silence under the stars. Blessed space for inner stillness and contemplation.",
                iconName = "stars"
            )
        }
    }

    /**
     * Extracts and formats the complete Vedic date alongside regular date.
     */
    fun getVedicDateData(dailyProfile: DailyVedicProfile): VedicDateData {
        val zdt = dailyProfile.currentZonedDateTime
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
        val zoneOffset = zdt.offset.toString()
        val zoneIdName = zdt.zone.id

        val p = dailyProfile.panchanga

        return VedicDateData(
            gregorianDateFormatted = zdt.format(dateFormatter),
            gregorianTimeFormatted = zdt.format(timeFormatter),
            timezoneDisplay = "$zoneIdName (UTC$zoneOffset)",
            vikramSamvat = p.vikramSamvat,
            shakaSamvat = p.shakaSamvat,
            masa = p.masa,
            paksha = p.tithi.paksha.displayName,
            tithiName = p.tithi.name,
            tithiDevanagari = p.tithi.devanagari,
            varaName = p.vara,
            varaDevanagari = p.varaDevanagari,
            nakshatraName = p.nakshatra.sanskritName,
            nakshatraPada = p.nakshatraPada,
            ritu = p.ritu,
            ayana = p.ayana,
            suryaRashi = "Surya in ${dailyProfile.panchanga.nakshatra.rulingGraha.displayName} domain",
            chandraRashi = p.tithi.paksha.devanagari
        )
    }

    private fun getPraharaInfo(number: Int, isDay: Boolean): VedicPraharaInfo {
        return when (number) {
            1 -> VedicPraharaInfo(1, "1st Prahar: Pratah (Morning)", "प्रथम प्रहर (प्रातः)", true, "Study, prayer, and morning sadhana")
            2 -> VedicPraharaInfo(2, "2nd Prahar: Madhyahan (Noon)", "द्वितीय प्रहर (मध्याह्न)", true, "Active duties, transactions, and karma")
            3 -> VedicPraharaInfo(3, "3rd Prahar: Aparahna (Afternoon)", "तृतीय प्रहर (अपराह्न)", true, "Completion of daily enterprise")
            4 -> VedicPraharaInfo(4, "4th Prahar: Sayankal (Sunset)", "चतुर्थ प्रहर (सायंकाल)", true, "Godhuli, sandhya vandanam, peace")
            5 -> VedicPraharaInfo(5, "5th Prahar: Pradosha (Evening)", "पंचम प्रहर (प्रदोष)", false, "Family satsang, light meal, reflection")
            6 -> VedicPraharaInfo(6, "6th Prahar: Nisheetha (Night)", "षष्ठ प्रहर (निशीथ)", false, "Rest, introspection, and sleep")
            7 -> VedicPraharaInfo(7, "7th Prahar: Triyama (Late Night)", "सप्तम प्रहर (त्रियामा)", false, "Deep slumber and silent contemplation")
            8 -> VedicPraharaInfo(8, "8th Prahar: Usha (Pre-Dawn)", "अष्टम प्रहर (उषाकाल)", false, "Brahma Muhurta awakening and prana flow")
            else -> VedicPraharaInfo(number, "$number Prahara", "प्रहर $number", isDay, "General Vedic time segment")
        }
    }

    private fun toDevanagariNumerals(n: Int): String {
        val devanagariDigits = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')
        return n.toString().map { devanagariDigits[it - '0'] }.joinToString("")
    }
}
