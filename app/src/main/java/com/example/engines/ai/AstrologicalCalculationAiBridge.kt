package com.example.engines.ai

import com.example.api.generateAstrologicalCalculationAiInterpretation
import com.example.core.model.DailyHarmonyScore
import com.example.core.model.KundaliChartData
import com.example.core.model.Nakshatra
import com.example.core.model.PanchangaData
import com.example.core.model.Rashi
import com.example.engines.jyotisha.AshtakootaMatchingResult
import com.example.engines.jyotisha.JyotishaEngine
import java.util.Locale

/**
 * AstrologicalCalculationAiBridge
 * 
 * Pipeline:
 * 1. Computational libraries generate exact mathematical/astronomical astrological details (Ephemeris, Jyotisha, Panchanga, Ashtakoota, Vastu, Numerology).
 * 2. Bridge formats the exact calculation output.
 * 3. Passes details to AI with mandatory directive: "RESPONSE SHOULD BE IN %VARIABLE_SELECTED_LANGUAGE%".
 * 4. Returns translated, culturally authentic interpretation to be visible to the user.
 */
object AstrologicalCalculationAiBridge {

    suspend fun interpretKundali(
        natalSummary: JyotishaEngine.NatalChartSummary,
        kundaliData: KundaliChartData,
        currentLanguage: String = "en"
    ): String {
        val planetaryDetails = StringBuilder()
        natalSummary.planetaryPositions.forEach { (graha, deg) ->
            val rashiIdx = (deg / 30.0).toInt().coerceIn(0, 11)
            val rashi = Rashi.values()[rashiIdx]
            val degInSign = deg % 30.0
            val nakshatraIdx = (deg / (360.0 / 27.0)).toInt().coerceIn(0, 26)
            val nak = Nakshatra.values()[nakshatraIdx]
            val pada = ((deg % (360.0 / 27.0)) / (360.0 / 108.0)).toInt() + 1
            planetaryDetails.appendLine(
                "• ${graha.displayName} (${graha.sanskritName}): ${rashi.westernName} (${rashi.sanskritName}) at ${String.format(Locale.US, "%.2f°", degInSign)}, Nakshatra: ${nak.sanskritName} Pada $pada"
            )
        }

        val bhavaDetails = StringBuilder()
        kundaliData.bhavas.forEach { b ->
            val grahasInHouse = b.occupyingGrahas.joinToString(", ") { it.displayName }
            bhavaDetails.appendLine("• House ${b.houseNumber} (${b.traditionalName} - ${b.devanagariName}, Rashi: ${b.rashi.westernName}): Occupants: ${if (grahasInHouse.isBlank()) "None" else grahasInHouse}")
        }

        val calculationDetails = """
            [CALCULATED VEDIC KUNDLI (D1) PARAMETERS]
            - Ascendant (Lagna): ${natalSummary.lagnaRashi.westernName} (${natalSummary.lagnaRashi.sanskritName}) at ${String.format(Locale.US, "%.2f°", natalSummary.lagnaDegrees)}
            - Moon Sign (Chandra Rashi): ${natalSummary.chandraRashi.westernName} (${natalSummary.chandraRashi.sanskritName})
            - Sun Sign (Surya Rashi): ${natalSummary.suryaRashi.westernName} (${natalSummary.suryaRashi.sanskritName})
            - Natal Nakshatra: ${natalSummary.janmaNakshatra.sanskritName} (${natalSummary.janmaNakshatra.devanagari}) (Pada ${natalSummary.janmaNakshatraPada})
            
            [GRAHA SPASHTA (PLANETARY POSITIONS)]:
            $planetaryDetails
            
            [BHAVA (HOUSE) DISPOSITIONS]:
            $bhavaDetails
        """.trimIndent()

        return generateAstrologicalCalculationAiInterpretation(
            calculationDetails = calculationDetails,
            astrologicalDomain = "Vedic Janam Kundli Analysis (D1 Birth Chart, Lagna, Planetary Dignities, Bhavas, and Remedies)",
            languageCode = currentLanguage
        )
    }

    suspend fun interpretCompatibility(
        matchResult: AshtakootaMatchingResult,
        seekerName: String,
        partnerName: String,
        seekerMoon: String,
        partnerMoon: String,
        currentLanguage: String = "en"
    ): String {
        val scoresBreakdown = matchResult.scores.joinToString("\n") { 
            "• ${it.kootaName} (${it.sanskritName}): ${it.pointsObtained} / ${it.maxPoints} pts -> ${it.description} (${it.details})" 
        }

        val calculationDetails = """
            [CALCULATED ASHTAKOOTA 36-POINT MATCHING DETAILS]
            - Seeker: $seekerName ($seekerMoon)
            - Partner: $partnerName ($partnerMoon)
            - Total Ashtakoota Score: ${matchResult.totalPoints} / 36 (${matchResult.compatibilityPercentage}%)
            - Rating Category: ${matchResult.ratingCategory}
            - Manglik Compatibility Verdict: ${matchResult.manglikCompatibilityVerdict}
            - Overall Classical Summary: ${matchResult.overallInterpretation}
            
            [INDIVIDUAL 8 KOOTA SCORES]:
            $scoresBreakdown
        """.trimIndent()

        return generateAstrologicalCalculationAiInterpretation(
            calculationDetails = calculationDetails,
            astrologicalDomain = "Vedic Ashtakoota Kundli Milan & Marriage Compatibility Analysis",
            languageCode = currentLanguage
        )
    }

    suspend fun interpretPanchanga(
        panchanga: PanchangaData,
        dailyHarmony: DailyHarmonyScore? = null,
        currentLanguage: String = "en"
    ): String {
        val calculationDetails = """
            [CALCULATED DAINIK PANCHANGA (5 LIMBS OF TIME)]
            - 1. Vara (Solar Day): ${panchanga.vara} (${panchanga.varaDevanagari})
            - 2. Tithi (Lunar Phase): ${panchanga.tithi.name} (${panchanga.tithi.paksha.displayName}), Number: ${panchanga.tithi.number}
            - 3. Nakshatra: ${panchanga.nakshatra.sanskritName} (Pada ${panchanga.nakshatraPada}, Ruling Lord: ${panchanga.nakshatra.rulingGraha.displayName})
            - 4. Yoga: ${panchanga.yoga}
            - 5. Karana: ${panchanga.karana}
            
            [SOLAR TIMINGS]:
            - Sunrise: ${panchanga.sunrise}
            - Sunset: ${panchanga.sunset}
            - Solar Noon: ${panchanga.solarNoon}
            
            [CALENDRICAL CYCLES]:
            - Lunar Month (Masa): ${panchanga.masa}
            - Season (Ritu): ${panchanga.ritu}
            - Solar Course (Ayana): ${panchanga.ayana}
            - Vikram Samvat: ${panchanga.vikramSamvat}, Shaka Samvat: ${panchanga.shakaSamvat}
            
            ${if (dailyHarmony != null) """
            [PERSONAL TARABALA HARMONY]:
            - Daily Cosmic Harmony Score: ${dailyHarmony.overallScorePercent}%
            - Tara Type: ${dailyHarmony.taraType.displayName}
            - Tarabala Classical Advice: ${dailyHarmony.advice}
            """.trimIndent() else ""}
        """.trimIndent()

        return generateAstrologicalCalculationAiInterpretation(
            calculationDetails = calculationDetails,
            astrologicalDomain = "Dainik Vedic Panchanga & Daily Planetary Energy Interpretation",
            languageCode = currentLanguage
        )
    }

    suspend fun interpretVastu(
        roomName: String,
        direction: String,
        suitability: String,
        explanation: String,
        remedy: String? = null,
        bestAlternatives: List<String> = emptyList(),
        currentLanguage: String = "en"
    ): String {
        val calculationDetails = """
            [CALCULATED VASTU SHASTRA SPATIAL EVALUATION]
            - Selected Room / Zone: $roomName
            - Direction Evaluated: $direction
            - Suitability Status: $suitability
            - Architectural Impact / Analysis: $explanation
            ${if (!remedy.isNullOrBlank()) "- Recommended Remedy: $remedy" else ""}
            ${if (bestAlternatives.isNotEmpty()) "- Recommended Alternative Directions: ${bestAlternatives.joinToString(", ")}" else ""}
        """.trimIndent()

        return generateAstrologicalCalculationAiInterpretation(
            calculationDetails = calculationDetails,
            astrologicalDomain = "Vastu Shastra Spatial Energy Audit & Remedial Consultation",
            languageCode = currentLanguage
        )
    }

    suspend fun interpretNumerology(
        fullName: String,
        birthDate: String,
        mulank: Int,
        bhagyank: Int,
        namank: Int,
        rulingGraha: String,
        favorableDays: String,
        favorableColors: String,
        currentLanguage: String = "en"
    ): String {
        val calculationDetails = """
            [CALCULATED VEDIC SANKHYA SHASTRA (NUMEROLOGY) PARAMETERS]
            - Full Name: $fullName
            - Date of Birth: $birthDate
            - Mulank (Birth Number / Root Number): $mulank (Ruled by: $rulingGraha)
            - Bhagyank (Destiny Number / Life Path): $bhagyank
            - Namank (Name Number): $namank
            - Favorable Cosmic Days: $favorableDays
            - Harmonic Colors: $favorableColors
        """.trimIndent()

        return generateAstrologicalCalculationAiInterpretation(
            calculationDetails = calculationDetails,
            astrologicalDomain = "Vedic Sankhya Shastra (Numerology Vibrational Analysis and Affirmations)",
            languageCode = currentLanguage
        )
    }
}
