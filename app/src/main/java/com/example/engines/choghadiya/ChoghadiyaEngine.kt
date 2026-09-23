package com.example.engines.choghadiya

import com.example.core.model.ChoghadiyaSlot
import com.example.core.model.ChoghadiyaType
import com.example.engines.astronomy.SolarTimes
import java.time.Duration
import java.time.ZonedDateTime

class ChoghadiyaEngine {

    companion object {
        // Continuous cycle of 7 Choghadiyas
        private val CYCLE = listOf(
            ChoghadiyaType.UDVEG,
            ChoghadiyaType.CHAL,
            ChoghadiyaType.LABH,
            ChoghadiyaType.AMRIT,
            ChoghadiyaType.KAAL,
            ChoghadiyaType.SHUBH,
            ChoghadiyaType.ROG
        )

        // First daytime Choghadiya by DayOfWeek (1 = Mon .. 7 = Sun)
        private fun getDayStartType(dayOfWeek: Int): ChoghadiyaType {
            return when (dayOfWeek) {
                1 -> ChoghadiyaType.AMRIT // Monday (Moon)
                2 -> ChoghadiyaType.ROG   // Tuesday (Mars)
                3 -> ChoghadiyaType.LABH  // Wednesday (Mercury)
                4 -> ChoghadiyaType.SHUBH // Thursday (Jupiter)
                5 -> ChoghadiyaType.CHAL  // Friday (Venus)
                6 -> ChoghadiyaType.KAAL  // Saturday (Saturn)
                else -> ChoghadiyaType.UDVEG // Sunday (Sun)
            }
        }

        // First nighttime Choghadiya by DayOfWeek
        private fun getNightStartType(dayOfWeek: Int): ChoghadiyaType {
            return when (dayOfWeek) {
                1 -> ChoghadiyaType.CHAL  // Monday night
                2 -> ChoghadiyaType.KAAL  // Tuesday night
                3 -> ChoghadiyaType.UDVEG // Wednesday night
                4 -> ChoghadiyaType.ROG   // Thursday night
                5 -> ChoghadiyaType.LABH  // Friday night
                6 -> ChoghadiyaType.AMRIT // Saturday night
                else -> ChoghadiyaType.SHUBH // Sunday night
            }
        }
    }

    /**
     * Calculates all 16 Choghadiya slots (8 Day + 8 Night) for the solar day.
     */
    fun calculateDailyChoghadiya(
        solarTimes: SolarTimes,
        nextDaySunrise: ZonedDateTime,
        dayOfWeek: Int
    ): List<ChoghadiyaSlot> {
        val slots = mutableListOf<ChoghadiyaSlot>()

        val sunrise = solarTimes.sunrise
        val sunset = solarTimes.sunset

        // Daytime 8 parts (Dinamana / 8)
        val dayDurationSeconds = Duration.between(sunrise, sunset).seconds.toDouble()
        val dayStepSeconds = dayDurationSeconds / 8.0

        val dayStart = getDayStartType(dayOfWeek)
        var currentCycleIdx = CYCLE.indexOf(dayStart)

        for (i in 0 until 8) {
            val start = sunrise.plusSeconds((i * dayStepSeconds).toLong())
            val end = if (i == 7) sunset else sunrise.plusSeconds(((i + 1) * dayStepSeconds).toLong())
            slots.add(
                ChoghadiyaSlot(
                    type = CYCLE[currentCycleIdx % CYCLE.size],
                    startTime = start,
                    endTime = end,
                    isNight = false
                )
            )
            currentCycleIdx++
        }

        // Nighttime 8 parts (Ratrimana / 8)
        val nightDurationSeconds = Duration.between(sunset, nextDaySunrise).seconds.toDouble()
        val nightStepSeconds = nightDurationSeconds / 8.0

        val nightStart = getNightStartType(dayOfWeek)
        currentCycleIdx = CYCLE.indexOf(nightStart)

        for (i in 0 until 8) {
            val start = sunset.plusSeconds((i * nightStepSeconds).toLong())
            val end = if (i == 7) nextDaySunrise else sunset.plusSeconds(((i + 1) * nightStepSeconds).toLong())
            slots.add(
                ChoghadiyaSlot(
                    type = CYCLE[currentCycleIdx % CYCLE.size],
                    startTime = start,
                    endTime = end,
                    isNight = true
                )
            )
            currentCycleIdx++
        }

        return slots
    }

    /**
     * Finds the currently active Choghadiya slot from the daily schedule.
     */
    fun findActiveChoghadiya(slots: List<ChoghadiyaSlot>, now: ZonedDateTime): ChoghadiyaSlot {
        return slots.firstOrNull { slot ->
            !now.isBefore(slot.startTime) && now.isBefore(slot.endTime)
        } ?: slots.first()
    }
}
