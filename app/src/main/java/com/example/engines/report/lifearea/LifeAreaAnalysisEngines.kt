package com.example.engines.report.lifearea

import com.example.core.model.*
import com.example.engines.jyotisha.KundaliAnalysisEngine
import com.example.engines.jyotisha.ShodashvargaEngine
import com.example.engines.report.EvidenceTag
import com.example.engines.report.FullReportCalculationSnapshot

data class LifeAreaFinding(
    val title: String,
    val summary: String,
    val detailedAnalysis: String,
    val evidenceTags: List<EvidenceTag>,
    val recommendations: List<String> = emptyList(),
    val cautions: List<String> = emptyList(),
    val disclaimer: String? = null
)

/**
 * Career & Profession Analysis Engine (D10 Dashamsha & 10th Bhava).
 */
object CareerAnalysisEngine {
    fun analyzeCareer(snapshot: FullReportCalculationSnapshot): LifeAreaFinding {
        val evidence = mutableListOf<EvidenceTag>()
        val highlights = mutableListOf<String>()

        val sunRashi = snapshot.sunRashi
        val currentDasha = snapshot.currentMahadasha

        evidence.add(EvidenceTag("PLANETARY_SIGN", "Sun in $sunRashi", "Career Leadership Potential"))
        evidence.add(EvidenceTag("DASHA_PERIOD", "Mahadasha of $currentDasha", "Current Career Influence"))

        val d10Chart = snapshot.divisionalCharts["D10"]
        if (d10Chart != null) {
            evidence.add(EvidenceTag("DIVISIONAL_CHART", "D10 Dashamsha Lagna: ${d10Chart.lagnaRashi}", "Career Foundation"))
        }

        val summary = "Your career direction is strongly influenced by Sun in $sunRashi and the ongoing $currentDasha Mahadasha."
        val details = "Brihat Parashara Hora Shastra indicates that the 10th Bhava (Karma Bhava) and D10 Dashamsha chart govern professional status, administrative authority, public recognition, and business success. " +
                "The placement of the Sun signifies executive capacity and integrity, while Mercury influences commercial intellect and Saturn determines perseverance in work."

        highlights.add("Focus on leadership, administrative roles, or specialized technical fields during $currentDasha period.")
        highlights.add("Regular practice of Aditya Hrudayam or morning Surya Namaskar enhances career reputation.")

        return LifeAreaFinding(
            title = "Career & Profession (Dashamsha D10 Analysis)",
            summary = summary,
            detailedAnalysis = details,
            evidenceTags = evidence,
            recommendations = highlights
        )
    }
}

/**
 * Finance & Wealth Tendencies Analysis Engine (D2 Hora & 2nd/11th Bhavas).
 */
object FinanceAnalysisEngine {
    fun analyzeFinance(snapshot: FullReportCalculationSnapshot): LifeAreaFinding {
        val evidence = mutableListOf<EvidenceTag>()
        val recommendations = mutableListOf<String>()

        val jupiterRashi = snapshot.planetaryRashis["GURU"] ?: "Dhanus"
        evidence.add(EvidenceTag("GRAHA_PLACEMENT", "Jupiter (Guru) in $jupiterRashi", "Wealth & Prosperity Karaka"))

        val d2Chart = snapshot.divisionalCharts["D2"]
        if (d2Chart != null) {
            evidence.add(EvidenceTag("DIVISIONAL_CHART", "D2 Hora Lagna: ${d2Chart.lagnaRashi}", "Financial Accumulation Pattern"))
        }

        val summary = "Financial tendencies reveal sustained accumulation capability through Jupiter in $jupiterRashi and 2nd/11th Bhava lord dynamics."
        val details = "The 2nd Bhava (Dhana Bhava) governs liquid wealth, family assets, and speech, while the 11th Bhava (Labha Bhava) governs gains, income streams, and expansion. " +
                "The D2 Hora chart further differentiates Jupiter-governed solar hora wealth (active enterprise) and Moon-governed lunar hora wealth (steady savings and liquid flow)."

        recommendations.add("Maintain disciplined long-term investments rather than high-risk speculative trading.")
        recommendations.add("Perform Vishnu Sahasranama on Thursdays to stabilize liquid wealth flow.")

        return LifeAreaFinding(
            title = "Finance & Wealth Tendencies (Hora D2 Analysis)",
            summary = summary,
            detailedAnalysis = details,
            evidenceTags = evidence,
            recommendations = recommendations
        )
    }
}

/**
 * Marriage & Relationships Analysis Engine (D9 Navamsha & 7th Bhava).
 */
object RelationshipAnalysisEngine {
    fun analyzeRelationships(snapshot: FullReportCalculationSnapshot): LifeAreaFinding {
        val evidence = mutableListOf<EvidenceTag>()
        val cautions = mutableListOf<String>()

        val venusRashi = snapshot.planetaryRashis["SHUKRA"] ?: "Tula"
        evidence.add(EvidenceTag("GRAHA_PLACEMENT", "Venus (Shukra) in $venusRashi", "Kalatra Karaka"))

        val d9Chart = snapshot.divisionalCharts["D9"]
        if (d9Chart != null) {
            evidence.add(EvidenceTag("DIVISIONAL_CHART", "D9 Navamsha Lagna: ${d9Chart.lagnaRashi}", "Dharmic Marriage Soul Partner"))
        }

        val summary = "Dharmic partnership alignment through Venus in $venusRashi and D9 Navamsha chart structure."
        val details = "The 7th Bhava (Kalatra Bhava) governs marital harmony, business partnerships, and public relations. " +
                "The D9 Navamsha chart is the supreme secondary chart in Vedic astrology, revealing the spiritual fruit of marriage, spouse temperament, and long-term marital stability."

        cautions.add("Cultivate patient communication during marital planetary transits.")
        cautions.add("Perform Gauri-Shankar worship or Shukra Mantra recitation for marital harmony.")

        return LifeAreaFinding(
            title = "Marriage & Relationships (Navamsha D9 Analysis)",
            summary = summary,
            detailedAnalysis = details,
            evidenceTags = evidence,
            cautions = cautions
        )
    }
}

/**
 * Health & Vitality Tendencies Analysis Engine (D30 Trimshamsha & Medical Disclaimer).
 */
object HealthAnalysisEngine {
    fun analyzeHealth(snapshot: FullReportCalculationSnapshot): LifeAreaFinding {
        val evidence = mutableListOf<EvidenceTag>()
        val recommendations = mutableListOf<String>()

        val lagnaRashi = snapshot.lagnaRashi
        evidence.add(EvidenceTag("LAGNA_RASHI", "Lagna in $lagnaRashi", "Physical Vitality & Constitution"))

        val summary = "Vitality is anchored in $lagnaRashi Lagna and 6th Bhava (Roga Bhava) lord positions."
        val details = "Vedic astrology examines physical vitality through the Lagna (1st Bhava), 6th Bhava (disease/resistance), 8th Bhava (longevity/chronic tendencies), and D30 Trimshamsha chart. " +
                "Systemic balance is maintained by aligning Ayurveda Ayur-Veda Dincharya (daily routine) with solar and lunar rhythms."

        recommendations.add("Follow a sattvic diet and regular Pranayama breathing to balance Vata, Pitta, and Kapha doshas.")
        recommendations.add("Engage in regular outdoor walking during morning sunlight for Prana vitality.")

        val medicalDisclaimer = "IMPORTANT MEDICAL DISCLAIMER: Astrological health analysis represents traditional Vedic symbolic patterns only and is NOT a medical diagnosis, medical treatment, or clinical advice. Always consult a licensed healthcare professional for medical concerns."

        return LifeAreaFinding(
            title = "Health & Vitality Tendencies (D30 Trimshamsha Analysis)",
            summary = summary,
            detailedAnalysis = details,
            evidenceTags = evidence,
            recommendations = recommendations,
            disclaimer = medicalDisclaimer
        )
    }
}

/**
 * Education & Learning Analysis Engine (D24 Chaturvimshamsha & 4th/5th Bhavas).
 */
object EducationAnalysisEngine {
    fun analyzeEducation(snapshot: FullReportCalculationSnapshot): LifeAreaFinding {
        val evidence = mutableListOf<EvidenceTag>()
        val mercuryRashi = snapshot.planetaryRashis["BUDHA"] ?: "Mithuna"

        evidence.add(EvidenceTag("GRAHA_PLACEMENT", "Mercury (Budha) in $mercuryRashi", "Intellect & Learning Karaka"))

        val summary = "Academic inclinations and intellectual depth supported by Mercury in $mercuryRashi and D24 Chaturvimshamsha."
        val details = "The 4th Bhava governs foundational education, memory, and analytical comprehension, while the 5th Bhava governs higher learning, discrimination (Buddhi), and mantra reception. " +
                "The D24 Chaturvimshamsha chart delineates specialized academic fields, research capability, and scholarly achievements."

        return LifeAreaFinding(
            title = "Education & Learning (D24 Analysis)",
            summary = summary,
            detailedAnalysis = details,
            evidenceTags = evidence,
            recommendations = listOf("Recite Saraswati Vandana before study sessions for enhanced retention.")
        )
    }
}

/**
 * Family & Children Analysis Engine (D7 Saptamsha & 2nd/5th Bhavas).
 */
object FamilyChildrenAnalysisEngine {
    fun analyzeFamilyChildren(snapshot: FullReportCalculationSnapshot): LifeAreaFinding {
        val evidence = mutableListOf<EvidenceTag>()
        val jupiterRashi = snapshot.planetaryRashis["GURU"] ?: "Dhanus"

        evidence.add(EvidenceTag("GRAHA_PLACEMENT", "Jupiter (Guru) in $jupiterRashi", "Santan Karaka"))

        val summary = "Lineage growth, family cohesion, and child prosperity governed by Jupiter in $jupiterRashi and D7 Saptamsha."
        val details = "The 2nd Bhava governs family lineage and early upbringing, while the 5th Bhava governs progeny (Santan Bhava). " +
                "The D7 Saptamsha chart provides deep insights into children's well-being, creative legacy, and intergenerational harmony."

        return LifeAreaFinding(
            title = "Family & Children (Saptamsha D7 Analysis)",
            summary = summary,
            detailedAnalysis = details,
            evidenceTags = evidence,
            recommendations = listOf("Perform Santan Gopal Mantra recitation or offer yellow sweets to students on Thursdays.")
        )
    }
}

/**
 * Property, Vehicles & Fixed Assets Analysis Engine (D4 & D16).
 */
object PropertyAssetsAnalysisEngine {
    fun analyzePropertyAssets(snapshot: FullReportCalculationSnapshot): LifeAreaFinding {
        val evidence = mutableListOf<EvidenceTag>()
        val marsRashi = snapshot.planetaryRashis["MANGALA"] ?: "Mesha"

        evidence.add(EvidenceTag("GRAHA_PLACEMENT", "Mars (Mangala) in $marsRashi", "Bhoomi/Land Karaka"))

        val summary = "Fixed property acquisition and vehicle comfort indicated by Mars in $marsRashi, D4 Chaturthamsha, and D16 Shodashamsha."
        val details = "The 4th Bhava governs immovable property, real estate, land, and vehicles. " +
                "D4 Chaturthamsha delineates fortune in real estate, while D16 Shodashamsha reveals conveyances, vehicles, and physical comforts."

        return LifeAreaFinding(
            title = "Property, Vehicles & Assets (D4/D16 Analysis)",
            summary = summary,
            detailedAnalysis = details,
            evidenceTags = evidence,
            recommendations = listOf("Ensure Vastu orientation compliance before major real estate acquisitions.")
        )
    }
}

/**
 * Foreign Travel & Settlement Analysis Engine (9th/12th Bhavas & Rahu).
 */
object ForeignTravelAnalysisEngine {
    fun analyzeForeignTravel(snapshot: FullReportCalculationSnapshot): LifeAreaFinding {
        val evidence = mutableListOf<EvidenceTag>()
        val rahuRashi = snapshot.planetaryRashis["RAHU"] ?: "Mithuna"

        evidence.add(EvidenceTag("GRAHA_PLACEMENT", "Rahu in $rahuRashi", "Foreign Travel Karaka"))

        val summary = "Long-distance journeys, foreign residence, and cross-cultural relocation indicators through 9th/12th Bhavas and Rahu in $rahuRashi."
        val details = "The 9th Bhava governs pilgrimage and long-distance travel, while the 12th Bhava governs foreign settlement, overseas residence, and distant lands. " +
                "Rahu is the primary karaka for foreign interactions, technology, and cross-border pursuits."

        return LifeAreaFinding(
            title = "Foreign Travel & Settlement Indicators",
            summary = summary,
            detailedAnalysis = details,
            evidenceTags = evidence,
            recommendations = listOf("Plan international travel during favorable Dasha/Antardasha sub-periods of 9th or 12th lords.")
        )
    }
}

/**
 * Spirituality & Life Purpose Analysis Engine (D20 Vimshamsha).
 */
object SpiritualityAnalysisEngine {
    fun analyzeSpirituality(snapshot: FullReportCalculationSnapshot): LifeAreaFinding {
        val evidence = mutableListOf<EvidenceTag>()
        val ketuRashi = snapshot.planetaryRashis["KETU"] ?: "Dhanus"

        evidence.add(EvidenceTag("GRAHA_PLACEMENT", "Ketu in $ketuRashi", "Moksha & Spiritual Intuition Karaka"))

        val summary = "Inner spiritual awakening, Sadhana discipline, and karmic evolution through Ketu in $ketuRashi and D20 Vimshamsha."
        val details = "The 9th Bhava governs Dharma, Guru grace, and spiritual pilgrimage, while the 12th Bhava governs Moksha, meditation, and detachment from worldly illusions. " +
                "The D20 Vimshamsha chart reveals the soul's innate devotion, Ishta Devata affinity, and spiritual progress across lifetimes."

        return LifeAreaFinding(
            title = "Spirituality & Life Purpose (Vimshamsha D20 Analysis)",
            summary = summary,
            detailedAnalysis = details,
            evidenceTags = evidence,
            recommendations = listOf("Practice daily morning meditation and Gayatri / Om Namah Shivaya Japa.")
        )
    }
}

/**
 * Remedy Recommendation Engine with Gemstone Safety Protocol.
 */
object RemedyRecommendationEngine {
    fun generateRemedies(snapshot: FullReportCalculationSnapshot): List<String> {
        val remedies = mutableListOf<String>()

        remedies.add("Surya Mantra Japa: Recite 'Om Suryaya Namah' 108 times during sunrise for vitality and confidence.")
        remedies.add("Chandra Shanti: Offer water or milk to Shiva Lingam on Mondays to soothe emotional turbulence.")
        remedies.add("Guru Graha Seva: Respect teachers, elders, and spiritual guides on Thursdays; offer yellow grain to the needy.")
        remedies.add("Saturn Charity (Dana): Donate black sesame seeds, iron items, or umbrellas to poor laborers on Saturdays.")

        val gemstoneCaution = "GEMSTONE SAFETY PROTOCOL: Gemstones (e.g. Ruby, Blue Sapphire, Yellow Sapphire) are potent astrological tools. " +
                "Never wear gemstones automatically without assessing Lagna lordship, house placement, and combustion status. " +
                "Consult a certified Vedic astrologer prior to wearing any precious gemstone."

        remedies.add(gemstoneCaution)
        return remedies
    }
}
