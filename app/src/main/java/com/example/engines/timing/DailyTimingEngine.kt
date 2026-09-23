package com.example.engines.timing

import com.example.core.model.TimingWindow
import com.example.engines.astronomy.SolarTimes
import java.time.Duration

class DailyTimingEngine {

    /**
     * Calculates Rahu Kalam, Yamaganda, and Gulika Kalam windows based on precise local sunrise/sunset.
     */
    fun calculateInauspiciousWindows(solarTimes: SolarTimes, dayOfWeek: Int): Triple<TimingWindow, TimingWindow, TimingWindow> {
        val sunrise = solarTimes.sunrise
        val sunset = solarTimes.sunset
        val dayDurationSeconds = Duration.between(sunrise, sunset).seconds.toDouble()
        val oneEighthSeconds = dayDurationSeconds / 8.0

        fun getEighthPart(partIndex1Based: Int): Pair<java.time.ZonedDateTime, java.time.ZonedDateTime> {
            val start = sunrise.plusSeconds(((partIndex1Based - 1) * oneEighthSeconds).toLong())
            val end = sunrise.plusSeconds((partIndex1Based * oneEighthSeconds).toLong())
            return start to end
        }

        // Rahu Kalam 1-based eighth part
        val rahuPart = when (dayOfWeek) {
            1 -> 2 // Monday
            2 -> 7 // Tuesday
            3 -> 5 // Wednesday
            4 -> 6 // Thursday
            5 -> 4 // Friday
            6 -> 3 // Saturday
            else -> 8 // Sunday (7)
        }
        val (rahuStart, rahuEnd) = getEighthPart(rahuPart)
        val rahuWindow = TimingWindow(
            name = "Rahu Kalam",
            devanagari = "राहु काल",
            startTime = rahuStart,
            endTime = rahuEnd,
            isAuspicious = false
        )

        // Yamaganda 1-based eighth part
        val yamaPart = when (dayOfWeek) {
            1 -> 4 // Monday
            2 -> 3 // Tuesday
            3 -> 2 // Wednesday
            4 -> 1 // Thursday
            5 -> 7 // Friday
            6 -> 6 // Saturday
            else -> 5 // Sunday
        }
        val (yamaStart, yamaEnd) = getEighthPart(yamaPart)
        val yamaWindow = TimingWindow(
            name = "Yamaganda",
            devanagari = "यमगण्ड",
            startTime = yamaStart,
            endTime = yamaEnd,
            isAuspicious = false
        )

        // Gulika Kalam 1-based eighth part
        val gulikaPart = when (dayOfWeek) {
            1 -> 6 // Monday
            2 -> 5 // Tuesday
            3 -> 4 // Wednesday
            4 -> 3 // Thursday
            5 -> 2 // Friday
            6 -> 1 // Saturday
            else -> 7 // Sunday
        }
        val (gulikaStart, gulikaEnd) = getEighthPart(gulikaPart)
        val gulikaWindow = TimingWindow(
            name = "Gulika Kalam",
            devanagari = "गुलिक काल",
            startTime = gulikaStart,
            endTime = gulikaEnd,
            isAuspicious = false
        )

        return Triple(rahuWindow, yamaWindow, gulikaWindow)
    }
}
