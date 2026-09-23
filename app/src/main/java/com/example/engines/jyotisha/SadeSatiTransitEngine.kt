package com.example.engines.jyotisha

import com.example.core.model.Graha
import com.example.core.model.Rashi
import java.time.LocalDate

enum class SadeSatiStatus(val displayName: String, val hindiName: String, val severityLevel: String) {
    PHASE_1_RISING("Sade Sati: Phase 1 (Rising)", "साढ़ेसाती: प्रथम चरण (उदय)", "Rising Karmic Awakening"),
    PHASE_2_PEAK("Sade Sati: Phase 2 (Peak / Janma Shani)", "साढ़ेसाती: द्वितीय चरण (शिखर - जन्म शनि)", "Deep Resilience & Transformation"),
    PHASE_3_SETTING("Sade Sati: Phase 3 (Setting)", "साढ़ेसाती: तृतीय चरण (अस्त)", "Establishing Stability & Reaping Rewards"),
    KANTAKA_SHANI("Kantaka Shani (4th House Transit)", "कंटक शनि (चतुर्थ भाव गोचर)", "Emotional Discipline & Patience"),
    ASHTAMA_SHANI("Ashtama Shani (8th House Transit)", "अष्टम शनि (कष्ट एवं जीवन परिवर्तन)", "Profound Karmic Reorganization"),
    NOT_ACTIVE("No Major Saturn Affliction", "शनि अनुकूल गोचर", "Supportive / Harmonious Transit")
}

data class SadeSatiReport(
    val status: SadeSatiStatus,
    val natalMoonSign: Rashi,
    val transitSaturnSign: Rashi,
    val houseFromMoon: Int,
    val phaseDescription: String,
    val lifeImpacts: List<String>,
    val traditionalRemedies: List<String>,
    val classicalStotra: String
)

data class GuruGocharReport(
    val transitJupiterSign: Rashi,
    val houseFromMoon: Int,
    val isAuspicious: Boolean,
    val blessingTitle: String,
    val description: String,
    val suggestedSadhana: String
)

data class PlanetaryTransitSummary(
    val currentSaturnReport: SadeSatiReport,
    val currentJupiterReport: GuruGocharReport,
    val activeTransitAlerts: List<String>
)

object SadeSatiTransitEngine {

    private data class SadeSatiDetails(
        val desc: String,
        val impacts: List<String>,
        val remedies: List<String>,
        val stotra: String
    )

    fun calculateSadeSati(natalMoon: Rashi, transitSaturn: Rashi): SadeSatiReport {
        val houseFromMoon = ((transitSaturn.ordinal - natalMoon.ordinal + 12) % 12) + 1

        val (status, details) = when (houseFromMoon) {
            12 -> {
                SadeSatiStatus.PHASE_1_RISING to SadeSatiDetails(
                    "Saturn enters the 12th house from your natal Moon sign. This begins the 7.5-year cycle, initiating financial realignment, subtle spiritual introspections, and shedding of outdated attachments.",
                    listOf("Increased expenses or investments in foundational assets", "Shift in residence, travel, or foreign connections", "Need for sound sleep routines and spiritual solitude"),
                    listOf("Chanting Hanuman Chalisa every Tuesday and Saturday", "Lighting a sesame oil lamp under a Peepal tree on Saturdays", "Donating black mustard seeds or dark woolen blankets to the needy"),
                    "ॐ शं शनैश्चराय नमः (Om Shaṁ Śanaiścarāya Namaḥ) • Dasharatha Shani Stotram"
                )
            }
            1 -> {
                SadeSatiStatus.PHASE_2_PEAK to SadeSatiDetails(
                    "Saturn transits directly over your natal Moon (Janma Shani). This is the focal heart of Sade Sati, testing mental resolve, forging unshakeable inner character, and rewarding rigorous integrity.",
                    listOf("Intense focus on health, emotional endurance, and life purpose", "Increased professional responsibilities and leadership duties", "Breakthroughs after disciplined perseverance"),
                    listOf("Daily recitation of Sundarkand or Hanuman Chalisa", "Offering water mixed with black sesame to Shiva Lingam on Saturdays", "Strict truthfulness and humble service to elders and workers"),
                    "नीलांजनसमाभासं रविपुत्रं यमाग्रजम् । छायामार्तण्डसम्भूतं तं नमामि शनैश्चरम् ॥"
                )
            }
            2 -> {
                SadeSatiStatus.PHASE_3_SETTING to SadeSatiDetails(
                    "Saturn moves into the 2nd house from your Moon sign. In this concluding phase, financial discipline crystallizes, family ties re-harmonize, and the profound lessons of the past 5 years bear enduring fruit.",
                    listOf("Restructuring savings, family finances, and dietary habits", "Speaking with measured calm and diplomacy", "Relief from peak mental pressure and steady gains"),
                    listOf("Feeding stray dogs or crows with chapatis/bread in the mornings", "Chanting Maha Mrityunjaya Mantra 11 times at twilight", "Maintaining non-acquisitive gratitude"),
                    "ॐ त्र्यम्बकं यजामहे सुगन्धिं पुष्टिवर्धनम् उर्वारुकमिव बन्धनान् मृत्योर्मुक्षीय मामृतात् ॥"
                )
            }
            4 -> {
                SadeSatiStatus.KANTAKA_SHANI to SadeSatiDetails(
                    "Saturn transits the 4th house (Sukha Bhava) from natal Moon. Demands patience in home matters, vehicle care, and cultivating heart-centered contentment.",
                    listOf("Focus on domestic harmony and maternal health", "Renovation or structural maintenance of living spaces", "Calming the restless mind through pranayama"),
                    listOf("Reciting Hanuman Ashtak", "Keeping a silver coin or drinking water from a silver cup", "Regular meditation to ground emotions"),
                    "ॐ प्रां प्रीं प्रौं सः शनैश्चराय नमः ॥"
                )
            }
            8 -> {
                SadeSatiStatus.ASHTAMA_SHANI to SadeSatiDetails(
                    "Saturn transits the 8th house from natal Moon. A deeply transformative 2.5-year period that cleanses hidden karmic baggage and awakens occult or spiritual awareness.",
                    listOf("Unpredictable events demanding spiritual surrender and maturity", "Profound inner purification and detachment from vanity", "Spiritual evolution through yogic sadhana"),
                    listOf("Reciting Shiva Tandava Stotram or Maha Mrityunjaya Japa", "Saturday oil charity (Tailabhisheka)", "Practicing regular Ahimsa (compassion) and humility"),
                    "ॐ हौं जूं सः ॐ भूर्भुवः स्वः ॐ त्र्यम्बकं यजामहे ॥"
                )
            }
            else -> {
                val isAuspiciousHouse = houseFromMoon in setOf(3, 6, 11)
                val statusTitle = if (isAuspiciousHouse) SadeSatiStatus.NOT_ACTIVE else SadeSatiStatus.NOT_ACTIVE
                val explanation = if (isAuspiciousHouse) {
                    "Saturn is currently placed in the ${houseFromMoon}th Upachaya house from your Moon sign, which classical Vedic astrology considers highly auspicious for overcoming obstacles, achieving professional victories, and gaining lasting recognition."
                } else {
                    "Saturn is in the ${houseFromMoon}th house from your natal Moon sign. Normal routine karmic duties apply with no severe Sade Sati influence."
                }
                statusTitle to SadeSatiDetails(
                    explanation,
                    listOf("Favorable focus on long-term career foundation", "Steady endurance and constructive willpower", "Steady growth without excessive strain"),
                    listOf("Continue regular karma yoga and integrity in professional commitments", "Honoring parents and mentor figures"),
                    "ॐ शं शनैश्चराय नमः ॥"
                )
            }
        }

        return SadeSatiReport(
            status = status,
            natalMoonSign = natalMoon,
            transitSaturnSign = transitSaturn,
            houseFromMoon = houseFromMoon,
            phaseDescription = details.desc,
            lifeImpacts = details.impacts,
            traditionalRemedies = details.remedies,
            classicalStotra = details.stotra
        )
    }

    fun calculateGuruGochar(natalMoon: Rashi, transitJupiter: Rashi): GuruGocharReport {
        val houseFromMoon = ((transitJupiter.ordinal - natalMoon.ordinal + 12) % 12) + 1
        val isAuspicious = houseFromMoon in setOf(2, 5, 7, 9, 11)

        val (title, desc, sadhana) = when (houseFromMoon) {
            2 -> Triple(
                "Dhana & Vani Vriddhi (2nd House Transit)",
                "Brihaspati bestows wealth, eloquent and sweet speech, family harmony, and financial security.",
                "Chanting Guru Gayatri or reading holy scriptures on Thursdays."
            )
            5 -> Triple(
                "Purva Punya & Buddhi Vikasa (5th House Transit)",
                "Supreme auspiciousness for intellectual brilliance, creative projects, higher education, and auspicious events with children.",
                "Offering yellow flowers and chana dal to Lord Vishnu or Guru idol."
            )
            7 -> Triple(
                "Kalatra & Vyapar Sukha (7th House Transit)",
                "Brings marital harmony, prosperous partnerships, public respect, and blossoming friendships.",
                "Worship of Lakshmi-Narayana and wearing subtle yellow/gold colors."
            )
            9 -> Triple(
                "Bhagya & Dharma Udaya (9th House Transit)",
                "The most exalted transit! Awakens spiritual fortune, divine grace, pilgrimages, and father's blessings.",
                "Visiting sacred temples, respecting gurus, and donating to spiritual causes."
            )
            11 -> Triple(
                "Labha & Sarvasiddhi (11th House Transit)",
                "Fulfillment of aspirations, abundant gains, wide networking, and social recognition.",
                "Practicing generous Annadanam (food charity) and thankful prayers."
            )
            else -> Triple(
                "Antarmukhi Sadhana (${houseFromMoon}th House Transit)",
                "Jupiter's placement encourages quiet wisdom, study, self-reflection, and inner spiritual refinement rather than loud external displays.",
                "Daily chanting of 'ॐ बृं बृहस्पतये नमः' and cultivating patience."
            )
        }

        return GuruGocharReport(
            transitJupiterSign = transitJupiter,
            houseFromMoon = houseFromMoon,
            isAuspicious = isAuspicious,
            blessingTitle = title,
            description = desc,
            suggestedSadhana = sadhana
        )
    }
}
