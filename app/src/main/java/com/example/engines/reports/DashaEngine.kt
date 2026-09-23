package com.example.engines.reports

import com.example.core.model.Graha
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class DashaPeriod(
    val lord: Graha,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val isCurrent: Boolean = false
)

data class AntardashaPeriod(
    val mahadashaLord: Graha,
    val antardashaLord: Graha,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val isCurrent: Boolean = false
)

data class PratyantardashaPeriod(
    val mahadashaLord: Graha,
    val antardashaLord: Graha,
    val pratyantardashaLord: Graha,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val isCurrent: Boolean = false
)

class DashaEngine {
    
    // Vimshottari Dasha durations in years
    private val dashaYears = mapOf(
        Graha.KETU to 7,
        Graha.SHUKRA to 20,
        Graha.SURYA to 6,
        Graha.CHANDRA to 10,
        Graha.MANGALA to 7,
        Graha.RAHU to 18,
        Graha.GURU to 16,
        Graha.SHANI to 19,
        Graha.BUDHA to 17
    )
    
    // The standard sequence of Dashas
    private val dashaSequence = listOf(
        Graha.KETU, Graha.SHUKRA, Graha.SURYA, Graha.CHANDRA,
        Graha.MANGALA, Graha.RAHU, Graha.GURU, Graha.SHANI, Graha.BUDHA
    )

    fun calculateVimshottariDasha(
        birthDate: ZonedDateTime, 
        moonLongitude: Double,
        currentDate: ZonedDateTime
    ): List<DashaPeriod> {
        // Moon longitude determines Nakshatra
        // Each Nakshatra is 13 degrees 20 minutes (13.3333 degrees)
        val nakshatraExtent = 360.0 / 27.0
        val nakshatraIndex = (moonLongitude / nakshatraExtent).toInt()
        val degreeInNakshatra = moonLongitude % nakshatraExtent
        
        // Find the starting Dasha Lord based on Nakshatra index (0 to 26)
        val startingLordIndex = nakshatraIndex % 9
        val startingLord = dashaSequence[startingLordIndex]
        
        // Calculate remaining fraction of the first dasha
        val fractionRemaining = 1.0 - (degreeInNakshatra / nakshatraExtent)
        val totalYearsOfFirstDasha = dashaYears[startingLord] ?: 0
        val yearsRemaining = totalYearsOfFirstDasha * fractionRemaining
        
        val periods = mutableListOf<DashaPeriod>()
        
        // Add the first dasha period
        val firstEndDate = birthDate.plus( (yearsRemaining * 365.25 * 24 * 60 * 60 * 1000).toLong(), ChronoUnit.MILLIS)
        periods.add(
            DashaPeriod(
                lord = startingLord,
                startDate = birthDate,
                endDate = firstEndDate,
                isCurrent = currentDate.isAfter(birthDate) && currentDate.isBefore(firstEndDate)
            )
        )
        
        var currentStart = firstEndDate
        var currentIndex = (startingLordIndex + 1) % 9
        
        // Calculate the rest of the 120-year cycle (9 dashas total, minus the 1st one = 8 more)
        for (i in 1..8) {
            val lord = dashaSequence[currentIndex]
            val durationYears = dashaYears[lord] ?: 0
            val currentEnd = currentStart.plus( (durationYears * 365.25 * 24 * 60 * 60 * 1000).toLong(), ChronoUnit.MILLIS)
            
            periods.add(
                DashaPeriod(
                    lord = lord,
                    startDate = currentStart,
                    endDate = currentEnd,
                    isCurrent = currentDate.isAfter(currentStart) && currentDate.isBefore(currentEnd)
                )
            )
            
            currentStart = currentEnd
            currentIndex = (currentIndex + 1) % 9
        }
        
        return periods
    }

    /**
     * Calculates the 9 Vimshottari Antardasha (Bhukti) sub-periods for a given Mahadasha.
     * Formula: Antardasha Duration = (Mahadasha Years * Antardasha Years) / 120 years.
     */
    fun calculateAntardashas(
        mahadasha: DashaPeriod,
        currentDate: ZonedDateTime
    ): List<AntardashaPeriod> {
        val mahaYears = dashaYears[mahadasha.lord] ?: 10
        val startIndex = dashaSequence.indexOf(mahadasha.lord).coerceAtLeast(0)
        val antardashas = mutableListOf<AntardashaPeriod>()

        var subStart = mahadasha.startDate
        for (i in 0..8) {
            val subLord = dashaSequence[(startIndex + i) % 9]
            val subYears = dashaYears[subLord] ?: 10
            val fractionOfMaha = subYears / 120.0
            val durationDays = mahaYears * fractionOfMaha * 365.25
            val subEnd = subStart.plus((durationDays * 24 * 60 * 60 * 1000).toLong(), ChronoUnit.MILLIS)

            antardashas.add(
                AntardashaPeriod(
                    mahadashaLord = mahadasha.lord,
                    antardashaLord = subLord,
                    startDate = subStart,
                    endDate = subEnd,
                    isCurrent = (currentDate.isEqual(subStart) || currentDate.isAfter(subStart)) && currentDate.isBefore(subEnd)
                )
            )
            subStart = subEnd
        }
        return antardashas
    }

    /**
     * Calculates the 9 Vimshottari Pratyantardasha (sub-sub-period) cycles for an Antardasha.
     * Formula: Pratyantardasha Duration = (Antardasha Duration * Pratyantardasha Years) / 120
     */
    fun calculatePratyantardashas(
        antardasha: AntardashaPeriod,
        currentDate: ZonedDateTime
    ): List<PratyantardashaPeriod> {
        val startIndex = dashaSequence.indexOf(antardasha.antardashaLord).coerceAtLeast(0)
        val pratyantardashas = mutableListOf<PratyantardashaPeriod>()
        val antardashaMillis = ChronoUnit.MILLIS.between(antardasha.startDate, antardasha.endDate).toDouble().coerceAtLeast(1.0)

        var subStart = antardasha.startDate
        for (i in 0..8) {
            val pratLord = dashaSequence[(startIndex + i) % 9]
            val pratYears = dashaYears[pratLord] ?: 10
            val fraction = pratYears / 120.0
            val durationMillis = (antardashaMillis * fraction).toLong()
            val subEnd = if (i == 8) antardasha.endDate else subStart.plus(durationMillis, ChronoUnit.MILLIS)

            pratyantardashas.add(
                PratyantardashaPeriod(
                    mahadashaLord = antardasha.mahadashaLord,
                    antardashaLord = antardasha.antardashaLord,
                    pratyantardashaLord = pratLord,
                    startDate = subStart,
                    endDate = subEnd,
                    isCurrent = (currentDate.isEqual(subStart) || currentDate.isAfter(subStart)) && currentDate.isBefore(subEnd)
                )
            )
            subStart = subEnd
        }
        return pratyantardashas
    }
}
