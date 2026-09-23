package com.example.engines.astronomy

import com.example.core.model.Graha
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class DashaPeriod(
    val lord: Graha,
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
    val subPeriods: List<DashaPeriod> = emptyList() // Antardashas
)

class VimshottariDashaCalculator {

    // Sequence of Dasha lords and their durations in years
    private val dashaLords = listOf(
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

    fun calculateDashas(moonLongitude: Double, birthTime: ZonedDateTime, numYears: Int = 120): List<DashaPeriod> {
        val totalYears = 120.0
        
        // 1 Nakshatra = 13.333 degrees (13 deg 20 min = 800 minutes)
        val nakshatraSpan = 360.0 / 27.0
        
        // Find which nakshatra the moon is in
        val nakshatraIndex = (moonLongitude / nakshatraSpan).toInt()
        val degreesPassedInNakshatra = moonLongitude % nakshatraSpan
        
        // Fraction of nakshatra passed
        val fractionPassed = degreesPassedInNakshatra / nakshatraSpan
        val fractionRemaining = 1.0 - fractionPassed
        
        val initialLordIndex = nakshatraIndex % 9
        val initialLord = dashaLords[initialLordIndex]
        
        val initialLordDuration = initialLord.second
        val remainingYearsInFirstDasha = initialLordDuration * fractionRemaining
        
        val periods = mutableListOf<DashaPeriod>()
        
        var currentStartDate = birthTime
        var currentLordIndex = initialLordIndex
        
        var isFirst = true
        
        while (ChronoUnit.YEARS.between(birthTime, currentStartDate) < numYears) {
            val lordPair = dashaLords[currentLordIndex]
            val durationYears = if (isFirst) remainingYearsInFirstDasha else lordPair.second.toDouble()
            
            // Calculate exact end date by adding days (durationYears * 365.2425)
            val durationDays = (durationYears * 365.2425).toLong()
            val endDate = currentStartDate.plusDays(durationDays)
            
            // Calculate Antardashas
            val subPeriods = calculateAntardashas(lordPair.first, currentStartDate, endDate)
            
            periods.add(
                DashaPeriod(
                    lord = lordPair.first,
                    startDate = currentStartDate,
                    endDate = endDate,
                    subPeriods = subPeriods
                )
            )
            
            currentStartDate = endDate
            currentLordIndex = (currentLordIndex + 1) % 9
            isFirst = false
        }
        
        return periods
    }
    
    private fun calculateAntardashas(mahadashaLord: Graha, mdStart: ZonedDateTime, mdEnd: ZonedDateTime): List<DashaPeriod> {
        val mdTotalDays = ChronoUnit.DAYS.between(mdStart, mdEnd)
        val mdLordIndex = dashaLords.indexOfFirst { it.first == mahadashaLord }
        
        val subPeriods = mutableListOf<DashaPeriod>()
        var currentStartDate = mdStart
        
        // Antardashas start with the Mahadasha lord
        for (i in 0 until 9) {
            val adLordIndex = (mdLordIndex + i) % 9
            val adLordPair = dashaLords[adLordIndex]
            
            // Proportion of AD = (AD Lord Years / 120) * MD Total Days
            val adDurationDays = ((adLordPair.second.toDouble() / 120.0) * mdTotalDays).toLong()
            val endDate = currentStartDate.plusDays(adDurationDays)
            
            subPeriods.add(
                DashaPeriod(
                    lord = adLordPair.first,
                    startDate = currentStartDate,
                    endDate = endDate
                )
            )
            currentStartDate = endDate
        }
        
        return subPeriods
    }
}
