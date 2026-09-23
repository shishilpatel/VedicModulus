package com.example.engines.muhurta

import com.example.core.model.TimingWindow
import com.example.engines.astronomy.SolarTimes
import java.time.Duration
import java.time.ZonedDateTime

import com.example.engines.mantra.MantraEngine

class MuhurtaEngine {
    private val mantraEngine = MantraEngine()

    /**
     * Calculates auspicious and sacred Muhurta windows:
     * - Brahma Muhurta (4th prahara of night / 2 muhurtas prior to sunrise)
     * - Abhijit Muhurta (8th daytime muhurta centered on solar noon)
     * - Pradosha Muhurta (evening twilight after sunset)
     * - Nishita Muhurta (exact midnight muhurta)
     */
    fun calculateMuhurtas(
        solarTimes: SolarTimes,
        nextDaySunrise: ZonedDateTime,
        dayOfWeek: Int
    ): List<TimingWindow> {
        val sunrise = solarTimes.sunrise
        val sunset = solarTimes.sunset
        val solarNoon = solarTimes.solarNoon

        val dayDurationSeconds = Duration.between(sunrise, sunset).seconds.toDouble()
        val daytimeMuhurtaSeconds = dayDurationSeconds / 15.0 // 15 Muhurtas per daytime

        val nightDurationSeconds = Duration.between(sunset, nextDaySunrise).seconds.toDouble()
        val nighttimeMuhurtaSeconds = nightDurationSeconds / 15.0

        // 1. Brahma Muhurta: 14th Muhurta of night (96 to 48 min before sunrise)
        val brahmaStart = sunrise.minusSeconds((2 * nighttimeMuhurtaSeconds).toLong())
        val brahmaEnd = sunrise.minusSeconds(nighttimeMuhurtaSeconds.toLong())
        val brahma = TimingWindow(
            name = "Brahma Muhurta",
            devanagari = "ब्रह्म मुहूर्त",
            startTime = brahmaStart,
            endTime = brahmaEnd,
            isAuspicious = true,
            deity = mantraEngine.getDeityForMuhurta("Brahma Muhurta")
        )

        // 2. Abhijit Muhurta: 8th Muhurta of daytime (starts at 7 * daytimeMuhurta)
        val abhijitStart = sunrise.plusSeconds((7 * daytimeMuhurtaSeconds).toLong())
        val abhijitEnd = sunrise.plusSeconds((8 * daytimeMuhurtaSeconds).toLong())
        // In classical Jyotisha, Abhijit is not favored on Wednesdays (Budhavara = 3)
        val abhijit = TimingWindow(
            name = "Abhijit Muhurta",
            devanagari = "अभिजित मुहूर्त",
            startTime = abhijitStart,
            endTime = abhijitEnd,
            isAuspicious = (dayOfWeek != 3),
            deity = mantraEngine.getDeityForMuhurta("Abhijit Muhurta")
        )

        // 3. Pradosha: Sunset to 2 Muhurtas post sunset
        val pradoshaEnd = sunset.plusSeconds((2 * nighttimeMuhurtaSeconds).toLong())
        val pradosha = TimingWindow(
            name = "Pradosha",
            devanagari = "प्रदोष काल",
            startTime = sunset,
            endTime = pradoshaEnd,
            isAuspicious = true,
            deity = mantraEngine.getDeityForMuhurta("Pradosha")
        )

        // 4. Nishita: 8th Muhurta of night (centered on solar midnight)
        val nishitaStart = sunset.plusSeconds((7 * nighttimeMuhurtaSeconds).toLong())
        val nishitaEnd = sunset.plusSeconds((8 * nighttimeMuhurtaSeconds).toLong())
        val nishita = TimingWindow(
            name = "Nishita Muhurta",
            devanagari = "निशीथ मुहूर्त",
            startTime = nishitaStart,
            endTime = nishitaEnd,
            isAuspicious = true,
            deity = mantraEngine.getDeityForMuhurta("Nishita Muhurta")
        )

        return listOf(brahma, abhijit, pradosha, nishita)
    }
}
