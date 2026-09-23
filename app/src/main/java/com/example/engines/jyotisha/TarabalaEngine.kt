package com.example.engines.jyotisha

import com.example.core.model.*

class TarabalaEngine {

    /**
     * Calculates the Navatara (Tarabala) from natal Janma Nakshatra to the transit Nakshatra.
     */
    fun calculateTarabala(natalNakshatra: Nakshatra, transitNakshatra: Nakshatra): TaraType {
        // Nakshatras 1..27
        val diff = ((transitNakshatra.number - natalNakshatra.number + 27) % 27)
        val taraIndex = (diff % 9) + 1 // 1 to 9
        return TaraType.values().firstOrNull { it.number == taraIndex } ?: TaraType.JANMA
    }

    /**
     * Calculates Chandrabala: The ordinal house position of the transit Moon's sign relative to Janma Rashi.
     * Classical Vedic Jyotisha considers houses 1, 3, 6, 7, 10, 11 auspicious for important activities.
     */
    fun calculateChandrabala(natalChandraRashi: Rashi, transitChandraRashi: Rashi): Pair<Int, Boolean> {
        val house = ((transitChandraRashi.number - natalChandraRashi.number + 12) % 12) + 1
        val isFavorable = house in listOf(1, 3, 6, 7, 10, 11)
        return house to isFavorable
    }

    /**
     * Synthesizes Tarabala and Chandrabala into a unified daily personal cosmic harmony assessment.
     */
    fun calculateDailyHarmony(
        natalNakshatra: Nakshatra,
        natalChandraRashi: Rashi,
        transitNakshatra: Nakshatra,
        transitChandraRashi: Rashi
    ): DailyHarmonyScore {
        val tara = calculateTarabala(natalNakshatra, transitNakshatra)
        val (chandraHouse, isChandraFavorable) = calculateChandrabala(natalChandraRashi, transitChandraRashi)

        var score = 50
        if (tara.isAuspicious) score += 30 else score -= 20
        if (isChandraFavorable) score += 20 else score -= 15

        val finalScore = score.coerceIn(15, 98)

        val advice = buildString {
            append("Tarabala: ${tara.displayName} — ${tara.description}. ")
            if (isChandraFavorable) {
                append("Chandrabala: Favorable (${chandraHouse}th house from Moon) supporting emotional ease and successful interactions.")
            } else {
                append("Chandrabala: Sensitive (${chandraHouse}th house from Moon). Exercise caution with major decisions or impulsive expenditures.")
            }
        }

        return DailyHarmonyScore(
            taraType = tara,
            chandrabalaHouse = chandraHouse,
            isChandrabalaFavorable = isChandraFavorable,
            overallScorePercent = finalScore,
            advice = advice
        )
    }
}
