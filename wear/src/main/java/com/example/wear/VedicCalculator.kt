package com.example.wear

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object VedicCalculator {

    data class ChoghadiyaInfo(
        val name: String,
        val devanagari: String,
        val isAuspicious: Boolean,
        val endTimeFormatted: String
    )

    data class HoraInfo(
        val graha: String,
        val symbol: String
    )

    private val DAY_HORA_PLANETS = listOf("Surya", "Shukra", "Budha", "Chandra", "Shani", "Guru", "Mangala")
    private val PLANET_SYMBOLS = mapOf(
        "Surya" to "☉",
        "Chandra" to "☽",
        "Mangala" to "♂",
        "Budha" to "☿",
        "Guru" to "♃",
        "Shukra" to "♀",
        "Shani" to "♄"
    )

    // Standard Day Choghadiya sequences by Day of Week
    private val DAY_CHOGHADIYA_CYCLES = mapOf(
        DayOfWeek.SUNDAY to listOf("Udveg", "Chal", "Labh", "Amrit", "Kaal", "Shubh", "Rog", "Udveg"),
        DayOfWeek.MONDAY to listOf("Amrit", "Kaal", "Shubh", "Rog", "Udveg", "Chal", "Labh", "Amrit"),
        DayOfWeek.TUESDAY to listOf("Rog", "Udveg", "Chal", "Labh", "Amrit", "Kaal", "Shubh", "Rog"),
        DayOfWeek.WEDNESDAY to listOf("Labh", "Amrit", "Kaal", "Shubh", "Rog", "Udveg", "Chal", "Labh"),
        DayOfWeek.THURSDAY to listOf("Shubh", "Rog", "Udveg", "Chal", "Labh", "Amrit", "Kaal", "Shubh"),
        DayOfWeek.FRIDAY to listOf("Chal", "Labh", "Amrit", "Kaal", "Shubh", "Rog", "Udveg", "Chal"),
        DayOfWeek.SATURDAY to listOf("Kaal", "Shubh", "Rog", "Udveg", "Chal", "Labh", "Amrit", "Kaal")
    )

    // Standard Night Choghadiya sequences
    private val NIGHT_CHOGHADIYA_CYCLES = mapOf(
        DayOfWeek.SUNDAY to listOf("Shubh", "Amrit", "Chal", "Rog", "Kaal", "Labh", "Udveg", "Shubh"),
        DayOfWeek.MONDAY to listOf("Chal", "Rog", "Kaal", "Labh", "Udveg", "Shubh", "Amrit", "Chal"),
        DayOfWeek.TUESDAY to listOf("Kaal", "Labh", "Udveg", "Shubh", "Amrit", "Chal", "Rog", "Kaal"),
        DayOfWeek.WEDNESDAY to listOf("Udveg", "Shubh", "Amrit", "Chal", "Rog", "Kaal", "Labh", "Udveg"),
        DayOfWeek.THURSDAY to listOf("Amrit", "Chal", "Rog", "Kaal", "Labh", "Udveg", "Shubh", "Amrit"),
        DayOfWeek.FRIDAY to listOf("Rog", "Kaal", "Labh", "Udveg", "Shubh", "Amrit", "Chal", "Rog"),
        DayOfWeek.SATURDAY to listOf("Labh", "Udveg", "Shubh", "Amrit", "Chal", "Rog", "Kaal", "Labh")
    )

    private val CHOGHADIYA_DEVANAGARI = mapOf(
        "Amrit" to "अमृत",
        "Shubh" to "शुभ",
        "Labh" to "लाभ",
        "Chal" to "चल",
        "Udveg" to "उद्वेग",
        "Rog" to "रोग",
        "Kaal" to "काल"
    )

    private val AUSPICIOUS_SET = setOf("Amrit", "Shubh", "Labh", "Chal")

    // Approximate sunrise at 6:00 AM, sunset at 6:00 PM
    fun calculateCurrentChoghadiya(now: LocalTime = LocalTime.now(), date: LocalDate = LocalDate.now()): ChoghadiyaInfo {
        val totalMinutes = now.hour * 60 + now.minute
        val sunriseMinutes = 6 * 60 // 06:00 AM
        val sunsetMinutes = 18 * 60 // 06:00 PM
        val isDay = totalMinutes in sunriseMinutes until sunsetMinutes

        val dayOfWeek = date.dayOfWeek
        val cycle = if (isDay) {
            DAY_CHOGHADIYA_CYCLES[dayOfWeek] ?: DAY_CHOGHADIYA_CYCLES[DayOfWeek.SUNDAY]!!
        } else {
            NIGHT_CHOGHADIYA_CYCLES[dayOfWeek] ?: NIGHT_CHOGHADIYA_CYCLES[DayOfWeek.SUNDAY]!!
        }

        // Each interval is approximately 90 minutes (1.5 hours)
        val elapsedMinutes = if (isDay) {
            totalMinutes - sunriseMinutes
        } else {
            if (totalMinutes >= sunsetMinutes) {
                totalMinutes - sunsetMinutes
            } else {
                (24 * 60 - sunsetMinutes) + totalMinutes
            }
        }

        val slotIndex = ((elapsedMinutes / 90) % 8).coerceIn(0, 7)
        val name = cycle[slotIndex]
        val endMinuteOfDay = if (isDay) {
            sunriseMinutes + (slotIndex + 1) * 90
        } else {
            (sunsetMinutes + (slotIndex + 1) * 90) % (24 * 60)
        }
        val endHour = endMinuteOfDay / 60
        val endMin = endMinuteOfDay % 60
        val endTimeStr = LocalTime.of(endHour % 24, endMin).format(DateTimeFormatter.ofPattern("hh:mm a"))

        return ChoghadiyaInfo(
            name = name,
            devanagari = CHOGHADIYA_DEVANAGARI[name] ?: name,
            isAuspicious = name in AUSPICIOUS_SET,
            endTimeFormatted = endTimeStr
        )
    }

    fun calculateCurrentHora(now: LocalTime = LocalTime.now(), date: LocalDate = LocalDate.now()): HoraInfo {
        // Hora lord of 1st hour is the ruler of the day
        val dayRulerIndex = when (date.dayOfWeek) {
            DayOfWeek.SUNDAY -> 0    // Surya
            DayOfWeek.MONDAY -> 3    // Chandra
            DayOfWeek.TUESDAY -> 6   // Mangala
            DayOfWeek.WEDNESDAY -> 2 // Budha
            DayOfWeek.THURSDAY -> 5  // Guru
            DayOfWeek.FRIDAY -> 1    // Shukra
            DayOfWeek.SATURDAY -> 4  // Shani
        }

        val hoursSinceSunrise = ((now.hour - 6) + 24) % 24
        val horaIndex = (dayRulerIndex + hoursSinceSunrise) % 7
        val planet = DAY_HORA_PLANETS[horaIndex]
        return HoraInfo(
            graha = planet,
            symbol = PLANET_SYMBOLS[planet] ?: "☉"
        )
    }

    fun getRahuKalamTime(date: LocalDate = LocalDate.now()): Pair<String, String> {
        // Traditional 1.5-hour Rahu periods from 6:00 AM
        return when (date.dayOfWeek) {
            DayOfWeek.SUNDAY -> "04:30 PM" to "06:00 PM"
            DayOfWeek.MONDAY -> "07:30 AM" to "09:00 AM"
            DayOfWeek.TUESDAY -> "03:00 PM" to "04:30 PM"
            DayOfWeek.WEDNESDAY -> "12:00 PM" to "01:30 PM"
            DayOfWeek.THURSDAY -> "01:30 PM" to "03:00 PM"
            DayOfWeek.FRIDAY -> "10:30 AM" to "12:00 PM"
            DayOfWeek.SATURDAY -> "09:00 AM" to "10:30 AM"
        }
    }

    fun createInitialSyncData(): SyncData {
        val choghadiya = calculateCurrentChoghadiya()
        val hora = calculateCurrentHora()
        val rahu = getRahuKalamTime()
        return SyncData(
            watchStyle = "COSMIC",
            primaryColorHex = 0xFFFFD166,
            accentColorHex = 0xFFF43F5E,
            currentHora = hora.graha,
            horaSymbol = hora.symbol,
            currentMuhurat = choghadiya.name,
            currentMuhuratDevanagari = choghadiya.devanagari,
            isAuspicious = choghadiya.isAuspicious,
            muhuratEndTime = choghadiya.endTimeFormatted,
            tithi = "Shukla Dashami",
            tithiDevanagari = "शुक्ल दशमी",
            nakshatra = "Rohini",
            rahuStart = rahu.first,
            rahuEnd = rahu.second,
            isRahuActive = false,
            isSyncedFromPhone = false,
            lastSyncTimestamp = System.currentTimeMillis()
        )
    }

    suspend fun calculateCurrentChoghadiyaAsync(now: LocalTime = LocalTime.now(), date: LocalDate = LocalDate.now()): ChoghadiyaInfo {
        return VedicCalculationDispatcher.executeAsync {
            calculateCurrentChoghadiya(now, date)
        }
    }

    suspend fun calculateCurrentHoraAsync(now: LocalTime = LocalTime.now(), date: LocalDate = LocalDate.now()): HoraInfo {
        return VedicCalculationDispatcher.executeAsync {
            calculateCurrentHora(now, date)
        }
    }

    suspend fun createInitialSyncDataAsync(): SyncData {
        return VedicCalculationDispatcher.executeAsync {
            createInitialSyncData()
        }
    }
}
