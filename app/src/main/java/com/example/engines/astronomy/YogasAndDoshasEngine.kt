package com.example.engines.astronomy

import com.example.core.model.Graha

data class AstrologicalCombination(
    val name: String,
    val type: CombinationType,
    val description: String,
    val severityOrStrength: String,
    val involvedGrahas: List<Graha>
)

enum class CombinationType {
    YOGA, DOSHA
}

class YogasAndDoshasEngine {

    fun calculateCombinations(spashtasList: List<GrahaSpashta>, lagnaDegrees: Double): List<AstrologicalCombination> {
        val combinations = mutableListOf<AstrologicalCombination>()
        
        // Convert Lagna to Rashi index
        val lagnaRashiIndex = (lagnaDegrees / 30).toInt()
        
        val houseMap = mutableMapOf<Int, MutableList<Graha>>()
        for (i in 1..12) houseMap[i] = mutableListOf()
        
        val grahaHouseMap = mutableMapOf<Graha, Int>()
        val grahaSpashtaMap = spashtasList.associateBy { it.graha }
        
        spashtasList.forEach { spashta ->
            val rashiIndex = spashta.rashi.number - 1
            val house = ((rashiIndex - lagnaRashiIndex + 12) % 12) + 1
            houseMap[house]?.add(spashta.graha)
            grahaHouseMap[spashta.graha] = house
        }

        // 1. Mangal Dosha (Kuja Dosha)
        // Mars in 1, 4, 7, 8, 12 from Lagna
        val marsHouse = grahaHouseMap[Graha.MANGALA]
        if (marsHouse in listOf(1, 4, 7, 8, 12)) {
            combinations.add(
                AstrologicalCombination(
                    name = "Mangal Dosha",
                    type = CombinationType.DOSHA,
                    description = "Mars is positioned in the $marsHouse house, creating traditional Mangal Dosha. This indicates high martial energy in relationships.",
                    severityOrStrength = if (marsHouse == 8 || marsHouse == 7) "High" else "Moderate",
                    involvedGrahas = listOf(Graha.MANGALA)
                )
            )
        }

        // 2. Gajakesari Yoga
        // Jupiter in Kendra (1,4,7,10) from Moon
        val jupHouse = grahaHouseMap[Graha.GURU]
        val moonHouse = grahaHouseMap[Graha.CHANDRA]
        if (jupHouse != null && moonHouse != null) {
            val dist = ((jupHouse - moonHouse + 12) % 12) + 1
            if (dist in listOf(1, 4, 7, 10)) {
                combinations.add(
                    AstrologicalCombination(
                        name = "Gajakesari Yoga",
                        type = CombinationType.YOGA,
                        description = "Jupiter is in a Kendra (angle) from the Moon. This highly auspicious yoga bestows wisdom, reputation, and sustained prosperity.",
                        severityOrStrength = "High",
                        involvedGrahas = listOf(Graha.GURU, Graha.CHANDRA)
                    )
                )
            }
        }

        // 3. Budhaditya Yoga
        // Sun and Mercury in the same house
        if (grahaHouseMap[Graha.SURYA] == grahaHouseMap[Graha.BUDHA]) {
            val sunSpashta = grahaSpashtaMap[Graha.SURYA]
            val mercSpashta = grahaSpashtaMap[Graha.BUDHA]
            // Ensure they are not too close (combustion)
            if (sunSpashta != null && mercSpashta != null && !mercSpashta.isCombust) {
                 combinations.add(
                    AstrologicalCombination(
                        name = "Budhaditya Yoga",
                        type = CombinationType.YOGA,
                        description = "Sun and Mercury are conjunct without Mercury being combust. Grants high intellect, communication skills, and success in learning.",
                        severityOrStrength = "High",
                        involvedGrahas = listOf(Graha.SURYA, Graha.BUDHA)
                    )
                )
            }
        }
        
        // 4. Kemadruma Yoga
        // No planets (excluding Sun, Rahu, Ketu) in 2nd and 12th from Moon
        if (moonHouse != null) {
            val h2FromMoon = ((moonHouse + 1 - 1) % 12) + 1
            val h12FromMoon = ((moonHouse + 11 - 1) % 12) + 1
            val validGrahas = listOf(Graha.MANGALA, Graha.BUDHA, Graha.GURU, Graha.SHUKRA, Graha.SHANI)
            
            val pIn2 = houseMap[h2FromMoon]?.intersect(validGrahas.toSet()) ?: emptySet()
            val pIn12 = houseMap[h12FromMoon]?.intersect(validGrahas.toSet()) ?: emptySet()
            val conjunct = houseMap[moonHouse]?.intersect(validGrahas.toSet()) ?: emptySet()
            
            if (pIn2.isEmpty() && pIn12.isEmpty() && conjunct.isEmpty()) {
                combinations.add(
                    AstrologicalCombination(
                        name = "Kemadruma Yoga",
                        type = CombinationType.DOSHA,
                        description = "Moon is isolated with no supporting planets on either side. Can indicate feelings of loneliness or mental struggles, but brings deep self-reliance.",
                        severityOrStrength = "Moderate",
                        involvedGrahas = listOf(Graha.CHANDRA)
                    )
                )
            }
        }

        return combinations
    }
}
