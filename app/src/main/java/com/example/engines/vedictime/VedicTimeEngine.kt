package com.example.engines.vedictime

import com.example.engines.astronomy.SolarTimes
import java.time.Duration
import java.time.ZonedDateTime

/**
 * Traditional Vedic day divisions (Prahara and Sandhyas) derived directly from solar events.
 */
class VedicTimeEngine {

    enum class VedicPeriod(val displayName: String, val devanagari: String, val description: String) {
        BRAHMA_MUHURTA("Brahma Muhurta", "ब्रह्म मुहूर्त", "Ambrosial hour of consciousness (96-48 min before sunrise)"),
        USHA_KALA("Usha Kala / Pratah Sandhya", "उषा काल", "Dawn twilight preceding sunrise"),
        PRATAH_KALA("Pratah Kala", "प्रातः काल", "Early morning post sunrise"),
        SANGAVA("Sangava", "संगव", "Forenoon & active preparation period"),
        MADHYAHNA("Madhyahna", "मध्याह्न", "Solar midday & peak vitality"),
        APARAHNA("Aparahna", "अपराह्न", "Afternoon introspection period"),
        SAYAHNA("Sayahna", "सायाह्न", "Late afternoon & evening twilight transition"),
        PRADOSHA("Pradosha", "प्रदोष", "Twilight period following sunset"),
        RATRI("Nishita / Ratri", "रात्रि", "Night contemplation & quietude")
    }

    /**
     * Determines the currently active Vedic period for a given local time and solar events.
     */
    fun getCurrentVedicPeriod(now: ZonedDateTime, solarTimes: SolarTimes): VedicPeriod {
        val sunrise = solarTimes.sunrise
        val sunset = solarTimes.sunset

        val dinamanaSeconds = Duration.between(sunrise, sunset).seconds.toDouble()
        val oneFifthDay = dinamanaSeconds / 5.0

        val brahmaStart = sunrise.minusMinutes(96)
        val brahmaEnd = sunrise.minusMinutes(48)

        return when {
            now.isAfter(brahmaStart) && now.isBefore(brahmaEnd) -> VedicPeriod.BRAHMA_MUHURTA
            now.isAfter(brahmaEnd) && now.isBefore(sunrise) -> VedicPeriod.USHA_KALA
            now.isAfter(sunrise) && now.isBefore(sunrise.plusSeconds(oneFifthDay.toLong())) -> VedicPeriod.PRATAH_KALA
            now.isAfter(sunrise.plusSeconds(oneFifthDay.toLong())) &&
                    now.isBefore(sunrise.plusSeconds((2 * oneFifthDay).toLong())) -> VedicPeriod.SANGAVA
            now.isAfter(sunrise.plusSeconds((2 * oneFifthDay).toLong())) &&
                    now.isBefore(sunrise.plusSeconds((3 * oneFifthDay).toLong())) -> VedicPeriod.MADHYAHNA
            now.isAfter(sunrise.plusSeconds((3 * oneFifthDay).toLong())) &&
                    now.isBefore(sunrise.plusSeconds((4 * oneFifthDay).toLong())) -> VedicPeriod.APARAHNA
            now.isAfter(sunrise.plusSeconds((4 * oneFifthDay).toLong())) &&
                    now.isBefore(sunset) -> VedicPeriod.SAYAHNA
            now.isAfter(sunset) && now.isBefore(sunset.plusMinutes(96)) -> VedicPeriod.PRADOSHA
            else -> VedicPeriod.RATRI
        }
    }
}
