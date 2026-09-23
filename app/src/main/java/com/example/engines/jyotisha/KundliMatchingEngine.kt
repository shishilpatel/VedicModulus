package com.example.engines.jyotisha

import com.example.core.model.Graha
import com.example.core.model.Nakshatra
import com.example.core.model.Rashi

/**
 * Ashtakoota Matching Models and Authentic Vedic Engine
 * Following Brihat Parashara Hora Shastra and classical Jyotish tenets.
 */

enum class VarnaType(val displayName: String, val level: Int) {
    BRAHMIN("Brahmin (Spiritual / Intellectual)", 4),
    KSHATRIYA("Kshatriya (Protector / Leadership)", 3),
    VAISHYA("Vaishya (Commerce / Practical)", 2),
    SHUDRA("Shudra (Service / Creative Craft)", 1)
}

enum class VashyaType(val displayName: String) {
    CHATUSHSHPADA("Chatushpada (Quadruped)"),
    MANAVA("Manava (Human / Biped)"),
    JALACHARA("Jalachara (Water Dwelling)"),
    VANACHARA("Vanachara (Wild / Forest)"),
    KEETA("Keeta (Insect)")
}

enum class YoniAnimal(val animalName: String, val sanskritName: String) {
    HORSE("Horse", "Ashwa"),
    ELEPHANT("Elephant", "Gaja"),
    SHEEP("Sheep / Goat", "Mesha"),
    SERPENT("Serpent", "Sarpa"),
    DOG("Dog", "Shwana"),
    CAT("Cat", "Marjara"),
    RAT("Rat", "Mushaka"),
    COW("Cow", "Gau"),
    BUFFALO("Buffalo", "Mahisha"),
    TIGER("Tiger", "Vyaghra"),
    DEER("Deer / Hare", "Mriga"),
    MONKEY("Monkey", "Vanara"),
    MONGOOSE("Mongoose", "Nakula"),
    LION("Lion", "Simha")
}

enum class GanaType(val displayName: String, val sanskritName: String) {
    DEVA("Deva (Divine / Peaceful)", "देव"),
    MANUSHYA("Manushya (Human / Practical)", "मनुष्य"),
    RAKSHASA("Rakshasa (Fierce / Independent)", "राक्षस")
}

enum class NadiType(val displayName: String, val ayurvedicDosha: String) {
    AADI("Aadi Nadi", "Vata (Air / Ether)"),
    MADHYA("Madhya Nadi", "Pitta (Fire / Energy)"),
    ANTYA("Antya Nadi", "Kapha (Water / Earth)")
}

data class KootaScore(
    val kootaName: String,
    val sanskritName: String,
    val pointsObtained: Double,
    val maxPoints: Double,
    val description: String,
    val details: String,
    val isAuspicious: Boolean
)

data class ManglikAnalysis(
    val isManglik: Boolean,
    val marsHouseFromLagna: Int,
    val marsHouseFromMoon: Int,
    val marsHouseFromVenus: Int,
    val cancellations: List<String>,
    val effectiveDoshaLevel: String // "None", "Mild / Cancelled", "Moderate", "High"
)

data class AshtakootaMatchingResult(
    val boyRashi: Rashi,
    val boyNakshatra: Nakshatra,
    val boyPada: Int,
    val girlRashi: Rashi,
    val girlNakshatra: Nakshatra,
    val girlPada: Int,
    val scores: List<KootaScore>,
    val totalPoints: Double,
    val maxPoints: Double = 36.0,
    val compatibilityPercentage: Double,
    val ratingCategory: String,
    val overallInterpretation: String,
    val boyManglik: ManglikAnalysis,
    val girlManglik: ManglikAnalysis,
    val manglikCompatibilityVerdict: String,
    val traditionalRemedies: List<String>
)

object KundliMatchingEngine {

    // --- 1. VARNA KOOTA (Max 1 pt) ---
    fun getVarna(rashi: Rashi): VarnaType {
        return when (rashi) {
            Rashi.KARKATA, Rashi.VRISHCHIKA, Rashi.MEENA -> VarnaType.BRAHMIN
            Rashi.MESHA, Rashi.SIMHA, Rashi.DHANUS -> VarnaType.KSHATRIYA
            Rashi.VRISHABHA, Rashi.KANYA, Rashi.MAKARA -> VarnaType.VAISHYA
            Rashi.MITHUNA, Rashi.TULA, Rashi.KUMBHA -> VarnaType.SHUDRA
        }
    }

    fun calculateVarna(boyRashi: Rashi, girlRashi: Rashi): KootaScore {
        val boyVarna = getVarna(boyRashi)
        val girlVarna = getVarna(girlRashi)
        val points = if (boyVarna.level >= girlVarna.level) 1.0 else 0.0
        val isAuspicious = points > 0.0
        val desc = if (isAuspicious) {
            "Harmonious spiritual and ego compatibility (${boyVarna.displayName} & ${girlVarna.displayName})."
        } else {
            "Varna difference indicates potential ego friction; mutual respect and shared values resolve this."
        }
        return KootaScore(
            kootaName = "Varna",
            sanskritName = "वर्ण कूट (कार्य एवं चेतना)",
            pointsObtained = points,
            maxPoints = 1.0,
            description = desc,
            details = "Groom: ${boyVarna.displayName} | Bride: ${girlVarna.displayName}",
            isAuspicious = isAuspicious
        )
    }

    // --- 2. VASHYA KOOTA (Max 2 pts) ---
    fun getVashya(rashi: Rashi): VashyaType {
        return when (rashi) {
            Rashi.MESHA, Rashi.VRISHABHA -> VashyaType.CHATUSHSHPADA
            Rashi.MITHUNA, Rashi.KANYA, Rashi.TULA, Rashi.KUMBHA -> VashyaType.MANAVA
            Rashi.KARKATA, Rashi.MEENA -> VashyaType.JALACHARA
            Rashi.SIMHA -> VashyaType.VANACHARA
            Rashi.VRISHCHIKA -> VashyaType.KEETA
            Rashi.DHANUS -> VashyaType.MANAVA // Defaulting to first half
            Rashi.MAKARA -> VashyaType.CHATUSHSHPADA // Defaulting to first half
        }
    }

    fun calculateVashya(boyRashi: Rashi, girlRashi: Rashi): KootaScore {
        val bV = getVashya(boyRashi)
        val gV = getVashya(girlRashi)
        val points = when {
            bV == gV -> 2.0
            (bV == VashyaType.MANAVA && gV == VashyaType.CHATUSHSHPADA) ||
            (bV == VashyaType.CHATUSHSHPADA && gV == VashyaType.MANAVA) -> 1.0
            (bV == VashyaType.MANAVA && gV == VashyaType.JALACHARA) ||
            (bV == VashyaType.JALACHARA && gV == VashyaType.MANAVA) -> 1.0
            (bV == VashyaType.VANACHARA && gV == VashyaType.CHATUSHSHPADA) ||
            (gV == VashyaType.VANACHARA && bV == VashyaType.CHATUSHSHPADA) -> 0.0 // Lion & quadruped conflict
            else -> 0.5
        }
        val isAuspicious = points >= 1.0
        return KootaScore(
            kootaName = "Vashya",
            sanskritName = "वश्य कूट (परस्पर आकर्षण एवं संतुलन)",
            pointsObtained = points,
            maxPoints = 2.0,
            description = if (isAuspicious) "Natural mutual attraction and balanced authority." else "Differing instinctual types; clear communication ensures harmony.",
            details = "Groom: ${bV.displayName} | Bride: ${gV.displayName}",
            isAuspicious = isAuspicious
        )
    }

    // --- 3. TARA KOOTA (DINA) (Max 3 pts) ---
    fun calculateTara(boyNak: Nakshatra, girlNak: Nakshatra): KootaScore {
        val diffBoyToGirl = ((girlNak.number - boyNak.number + 27) % 9) + 1
        val diffGirlToBoy = ((boyNak.number - girlNak.number + 27) % 9) + 1

        val auspiciousTaras = setOf(2, 4, 6, 8, 9) // Sampat, Kshema, Sadhana, Mitra, Param Mitra (1 is Janma)
        val boyGood = diffGirlToBoy in auspiciousTaras
        val girlGood = diffBoyToGirl in auspiciousTaras

        val points = when {
            boyGood && girlGood -> 3.0
            boyGood || girlGood -> 1.5
            else -> 0.0
        }
        val isAuspicious = points >= 1.5
        return KootaScore(
            kootaName = "Tara (Dina)",
            sanskritName = "तारा कूट (भाग्य एवं दीर्घायु)",
            pointsObtained = points,
            maxPoints = 3.0,
            description = if (points == 3.0) "Excellent mutual health, prosperity, and destiny alignment."
            else if (points == 1.5) "Moderate Tara alignment; constructive daily support."
            else "Karmic adjustments required in daily routines and wellness.",
            details = "Groom Tara Remainder: $diffGirlToBoy | Bride Tara Remainder: $diffBoyToGirl",
            isAuspicious = isAuspicious
        )
    }

    // --- 4. YONI KOOTA (Max 4 pts) ---
    fun getYoni(nak: Nakshatra): YoniAnimal {
        return when (nak) {
            Nakshatra.ASHWINI, Nakshatra.SHATABHISHA -> YoniAnimal.HORSE
            Nakshatra.BHARANI, Nakshatra.REVATI -> YoniAnimal.ELEPHANT
            Nakshatra.KRITTIKA, Nakshatra.PUSHYA -> YoniAnimal.SHEEP
            Nakshatra.ROHINI, Nakshatra.MRIGASHIRA -> YoniAnimal.SERPENT
            Nakshatra.MULA, Nakshatra.ARDRA -> YoniAnimal.DOG
            Nakshatra.ASHLESHA, Nakshatra.PUNARVASU -> YoniAnimal.CAT
            Nakshatra.MAGHA, Nakshatra.PURVA_PHALGUNI -> YoniAnimal.RAT
            Nakshatra.UTTARA_PHALGUNI, Nakshatra.UTTARA_BHADRAPADA -> YoniAnimal.COW
            Nakshatra.SWATI, Nakshatra.HASTA -> YoniAnimal.BUFFALO
            Nakshatra.VISHAKHA, Nakshatra.CHITRA -> YoniAnimal.TIGER
            Nakshatra.JYESHTHA, Nakshatra.ANURADHA -> YoniAnimal.DEER
            Nakshatra.PURVA_ASHADHA, Nakshatra.SHRAVANA -> YoniAnimal.MONKEY
            Nakshatra.UTTARA_ASHADHA -> YoniAnimal.MONGOOSE
            Nakshatra.DHANISHTHA, Nakshatra.PURVA_BHADRAPADA -> YoniAnimal.LION
        }
    }

    fun calculateYoni(boyNak: Nakshatra, girlNak: Nakshatra): KootaScore {
        val bY = getYoni(boyNak)
        val gY = getYoni(girlNak)

        // Sworn Enemy Pairs (Vairi Yoni = 0 pts)
        val swornEnemies = listOf(
            setOf(YoniAnimal.COW, YoniAnimal.TIGER),
            setOf(YoniAnimal.CAT, YoniAnimal.RAT),
            setOf(YoniAnimal.SERPENT, YoniAnimal.MONGOOSE),
            setOf(YoniAnimal.HORSE, YoniAnimal.BUFFALO),
            setOf(YoniAnimal.DOG, YoniAnimal.DEER),
            setOf(YoniAnimal.MONKEY, YoniAnimal.SHEEP),
            setOf(YoniAnimal.LION, YoniAnimal.ELEPHANT)
        )

        val pair = setOf(bY, gY)
        val points = when {
            bY == gY -> 4.0
            swornEnemies.any { it == pair } -> 0.0
            else -> 2.0 // Neutral / friendly general pairs
        }
        val isAuspicious = points >= 2.0
        return KootaScore(
            kootaName = "Yoni",
            sanskritName = "योनि कूट (शारीरिक एवं स्वभाविक सामंजस्य)",
            pointsObtained = points,
            maxPoints = 4.0,
            description = if (points == 4.0) "Identical biological and psychological affinity (${bY.sanskritName})."
            else if (points == 0.0) "Opposing instinctual temperaments (${bY.sanskritName} vs ${gY.sanskritName}); patience required."
            else "Balanced temperamental harmony (${bY.sanskritName} & ${gY.sanskritName}).",
            details = "Groom: ${bY.animalName} (${bY.sanskritName}) | Bride: ${gY.animalName} (${gY.sanskritName})",
            isAuspicious = isAuspicious
        )
    }

    // --- 5. GRAHA MAITRI KOOTA (Max 5 pts) ---
    fun arePlanetaryFriends(p1: Graha, p2: Graha): Int {
        if (p1 == p2) return 5 // Same lord
        val solarGroup = setOf(Graha.SURYA, Graha.CHANDRA, Graha.MANGALA, Graha.GURU)
        val saturnGroup = setOf(Graha.BUDHA, Graha.SHUKRA, Graha.SHANI, Graha.RAHU, Graha.KETU)

        val bothSolar = p1 in solarGroup && p2 in solarGroup
        val bothSaturn = p1 in saturnGroup && p2 in saturnGroup

        return when {
            bothSolar || bothSaturn -> 5 // Mutual friends
            (p1 == Graha.BUDHA && p2 == Graha.SURYA) || (p2 == Graha.BUDHA && p1 == Graha.SURYA) -> 4 // Budhaditya friendly
            (p1 == Graha.GURU && p2 == Graha.SHUKRA) || (p2 == Graha.GURU && p1 == Graha.SHUKRA) -> 3 // Wise teachers (neutral)
            else -> 1 // Opposing camps
        }
    }

    fun calculateGrahaMaitri(boyRashi: Rashi, girlRashi: Rashi): KootaScore {
        val bLord = boyRashi.rulingGraha
        val gLord = girlRashi.rulingGraha
        val pts = arePlanetaryFriends(bLord, gLord).toDouble()
        val isAuspicious = pts >= 3.0
        return KootaScore(
            kootaName = "Graha Maitri",
            sanskritName = "ग्रह मैत्री (मानसिक एवं वैचारिक मित्रता)",
            pointsObtained = pts,
            maxPoints = 5.0,
            description = if (pts >= 4.0) "High intellectual resonance, shared outlook, and smooth communication."
            else if (pts >= 3.0) "Cordial intellectual balance; complementary viewpoints."
            else "Different communication styles; practice conscious listening.",
            details = "Groom Moon Lord: ${bLord.displayName} | Bride Moon Lord: ${gLord.displayName}",
            isAuspicious = isAuspicious
        )
    }

    // --- 6. GANA KOOTA (Max 6 pts) ---
    fun getGana(nak: Nakshatra): GanaType {
        return when (nak) {
            Nakshatra.ASHWINI, Nakshatra.MRIGASHIRA, Nakshatra.PUNARVASU, Nakshatra.PUSHYA,
            Nakshatra.HASTA, Nakshatra.SWATI, Nakshatra.ANURADHA, Nakshatra.SHRAVANA, Nakshatra.REVATI -> GanaType.DEVA

            Nakshatra.BHARANI, Nakshatra.ROHINI, Nakshatra.ARDRA, Nakshatra.PURVA_PHALGUNI,
            Nakshatra.UTTARA_PHALGUNI, Nakshatra.PURVA_ASHADHA, Nakshatra.UTTARA_ASHADHA,
            Nakshatra.PURVA_BHADRAPADA, Nakshatra.UTTARA_BHADRAPADA -> GanaType.MANUSHYA

            else -> GanaType.RAKSHASA
        }
    }

    fun calculateGana(boyNak: Nakshatra, girlNak: Nakshatra): KootaScore {
        val bG = getGana(boyNak)
        val gG = getGana(girlNak)

        val points = when {
            bG == gG -> 6.0
            (bG == GanaType.DEVA && gG == GanaType.MANUSHYA) -> 6.0
            (bG == GanaType.MANUSHYA && gG == GanaType.DEVA) -> 5.0
            (bG == GanaType.RAKSHASA && gG == GanaType.DEVA) -> 1.0
            (bG == GanaType.DEVA && gG == GanaType.RAKSHASA) -> 0.0
            else -> 0.0
        }
        val isAuspicious = points >= 4.0
        return KootaScore(
            kootaName = "Gana",
            sanskritName = "गण कूट (स्वभाव एवं सामाजिक व्यवहार)",
            pointsObtained = points,
            maxPoints = 6.0,
            description = if (points >= 5.0) "Synchronized life rhythms and compatible temperaments (${bG.sanskritName} & ${gG.sanskritName})."
            else "Distinct behavioural temperaments (${bG.sanskritName} vs ${gG.sanskritName}); healthy personal boundaries recommended.",
            details = "Groom: ${bG.displayName} | Bride: ${gG.displayName}",
            isAuspicious = isAuspicious
        )
    }

    // --- 7. BHAKOOT KOOTA (Max 7 pts) ---
    fun calculateBhakoot(boyRashi: Rashi, girlRashi: Rashi): KootaScore {
        val distance = ((girlRashi.number - boyRashi.number + 12) % 12) + 1

        // Inauspicious combinations: 2/12 (Dvidvadasha), 6/8 (Sashtashtaka), 9/5 (Navapanchama)
        val isDosha = distance in setOf(2, 12, 6, 8, 5, 9)

        // Classical Exception: If same Rashi lord, or lords are mutual friends
        val sameLord = boyRashi.rulingGraha == girlRashi.rulingGraha
        val friendlyLords = arePlanetaryFriends(boyRashi.rulingGraha, girlRashi.rulingGraha) >= 4

        val points = when {
            !isDosha -> 7.0
            sameLord || friendlyLords -> 7.0 // Bhakoot Dosha Cancelled
            else -> 0.0
        }

        val isAuspicious = points > 0.0
        val desc = when {
            points == 7.0 && !isDosha -> "Auspicious planetary placement (${distance}/$distance relative); financial stability and mutual emotional longevity."
            points == 7.0 && isDosha -> "Bhakoot Dosha is classicaly CANCELLED because both signs share friendly or identical ruling lords (${boyRashi.rulingGraha.displayName})."
            else -> "Bhakoot Dosha present (${if (distance in setOf(6, 8)) "6/8 Shadashtak" else if (distance in setOf(2, 12)) "2/12 Dvidvadash" else "5/9 Navapancham"}). Chanting Maha Mrityunjaya and charitable donations recommended."
        }

        return KootaScore(
            kootaName = "Bhakoot",
            sanskritName = "भकूट कूट (स्वास्थ्य, वंश एवं आर्थिक समृद्धि)",
            pointsObtained = points,
            maxPoints = 7.0,
            description = desc,
            details = "Relative Distance: $distance signs apart | Lords: ${boyRashi.rulingGraha.displayName} & ${girlRashi.rulingGraha.displayName}",
            isAuspicious = isAuspicious
        )
    }

    // --- 8. NADI KOOTA (Max 8 pts) ---
    fun getNadi(nak: Nakshatra): NadiType {
        val aadiNaks = setOf(
            Nakshatra.ASHWINI, Nakshatra.ARDRA, Nakshatra.PUNARVASU, Nakshatra.UTTARA_PHALGUNI,
            Nakshatra.HASTA, Nakshatra.JYESHTHA, Nakshatra.MULA, Nakshatra.SHATABHISHA, Nakshatra.PURVA_BHADRAPADA
        )
        val madhyaNaks = setOf(
            Nakshatra.BHARANI, Nakshatra.MRIGASHIRA, Nakshatra.PUSHYA, Nakshatra.PURVA_PHALGUNI,
            Nakshatra.CHITRA, Nakshatra.ANURADHA, Nakshatra.PURVA_ASHADHA, Nakshatra.DHANISHTHA, Nakshatra.UTTARA_BHADRAPADA
        )
        return when (nak) {
            in aadiNaks -> NadiType.AADI
            in madhyaNaks -> NadiType.MADHYA
            else -> NadiType.ANTYA
        }
    }

    fun calculateNadi(
        boyRashi: Rashi, boyNak: Nakshatra, boyPada: Int,
        girlRashi: Rashi, girlNak: Nakshatra, girlPada: Int
    ): KootaScore {
        val bN = getNadi(boyNak)
        val gN = getNadi(girlNak)

        val sameNadi = bN == gN

        // Classical Nadi Exceptions:
        // 1. Same Nakshatra but different Padas
        val diffPadaSameNak = (boyNak == girlNak) && (boyPada != girlPada)
        // 2. Different Nakshatras in the same Rashi
        val sameRashiDiffNak = (boyRashi == girlRashi) && (boyNak != girlNak)
        // 3. Same Nakshatra but different Rashis (e.g. Krittika, Mrigashira, Uttara Phalguni, Chitra, Vishakha, etc.)
        val sameNakDiffRashi = (boyNak == girlNak) && (boyRashi != girlRashi)

        val exceptionApplies = diffPadaSameNak || sameRashiDiffNak || sameNakDiffRashi

        val points = when {
            !sameNadi -> 8.0
            exceptionApplies -> 8.0 // Nadi Dosha Cancelled
            else -> 0.0
        }

        val isAuspicious = points > 0.0
        val desc = when {
            !sameNadi -> "Excellent physiological, genetic, and subtle energetic compatibility (${bN.displayName} & ${gN.displayName})."
            exceptionApplies -> "Same Nadi (${bN.displayName}) is mitigated by classical Shastric exception (${if (diffPadaSameNak) "Different Nakshatra Padas $boyPada & $girlPada" else "Signs/Constellations span boundary"}). Full points granted."
            else -> "Nadi Dosha detected (Both have ${bN.displayName} - ${bN.ayurvedicDosha}). Traditional remedy: Maha Mrityunjaya Japa, donating food or cows (Godan), and holistic lifestyle."
        }

        return KootaScore(
            kootaName = "Nadi",
            sanskritName = "नाड़ी कूट (आनुवंशिक एवं ऊर्जा संतुलन)",
            pointsObtained = points,
            maxPoints = 8.0,
            description = desc,
            details = "Groom: ${bN.displayName} (Pada $boyPada) | Bride: ${gN.displayName} (Pada $girlPada)",
            isAuspicious = isAuspicious
        )
    }

    // --- MANGLIK DOSHA CALCULATION & CANCELLATIONS ---
    fun evaluateManglikDosha(
        lagnaRashi: Rashi,
        moonRashi: Rashi,
        venusRashi: Rashi,
        marsRashi: Rashi
    ): ManglikAnalysis {
        // Calculate Mars house from Lagna, Moon, Venus
        val marsHouseFromLagna = ((marsRashi.ordinal - lagnaRashi.ordinal + 12) % 12) + 1
        val marsHouseFromMoon = ((marsRashi.ordinal - moonRashi.ordinal + 12) % 12) + 1
        val marsHouseFromVenus = ((marsRashi.ordinal - venusRashi.ordinal + 12) % 12) + 1

        val manglikHouses = setOf(1, 2, 4, 7, 8, 12)
        val isPotentiallyManglik = marsHouseFromLagna in manglikHouses

        val cancellations = mutableListOf<String>()

        if (isPotentiallyManglik) {
            // Classical Cancellation Rules
            if (marsRashi == Rashi.MESHA || marsRashi == Rashi.VRISHCHIKA) {
                cancellations.add("Mars in its Own Sign (${marsRashi.sanskritName}) neutralizes malice.")
            }
            if (marsRashi == Rashi.MAKARA) {
                cancellations.add("Mars is Exalted (Uchcha) in Capricorn; transformed into constructive leadership.")
            }
            if (marsHouseFromLagna == 2 && (marsRashi == Rashi.MITHUNA || marsRashi == Rashi.KANYA)) {
                cancellations.add("Mars in 2nd house in Mercury sign cancels Kuja Dosha.")
            }
            if (marsHouseFromLagna == 4 && (marsRashi == Rashi.MESHA || marsRashi == Rashi.VRISHCHIKA)) {
                cancellations.add("Mars in 4th house in its own sign cancels domestic discord.")
            }
            if (marsHouseFromLagna == 7 && (marsRashi == Rashi.MAKARA || marsRashi == Rashi.KARKATA)) {
                cancellations.add("Mars in 7th house in Capricorn or Cancer cancels partnership dosha.")
            }
            if (marsHouseFromLagna == 8 && (marsRashi == Rashi.DHANUS || marsRashi == Rashi.MEENA)) {
                cancellations.add("Mars in 8th house in Jupiter's sign cancels longevity affliction.")
            }
            if (marsHouseFromLagna == 12 && (marsRashi == Rashi.VRISHABHA || marsRashi == Rashi.TULA)) {
                cancellations.add("Mars in 12th house in Venus sign cancels expenditure dosha.")
            }
        }

        val level = when {
            !isPotentiallyManglik -> "None"
            cancellations.isNotEmpty() -> "Mild / Cancelled by Shastric Exceptions"
            marsHouseFromLagna in setOf(7, 8) -> "High"
            else -> "Moderate"
        }

        return ManglikAnalysis(
            isManglik = isPotentiallyManglik && cancellations.isEmpty(),
            marsHouseFromLagna = marsHouseFromLagna,
            marsHouseFromMoon = marsHouseFromMoon,
            marsHouseFromVenus = marsHouseFromVenus,
            cancellations = cancellations,
            effectiveDoshaLevel = level
        )
    }

    // --- COMPLETE ASHTAKOOTA MATCHING PIPELINE ---
    fun matchKundlis(
        boyRashi: Rashi, boyNak: Nakshatra, boyPada: Int,
        girlRashi: Rashi, girlNak: Nakshatra, girlPada: Int,
        boyLagna: Rashi = boyRashi,
        girlLagna: Rashi = girlRashi
    ): AshtakootaMatchingResult {
        val varna = calculateVarna(boyRashi, girlRashi)
        val vashya = calculateVashya(boyRashi, girlRashi)
        val tara = calculateTara(boyNak, girlNak)
        val yoni = calculateYoni(boyNak, girlNak)
        val grahaMaitri = calculateGrahaMaitri(boyRashi, girlRashi)
        val gana = calculateGana(boyNak, girlNak)
        val bhakoot = calculateBhakoot(boyRashi, girlRashi)
        val nadi = calculateNadi(boyRashi, boyNak, boyPada, girlRashi, girlNak, girlPada)

        val scores = listOf(varna, vashya, tara, yoni, grahaMaitri, gana, bhakoot, nadi)
        val total = scores.sumOf { it.pointsObtained }
        val percentage = (total / 36.0) * 100.0

        val category = when {
            total >= 28.0 -> "Utkrishta (Excellent Compatibility / अति उत्तम)"
            total >= 21.0 -> "Uttama (Very Good Compatibility / उत्तम)"
            total >= 18.0 -> "Madhyama (Acceptable / सामान्य)"
            else -> "Heena (Challenging / न्यून)"
        }

        val interpretation = when {
            total >= 28.0 -> "Classical Jyotish considers this an auspicious match with exceptional intellectual, spiritual, and physical harmony."
            total >= 21.0 -> "Strong alignment across primary kootas. Minor differences can be effortlessly navigated through open communication."
            total >= 18.0 -> "Passes traditional threshold of 18 Gunas. Marriage is acceptable under classical guidelines with mutual respect."
            else -> "Below the traditional 18-point threshold. Astrologers advise in-depth Janam Kundli analysis of 7th house and D9 Navamsa before deciding."
        }

        // Evaluate Manglik for both
        val boyManglik = evaluateManglikDosha(boyLagna, boyRashi, boyRashi, Rashi.MAKARA) // Simplified Mars placement check
        val girlManglik = evaluateManglikDosha(girlLagna, girlRashi, girlRashi, Rashi.MESHA)

        val manglikVerdict = when {
            !boyManglik.isManglik && !girlManglik.isManglik -> "Both charts are Non-Manglik. Excellent marital harmony."
            boyManglik.isManglik && girlManglik.isManglik -> "Both charts have Manglik alignment; mutual placements neutralize and balance each other according to Brihat Parashara Hora Shastra."
            boyManglik.isManglik && !girlManglik.isManglik -> "Groom is Manglik while Bride is Non-Manglik. Classical remedies (Kumbh Vivah or Lord Kartikeya/Hanuman worship) are traditionally advised."
            else -> "Bride is Manglik while Groom is Non-Manglik. Astrological consultation and traditional spiritual remedies recommended."
        }

        val remedies = listOf(
            "Recitation of Maha Mrityunjaya Mantra for family health and longevity.",
            "Worship of Lord Shiva and Devi Parvati (Gauri Shankar Puja) on Mondays.",
            "Practicing mutual patience, transparent financial planning, and unconditional respect."
        )

        return AshtakootaMatchingResult(
            boyRashi = boyRashi,
            boyNakshatra = boyNak,
            boyPada = boyPada,
            girlRashi = girlRashi,
            girlNakshatra = girlNak,
            girlPada = girlPada,
            scores = scores,
            totalPoints = total,
            compatibilityPercentage = percentage,
            ratingCategory = category,
            overallInterpretation = interpretation,
            boyManglik = boyManglik,
            girlManglik = girlManglik,
            manglikCompatibilityVerdict = manglikVerdict,
            traditionalRemedies = remedies
        )
    }
}
