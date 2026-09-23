package com.example.engines.chakra

import com.example.core.model.chakra.ChakraType
import com.example.core.model.chakra.MeditationMode

data class GuidedMeditationGuide(
    val chakraType: ChakraType,
    val title: String,
    val focusTheme: String,
    val visualizationPrompt: String,
    val steps: List<String>,
    val closingAffirmation: String
)

object MeditationRepository {

    val DURATION_PRESETS_MINUTES = listOf(5, 10, 15, 20, 30, 45, 60)

    val GUIDED_MEDITATIONS: Map<ChakraType, GuidedMeditationGuide> = mapOf(
        ChakraType.MULADHARA to GuidedMeditationGuide(
            chakraType = ChakraType.MULADHARA,
            title = "Prithvi Sthirata: Root Grounding Contemplation",
            focusTheme = "Foundational stability, earth connection, and unshakeable inner calm",
            visualizationPrompt = "Visualize a deep crimson four-petaled lotus at the base of the spine, rooted firmly into the golden heart of the Earth.",
            steps = listOf(
                "Sit comfortably with your spine erect and shoulders relaxed. Feel the physical points of contact between your body and the seat beneath you.",
                "Draw your awareness to the base of your spine, the perineum. Sense the unwavering solidity of the Earth supporting you without effort.",
                "As you inhale, imagine drawing grounding Prana upwards from the earth into the root. As you exhale, release any lingering restlessness or tension.",
                "Quietly chant or mentally vibrate the seed syllable 'LAM' (लं). Feel its deep resonant hum stabilizing your body and calming mental chatter.",
                "Rest in the steady knowledge that you are secure, present, and held by existence."
            ),
            closingAffirmation = "Om Prithvyai Namah. I am rooted, calm, and present in this moment."
        ),
        ChakraType.SVADHISTHANA to GuidedMeditationGuide(
            chakraType = ChakraType.SVADHISTHANA,
            title = "Jala Pravaha: Sacral Fluidity Contemplation",
            focusTheme = "Emotional fluidity, creative receptivity, and sensory ease",
            visualizationPrompt = "Envision a glowing vermilion-orange six-petaled lotus at the pelvic center, cradling a silvery crescent moon above quiet pristine waters.",
            steps = listOf(
                "Rest attention in the lower abdomen, below the navel. Breathe deeply into the pelvic bowl, feeling it expand with ease.",
                "Acknowledge the fluid nature of thoughts and emotions, watching them ripple across awareness like gentle waves without judgment.",
                "Inhale freshness and renewal. Exhale rigidity, letting go of resistance to life's organic rhythm.",
                "Intone the bija syllable 'VAM' (वं). Experience the soothing vibration washing through your lower back and sacrum.",
                "Sense a state of effortless flow, creative openness, and gentle emotional peace."
            ),
            closingAffirmation = "Om Jalabhimanini Devyai Namah. I welcome natural harmony and ease."
        ),
        ChakraType.MANIPURA to GuidedMeditationGuide(
            chakraType = ChakraType.MANIPURA,
            title = "Agni Tejas: Solar Radiance Contemplation",
            focusTheme = "Inner fortitude, metabolic clarity, and purposeful discernment",
            visualizationPrompt = "Visualize a bright ten-petaled golden lotus at the navel center, harboring a luminous downward-pointing solar triangle of pure golden fire.",
            steps = listOf(
                "Bring your focus to the solar plexus, just above the navel. Feel the natural warmth radiating from your digestive center.",
                "With every gentle breath, nurture this inner flame. It digests not only food, but also thoughts, experiences, and past burdens.",
                "Inhale confidence, clarity, and vital courage. Exhale self-doubt and lethargy.",
                "Vibrate the fiery seed syllable 'RAM' (रं). Feel the golden amber light radiating outward from your center through every cell.",
                "Abide in dignified inner strength, self-discipline, and radiant purpose."
            ),
            closingAffirmation = "Om Agnaye Namah. My inner flame burns clear, purposeful, and pure."
        ),
        ChakraType.ANAHATA to GuidedMeditationGuide(
            chakraType = ChakraType.ANAHATA,
            title = "Hridaya Karuna: Heart Compassion Contemplation",
            focusTheme = "Boundless loving-kindness, inner silence, and universal goodwill",
            visualizationPrompt = "Visualize an emerald green twelve-petaled lotus in the center of the chest, surrounding two interlaced triangles with an unquenchable golden flame of consciousness at its center.",
            steps = listOf(
                "Gently place your mental focus in the center of your chest, at the spiritual heart (Hridaya).",
                "Breathe into the chest cavity, allowing the heart space to soften, widen, and unburden itself.",
                "Cultivate a feeling of benevolent goodwill toward yourself, your loved ones, and all sentient beings across the cosmos.",
                "Chant or mentally reverberate the bija mantra 'YAM' (यं). Hear the unstruck sound (Anahata Dhwani) whispering in the background of stillness.",
                "Rest in compassionate awareness, where love is not a reaction, but the natural state of existence."
            ),
            closingAffirmation = "Om Shanti Shanti Shanti. May all beings everywhere be happy, peaceful, and free."
        ),
        ChakraType.VISHUDDHA to GuidedMeditationGuide(
            chakraType = ChakraType.VISHUDDHA,
            title = "Akasha Shuddhi: Throat Clarity Contemplation",
            focusTheme = "Truthful expression, spacious listening, and purified discernment",
            visualizationPrompt = "Visualize a serene sky-blue or lapis sixteen-petaled lotus at the throat pit, holding a pristine silver circle reflecting infinite empty space.",
            steps = listOf(
                "Bring awareness to the hollow of the throat and vocal center. Soften your jaw, tongue, and neck.",
                "Contemplate the element of Space (Akasha)—all-pervading, accommodating everything, yet untainted by what occurs within it.",
                "Allow your breathing to sound like distant ocean waves (Ujjayi), smooth and effortless.",
                "Resonate the seed mantra 'HAM' (हं) with an open throat. Feel the pure vibrational resonance clearing away unspoken tensions.",
                "Recognize your speech as a sacred instrument of truth (Satya) and compassionate upliftment."
            ),
            closingAffirmation = "Satyam Eva Jayate. May my words be gentle, true, and aligned with wisdom."
        ),
        ChakraType.AJNA to GuidedMeditationGuide(
            chakraType = ChakraType.AJNA,
            title = "Jnana Drishti: Third Eye Intuitive Contemplation",
            focusTheme = "Clarity of vision, intuitive discernment, and silent witnessing awareness",
            visualizationPrompt = "Visualize a deep indigo two-petaled lotus between the eyebrows, centering a lustrous silver lingam radiating the brilliant clarity of a full moon.",
            steps = listOf(
                "Gently rest your internal gaze at the midpoint between the eyebrows (Bhrumadhya).",
                "Do not strain the physical eyes. Simply let attention settle effortlessly into this silent command center.",
                "Step back from the flow of thoughts, observing them impartially as passing clouds in the boundless sky of awareness.",
                "Intone the sacred syllable 'OM' (ॐ). Feel the vibration rise from the throat, illumine the brow, and dissolve into the crown.",
                "Abide as the silent Witness (Sakshi), untangled from fleeting dualities."
            ),
            closingAffirmation = "Om Jyotiraham. I am the witness of the light of consciousness."
        ),
        ChakraType.SAHASRARA to GuidedMeditationGuide(
            chakraType = ChakraType.SAHASRARA,
            title = "Moksha Nirvana: Crown Transcendence Contemplation",
            focusTheme = "Universal oneness, non-dual absorption, and unconditioned peace",
            visualizationPrompt = "Visualize a luminous thousand-petaled lotus blossoming at the crown of the head, radiating brilliant violet-white light into the infinite cosmos.",
            steps = listOf(
                "Direct attention to the crown of your head (Brahmarandhra) and the subtle space just above it.",
                "Sense the dissolution of artificial boundaries between the inner observer and the outer universe.",
                "Allow all mental constructs, identities, and concepts of time to gently subside into primordial silence.",
                "Let the vibration of 'OM' dissolve into pure, ungraspable awareness (Nirguna Shanti).",
                "Rest completely in Sahaja Samadhi—natural, unconditioned, and timeless presence."
            ),
            closingAffirmation = "Aham Brahmasmi. Tat Tvam Asi. Pure Awareness is here and now."
        )
    )

    const val SPIRITUAL_DISCLAIMER =
        "Traditional Spiritual Practice Notice: The chakra system, bija mantras, and yogic energy centers (nadis) described here originate in classical spiritual texts such as the Shat-Chakra-Nirupana, Goraksha Paddhati, and Yoga Upanishads. They are intended solely for personal contemplation, meditation, and spiritual inquiry. They are not scientifically proven medical anatomies, nor are they intended to diagnose, treat, or cure any physical, mental, or medical condition."
}
