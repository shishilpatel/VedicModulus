package com.example.engines.hora

import com.example.core.model.Graha
import com.example.core.model.HoraSlot
import com.example.engines.astronomy.SolarTimes
import java.time.Duration
import java.time.ZonedDateTime

class HoraEngine {

    companion object {
        // Chaldean planetary order from slowest to fastest
        private val CHALDEAN_ORDER = listOf(
            Graha.SHANI,
            Graha.GURU,
            Graha.MANGALA,
            Graha.SURYA,
            Graha.SHUKRA,
            Graha.BUDHA,
            Graha.CHANDRA
        )

        private fun getFirstHoraLord(dayOfWeek: Int): Graha {
            return when (dayOfWeek) {
                1 -> Graha.CHANDRA // Monday
                2 -> Graha.MANGALA // Tuesday
                3 -> Graha.BUDHA   // Wednesday
                4 -> Graha.GURU    // Thursday
                5 -> Graha.SHUKRA  // Friday
                6 -> Graha.SHANI   // Saturday
                else -> Graha.SURYA // Sunday (7)
            }
        }
    }

    /**
     * Calculates 24 Horas (12 Day Horas + 12 Night Horas) starting at Sunrise.
     */
    fun calculateDailyHoras(
        solarTimes: SolarTimes,
        nextDaySunrise: ZonedDateTime,
        dayOfWeek: Int
    ): List<HoraSlot> {
        val horas = mutableListOf<HoraSlot>()
        val sunrise = solarTimes.sunrise
        val sunset = solarTimes.sunset

        val dayDurationSeconds = Duration.between(sunrise, sunset).seconds.toDouble()
        val dayHoraSeconds = dayDurationSeconds / 12.0

        val firstLord = getFirstHoraLord(dayOfWeek)
        var currentIndex = CHALDEAN_ORDER.indexOf(firstLord)

        // 12 Daytime Horas
        for (i in 0 until 12) {
            val start = sunrise.plusSeconds((i * dayHoraSeconds).toLong())
            val end = if (i == 11) sunset else sunrise.plusSeconds(((i + 1) * dayHoraSeconds).toLong())
            horas.add(
                HoraSlot(
                    rulingGraha = CHALDEAN_ORDER[currentIndex % CHALDEAN_ORDER.size],
                    startTime = start,
                    endTime = end,
                    horaNumber = i + 1
                )
            )
            currentIndex++
        }

        // 12 Nighttime Horas
        val nightDurationSeconds = Duration.between(sunset, nextDaySunrise).seconds.toDouble()
        val nightHoraSeconds = nightDurationSeconds / 12.0

        for (i in 0 until 12) {
            val start = sunset.plusSeconds((i * nightHoraSeconds).toLong())
            val end = if (i == 11) nextDaySunrise else sunset.plusSeconds(((i + 1) * nightHoraSeconds).toLong())
            horas.add(
                HoraSlot(
                    rulingGraha = CHALDEAN_ORDER[currentIndex % CHALDEAN_ORDER.size],
                    startTime = start,
                    endTime = end,
                    horaNumber = 13 + i
                )
            )
            currentIndex++
        }

        return horas
    }

    fun findActiveHora(horas: List<HoraSlot>, now: ZonedDateTime): HoraSlot {
        return horas.firstOrNull { slot ->
            !now.isBefore(slot.startTime) && now.isBefore(slot.endTime)
        } ?: horas.first()
    }
}
