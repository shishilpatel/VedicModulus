package com.example.engines.jyotisha

import com.example.core.model.BhavaDetail
import com.example.core.model.Graha
import com.example.core.model.KundaliChartData
import com.example.core.model.Rashi
import kotlin.math.floor

/**
 * Shodashvarga (16 Divisional Charts) Engine according to Brihat Parashara Hora Shastra.
 *
 * Provides accurate algorithmic sign mappings for all 16 classical Vargas:
 * D1 (Rashi), D2 (Hora), D3 (Drekkana), D4 (Chaturthamsha), D7 (Saptamsha),
 * D9 (Navamsha), D10 (Dashamsha), D12 (Dwadashamsha), D16 (Shodashamsha),
 * D20 (Vimshamsha), D24 (Chaturvimshamsha), D27 (Saptavimshamsha), D30 (Trimshamsha),
 * D40 (Khavedamsha), D45 (Akshavedamsha), D60 (Shashtiamsha).
 */
class ShodashvargaEngine {

    enum class VargaType(
        val code: String,
        val divisionNumber: Int,
        val title: String,
        val sanskritName: String,
        val signification: String,
        val category: String
    ) {
        D1("D1", 1, "Rashi", "राशि (D1)", "Physical body, vitality, overall life orientation", "Shadvarga"),
        D2("D2", 2, "Hora", "होरा (D2)", "Accumulated wealth, financial prosperity & assets", "Shadvarga"),
        D3("D3", 3, "Drekkana", "द्रेष्काण (D3)", "Siblings, courage, vigor & brothers/sisters", "Shadvarga"),
        D4("D4", 4, "Chaturthamsha", "चतुर्थांश (D4)", "Fortune, real estate, property & fixed assets", "Dashavarga"),
        D7("D7", 7, "Saptamsha", "सप्तांश (D7)", "Progeny, children, legacy & creative fruit", "Saptavarga"),
        D9("D9", 9, "Navamsha", "नवांश (D9)", "Dharma, marriage, spouse & soul's inner path", "Shadvarga"),
        D10("D10", 10, "Dashamsha", "दशांश (D10)", "Career, profession, social prestige, power & fame", "Dashavarga"),
        D12("D12", 12, "Dwadashamsha", "द्वादशांश (D12)", "Parents, lineage, ancestral roots & paternal grace", "Shadvarga"),
        D16("D16", 16, "Shodashamsha", "षोडशांश (D16)", "Vehicles, conveyances, comforts & inner happiness", "Dashavarga"),
        D20("D20", 20, "Vimshamsha", "विंशांश (D20)", "Spiritual progress, sadhana, devotion & upasana", "Shodashvarga"),
        D24("D24", 24, "Chaturvimshamsha", "चतुर्विंशांश (D24)", "Higher education, intellect, academic mastery & skills", "Shodashvarga"),
        D27("D27", 27, "Saptavimshamsha", "सप्तविंशांश (D27)", "Subconscious strengths, physical stamina & endurance", "Shodashvarga"),
        D30("D30", 30, "Trimshamsha", "त्रिंशांश (D30)", "Misfortunes, evils, obstacles, karmic afflictions & health", "Shadvarga"),
        D40("D40", 40, "Khavedamsha", "खवेदांश (D40)", "Auspicious & inauspicious events, auspiciousness", "Shodashvarga"),
        D45("D45", 45, "Akshavedamsha", "अक्षवेदांश (D45)", "General well-being, character & moral integrity", "Shodashvarga"),
        D60("D60", 60, "Shashtiamsha", "षष्ट्यंश (D60)", "Past life Karma, supreme decisive divisional chart", "Shodashvarga")
    }

    /**
     * Computes the complete 12-Bhava chart layout for any requested Varga type.
     */
    fun calculateDivisionalChart(
        vargaType: VargaType,
        planetaryPositions: Map<Graha, Double>,
        lagnaDegrees: Double
    ): KundaliChartData {
        val allRashis = Rashi.values()

        // Calculate Varga Ascendant (Lagna)
        val vargaLagnaRashi = computeVargaRashi(lagnaDegrees, vargaType)
        val vargaLagnaIdx = vargaLagnaRashi.ordinal

        // Calculate Varga sign for each planet
        val vargaPlanetRashis = planetaryPositions.mapValues { (_, deg) ->
            computeVargaRashi(deg, vargaType)
        }

        // Map planets to houses from Varga Lagna
        val planetsInHouse = mutableMapOf<Int, MutableList<Graha>>()
        for (i in 1..12) planetsInHouse[i] = mutableListOf()

        vargaPlanetRashis.forEach { (graha, rashi) ->
            val houseNum = ((rashi.ordinal - vargaLagnaIdx + 12) % 12) + 1
            planetsInHouse[houseNum]?.add(graha)
        }

        // Build Bhavas
        val bhavas = (1..12).map { houseNum ->
            val houseRashi = allRashis[(vargaLagnaIdx + (houseNum - 1)) % 12]
            BhavaDetail(
                houseNumber = houseNum,
                traditionalName = "${vargaType.code} H$houseNum",
                devanagariName = "${vargaType.sanskritName.substringBefore(" ")} भाव $houseNum",
                rashi = houseRashi,
                occupyingGrahas = planetsInHouse[houseNum] ?: emptyList(),
                significations = "${vargaType.title}: ${vargaType.signification}",
                karakaGraha = Graha.GURU
            )
        }

        // Converted positions in varga longitudes for chart canvas
        val vargaDegreesMap = vargaPlanetRashis.mapValues { (_, rashi) ->
            rashi.ordinal * 30.0 + 15.0 // centered in varga sign
        }

        return KundaliChartData(
            lagnaRashi = vargaLagnaRashi,
            lagnaDegrees = lagnaDegrees % 30.0,
            bhavas = bhavas,
            grahaPositions = vargaDegreesMap
        )
    }

    /**
     * Classical Parashari formula to calculate the resultant Rashi for any divisional division.
     */
    fun computeVargaRashi(longitudeDeg: Double, varga: VargaType): Rashi {
        val allRashis = Rashi.values()
        val normalized = (longitudeDeg % 360.0 + 360.0) % 360.0
        val rashiIndex = (normalized / 30.0).toInt().coerceIn(0, 11)
        val degInRashi = normalized % 30.0
        val isOddSign = (rashiIndex % 2 == 0) // 0-based: 0 (Aries), 2 (Gemini), 4 (Leo)... are odd signs

        return when (varga) {
            VargaType.D1 -> allRashis[rashiIndex]

            VargaType.D2 -> { // Hora (15° divisions: Sun=Leo, Moon=Cancer)
                val horaIdx = if (degInRashi < 15.0) 0 else 1
                if (isOddSign) {
                    if (horaIdx == 0) Rashi.SIMHA else Rashi.KARKATA
                } else {
                    if (horaIdx == 0) Rashi.KARKATA else Rashi.SIMHA
                }
            }

            VargaType.D3 -> { // Drekkana (10° divisions: 1st, 5th, 9th sign)
                val part = (degInRashi / 10.0).toInt().coerceIn(0, 2)
                val targetIdx = (rashiIndex + (part * 4)) % 12
                allRashis[targetIdx]
            }

            VargaType.D4 -> { // Chaturthamsha (7°30' divisions: 1st, 4th, 7th, 10th sign)
                val part = (degInRashi / 7.5).toInt().coerceIn(0, 3)
                val targetIdx = (rashiIndex + (part * 3)) % 12
                allRashis[targetIdx]
            }

            VargaType.D7 -> { // Saptamsha (4°17'08.57" divisions: odd start from self, even from 7th)
                val span = 30.0 / 7.0
                val part = (degInRashi / span).toInt().coerceIn(0, 6)
                val startIdx = if (isOddSign) rashiIndex else (rashiIndex + 6) % 12
                val targetIdx = (startIdx + part) % 12
                allRashis[targetIdx]
            }

            VargaType.D9 -> { // Navamsha (3°20' divisions: Fire start Aries, Earth start Cap, Air start Lib, Water start Can)
                val span = 30.0 / 9.0 // 3.3333333333
                val part = (degInRashi / span).toInt().coerceIn(0, 8)
                val startSign = when (rashiIndex % 4) {
                    0 -> 0 // Fiery (Aries, Leo, Sag) -> Start Aries (0)
                    1 -> 9 // Earthy (Taurus, Virgo, Cap) -> Start Capricorn (9)
                    2 -> 6 // Airy (Gemini, Libra, Aqu) -> Start Libra (6)
                    else -> 3 // Watery (Cancer, Scorp, Pisces) -> Start Cancer (3)
                }
                val targetIdx = (startSign + part) % 12
                allRashis[targetIdx]
            }

            VargaType.D10 -> { // Dashamsha (3° divisions: odd start from self, even from 9th)
                val part = (degInRashi / 3.0).toInt().coerceIn(0, 9)
                val startIdx = if (isOddSign) rashiIndex else (rashiIndex + 8) % 12
                val targetIdx = (startIdx + part) % 12
                allRashis[targetIdx]
            }

            VargaType.D12 -> { // Dwadashamsha (2°30' divisions: start from self)
                val part = (degInRashi / 2.5).toInt().coerceIn(0, 11)
                val targetIdx = (rashiIndex + part) % 12
                allRashis[targetIdx]
            }

            VargaType.D16 -> { // Shodashamsha (1°52'30" divisions: Movable from Aries, Fixed from Leo, Dual from Sag)
                val span = 30.0 / 16.0
                val part = (degInRashi / span).toInt().coerceIn(0, 15)
                val startSign = when (rashiIndex % 3) {
                    0 -> 0 // Movable -> Aries (0)
                    1 -> 4 // Fixed -> Leo (4)
                    else -> 8 // Dual -> Sagittarius (8)
                }
                val targetIdx = (startSign + part) % 12
                allRashis[targetIdx]
            }

            VargaType.D20 -> { // Vimshamsha (1°30' divisions: Movable from Aries, Fixed from Sag, Dual from Leo)
                val span = 1.5
                val part = (degInRashi / span).toInt().coerceIn(0, 19)
                val startSign = when (rashiIndex % 3) {
                    0 -> 0 // Movable -> Aries
                    1 -> 8 // Fixed -> Sagittarius
                    else -> 4 // Dual -> Leo
                }
                val targetIdx = (startSign + part) % 12
                allRashis[targetIdx]
            }

            VargaType.D24 -> { // Chaturvimshamsha (1°15' divisions: odd from Leo, even from Cancer)
                val span = 1.25
                val part = (degInRashi / span).toInt().coerceIn(0, 23)
                val startSign = if (isOddSign) 4 else 3 // Leo (4), Cancer (3)
                val targetIdx = (startSign + part) % 12
                allRashis[targetIdx]
            }

            VargaType.D27 -> { // Saptavimshamsha (1°06'40" divisions: Fire from Aries, Earth from Cancer, Air from Libra, Water from Cap)
                val span = 30.0 / 27.0
                val part = (degInRashi / span).toInt().coerceIn(0, 26)
                val startSign = when (rashiIndex % 4) {
                    0 -> 0 // Fire -> Aries
                    1 -> 3 // Earth -> Cancer
                    2 -> 6 // Air -> Libra
                    else -> 9 // Water -> Capricorn
                }
                val targetIdx = (startSign + part) % 12
                allRashis[targetIdx]
            }

            VargaType.D30 -> { // Trimshamsha (5 unequal planetary zones)
                if (isOddSign) {
                    when {
                        degInRashi < 5.0 -> Rashi.MESHA      // Mars (0-5°)
                        degInRashi < 10.0 -> Rashi.KUMBHA   // Saturn (5-10°)
                        degInRashi < 18.0 -> Rashi.DHANUS   // Jupiter (10-18°)
                        degInRashi < 25.0 -> Rashi.MITHUNA  // Mercury (18-25°)
                        else -> Rashi.TULA                  // Venus (25-30°)
                    }
                } else {
                    when {
                        degInRashi < 5.0 -> Rashi.VRISHABHA // Venus (0-5°)
                        degInRashi < 12.0 -> Rashi.KANYA    // Mercury (5-12°)
                        degInRashi < 20.0 -> Rashi.MEENA    // Jupiter (12-20°)
                        degInRashi < 25.0 -> Rashi.MAKARA   // Saturn (20-25°)
                        else -> Rashi.VRISHCHIKA            // Mars (25-30°)
                    }
                }
            }

            VargaType.D40 -> { // Khavedamsha (45' divisions: odd from Aries, even from Libra)
                val span = 0.75
                val part = (degInRashi / span).toInt().coerceIn(0, 39)
                val startSign = if (isOddSign) 0 else 6
                val targetIdx = (startSign + part) % 12
                allRashis[targetIdx]
            }

            VargaType.D45 -> { // Akshavedamsha (40' divisions: Movable from Aries, Fixed from Leo, Dual from Sag)
                val span = 40.0 / 60.0
                val part = (degInRashi / span).toInt().coerceIn(0, 44)
                val startSign = when (rashiIndex % 3) {
                    0 -> 0
                    1 -> 4
                    else -> 8
                }
                val targetIdx = (startSign + part) % 12
                allRashis[targetIdx]
            }

            VargaType.D60 -> { // Shashtiamsha (30' divisions: start from self)
                val span = 0.5
                val part = (degInRashi / span).toInt().coerceIn(0, 59)
                val targetIdx = (rashiIndex + part) % 12
                allRashis[targetIdx]
            }
        }
    }
}
