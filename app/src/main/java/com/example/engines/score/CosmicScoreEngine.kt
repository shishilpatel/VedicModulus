package com.example.engines.score

import com.example.core.model.BirthProfile
import com.example.core.model.CosmicScore
import com.example.core.model.DailyVedicProfile
import com.example.core.model.PersonalizedProfile
import java.time.ZonedDateTime
import kotlin.random.Random

class CosmicScoreEngine {
    /**
     * Calculates a daily personalized score.
     * Note: This is a traditional Vedic-inspired score, not a scientifically validated prediction.
     */
    fun calculateScore(
        personalizedProfile: PersonalizedProfile?,
        dailyProfile: DailyVedicProfile
    ): CosmicScore {
        val daySeed = dailyProfile.currentZonedDateTime.toLocalDate().toEpochDay()
        val userSeed = personalizedProfile?.userBirthProfile?.fullName?.hashCode()?.toLong() ?: 1L
        
        val random = Random(daySeed + userSeed)
        
        // Base modifiers based on Tithi, Nakshatra, Hora
        var luckBase = 60
        var energyBase = 60
        var calmBase = 60
        
        // Choghadiya impact
        if (dailyProfile.currentChoghadiya.type.isAuspicious) {
            luckBase += 15
            calmBase += 5
        } else {
            luckBase -= 15
            calmBase -= 10
        }
        
        // Tithi impact
        val tithiNumber = dailyProfile.panchanga.tithi.number
        if (tithiNumber in listOf(4, 9, 14, 19, 24, 29)) { // Rikta Tithis
            energyBase -= 10
            luckBase -= 10
        } else if (tithiNumber in listOf(5, 10, 15, 20, 25, 30)) { // Purna Tithis
            energyBase += 10
            calmBase += 10
        }

        val luck = (luckBase + random.nextInt(31)).coerceIn(1, 100)
        val energy = (energyBase + random.nextInt(31)).coerceIn(1, 100)
        val calm = (calmBase + random.nextInt(31)).coerceIn(1, 100)
        
        val overall = (luck + energy + calm) / 3
        
        return CosmicScore(luck, energy, calm, overall)
    }
}
