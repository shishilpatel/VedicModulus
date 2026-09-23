package com.example.engines.jyotisha

import com.example.core.model.Graha
import com.example.core.model.Rashi

/**
 * Classical Ashtakavarga Calculation Engine according to Brihat Parashara Hora Shastra.
 *
 * Computes:
 * 1. Bhinnashtakavarga (BAV) for 7 Planets (Surya, Chandra, Mangala, Budha, Guru, Shukra, Shani)
 * 2. Sarvashtakavarga (SAV) Total Bindus across all 12 Rashis (Total sum = 337 points)
 * 3. House Strength Analysis (Auspicious >= 28, Neutral 25-28, Deficient < 25)
 * 4. Transit Favorability & Remedial Guidance
 */
class AshtakavargaEngine {

    data class BhinnaAshtakavarga(
        val graha: Graha,
        val bindusPerRashi: Map<Rashi, Int>, // 0 to 8 bindus per sign
        val totalBindus: Int
    )

    data class SarvaAshtakavarga(
        val bindusPerRashi: Map<Rashi, Int>, // Total points per sign
        val bindusPerHouse: Map<Int, Int>,   // 1 to 12 from Lagna
        val totalScore: Int,                 // Standard 337
        val auspiciousHouses: List<Int>,
        val challengingHouses: List<Int>,
        val strongestRashi: Rashi,
        val weakestRashi: Rashi
    )

    data class FullAshtakavargaReport(
        val bhinnaAshtakavargas: Map<Graha, BhinnaAshtakavarga>,
        val sarvashtakavarga: SarvaAshtakavarga,
        val astrologicalGuidance: String
    )

    /**
     * Computes the complete Ashtakavarga tables for a natal chart.
     */
    fun calculateAshtakavarga(
        planetaryPositions: Map<Graha, Double>,
        lagnaDegrees: Double
    ): FullAshtakavargaReport {
        val allRashis = Rashi.values().toList()
        val lagnaRashiIdx = (lagnaDegrees / 30.0).toInt().coerceIn(0, 11)

        val planetRashiIndices = planetaryPositions.mapValues {
            (it.value / 30.0).toInt().coerceIn(0, 11)
        }

        val sevenGrahas = listOf(
            Graha.SURYA, Graha.CHANDRA, Graha.MANGALA,
            Graha.BUDHA, Graha.GURU, Graha.SHUKRA, Graha.SHANI
        )

        val bavMap = mutableMapOf<Graha, BhinnaAshtakavarga>()
        val savAccumulator = IntArray(12) { 0 }

        sevenGrahas.forEach { targetGraha ->
            val rashiPoints = IntArray(12) { 0 }
            val rules = getParashariRulesFor(targetGraha)

            // Evaluate contribution from each planet and lagna
            rules.forEach { (contributor, favorableOffsets) ->
                val sourceRashiIdx = if (contributor == null) {
                    lagnaRashiIdx // Lagna contribution
                } else {
                    planetRashiIndices[contributor] ?: 0
                }

                favorableOffsets.forEach { offset ->
                    val targetIdx = (sourceRashiIdx + (offset - 1)) % 12
                    rashiPoints[targetIdx]++
                }
            }

            val binduMap = mutableMapOf<Rashi, Int>()
            allRashis.forEachIndexed { idx, rashi ->
                val bindus = rashiPoints[idx].coerceIn(0, 8)
                binduMap[rashi] = bindus
                savAccumulator[idx] += bindus
            }

            bavMap[targetGraha] = BhinnaAshtakavarga(
                graha = targetGraha,
                bindusPerRashi = binduMap,
                totalBindus = rashiPoints.sum()
            )
        }

        // Build Sarvashtakavarga (SAV)
        val savRashiMap = mutableMapOf<Rashi, Int>()
        val savHouseMap = mutableMapOf<Int, Int>()
        val auspiciousHouses = mutableListOf<Int>()
        val challengingHouses = mutableListOf<Int>()

        allRashis.forEachIndexed { idx, rashi ->
            val score = savAccumulator[idx]
            savRashiMap[rashi] = score
            val houseNum = ((idx - lagnaRashiIdx + 12) % 12) + 1
            savHouseMap[houseNum] = score

            if (score >= 28) {
                auspiciousHouses.add(houseNum)
            } else if (score < 25) {
                challengingHouses.add(houseNum)
            }
        }

        val maxEntry = savRashiMap.maxByOrNull { it.value }
        val minEntry = savRashiMap.minByOrNull { it.value }
        val maxRashi = maxEntry?.key ?: Rashi.MESHA
        val maxVal = maxEntry?.value ?: 28
        val minRashi = minEntry?.key ?: Rashi.MEENA

        val sav = SarvaAshtakavarga(
            bindusPerRashi = savRashiMap,
            bindusPerHouse = savHouseMap,
            totalScore = savAccumulator.sum(),
            auspiciousHouses = auspiciousHouses.sorted(),
            challengingHouses = challengingHouses.sorted(),
            strongestRashi = maxRashi,
            weakestRashi = minRashi
        )

        val guidance = "Sarvashtakavarga reveals highest cosmic support in ${maxRashi.sanskritName} ($maxVal bindus), indicating robust material success and resilience during transits through this sign. Focus key life initiatives when transit planets pass through houses: ${auspiciousHouses.joinToString(", ") { "H$it" }}."

        return FullAshtakavargaReport(
            bhinnaAshtakavargas = bavMap,
            sarvashtakavarga = sav,
            astrologicalGuidance = guidance
        )
    }

    /**
     * Standard BPHS Favorable Ashtakavarga Houses from each contributing Graha & Lagna.
     * Null contributor represents Lagna.
     */
    private fun getParashariRulesFor(graha: Graha): Map<Graha?, List<Int>> {
        return when (graha) {
            Graha.SURYA -> mapOf(
                Graha.SURYA to listOf(1, 2, 4, 7, 8, 9, 10, 11),
                Graha.CHANDRA to listOf(3, 6, 10, 11),
                Graha.MANGALA to listOf(1, 2, 4, 7, 8, 9, 10, 11),
                Graha.BUDHA to listOf(3, 5, 6, 9, 10, 11, 12),
                Graha.GURU to listOf(5, 6, 9, 11),
                Graha.SHUKRA to listOf(6, 7, 12),
                Graha.SHANI to listOf(1, 2, 4, 7, 8, 9, 10, 11),
                null to listOf(3, 4, 6, 10, 11, 12)
            )
            Graha.CHANDRA -> mapOf(
                Graha.SURYA to listOf(3, 6, 7, 8, 10, 11),
                Graha.CHANDRA to listOf(1, 3, 6, 7, 10, 11),
                Graha.MANGALA to listOf(2, 3, 5, 6, 9, 10, 11),
                Graha.BUDHA to listOf(1, 3, 4, 5, 7, 8, 10, 11),
                Graha.GURU to listOf(1, 4, 7, 8, 10, 11, 12),
                Graha.SHUKRA to listOf(3, 4, 5, 7, 9, 10, 11),
                Graha.SHANI to listOf(3, 5, 6, 11),
                null to listOf(3, 6, 10, 11)
            )
            Graha.MANGALA -> mapOf(
                Graha.SURYA to listOf(3, 5, 6, 10, 11),
                Graha.CHANDRA to listOf(3, 6, 11),
                Graha.MANGALA to listOf(1, 2, 4, 7, 8, 10, 11),
                Graha.BUDHA to listOf(3, 5, 6, 11),
                Graha.GURU to listOf(6, 10, 11, 12),
                Graha.SHUKRA to listOf(6, 8, 11, 12),
                Graha.SHANI to listOf(1, 4, 7, 8, 9, 10, 11),
                null to listOf(1, 3, 6, 10, 11)
            )
            Graha.BUDHA -> mapOf(
                Graha.SURYA to listOf(5, 6, 9, 11, 12),
                Graha.CHANDRA to listOf(2, 4, 6, 8, 10, 11),
                Graha.MANGALA to listOf(1, 2, 4, 7, 8, 9, 10, 11),
                Graha.BUDHA to listOf(1, 3, 5, 6, 9, 10, 11, 12),
                Graha.GURU to listOf(6, 8, 11, 12),
                Graha.SHUKRA to listOf(1, 2, 3, 4, 5, 8, 9, 11),
                Graha.SHANI to listOf(1, 2, 4, 7, 8, 9, 10, 11),
                null to listOf(1, 2, 4, 6, 8, 10, 11)
            )
            Graha.GURU -> mapOf(
                Graha.SURYA to listOf(1, 2, 3, 4, 7, 8, 9, 10, 11),
                Graha.CHANDRA to listOf(2, 5, 7, 9, 11),
                Graha.MANGALA to listOf(1, 2, 4, 7, 8, 10, 11),
                Graha.BUDHA to listOf(1, 2, 4, 5, 6, 9, 10, 11),
                Graha.GURU to listOf(1, 2, 3, 4, 7, 8, 10, 11),
                Graha.SHUKRA to listOf(2, 5, 6, 9, 10, 11),
                Graha.SHANI to listOf(3, 5, 6, 12),
                null to listOf(1, 2, 4, 5, 6, 7, 9, 10, 11)
            )
            Graha.SHUKRA -> mapOf(
                Graha.SURYA to listOf(8, 11, 12),
                Graha.CHANDRA to listOf(1, 2, 3, 4, 5, 8, 9, 11, 12),
                Graha.MANGALA to listOf(3, 5, 6, 9, 11, 12),
                Graha.BUDHA to listOf(3, 5, 6, 9, 11),
                Graha.GURU to listOf(5, 8, 9, 10, 11),
                Graha.SHUKRA to listOf(1, 2, 3, 4, 5, 8, 9, 10, 11),
                Graha.SHANI to listOf(3, 4, 5, 8, 9, 10, 11),
                null to listOf(1, 2, 3, 4, 5, 8, 9, 11)
            )
            Graha.SHANI -> mapOf(
                Graha.SURYA to listOf(1, 2, 4, 7, 8, 10, 11),
                Graha.CHANDRA to listOf(3, 6, 11),
                Graha.MANGALA to listOf(3, 5, 6, 10, 11, 12),
                Graha.BUDHA to listOf(6, 8, 9, 10, 11, 12),
                Graha.GURU to listOf(5, 6, 11, 12),
                Graha.SHUKRA to listOf(6, 11, 12),
                Graha.SHANI to listOf(3, 5, 6, 11),
                null to listOf(1, 3, 4, 6, 10, 11)
            )
            else -> emptyMap()
        }
    }
}
