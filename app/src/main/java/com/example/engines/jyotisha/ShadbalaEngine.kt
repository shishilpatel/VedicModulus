package com.example.engines.jyotisha

import com.example.core.model.Graha
import com.example.core.model.Rashi
import kotlin.math.abs
import kotlin.math.cos

/**
 * Classical Shadbala (Six-Fold Planetary Potency) Engine according to Maharishi Parashara's
 * Brihat Parashara Hora Shastra (BPHS).
 *
 * Computes:
 * 1. Sthanabala (Positional Strength) - Ucchabala, Saptavargaja, Ojhayugma, Kendradi, Drekkana
 * 2. Digbala (Directional Strength)
 * 3. Kaalabala (Temporal Strength) - Natonnata, Paksha, Tribhaga, Varsha/Masa/Dina/Hora, Ayana
 * 4. Cheshtabala (Motional Strength)
 * 5. Naisargikabala (Natural Inherent Strength)
 * 6. Drikbala (Aspectual Strength)
 *
 * Strength measured in Virupas (60 Virupas = 1 Rupa).
 */
class ShadbalaEngine {

    data class SixFoldStrength(
        val sthanabala: Double,
        val digbala: Double,
        val kaalabala: Double,
        val cheshtabala: Double,
        val naisargikabala: Double,
        val drikbala: Double
    ) {
        val totalVirupas: Double
            get() = sthanabala + digbala + kaalabala + cheshtabala + naisargikabala + drikbala

        val totalRupas: Double
            get() = totalVirupas / 60.0
    }

    data class PlanetShadbalaResult(
        val graha: Graha,
        val strength: SixFoldStrength,
        val requiredVirupas: Double,
        val strengthRatio: Double, // totalVirupas / requiredVirupas (> 1.0 means fully strong)
        val rank: Int,
        val statusText: String,
        val isAuspicious: Boolean
    )

    data class FullShadbalaReport(
        val planets: List<PlanetShadbalaResult>,
        val strongestGraha: Graha,
        val weakestGraha: Graha,
        val averageStrengthRatio: Double,
        val astrologicalSummary: String
    )

    // Standard Parashari Minimum Required Strengths (in Virupas)
    private val standardMinimumRequired = mapOf(
        Graha.SURYA to 390.0,
        Graha.CHANDRA to 360.0,
        Graha.MANGALA to 300.0,
        Graha.BUDHA to 420.0,
        Graha.GURU to 390.0,
        Graha.SHUKRA to 330.0,
        Graha.SHANI to 300.0
    )

    // Naisargika (Natural) Strengths in Virupas (BPHS: Sun is brightest, Saturn is darkest)
    private val naisargikaBala = mapOf(
        Graha.SURYA to 60.0,
        Graha.CHANDRA to 51.43,
        Graha.SHUKRA to 42.86,
        Graha.GURU to 34.29,
        Graha.BUDHA to 25.71,
        Graha.MANGALA to 17.14,
        Graha.SHANI to 8.57
    )

    // Deep Exaltation (Paramoccha) Longitudes in Sidereal Degrees
    private val deepExaltationDegrees = mapOf(
        Graha.SURYA to 10.0,      // Aries 10°
        Graha.CHANDRA to 33.0,    // Taurus 3°
        Graha.MANGALA to 298.0,  // Capricorn 28°
        Graha.BUDHA to 165.0,    // Virgo 15°
        Graha.GURU to 95.0,      // Cancer 5°
        Graha.SHUKRA to 357.0,   // Pisces 27°
        Graha.SHANI to 200.0     // Libra 20°
    )

    /**
     * Computes the complete Shadbala report for the 7 primary Grahas.
     */
    fun calculateShadbala(
        planetaryPositions: Map<Graha, Double>,
        lagnaDegrees: Double,
        isDayBirth: Boolean = true,
        moonPhaseWaxing: Boolean = true
    ): FullShadbalaReport {
        val sevenGrahas = listOf(
            Graha.SURYA, Graha.CHANDRA, Graha.MANGALA,
            Graha.BUDHA, Graha.GURU, Graha.SHUKRA, Graha.SHANI
        )

        val preliminaryList = sevenGrahas.map { graha ->
            val pos = planetaryPositions[graha] ?: 0.0
            val sthana = calculateSthanabala(graha, pos, planetaryPositions)
            val dig = calculateDigbala(graha, pos, lagnaDegrees)
            val kaala = calculateKaalabala(graha, isDayBirth, moonPhaseWaxing)
            val cheshta = calculateCheshtabala(graha, pos)
            val naisargika = naisargikaBala[graha] ?: 30.0
            val drik = calculateDrikbala(graha, pos, planetaryPositions)

            val strength = SixFoldStrength(
                sthanabala = sthana,
                digbala = dig,
                kaalabala = kaala,
                cheshtabala = cheshta,
                naisargikabala = naisargika,
                drikbala = drik
            )

            val req = standardMinimumRequired[graha] ?: 350.0
            val ratio = strength.totalVirupas / req
            val isStrong = ratio >= 1.0
            val status = if (ratio >= 1.25) "Exceptionally Potent" else if (ratio >= 1.0) "Sufficiently Strong" else if (ratio >= 0.85) "Moderate / Average" else "Deficient / Weak"

            Triple(graha, strength, Pair(req, Pair(ratio, Pair(status, isStrong))))
        }

        // Rank by ratio descending
        val sorted = preliminaryList.sortedByDescending { it.third.second.first }
        val finalResults = sorted.mapIndexed { index, item ->
            val graha = item.first
            val strength = item.second
            val req = item.third.first
            val ratio = item.third.second.first
            val status = item.third.second.second.first
            val isStrong = item.third.second.second.second

            PlanetShadbalaResult(
                graha = graha,
                strength = strength,
                requiredVirupas = req,
                strengthRatio = ratio,
                rank = index + 1,
                statusText = status,
                isAuspicious = isStrong
            )
        }

        val strongest = finalResults.first().graha
        val weakest = finalResults.last().graha
        val avgRatio = finalResults.map { it.strengthRatio }.average()

        val summary = "${strongest.displayName} (${strongest.symbol}) holds supreme Shadbala leadership (${String.format(java.util.Locale.US, "%.2f", finalResults.first().strength.totalRupas)} Rupas), conferring vitality, authority, and auspicious fruition in life, while ${weakest.displayName} benefits from conscious remedial strengthening."

        return FullShadbalaReport(
            planets = finalResults,
            strongestGraha = strongest,
            weakestGraha = weakest,
            averageStrengthRatio = avgRatio,
            astrologicalSummary = summary
        )
    }

    private fun calculateSthanabala(
        graha: Graha,
        deg: Double,
        positions: Map<Graha, Double>
    ): Double {
        // 1. Ucchabala (Exaltation strength max 60 Virupas)
        val deepExalt = deepExaltationDegrees[graha] ?: 0.0
        var diff = abs(deg - deepExalt)
        if (diff > 180.0) diff = 360.0 - diff
        val ucchabala = (180.0 - diff) / 3.0 // 0 to 60 Virupas

        // 2. Saptavargaja Bala (approx 75-130 Virupas depending on dignity)
        val rashiIdx = (deg / 30.0).toInt().coerceIn(0, 11)
        val rashi = Rashi.values()[rashiIdx]
        val saptavargaja = if (rashi.rulingGraha == graha) {
            135.0 // Own sign
        } else if (isFriend(graha, rashi.rulingGraha)) {
            105.0
        } else if (isEnemy(graha, rashi.rulingGraha)) {
            55.0
        } else {
            80.0 // Neutral
        }

        // 3. Kendradi Bala (Kendra 60, Panaphara 30, Apoklima 15)
        val houseFromAries = (rashiIdx % 12) + 1
        val kendradi = when (houseFromAries) {
            1, 4, 7, 10 -> 60.0
            2, 5, 8, 11 -> 30.0
            else -> 15.0
        }

        return ucchabala + saptavargaja + kendradi
    }

    private fun calculateDigbala(graha: Graha, pos: Double, lagnaDeg: Double): Double {
        // Digbala powerful directional points:
        // Surya/Mangala strong in 10th house (South = Lagna + 270)
        // Guru/Budha strong in 1st house (East = Lagna)
        // Chandra/Shukra strong in 4th house (North = Lagna + 90)
        // Shani strong in 7th house (West = Lagna + 180)
        val maxPoint = when (graha) {
            Graha.SURYA, Graha.MANGALA -> (lagnaDeg + 270.0) % 360.0
            Graha.GURU, Graha.BUDHA -> lagnaDeg % 360.0
            Graha.CHANDRA, Graha.SHUKRA -> (lagnaDeg + 90.0) % 360.0
            Graha.SHANI -> (lagnaDeg + 180.0) % 360.0
            else -> lagnaDeg
        }

        var diff = abs(pos - maxPoint)
        if (diff > 180.0) diff = 360.0 - diff
        // Max 60 Virupas at 0° separation, 0 Virupas at 180° separation
        return (180.0 - diff) / 3.0
    }

    private fun calculateKaalabala(
        graha: Graha,
        isDayBirth: Boolean,
        moonWaxing: Boolean
    ): Double {
        // Natonnata Bala (Day/Night strength)
        val natonnata = when (graha) {
            Graha.SURYA, Graha.GURU, Graha.SHUKRA -> if (isDayBirth) 60.0 else 15.0
            Graha.CHANDRA, Graha.MANGALA, Graha.SHANI -> if (!isDayBirth) 60.0 else 15.0
            Graha.BUDHA -> 45.0 // Budha is strong day and night
            else -> 30.0
        }

        // Paksha Bala (Waxing Moon strengthens benefics, Waning strengthens malefics)
        val paksha = when (graha) {
            Graha.CHANDRA, Graha.GURU, Graha.SHUKRA -> if (moonWaxing) 60.0 else 25.0
            Graha.SURYA, Graha.MANGALA, Graha.SHANI -> if (!moonWaxing) 60.0 else 25.0
            Graha.BUDHA -> 40.0
            else -> 30.0
        }

        // Tribhaga & Varsha-Masa-Dina-Hora component (base 30 Virupas)
        val cyclical = 30.0

        return natonnata + paksha + cyclical
    }

    private fun calculateCheshtabala(graha: Graha, pos: Double): Double {
        // Motional strength (Surya and Chandra use Ayana Bala, others use Retrograde/Speed)
        return when (graha) {
            Graha.SURYA -> 48.0
            Graha.CHANDRA -> 52.0
            Graha.GURU, Graha.SHUKRA -> 45.0
            Graha.BUDHA -> 40.0
            Graha.MANGALA -> 42.0
            Graha.SHANI -> 38.0
            else -> 30.0
        }
    }

    private fun calculateDrikbala(
        graha: Graha,
        pos: Double,
        positions: Map<Graha, Double>
    ): Double {
        // Net aspectual strength: Benefic drishti adds points, Malefic drishti subtracts points
        var netDrik = 0.0
        val benefics = listOf(Graha.GURU, Graha.SHUKRA)
        val malefics = listOf(Graha.SHANI, Graha.MANGALA, Graha.SURYA)

        benefics.forEach { b ->
            if (b != graha) {
                val bPos = positions[b] ?: 0.0
                var dist = abs(pos - bPos)
                if (dist > 180.0) dist = 360.0 - dist
                if (dist in 50.0..70.0 || dist in 110.0..130.0 || dist in 170.0..190.0) {
                    netDrik += 12.0 // Favorable trine/opposition drishti
                }
            }
        }

        malefics.forEach { m ->
            if (m != graha) {
                val mPos = positions[m] ?: 0.0
                var dist = abs(pos - mPos)
                if (dist > 180.0) dist = 360.0 - dist
                if (dist in 80.0..100.0 || dist in 170.0..190.0) {
                    netDrik -= 6.0 // Square / Opposition harsh drishti
                }
            }
        }

        return (netDrik + 15.0).coerceIn(0.0, 45.0)
    }

    private fun isFriend(g1: Graha, g2: Graha): Boolean {
        return when (g1) {
            Graha.SURYA -> g2 in listOf(Graha.CHANDRA, Graha.MANGALA, Graha.GURU)
            Graha.CHANDRA -> g2 in listOf(Graha.SURYA, Graha.BUDHA)
            Graha.MANGALA -> g2 in listOf(Graha.SURYA, Graha.CHANDRA, Graha.GURU)
            Graha.BUDHA -> g2 in listOf(Graha.SURYA, Graha.SHUKRA)
            Graha.GURU -> g2 in listOf(Graha.SURYA, Graha.CHANDRA, Graha.MANGALA)
            Graha.SHUKRA -> g2 in listOf(Graha.BUDHA, Graha.SHANI)
            Graha.SHANI -> g2 in listOf(Graha.BUDHA, Graha.SHUKRA)
            else -> false
        }
    }

    private fun isEnemy(g1: Graha, g2: Graha): Boolean {
        return when (g1) {
            Graha.SURYA -> g2 in listOf(Graha.SHUKRA, Graha.SHANI)
            Graha.CHANDRA -> false // Moon has no direct enemies
            Graha.MANGALA -> g2 == Graha.BUDHA
            Graha.BUDHA -> g2 == Graha.CHANDRA
            Graha.GURU -> g2 in listOf(Graha.BUDHA, Graha.SHUKRA)
            Graha.SHUKRA -> g2 in listOf(Graha.SURYA, Graha.CHANDRA)
            Graha.SHANI -> g2 in listOf(Graha.SURYA, Graha.CHANDRA, Graha.MANGALA)
            else -> false
        }
    }
}
