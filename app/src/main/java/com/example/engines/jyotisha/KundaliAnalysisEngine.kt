package com.example.engines.jyotisha

import com.example.core.model.*

enum class PlanetaryDignity(val displayName: String, val sanskritName: String, val description: String) {
    EXALTED("Exalted", "उच्च (Uchcha)", "Peak spiritual and material potency"),
    OWN_SIGN("Own Sign", "स्वक्षेत्र (Sva-kshetra)", "Stable, comfortable and potent foundation"),
    FRIENDLY("Friendly Sign", "मित्र राशि (Mitra Rashi)", "Harmonious and constructive expression"),
    NEUTRAL("Neutral Sign", "सम राशि (Sama Rashi)", "Balanced, conditional manifestation"),
    ENEMY("Inimical Sign", "शत्रु राशि (Shatru Rashi)", "Friction, struggle or delayed fruition"),
    DEBILITATED("Debilitated", "नीच (Neecha)", "Challenged vitality; requires remedies & spiritual effort")
}

data class OccupantAnalysis(
    val graha: Graha,
    val degreeInRashi: Double,
    val dignity: PlanetaryDignity,
    val note: String
)

data class AspectAnalysis(
    val castingGraha: Graha,
    val aspectType: String, // e.g., "7th Direct Aspect", "5th Trine Aspect", "10th Upachaya Aspect"
    val fromHouse: Int,
    val influenceQuality: String // Auspicious / Challenging / Transformative
)

data class BhavaDeepAnalysis(
    val houseNumber: Int,
    val bhavaDetail: BhavaDetail,
    val houseLord: Graha,
    val lordPlacedInHouse: Int,
    val lordPlacementSign: Rashi,
    val occupants: List<OccupantAnalysis>,
    val aspectsReceived: List<AspectAnalysis>,
    val transitOccupants: List<Graha>
)

object KundaliAnalysisEngine {

    fun getRashiLord(rashi: Rashi): Graha = when (rashi) {
        Rashi.MESHA, Rashi.VRISHCHIKA -> Graha.MANGALA
        Rashi.VRISHABHA, Rashi.TULA -> Graha.SHUKRA
        Rashi.MITHUNA, Rashi.KANYA -> Graha.BUDHA
        Rashi.KARKATA -> Graha.CHANDRA
        Rashi.SIMHA -> Graha.SURYA
        Rashi.DHANUS, Rashi.MEENA -> Graha.GURU
        Rashi.MAKARA, Rashi.KUMBHA -> Graha.SHANI
    }

    fun calculateDignity(graha: Graha, rashi: Rashi): PlanetaryDignity {
        return when (graha) {
            Graha.SURYA -> when (rashi) {
                Rashi.MESHA -> PlanetaryDignity.EXALTED
                Rashi.TULA -> PlanetaryDignity.DEBILITATED
                Rashi.SIMHA -> PlanetaryDignity.OWN_SIGN
                Rashi.DHANUS, Rashi.MEENA, Rashi.KARKATA, Rashi.VRISHCHIKA -> PlanetaryDignity.FRIENDLY
                Rashi.MITHUNA, Rashi.KANYA -> PlanetaryDignity.NEUTRAL
                else -> PlanetaryDignity.ENEMY
            }
            Graha.CHANDRA -> when (rashi) {
                Rashi.VRISHABHA -> PlanetaryDignity.EXALTED
                Rashi.VRISHCHIKA -> PlanetaryDignity.DEBILITATED
                Rashi.KARKATA -> PlanetaryDignity.OWN_SIGN
                Rashi.MESHA, Rashi.SIMHA, Rashi.MITHUNA, Rashi.KANYA -> PlanetaryDignity.FRIENDLY
                else -> PlanetaryDignity.NEUTRAL
            }
            Graha.MANGALA -> when (rashi) {
                Rashi.MAKARA -> PlanetaryDignity.EXALTED
                Rashi.KARKATA -> PlanetaryDignity.DEBILITATED
                Rashi.MESHA, Rashi.VRISHCHIKA -> PlanetaryDignity.OWN_SIGN
                Rashi.SIMHA, Rashi.DHANUS, Rashi.MEENA -> PlanetaryDignity.FRIENDLY
                Rashi.VRISHABHA, Rashi.TULA -> PlanetaryDignity.NEUTRAL
                else -> PlanetaryDignity.ENEMY
            }
            Graha.BUDHA -> when (rashi) {
                Rashi.KANYA -> PlanetaryDignity.EXALTED
                Rashi.MEENA -> PlanetaryDignity.DEBILITATED
                Rashi.MITHUNA -> PlanetaryDignity.OWN_SIGN
                Rashi.VRISHABHA, Rashi.SIMHA, Rashi.TULA -> PlanetaryDignity.FRIENDLY
                Rashi.MAKARA, Rashi.KUMBHA -> PlanetaryDignity.NEUTRAL
                else -> PlanetaryDignity.ENEMY
            }
            Graha.GURU -> when (rashi) {
                Rashi.KARKATA -> PlanetaryDignity.EXALTED
                Rashi.MAKARA -> PlanetaryDignity.DEBILITATED
                Rashi.DHANUS, Rashi.MEENA -> PlanetaryDignity.OWN_SIGN
                Rashi.MESHA, Rashi.SIMHA, Rashi.VRISHCHIKA -> PlanetaryDignity.FRIENDLY
                Rashi.MITHUNA, Rashi.KANYA -> PlanetaryDignity.ENEMY
                else -> PlanetaryDignity.NEUTRAL
            }
            Graha.SHUKRA -> when (rashi) {
                Rashi.MEENA -> PlanetaryDignity.EXALTED
                Rashi.KANYA -> PlanetaryDignity.DEBILITATED
                Rashi.VRISHABHA, Rashi.TULA -> PlanetaryDignity.OWN_SIGN
                Rashi.MITHUNA, Rashi.MAKARA, Rashi.KUMBHA -> PlanetaryDignity.FRIENDLY
                Rashi.MESHA, Rashi.VRISHCHIKA -> PlanetaryDignity.NEUTRAL
                else -> PlanetaryDignity.ENEMY
            }
            Graha.SHANI -> when (rashi) {
                Rashi.TULA -> PlanetaryDignity.EXALTED
                Rashi.MESHA -> PlanetaryDignity.DEBILITATED
                Rashi.MAKARA, Rashi.KUMBHA -> PlanetaryDignity.OWN_SIGN
                Rashi.VRISHABHA, Rashi.MITHUNA, Rashi.KANYA -> PlanetaryDignity.FRIENDLY
                Rashi.DHANUS, Rashi.MEENA -> PlanetaryDignity.NEUTRAL
                else -> PlanetaryDignity.ENEMY
            }
            Graha.RAHU -> when (rashi) {
                Rashi.VRISHABHA, Rashi.MITHUNA -> PlanetaryDignity.EXALTED
                Rashi.VRISHCHIKA, Rashi.DHANUS -> PlanetaryDignity.DEBILITATED
                Rashi.KUMBHA -> PlanetaryDignity.OWN_SIGN
                else -> PlanetaryDignity.NEUTRAL
            }
            Graha.KETU -> when (rashi) {
                Rashi.VRISHCHIKA, Rashi.DHANUS -> PlanetaryDignity.EXALTED
                Rashi.VRISHABHA, Rashi.MITHUNA -> PlanetaryDignity.DEBILITATED
                Rashi.VRISHCHIKA -> PlanetaryDignity.OWN_SIGN
                else -> PlanetaryDignity.NEUTRAL
            }
        }
    }

    fun analyzeBhava(
        targetHouseNumber: Int,
        chartData: KundaliChartData,
        transitPlanets: Map<Graha, Double> = emptyMap()
    ): BhavaDeepAnalysis {
        val bhava = chartData.bhavas.firstOrNull { it.houseNumber == targetHouseNumber }
            ?: chartData.bhavas[0]

        val houseLord = getRashiLord(bhava.rashi)

        // Find which house the house lord is placed in
        var lordPlacedInHouse = targetHouseNumber
        var lordPlacementSign = bhava.rashi
        val lordPosDeg = chartData.grahaPositions[houseLord]
        if (lordPosDeg != null) {
            val lordRashiIdx = (lordPosDeg / 30.0).toInt().coerceIn(0, 11)
            lordPlacementSign = Rashi.values()[lordRashiIdx]
            lordPlacedInHouse = ((lordRashiIdx - chartData.lagnaRashi.ordinal + 12) % 12) + 1
        }

        // Occupant Analysis
        val occupants = bhava.occupyingGrahas.map { graha ->
            val deg = chartData.grahaPositions[graha] ?: 0.0
            val degInRashi = deg % 30.0
            val dignity = calculateDignity(graha, bhava.rashi)
            val note = when (dignity) {
                PlanetaryDignity.EXALTED -> "Uchcha Bala (Peak Strength)"
                PlanetaryDignity.OWN_SIGN -> "Sva-Bala (Self Rooted)"
                PlanetaryDignity.DEBILITATED -> "Neecha (Spiritual Alchemy Needed)"
                PlanetaryDignity.FRIENDLY -> "Mitra Bala (Supportive)"
                else -> "Sama Bala (Neutral Balance)"
            }
            OccupantAnalysis(graha, degInRashi, dignity, note)
        }

        // Drishti (Aspects) Received by this House
        val aspects = mutableListOf<AspectAnalysis>()
        chartData.grahaPositions.forEach { (graha, deg) ->
            val grahaRashiIdx = (deg / 30.0).toInt().coerceIn(0, 11)
            val fromHouse = ((grahaRashiIdx - chartData.lagnaRashi.ordinal + 12) % 12) + 1
            val distance = ((targetHouseNumber - fromHouse + 12) % 12) + 1

            when {
                distance == 7 -> {
                    aspects.add(
                        AspectAnalysis(
                            castingGraha = graha,
                            aspectType = "7th Direct Aspect (सप्तम दृष्टि)",
                            fromHouse = fromHouse,
                            influenceQuality = if (graha in listOf(Graha.GURU, Graha.SHUKRA, Graha.BUDHA)) "Benefic & Auspicious" else "Transformative & Intense"
                        )
                    )
                }
                graha == Graha.MANGALA && (distance == 4 || distance == 8) -> {
                    aspects.add(
                        AspectAnalysis(
                            castingGraha = graha,
                            aspectType = "${distance}th Special Mars Aspect (${if (distance == 4) "चतुर्थ" else "अष्टम"} दृष्टि)",
                            fromHouse = fromHouse,
                            influenceQuality = "Dynamic & Protective Willpower"
                        )
                    )
                }
                (graha == Graha.GURU || graha == Graha.RAHU || graha == Graha.KETU) && (distance == 5 || distance == 9) -> {
                    aspects.add(
                        AspectAnalysis(
                            castingGraha = graha,
                            aspectType = "${distance}th Sacred Trine Aspect (${if (distance == 5) "पंचम" else "नवम"} दृष्टि)",
                            fromHouse = fromHouse,
                            influenceQuality = if (graha == Graha.GURU) "Divine Grace & Wisdom (Amrita Drishti)" else "Karmic Evolution"
                        )
                    )
                }
                graha == Graha.SHANI && (distance == 3 || distance == 10) -> {
                    aspects.add(
                        AspectAnalysis(
                            castingGraha = graha,
                            aspectType = "${distance}th Special Saturn Aspect (${if (distance == 3) "तृतीय" else "दशम"} दृष्टि)",
                            fromHouse = fromHouse,
                            influenceQuality = "Karmic Discipline & Endurance"
                        )
                    )
                }
            }
        }

        // Transit Occupants (Gochar)
        val transits = mutableListOf<Graha>()
        transitPlanets.forEach { (graha, deg) ->
            val transitRashiIdx = (deg / 30.0).toInt().coerceIn(0, 11)
            val transitHouse = ((transitRashiIdx - chartData.lagnaRashi.ordinal + 12) % 12) + 1
            if (transitHouse == targetHouseNumber) {
                transits.add(graha)
            }
        }

        return BhavaDeepAnalysis(
            houseNumber = targetHouseNumber,
            bhavaDetail = bhava,
            houseLord = houseLord,
            lordPlacedInHouse = lordPlacedInHouse,
            lordPlacementSign = lordPlacementSign,
            occupants = occupants,
            aspectsReceived = aspects,
            transitOccupants = transits
        )
    }
}
