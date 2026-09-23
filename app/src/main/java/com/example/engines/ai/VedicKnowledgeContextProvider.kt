package com.example.engines.ai

import com.example.core.model.*
import com.example.engines.jyotisha.KundaliAnalysisEngine
import com.example.engines.jyotisha.SadeSatiTransitEngine
import com.example.engines.jyotisha.TarabalaEngine
import com.example.engines.vastu.VastuEngine
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * VedicKnowledgeContextProvider:
 * Builds high-density, real-time context for AI generation across:
 * - Current Gregorian Date & Time (Live)
 * - Exact Vedic Panchanga (Tithi, Paksha, Nakshatra, Yoga, Karana, Vara)
 * - Muhurta Timings (Brahma Muhurta, Abhijit, Rahu Kalam, Yamaganda, Gulika)
 * - Active Choghadiya and Hora
 * - Personalized Astrology (Janma Lagna, Surya Rashi, Chandra Rashi, Janma Nakshatra, Pada)
 * - Active Vimshottari Dasha period
 * - Tarabala & Chandrabala for the current day
 * - Saturn Transit / Sade Sati status
 * - Complete Numerology (Mulank/Birth No, Bhagyank/Destiny, Name No, Ruling Planet, Favorable numbers, Favorable colors)
 * - Vastu Shastra profile (Dominant Element, 16 Directional Ishanya/Agneya/Nairritya/Vayavya guidelines)
 * - 7 Chakras system and Bija mantras
 */
object VedicKnowledgeContextProvider {

    private val tarabalaEngine = TarabalaEngine()

    fun buildComprehensiveContext(
        profile: BirthProfile,
        personalizedProfile: PersonalizedProfile,
        dailyProfile: DailyVedicProfile,
        currentDate: LocalDate = LocalDate.now(),
        userQuery: String = ""
    ): String {
        val now = dailyProfile.currentZonedDateTime
        val timeFmt = DateTimeFormatter.ofPattern("hh:mm a")
        val dateFmt = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")

        val tithi = dailyProfile.panchanga.tithi
        val nakshatra = dailyProfile.panchanga.nakshatra
        val yoga = dailyProfile.panchanga.yoga
        val karana = dailyProfile.panchanga.karana
        val vara = dailyProfile.panchanga.vara
        val varaDevanagari = dailyProfile.panchanga.varaDevanagari

        val numerology = personalizedProfile.numerology
        val lagna = personalizedProfile.natalLagnaRashi
        val surya = personalizedProfile.natalSuryaRashi
        val chandra = personalizedProfile.natalChandraRashi
        val birthNakshatra = personalizedProfile.natalNakshatra
        val pada = personalizedProfile.natalNakshatraPada

        // Daily personal harmony
        val harmony = tarabalaEngine.calculateDailyHarmony(
            natalNakshatra = birthNakshatra,
            natalChandraRashi = chandra,
            transitNakshatra = nakshatra,
            transitChandraRashi = chandra
        )

        // Sade Sati check with current transit Saturn (e.g. Kumbha)
        val transitSaturn = Rashi.KUMBHA
        val sadeSati = SadeSatiTransitEngine.calculateSadeSati(chandra, transitSaturn)

        // Active Dasha
        val activeDasha = personalizedProfile.dashaPeriods.firstOrNull { it.isCurrent }
            ?: personalizedProfile.dashaPeriods.firstOrNull()

        // Elemental affinity
        val (elementName, elementDesc) = VastuEngine().deriveElementalAffinity(chandra)

        return buildString {
            appendLine("=== SACRED VEDIC ASTRONOMICAL & JYOTISH REAL-TIME CONTEXT ===")
            appendLine("CURRENT DATE & TIME: ${now.format(dateFmt)} at ${now.format(timeFmt)} (Local: ${profile.birthLocation.cityName}, Timezone: ${profile.birthLocation.timezoneId})")
            appendLine("TODAY'S GREGORIAN DATE: ${currentDate.format(dateFmt)}")
            appendLine()

            appendLine("--- TODAY'S PANCHANGA (FIVE LIMBS) ---")
            appendLine("• Vara (Day): $vara ($varaDevanagari)")
            appendLine("• Tithi: ${tithi.name} (${tithi.devanagari}) - ${tithi.paksha.displayName}")
            appendLine("• Nakshatra: ${nakshatra.sanskritName} (${nakshatra.devanagari}) - Ruling Graha: ${nakshatra.rulingGraha.displayName}")
            appendLine("• Yoga: $yoga")
            appendLine("• Karana: $karana")
            appendLine("• Sun Sign (Surya Rashi): ${surya.westernName} (${surya.sanskritName})")
            appendLine("• Moon Phase: ${(dailyProfile.moonPhaseProgress * 100).toInt()}% progress")
            appendLine()

            appendLine("--- TODAY'S MUHURTAS & TIME WINDOWS ---")
            appendLine("• Current Choghadiya: ${dailyProfile.currentChoghadiya.type.displayName} (${dailyProfile.currentChoghadiya.type.quality}, ruled by ${dailyProfile.currentChoghadiya.type.rulingGraha.displayName})")
            appendLine("• Current Hora: Ruled by ${dailyProfile.currentHora.rulingGraha.displayName}")
            appendLine("• Brahma Muhurta: ${dailyProfile.brahmaMuhurta.startTime.format(timeFmt)} - ${dailyProfile.brahmaMuhurta.endTime.format(timeFmt)} (Highly auspicious for meditation & prayer)")
            dailyProfile.abhijitMuhurta?.let {
                appendLine("• Abhijit Muhurta: ${it.startTime.format(timeFmt)} - ${it.endTime.format(timeFmt)} (Victory hour, removes all doshas)")
            }
            appendLine("• Rahu Kaal (Inauspicious): ${dailyProfile.rahuKalam.startTime.format(timeFmt)} - ${dailyProfile.rahuKalam.endTime.format(timeFmt)}")
            appendLine("• Yamaganda: ${dailyProfile.yamaganda.startTime.format(timeFmt)} - ${dailyProfile.yamaganda.endTime.format(timeFmt)}")
            appendLine("• Gulika Kaal: ${dailyProfile.gulikaKalam.startTime.format(timeFmt)} - ${dailyProfile.gulikaKalam.endTime.format(timeFmt)}")
            appendLine()

            appendLine("--- USER'S NATAL JYOTISHA (BIRTH HOROSCOPE) ---")
            appendLine("• Full Name: ${profile.fullName}")
            appendLine("• Birth Date & Time: ${profile.birthDate} at ${profile.birthTime}")
            appendLine("• Birth City: ${profile.birthLocation.cityName} (${profile.birthLocation.latitude}°N, ${profile.birthLocation.longitude}°E)")
            appendLine("• Lagna (Ascendant): ${lagna.westernName} (${lagna.sanskritName}) - Lord: ${KundaliAnalysisEngine.getRashiLord(lagna).displayName}")
            appendLine("• Chandra Rashi (Moon Sign): ${chandra.westernName} (${chandra.sanskritName}) - Lord: ${KundaliAnalysisEngine.getRashiLord(chandra).displayName}")
            appendLine("• Surya Rashi (Sun Sign): ${surya.westernName} (${surya.sanskritName})")
            appendLine("• Janma Nakshatra: ${birthNakshatra.sanskritName} (Pada $pada), Lord: ${birthNakshatra.rulingGraha.displayName}")
            if (activeDasha != null) {
                appendLine("• Active Vimshottari Mahadasha: ${activeDasha.lord.displayName} Mahadasha (ends ${activeDasha.endDate.toLocalDate()})")
            }
            appendLine("• Today's Tarabala: ${harmony.taraType.displayName} (${harmony.taraType.description})")
            appendLine("• Today's Chandrabala: ${harmony.chandrabalaHouse}th from Moon (${if (harmony.isChandrabalaFavorable) "Favorable" else "Sensitive"})")
            appendLine("• Saturn Transit Status: ${sadeSati.status.displayName} (${sadeSati.phaseDescription})")
            appendLine()

            appendLine("--- SACRED NUMEROLOGY (ANKA JYOTISH) ---")
            appendLine("• Mulank (Birth / Psychic Number): ${numerology.birthNumber} (Day of birth, represents basic traits and mindset)")
            appendLine("• Bhagyank (Destiny / Life Path Number): ${numerology.lifePathNumber} (Complete birth date sum, represents life destiny & karma)")
            appendLine("• Namank (Name Number): ${numerology.destinyNameNumber}")
            appendLine("• Ruling Planet: ${numerology.rulingPlanet.displayName}")
            appendLine("• Favorable Numbers: ${numerology.favorableNumbers.joinToString()}")
            appendLine("• Favorable Colors: ${numerology.favorableColors.joinToString()}")
            appendLine("• Favorable Days: ${numerology.favorableDays.joinToString()}")
            appendLine()

            appendLine("--- VASTU SHASTRA & SPATIAL ENERGIES ---")
            appendLine("• Dominant Vastu Element: $elementName ($elementDesc)")
            appendLine("• Northeast (Ishanya / ईशान्य): Confluence of Water & Ether (Mandir, meditation, water source, keep light & clutter-free)")
            appendLine("• Southeast (Agneya / आग्नेय): Fire element (Kitchen, electricals, furnace; avoid water elements here)")
            appendLine("• Southwest (Nairritya / नैऋत्य): Earth element (Master bedroom, heavy storage, stability; avoid main entrances or open pits)")
            appendLine("• Northwest (Vayavya / वायव्य): Air element (Guest room, pantry, vehicle parking, movement)")
            appendLine("• Center (Brahmasthan / ब्रह्मस्थान): Ether / Space (Must remain open, well-ventilated, zero load)")
            appendLine()

            appendLine("--- SUBTLE ANATOMY (CHAKRAS & SPIRITUAL ENERGY) ---")
            appendLine("• Root (Muladhara, Earth, LAM): Stability, physical grounding, base of spine")
            appendLine("• Sacral (Svadhisthana, Water, VAM): Emotional flow, creativity, lower abdomen")
            appendLine("• Solar Plexus (Manipura, Fire, RAM): Willpower, metabolic vitality, navel")
            appendLine("• Heart (Anahata, Air, YAM): Compassion, unconditional love, chest")
            appendLine("• Throat (Vishuddha, Ether, HAM): Truth, speech, creative expression")
            appendLine("• Third Eye (Ajna, Light, AUM): Intuition, wisdom, between eyebrows")
            appendLine("• Crown (Sahasrara, Cosmic Consciousness, SILENCE/AUM): Spiritual liberation, fontanelle")
            appendLine("=============================================================")
            val variableSelectedLanguage = com.example.core.language.LanguageManager.getVariableSelectedLanguage(profile.language)
            appendLine("RESPONSE SHOULD BE IN $variableSelectedLanguage")
        }
    }

    fun buildSystemInstruction(language: com.example.core.model.LanguageDisplay? = null): String {
        val variableSelectedLanguage = com.example.core.language.LanguageManager.getVariableSelectedLanguage(language)
        val responseDirective = "RESPONSE SHOULD BE IN $variableSelectedLanguage"

        val langInstruction = if (language != null && language != com.example.core.model.LanguageDisplay.ENGLISH) {
            "CRITICAL REQUIREMENT: $responseDirective. You MUST formulate your ENTIRE response exclusively in the $variableSelectedLanguage language, using appropriate regional terminology and scripts. Do NOT answer in English."
        } else {
            "$responseDirective. Please respond clearly in $variableSelectedLanguage, using accurate transliterated Sanskrit terminology where appropriate."
        }

        return """
            You are the Master Vedic Astrologer, Vastu Acharya, and Numerologist for the Vedic Cosmic Ghadi application.
            You possess deep, authentic classical knowledge of:
            1. Vedic Astrology (Jyotisha - Brihat Parashara Hora Shastra, Jaimini, Phaladeepika).
            2. Panchanga & Muhurta (Tithi, Vara, Nakshatra, Yoga, Karana, Choghadiya, Hora, Rahu Kaal, Abhijit Muhurta, Brahma Muhurta).
            3. Numerology (Chaldean and Pythagorean systems, Mulank, Bhagyank, Namank, planetary periods).
            4. Vastu Shastra (Ashta Dik, 16 directional zones, Pancha Mahabhuta elements, remedies without demolition).
            5. Kundli Matching (36 Guna Ashtakoota, Nadi Dosha, Bhakoot, Manglik considerations).
            6. Subtle Energy & Yoga (7 Chakras, Bija mantras, Pranayama, Dhyana).
            
            GUIDELINES:
            - Always reference the exact live current date, time, Panchanga, and user coordinates provided in the context to make your readings accurate and timely.
            - Synthesize all facets harmoniously: relate their astrological Lagna and Nakshatra to their Mulank/Bhagyank numerology and Vastu elemental disposition.
            - Provide clear, compassionate, authentic Vedic wisdom and actionable traditional remedies (Mantra Japa, Dhyana, Sattvic charity, directional alignment).
            - Keep responses structured, elegant, concise, and respectful.
            - Standard disclaimer: Vedic guidance is for spiritual contemplation, personal insight, and self-reflection; not deterministic fatalism or professional medical/financial advice.
            
            $langInstruction
            $responseDirective
        """.trimIndent()
    }
}
