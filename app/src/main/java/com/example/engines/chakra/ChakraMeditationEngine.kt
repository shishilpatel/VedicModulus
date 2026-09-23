package com.example.engines.chakra

import com.example.core.model.BirthProfile
import com.example.core.model.PersonalizedProfile
import com.example.core.model.Rashi
import com.example.core.model.chakra.ChakraTradition
import com.example.core.model.chakra.ChakraType
import com.example.core.model.chakra.MeditationMode
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PersonalizedChakraSuggestion(
    val recommendedChakra: ChakraType,
    val astrologicalReason: String,
    val traditionalPracticeNote: String
)

data class SevenChakraJourneyState(
    val isActive: Boolean = false,
    val currentChakraIndex: Int = 0,
    val secondsInCurrentChakra: Int = 0,
    val secondsPerChakra: Int = 120, // default 2 minutes per chakra
    val totalProgressFraction: Float = 0f
) {
    val currentChakra: ChakraType
        get() = ChakraType.values().getOrElse(currentChakraIndex) { ChakraType.MULADHARA }
}

class ChakraMeditationEngine {

    private val _selectedTradition = MutableStateFlow(ChakraTradition.SATYANANDA_BIHAR)
    val selectedTradition: StateFlow<ChakraTradition> = _selectedTradition.asStateFlow()

    private val _journeyState = MutableStateFlow(SevenChakraJourneyState())
    val journeyState: StateFlow<SevenChakraJourneyState> = _journeyState.asStateFlow()

    private var journeyJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun setTradition(tradition: ChakraTradition) {
        _selectedTradition.value = tradition
    }

    /**
     * Recommends a traditional chakra contemplative focus based on the user's Jyotish
     * Lagna (Ascendant), Moon Rashi, or Numerology, accompanied by strict classical disclaimers.
     */
    fun getPersonalizedSuggestion(
        profile: BirthProfile?,
        personalizedProfile: PersonalizedProfile?
    ): PersonalizedChakraSuggestion {
        val moonRashi = personalizedProfile?.natalChandraRashi
        val lagnaRashi = personalizedProfile?.natalLagnaRashi

        val (chakra, reason) = when (moonRashi) {
            Rashi.MESHA, Rashi.SIMHA, Rashi.DHANUS -> Pair(
                ChakraType.MANIPURA,
                "Your Moon is in a Fiery Agni Rashi (${moonRashi.sanskritName} / ${moonRashi.westernName}). Classical texts recommend solar plexus contemplation (Manipura) to channel vitality into calm, focused purpose."
            )
            Rashi.VRISHABHA, Rashi.KANYA, Rashi.MAKARA -> Pair(
                ChakraType.MULADHARA,
                "Your Moon is in an Earth Prithvi Rashi (${moonRashi.sanskritName} / ${moonRashi.westernName}). Traditional grounding contemplation at the Muladhara center strengthens bodily stillness and unwavering patience."
            )
            Rashi.MITHUNA, Rashi.TULA, Rashi.KUMBHA -> Pair(
                ChakraType.ANAHATA,
                "Your Moon is in an Airy Vayu Rashi (${moonRashi.sanskritName} / ${moonRashi.westernName}). Heart center contemplation (Anahata) balances rapid mental mobility with steady emotional serenity."
            )
            Rashi.KARKATA, Rashi.VRISHCHIKA, Rashi.MEENA -> Pair(
                ChakraType.SVADHISTHANA,
                "Your Moon is in a Water Jala Rashi (${moonRashi.sanskritName} / ${moonRashi.westernName}). Sacral center contemplation (Svadhisthana) harmonizes deep lunar emotional tides into creative tranquility."
            )
            else -> Pair(
                ChakraType.ANAHATA,
                "Universal Heart Center contemplation (Anahata) is recommended as a balanced starting point for all seekers in the classical tradition."
            )
        }

        return PersonalizedChakraSuggestion(
            recommendedChakra = chakra,
            astrologicalReason = reason,
            traditionalPracticeNote = "Notice: Astrological correspondences are traditional symbolic frameworks from classical texts. They are personal contemplation aids, not medical diagnoses or claims of blocked physical energy."
        )
    }

    fun startSevenChakraJourney(
        totalDurationMinutes: Int = 14,
        onChakraTransition: ((ChakraType) -> Unit)? = null,
        onJourneyComplete: (() -> Unit)? = null
    ) {
        journeyJob?.cancel()
        val chakras = ChakraType.values()
        val secondsPerChakra = (totalDurationMinutes * 60) / chakras.size

        _journeyState.value = SevenChakraJourneyState(
            isActive = true,
            currentChakraIndex = 0,
            secondsInCurrentChakra = 0,
            secondsPerChakra = secondsPerChakra,
            totalProgressFraction = 0f
        )
        onChakraTransition?.invoke(chakras[0])

        journeyJob = scope.launch {
            val totalSeconds = totalDurationMinutes * 60
            var elapsedTotal = 0

            while (isActive && _journeyState.value.isActive) {
                delay(1000L)
                elapsedTotal++
                val cur = _journeyState.value
                val newSecInChakra = cur.secondsInCurrentChakra + 1

                if (newSecInChakra >= cur.secondsPerChakra) {
                    val nextIdx = cur.currentChakraIndex + 1
                    if (nextIdx < chakras.size) {
                        _journeyState.value = cur.copy(
                            currentChakraIndex = nextIdx,
                            secondsInCurrentChakra = 0,
                            totalProgressFraction = elapsedTotal.toFloat() / totalSeconds.toFloat()
                        )
                        onChakraTransition?.invoke(chakras[nextIdx])
                    } else {
                        // Journey finished
                        _journeyState.value = cur.copy(
                            isActive = false,
                            totalProgressFraction = 1f
                        )
                        onJourneyComplete?.invoke()
                        break
                    }
                } else {
                    _journeyState.value = cur.copy(
                        secondsInCurrentChakra = newSecInChakra,
                        totalProgressFraction = elapsedTotal.toFloat() / totalSeconds.toFloat()
                    )
                }
            }
        }
    }

    fun stopSevenChakraJourney() {
        journeyJob?.cancel()
        _journeyState.value = SevenChakraJourneyState(isActive = false)
    }

    fun release() {
        journeyJob?.cancel()
        scope.cancel()
    }
}
