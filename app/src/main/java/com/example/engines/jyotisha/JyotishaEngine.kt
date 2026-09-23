package com.example.engines.jyotisha

import com.example.core.model.*
import com.example.engines.astronomy.AstronomicalEngine
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

class JyotishaEngine(private val astronomicalEngine: AstronomicalEngine) {

    companion object {
        private const val DEG2RAD = Math.PI / 180.0
        private const val RAD2DEG = 180.0 / Math.PI

        private fun normalizeDeg(d: Double): Double {
            var v = d % 360.0
            if (v < 0) v += 360.0
            return v
        }
    }

    data class NatalChartSummary(
        val lagnaRashi: Rashi,
        val lagnaDegrees: Double,
        val suryaRashi: Rashi,
        val chandraRashi: Rashi,
        val janmaNakshatra: Nakshatra,
        val janmaNakshatraPada: Int,
        val planetaryPositions: Map<Graha, Double> // Sidereal degrees
    )

    /**
     * Calculates the complete natal horoscope coordinates including Lagna (Ascendant),
     * Surya Rashi, Chandra Rashi, and Janma Nakshatra.
     */
    fun calculateNatalChart(
        birthDate: LocalDate,
        birthTime: LocalTime,
        location: LocationData,
        ayanamsha: AyanamshaSystem = AyanamshaSystem.LAHIRI
    ): NatalChartSummary {
        val zoneId = try {
            ZoneId.of(location.timezoneId)
        } catch (e: Exception) {
            ZoneId.of("UTC")
        }
        val birthZdt = ZonedDateTime.of(birthDate, birthTime, zoneId)

        val ayanamshaDeg = when (ayanamsha) {
            AyanamshaSystem.LAHIRI -> astronomicalEngine.calculateLahiriAyanamsha(birthZdt)
            AyanamshaSystem.KP -> astronomicalEngine.calculateLahiriAyanamsha(birthZdt) - 0.1
            AyanamshaSystem.RAMAN -> astronomicalEngine.calculateLahiriAyanamsha(birthZdt) - 1.4486
        }

        // Planetary longitudes
        val positions = astronomicalEngine.planetaryPositions(birthZdt, ayanamshaDeg)
        val siderealPositions = positions.mapValues { it.value.siderealLongitudeDeg }

        val sunSidereal = siderealPositions[Graha.SURYA] ?: 0.0
        val moonSidereal = siderealPositions[Graha.CHANDRA] ?: 0.0

        // Surya Rashi (1-12)
        val suryaRashiIdx = (sunSidereal / 30.0).toInt().coerceIn(0, 11)
        val suryaRashi = Rashi.values()[suryaRashiIdx]

        // Chandra Rashi (1-12)
        val chandraRashiIdx = (moonSidereal / 30.0).toInt().coerceIn(0, 11)
        val chandraRashi = Rashi.values()[chandraRashiIdx]

        // Janma Nakshatra & Pada
        val nakshatraSpan = 360.0 / 27.0
        val nakshatraIdx = (moonSidereal / nakshatraSpan).toInt().coerceIn(0, 26)
        val janmaNakshatra = Nakshatra.values()[nakshatraIdx]
        val remDeg = moonSidereal % nakshatraSpan
        val pada = (remDeg / (nakshatraSpan / 4.0)).toInt().coerceIn(0, 3) + 1

        // Calculate Lagna (Ascendant)
        val lagnaSidereal = calculateLagna(birthZdt, location.latitude, location.longitude, ayanamshaDeg)
        val lagnaRashiIdx = (lagnaSidereal / 30.0).toInt().coerceIn(0, 11)
        val lagnaRashi = Rashi.values()[lagnaRashiIdx]

        return NatalChartSummary(
            lagnaRashi = lagnaRashi,
            lagnaDegrees = lagnaSidereal % 30.0,
            suryaRashi = suryaRashi,
            chandraRashi = chandraRashi,
            janmaNakshatra = janmaNakshatra,
            janmaNakshatraPada = pada,
            planetaryPositions = siderealPositions
        )
    }

    /**
     * Generates a 12-Bhava North Indian Diamond chart structure mapping Rashis and Grahas.
     */
    fun generateKundaliChart(natalSummary: NatalChartSummary): KundaliChartData {
        val houseSignifications = listOf(
            Triple("Tanu Bhava", "तनू भाव", "Self, vitality, physical constitution, character & life orientation"),
            Triple("Dhana Bhava", "धन भाव", "Accumulated wealth, speech, nourishment, family lineage & values"),
            Triple("Sahaja Bhava", "सहज भाव", "Courage, valor, siblings, communications & deliberate efforts"),
            Triple("Sukha Bhava", "सुख भाव", "Mother, inner happiness, emotional peace, vehicles & real estate"),
            Triple("Putra Bhava", "पुत्र भाव", "Children, intelligence, Purva Punya, creativity & mantric wisdom"),
            Triple("Ari Bhava", "अरि भाव", "Obstacles, debts, competitive stamina, healing & daily service"),
            Triple("Yuvati Bhava", "युवति भाव", "Spouse, intimate partnerships, business alliances & public dealings"),
            Triple("Randhra Bhava", "रन्ध्र भाव", "Longevity, sudden transformation, mystical sciences & deep secrets"),
            Triple("Bhagya Bhava", "भाग्य भाव", "Dharma, fortune, father, guru, higher philosophy & divine grace"),
            Triple("Karma Bhava", "कर्म भाव", "Profession, leadership, societal status, righteous action & honor"),
            Triple("Labha Bhava", "लाभ भाव", "Gains, fulfillment of desires, prosperity, friends & networks"),
            Triple("Vyaya Bhava", "व्यय भाव", "Spiritual liberation (Moksha), meditation, foreign travels & detachment")
        )

        val karakas = listOf(
            Graha.SURYA, Graha.GURU, Graha.MANGALA, Graha.CHANDRA,
            Graha.GURU, Graha.SHANI, Graha.SHUKRA, Graha.SHANI,
            Graha.GURU, Graha.SURYA, Graha.GURU, Graha.KETU
        )

        val lagnaIdx = natalSummary.lagnaRashi.ordinal
        val allRashis = Rashi.values()

        // Map each planet to its house
        val planetsInHouse = mutableMapOf<Int, MutableList<Graha>>()
        for (i in 1..12) planetsInHouse[i] = mutableListOf()

        natalSummary.planetaryPositions.forEach { (graha, siderealDeg) ->
            val rashiIdx = (siderealDeg / 30.0).toInt().coerceIn(0, 11)
            val houseNum = ((rashiIdx - lagnaIdx + 12) % 12) + 1
            planetsInHouse[houseNum]?.add(graha)
        }

        val bhavas = (1..12).map { houseNum ->
            val rashi = allRashis[(lagnaIdx + (houseNum - 1)) % 12]
            val (name, devanagari, sigs) = houseSignifications[houseNum - 1]
            BhavaDetail(
                houseNumber = houseNum,
                traditionalName = name,
                devanagariName = devanagari,
                rashi = rashi,
                occupyingGrahas = planetsInHouse[houseNum] ?: emptyList(),
                significations = sigs,
                karakaGraha = karakas[houseNum - 1]
            )
        }

        return KundaliChartData(
            lagnaRashi = natalSummary.lagnaRashi,
            lagnaDegrees = natalSummary.lagnaDegrees,
            bhavas = bhavas,
            grahaPositions = natalSummary.planetaryPositions
        )
    }

    /**
     * Derives the eastern ascending degree on the ecliptic (Lagna / Ascendant)
     * using Local Sidereal Time (RAMC) and geographical latitude.
     */
    private fun calculateLagna(
        dateTime: ZonedDateTime,
        latitude: Double,
        longitude: Double,
        ayanamshaDeg: Double
    ): Double {
        val utc = dateTime.withZoneSameInstant(java.time.ZoneOffset.UTC)
        val jd = toJulianDay(utc)
        val t = (jd - 2451545.0) / 36525.0

        // Greenwich Mean Sidereal Time (GMST in degrees)
        var gmst = 280.46061837 + 360.98564736629 * (jd - 2451545.0) + 0.000387933 * t * t
        gmst = normalizeDeg(gmst)

        // Local Sidereal Time (LST in degrees)
        val lst = normalizeDeg(gmst + longitude)
        val lstRad = lst * DEG2RAD

        // True obliquity of ecliptic
        val epsRad = 23.4392911 * DEG2RAD
        val latRad = latitude * DEG2RAD

        // Ascendant formula: tan(Asc) = -cos(RAMC) / (sin(RAMC)*cos(eps) + tan(lat)*sin(eps))
        val y = -cos(lstRad)
        val x = sin(lstRad) * cos(epsRad) + tan(latRad) * sin(epsRad)
        var ascTropical = atan2(y, x) * RAD2DEG
        ascTropical = normalizeDeg(ascTropical)

        // Convert to Sidereal (Nirayana) Lagna
        return normalizeDeg(ascTropical - ayanamshaDeg)
    }

    private fun toJulianDay(utc: ZonedDateTime): Double {
        var year = utc.year
        var month = utc.monthValue
        val day = utc.dayOfMonth.toDouble() +
                (utc.hour + utc.minute / 60.0 + utc.second / 3600.0) / 24.0

        if (month <= 2) {
            year -= 1
            month += 12
        }

        val a = floor(year / 100.0)
        val b = 2 - a + floor(a / 4.0)

        return floor(365.25 * (year + 4716)) + floor(30.6001 * (month + 1)) + day + b - 1524.5
    }
}
