package com.example.engines.panchanga

import com.example.core.model.*
import com.example.engines.astronomy.AstronomicalEngine
import java.time.ZonedDateTime

class PanchangaEngine(private val astronomicalEngine: AstronomicalEngine) {

    companion object {
        val YOGA_NAMES = listOf(
            "Vishkambha", "Priti", "Ayushman", "Saubhagya", "Shobhana",
            "Atiganda", "Sukarma", "Dhriti", "Shoola", "Ganda",
            "Vriddhi", "Dhruva", "Vyaghata", "Harshana", "Vajra",
            "Siddhi", "Vyatipata", "Variyan", "Parigha", "Shiva",
            "Siddha", "Sadhya", "Shubha", "Shukla", "Brahma",
            "Indra", "Vaidhriti"
        )

        val TITHI_NAMES = listOf(
            "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
            "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
            "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Purnima",
            "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami",
            "Shashthi", "Saptami", "Ashtami", "Navami", "Dashami",
            "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Amavasya"
        )

        val TITHI_DEVANAGARI = listOf(
            "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पञ्चमी",
            "षष्ठी", "सप्तमी", "अष्टमी", "नवमी", "दशमी",
            "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी", "पूर्णिमा",
            "प्रतिपदा", "द्वितीया", "तृतीया", "चतुर्थी", "पञ्चमी",
            "षष्ठी", "सप्तमी", "अष्टमी", "नवमी", "दशमी",
            "एकादशी", "द्वादशी", "त्रयोदशी", "चतुर्दशी", "अमावस्या"
        )

        val MASA_NAMES = listOf(
            "Chaitra", "Vaishakha", "Jyeshtha", "Ashadha",
            "Shravana", "Bhadrapada", "Ashvina", "Kartika",
            "Margashirsha", "Pausha", "Magha", "Phalguna"
        )

        private fun normalizeDeg(d: Double): Double {
            var v = d % 360.0
            if (v < 0) v += 360.0
            return v
        }
    }

    /**
     * Calculates complete Panchanga for a given date, location, ayanamsha, and tradition.
     */
    fun calculatePanchanga(
        dateTime: ZonedDateTime,
        latitude: Double,
        longitude: Double,
        ayanamsha: AyanamshaSystem = AyanamshaSystem.LAHIRI,
        tradition: CalendarTradition = CalendarTradition.AMANTA
    ): PanchangaData {
        val solarTimes = astronomicalEngine.calculateSolarTimes(dateTime, latitude, longitude)

        // Calculate Ayanamsha degrees
        val ayanamshaDeg = when (ayanamsha) {
            AyanamshaSystem.LAHIRI -> astronomicalEngine.calculateLahiriAyanamsha(dateTime)
            AyanamshaSystem.KP -> astronomicalEngine.calculateLahiriAyanamsha(dateTime) - (6.0 / 60.0)
            AyanamshaSystem.RAMAN -> astronomicalEngine.calculateLahiriAyanamsha(dateTime) - 1.4486
        }

        // Sidereal positions
        val sunTrop = astronomicalEngine.sunPosition(dateTime).tropicalLongitudeDeg
        val moonTrop = astronomicalEngine.moonPosition(dateTime).tropicalLongitudeDeg

        val sunSidereal = normalizeDeg(sunTrop - ayanamshaDeg)
        val moonSidereal = normalizeDeg(moonTrop - ayanamshaDeg)

        // 1. Vara (Day of Week)
        val dayOfWeek = dateTime.dayOfWeek
        val vara = when (dayOfWeek.value) {
            1 -> "Somavara" to "सोमवार" // Monday
            2 -> "Mangalavara" to "मंगलवार" // Tuesday
            3 -> "Budhavara" to "बुधवार" // Wednesday
            4 -> "Guruvara" to "गुरुवार" // Thursday
            5 -> "Shukravara" to "शुक्रवार" // Friday
            6 -> "Shanivara" to "शनिवार" // Saturday
            else -> "Ravivara" to "रविवार" // Sunday (7)
        }

        // 2. Tithi calculation
        val diffDeg = normalizeDeg(moonSidereal - sunSidereal)
        val tithiIndex = (diffDeg / 12.0).toInt().coerceIn(0, 29)
        val tithiNumber = tithiIndex + 1
        val paksha = if (tithiNumber <= 15) Paksha.SHUKLA else Paksha.KRISHNA
        val tithiInfo = TithiInfo(
            number = tithiNumber,
            name = TITHI_NAMES[tithiIndex],
            devanagari = TITHI_DEVANAGARI[tithiIndex],
            paksha = paksha
        )

        // 3. Nakshatra calculation (13°20' = 13.333333° each)
        val nakshatraSpan = 360.0 / 27.0
        val nakshatraIndex = (moonSidereal / nakshatraSpan).toInt().coerceIn(0, 26)
        val nakshatra = Nakshatra.values()[nakshatraIndex]

        // Nakshatra Pada (3°20' = 3.333333° each)
        val remDeg = moonSidereal % nakshatraSpan
        val pada = (remDeg / (nakshatraSpan / 4.0)).toInt().coerceIn(0, 3) + 1

        // 4. Yoga calculation ((Moon + Sun) / 13°20')
        val sumDeg = normalizeDeg(moonSidereal + sunSidereal)
        val yogaIndex = (sumDeg / nakshatraSpan).toInt().coerceIn(0, 26)
        val yoga = YOGA_NAMES[yogaIndex]

        // 5. Karana calculation (Half Tithi = 6°)
        val karanaIndex = (diffDeg / 6.0).toInt().coerceIn(0, 59)
        val karana = calculateKarana(karanaIndex)

        // 6. Masa (Lunar Month)
        val solarRashiIndex = (sunSidereal / 30.0).toInt().coerceIn(0, 11)
        val masaName = MASA_NAMES[solarRashiIndex]

        // 7. Ritu (6 seasons, 2 solar rashis each)
        val ritu = when (solarRashiIndex / 2) {
            0 -> "Vasanta (Spring)"
            1 -> "Grishma (Summer)"
            2 -> "Varsha (Monsoon)"
            3 -> "Sharad (Autumn)"
            4 -> "Hemanta (Pre-Winter)"
            else -> "Shishira (Winter)"
        }

        // 8. Ayana
        val ayana = if (sunSidereal >= 270.0 || sunSidereal < 90.0) "Uttarayana" else "Dakshinayana"

        // 9. Samvat eras
        val gYear = dateTime.year
        val vikramSamvat = gYear + 57
        val shakaSamvat = gYear - 78

        return PanchangaData(
            vara = vara.first,
            varaDevanagari = vara.second,
            tithi = tithiInfo,
            nakshatra = nakshatra,
            nakshatraPada = pada,
            yoga = yoga,
            karana = karana,
            masa = masaName,
            ritu = ritu,
            ayana = ayana,
            vikramSamvat = vikramSamvat,
            shakaSamvat = shakaSamvat,
            sunrise = solarTimes.sunrise,
            sunset = solarTimes.sunset,
            solarNoon = solarTimes.solarNoon
        )
    }

    private fun calculateKarana(index: Int): String {
        // Karana 1: Kimstughna (Shukla Pratipada 1st half)
        // Karana 2-57: 8 cycles of 7 movable karanas (Bava, Balava, Kaulava, Taitila, Garija, Vanija, Vishti)
        // Karana 58: Shakuni
        // Karana 59: Chatushpada
        // Karana 60: Naga
        val movable = listOf("Bava", "Balava", "Kaulava", "Taitila", "Garija", "Vanija", "Vishti (Bhadra)")
        return when (index) {
            0 -> "Kinstughna"
            57 -> "Shakuni"
            58 -> "Chatushpada"
            59 -> "Naga"
            else -> movable[(index - 1) % 7]
        }
    }
}
