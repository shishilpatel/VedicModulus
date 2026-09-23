package com.example

import com.example.core.model.BirthProfile
import com.example.core.model.LocationData
import com.example.core.model.AyanamshaSystem
import com.example.core.model.CalendarTradition
import com.example.core.model.NumerologySystem
import com.example.core.model.LanguageDisplay
import com.example.core.model.WatchFaceStyle
import com.example.core.model.PersonalizedProfile
import com.example.core.model.Rashi
import com.example.core.model.Nakshatra
import com.example.core.model.NumerologyProfile
import com.example.core.model.Graha
import com.example.core.model.chakra.BreathingPattern
import com.example.core.model.chakra.ChakraTradition
import com.example.core.model.chakra.ChakraType
import com.example.engines.chakra.BreathingExerciseEngine
import com.example.engines.chakra.BreathingPhase
import com.example.engines.chakra.ChakraMeditationEngine
import com.example.engines.chakra.MantraRepository
import com.example.engines.chakra.MeditationRepository
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ChakraEngineTest {

    @Test
    fun testSevenChakrasOrderAndBijaMantras() {
        val chakras = ChakraType.values()
        assertEquals(7, chakras.size)

        assertEquals("LAM", ChakraType.MULADHARA.bijaTransliteration)
        assertEquals("VAM", ChakraType.SVADHISTHANA.bijaTransliteration)
        assertEquals("RAM", ChakraType.MANIPURA.bijaTransliteration)
        assertEquals("YAM", ChakraType.ANAHATA.bijaTransliteration)
        assertEquals("HAM", ChakraType.VISHUDDHA.bijaTransliteration)
        assertEquals("OM / AUM", ChakraType.AJNA.bijaTransliteration)
        assertEquals("OM / SILENCE", ChakraType.SAHASRARA.bijaTransliteration)
    }

    @Test
    fun testMantraRepositoryCompleteness() {
        val allMantras = MantraRepository.getAllMantras()
        assertTrue(allMantras.isNotEmpty())
        assertTrue(allMantras.any { it.id == "bija_lam" })
        assertTrue(allMantras.any { it.id == "vedic_gayatri" })
        assertTrue(allMantras.any { it.id == "vedic_mahamrityunjaya" })

        val muladharaMantra = MantraRepository.getMantraForChakra(ChakraType.MULADHARA)
        assertNotNull(muladharaMantra)
        assertEquals("लं", muladharaMantra?.sanskrit)
    }

    @Test
    fun testBreathingEnginePatternInitialization() {
        val engine = BreathingExerciseEngine()
        val initial = engine.state.value
        assertEquals(BreathingPattern.SAMA_VRITTI, initial.pattern)
        assertEquals(BreathingPhase.INHALE, initial.phase)
        assertEquals(4, initial.secondsRemainingInPhase)
        assertFalse(initial.isRunning)

        engine.setPattern(BreathingPattern.VISHAMA_VRITTI)
        val updated = engine.state.value
        assertEquals(BreathingPattern.VISHAMA_VRITTI, updated.pattern)
        assertEquals(4, updated.secondsRemainingInPhase)
    }

    @Test
    fun testPersonalizedAstrologicalSuggestion() {
        val engine = ChakraMeditationEngine()

        val dummyBirthProfile = BirthProfile(
            fullName = "Arjun Sharma",
            birthDate = LocalDate.of(1990, 5, 15),
            birthTime = LocalTime.of(10, 30),
            birthLocation = LocationData("India", "Delhi", "Central Delhi", "New Delhi", "New Delhi", 28.6139, 77.2090, "Asia/Kolkata"),
            ayanamsha = AyanamshaSystem.LAHIRI,
            calendarTradition = CalendarTradition.AMANTA,
            numerologySystem = NumerologySystem.CHALDEAN,
            language = LanguageDisplay.ENGLISH,
            preferredStyle = WatchFaceStyle.CHAKRA,
            hasSmartWatch = false
        )

        val personalizedProfile = PersonalizedProfile(
            userBirthProfile = dummyBirthProfile,
            natalLagnaRashi = Rashi.MESHA,
            natalSuryaRashi = Rashi.VRISHABHA,
            natalChandraRashi = Rashi.MESHA, // Fiery Agni sign
            natalNakshatra = Nakshatra.ASHWINI,
            natalNakshatraPada = 1,
            numerology = NumerologyProfile(6, 3, 1, listOf(6, 3, 9), listOf("Yellow", "Gold"), listOf("Thursday", "Friday"), Graha.SHUKRA),
            dominantVastuElement = "Fire",
            favorableColorsHex = listOf(0xFFFFB703)
        )

        val suggestion = engine.getPersonalizedSuggestion(dummyBirthProfile, personalizedProfile)
        assertEquals(ChakraType.MANIPURA, suggestion.recommendedChakra)
        assertTrue(suggestion.traditionalPracticeNote.contains("Notice"))
    }

    @Test
    fun testGuidedMeditationsExistForAllChakras() {
        ChakraType.values().forEach { chakra ->
            val guide = MeditationRepository.GUIDED_MEDITATIONS[chakra]
            assertNotNull("Guided meditation should exist for ${chakra.name}", guide)
            assertTrue(guide!!.steps.isNotEmpty())
            assertTrue(guide.closingAffirmation.isNotBlank())
        }
    }
}

