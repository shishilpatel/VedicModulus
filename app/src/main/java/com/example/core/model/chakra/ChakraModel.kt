package com.example.core.model.chakra

import androidx.compose.ui.graphics.Color

/**
 * Traditional 7-Chakra enumeration based on classical Hatha & Tantric literature
 * (e.g. Shat-Chakra-Nirupana and Goraksha Paddhati).
 *
 * NOTE: Chakras are presented strictly as traditional spiritual contemplation
 * frameworks and subtle energy anatomy, not as scientifically proven medical anatomy.
 */
enum class ChakraType(
    val id: String,
    val order: Int,
    val sanskritName: String,
    val englishName: String,
    val hindiName: String,
    val gujaratiName: String,
    val traditionalElement: String,
    val sanskritElement: String,
    val bijaMantra: String,
    val bijaTransliteration: String,
    val pronunciationGuide: String,
    val colorHex: Long,
    val petals: Int,
    val bodyLocation: String,
    val audioFileName: String,
    val recommendedAsanas: List<String>,
    val recommendedPranayama: String,
    val recommendedMudra: String,
    val jyotishPlanetaryAssociation: String,
    val educationalExplanation: String
) {
    MULADHARA(
        id = "muladhara",
        order = 1,
        sanskritName = "मूलाधार",
        englishName = "Root Center",
        hindiName = "मूलाधार चक्र",
        gujaratiName = "મૂલાધાર ચક્ર",
        traditionalElement = "Earth (Prithvi)",
        sanskritElement = "पृथ्वी",
        bijaMantra = "लं",
        bijaTransliteration = "LAM",
        pronunciationGuide = "Pronounce 'LUM' with deep nasal resonance on 'm', sounding from base.",
        colorHex = 0xFFE53935, // Deep Crimson Earth
        petals = 4,
        bodyLocation = "Perineum / Base of Spine",
        audioFileName = "audio/chakras/chakra_muladhara_lam.mp3",
        recommendedAsanas = listOf("Tadasana (Mountain Pose)", "Virabhadrasana I (Warrior)", "Vrikshasana (Tree Pose)", "Malasana (Garland Pose)"),
        recommendedPranayama = "Nadi Shodhana with deep pelvic grounding",
        recommendedMudra = "Prithvi Mudra (Earth Gesture)",
        jyotishPlanetaryAssociation = "Mars (Mangala) & Saturn (Shani)",
        educationalExplanation = "Muladhara translates to 'Root Support'. In classical yoga, it represents foundational stability, bodily awareness, and connection to the Earth element. Contemplation here cultivates rooted calmness and steady composure."
    ),

    SVADHISTHANA(
        id = "svadhisthana",
        order = 2,
        sanskritName = "स्वाधिष्ठान",
        englishName = "Sacral Center",
        hindiName = "स्वाधिष्ठान चक्र",
        gujaratiName = "સ્વાધિષ્ઠાન ચક્ર",
        traditionalElement = "Water (Jala / Apas)",
        sanskritElement = "जल",
        bijaMantra = "वं",
        bijaTransliteration = "VAM",
        pronunciationGuide = "Pronounce 'VUM' with gentle rounded lips and flowing resonance.",
        colorHex = 0xFFFB8C00, // Sacred Saffron / Orange
        petals = 6,
        bodyLocation = "Lower Abdomen / Sacrum",
        audioFileName = "audio/chakras/chakra_svadisthana_vam.mp3",
        recommendedAsanas = listOf("Baddha Konasana (Bound Angle)", "Bhujangasana (Cobra Pose)", "Setu Bandhasana (Bridge)", "Anjaneyasana (Low Lunge)"),
        recommendedPranayama = "Sheetali / Sheetkari or flowing Ujjayi",
        recommendedMudra = "Varun Mudra (Water Balance Gesture)",
        jyotishPlanetaryAssociation = "Venus (Shukra) & Moon (Chandra)",
        educationalExplanation = "Svadhisthana translates to 'One's Own Abode'. Associated traditionally with the Water element, it symbolizes emotional fluidity, creativity, sensory harmony, and the flow of vital fluids."
    ),

    MANIPURA(
        id = "manipura",
        order = 3,
        sanskritName = "मणिपूर",
        englishName = "Solar Plexus Center",
        hindiName = "मणिपुर चक्र",
        gujaratiName = "મણિપૂર ચક્ર",
        traditionalElement = "Fire (Agni)",
        sanskritElement = "अग्नि",
        bijaMantra = "रं",
        bijaTransliteration = "RAM",
        pronunciationGuide = "Pronounce 'RUM' with a rolled 'r' and warm solar resonance from the navel.",
        colorHex = 0xFFFDD835, // Radiant Golden Amber
        petals = 10,
        bodyLocation = "Navel / Upper Abdomen",
        audioFileName = "audio/chakras/chakra_manipura_ram.mp3",
        recommendedAsanas = listOf("Navasana (Boat Pose)", "Dhanurasana (Bow Pose)", "Ardha Matsyendrasana (Spinal Twist)", "Phalakasana (Plank)"),
        recommendedPranayama = "Kapalabhati & Bhastrika (Fire Breath)",
        recommendedMudra = "Surya Mudra (Sun / Heat Gesture)",
        jyotishPlanetaryAssociation = "Sun (Surya) & Mars (Mangala)",
        educationalExplanation = "Manipura translates to 'City of Jewels'. In Hatha Yoga, it is the seat of the digestive fire (Jatharagni) and the vital metabolic warmth, symbolizing determination, vitality, and digestive clarity."
    ),

    ANAHATA(
        id = "anahata",
        order = 4,
        sanskritName = "अनाहत",
        englishName = "Heart Center",
        hindiName = "अनाहत चक्र",
        gujaratiName = "અનાહત ચક્ર",
        traditionalElement = "Air (Vayu)",
        sanskritElement = "वायु",
        bijaMantra = "यं",
        bijaTransliteration = "YAM",
        pronunciationGuide = "Pronounce 'YUM' softly from the chest, expanding into the heart cave.",
        colorHex = 0xFF43A047, // Forest Green / Emerald
        petals = 12,
        bodyLocation = "Center of Chest (Hridaya)",
        audioFileName = "audio/chakras/chakra_anahata_yam.mp3",
        recommendedAsanas = listOf("Ustrasana (Camel Pose)", "Matsyasana (Fish Pose)", "Chakrasana (Wheel)", "Gomukhasana (Cow Face)"),
        recommendedPranayama = "Anulom Vilom with heart-centered exhalation",
        recommendedMudra = "Hridaya Mudra or Vayu Mudra",
        jyotishPlanetaryAssociation = "Mercury (Budha) & Moon (Chandra)",
        educationalExplanation = "Anahata means 'Unstruck Sound'—the transcendental sound heard in deep silence. Traditional scriptures honor it as the dwelling place of the Jivatman (individual soul), radiating compassion, universal love, and selfless devotion."
    ),

    VISHUDDHA(
        id = "vishuddha",
        order = 5,
        sanskritName = "विशुद्ध",
        englishName = "Throat Center",
        hindiName = "विशुद्ध चक्र",
        gujaratiName = "વિશુદ્ધ ચક્ર",
        traditionalElement = "Ether / Space (Akasha)",
        sanskritElement = "आकाश",
        bijaMantra = "हं",
        bijaTransliteration = "HAM",
        pronunciationGuide = "Pronounce 'HUM' with an open throat and resonant airy vibration.",
        colorHex = 0xFF00ACC1, // Lapis / Sky Azure
        petals = 16,
        bodyLocation = "Throat / Thyroid Area",
        audioFileName = "audio/chakras/chakra_vishuddha_ham.mp3",
        recommendedAsanas = listOf("Sarvangasana (Shoulderstand)", "Halasana (Plow Pose)", "Simhasana (Lion Pose)", "Ustrasana (Throat extension)"),
        recommendedPranayama = "Ujjayi Pranayama (Ocean Breath) & Jalandhara Bandha",
        recommendedMudra = "Shunya Mudra (Space / Ether Gesture)",
        jyotishPlanetaryAssociation = "Jupiter (Guru) & Mercury (Budha)",
        educationalExplanation = "Vishuddha translates to 'Especially Pure'. It governs truthful expression, speech (Vak Shakti), and receptivity. In yoga philosophy, it purifies dualistic thoughts into harmonious wisdom."
    ),

    AJNA(
        id = "ajna",
        order = 6,
        sanskritName = "आज्ञा",
        englishName = "Third Eye / Command Center",
        hindiName = "आज्ञा चक्र",
        gujaratiName = "આજ્ઞા ચક્ર",
        traditionalElement = "Mind / Inner Light (Manas / Jyoti)",
        sanskritElement = "मनस् / ज्योति",
        bijaMantra = "ॐ",
        bijaTransliteration = "OM / AUM",
        pronunciationGuide = "Chant 'A-U-M' progressing from throat to lips and resolving in inner silence.",
        colorHex = 0xFF3949AB, // Indigo / Midnight Blue
        petals = 2,
        bodyLocation = "Bhrumadhya (Space Between Eyebrows)",
        audioFileName = "audio/chakras/chakra_ajna_om.mp3",
        recommendedAsanas = listOf("Balasana (Child Pose with forehead down)", "Padmasana (Lotus)", "Trataka (Gazing Practice)", "Garudasana (Eagle)"),
        recommendedPranayama = "Bhramari Pranayama (Humming Bee Breath)",
        recommendedMudra = "Hakini Mudra or Chinmaya Mudra",
        jyotishPlanetaryAssociation = "Jupiter (Guru), Sun (Surya) & Moon (Chandra)",
        educationalExplanation = "Ajna translates to 'Command' or 'Perception'. The meeting point of the three primary nadis (Ida, Pingala, and Sushumna). Traditional meditation here dissolves mental dualities and awakens intuitive discernment (Viveka)."
    ),

    SAHASRARA(
        id = "sahasrara",
        order = 7,
        sanskritName = "सहस्रार",
        englishName = "Crown Center (Thousand Petaled)",
        hindiName = "सहस्रार चक्र",
        gujaratiName = "સહસ્રાર ચક્ર",
        traditionalElement = "Pure Consciousness (Chaitanya / Shiva-Shakti)",
        sanskritElement = "परमचेतना",
        bijaMantra = "ॐ",
        bijaTransliteration = "OM / SILENCE",
        pronunciationGuide = "Silent contemplation of the primordial vibration Omkara, merging into Nishpanna (pure silence).",
        colorHex = 0xFF8E24AA, // Radiant Violet / Pure Brilliant White
        petals = 1000,
        bodyLocation = "Brahmarandhra (Crown of Head)",
        audioFileName = "audio/chakras/chakra_sahasrara_om.mp3",
        recommendedAsanas = listOf("Shirshasana (Headstand)", "Savasana (Corpse Pose / Absorption)", "Padmasana (Lotus Dhyana)", "Yoga Mudrasana"),
        recommendedPranayama = "Kevala Kumbhaka (Spontaneous Breath Suspension)",
        recommendedMudra = "Dhyana Mudra or Anjali Mudra held above head",
        jyotishPlanetaryAssociation = "Ketu (Moksha Karaka) & Jupiter (Spiritual Realization)",
        educationalExplanation = "Sahasrara represents the thousand-petaled lotus at the crown. In Kashmir Shaivism and classical Tantra, it is the transcendental union of Shiva (Consciousness) and Shakti (Energy)—the realization of Oneness beyond form."
    );

    val composeColor: Color get() = Color(colorHex)
}

/**
 * Traditions & lineages for chakra contemplation.
 * Allows user to configure different scriptural interpretations.
 */
enum class ChakraTradition(
    val id: String,
    val title: String,
    val scriptureReference: String,
    val crownSoundPreference: String,
    val description: String
) {
    SATYANANDA_BIHAR(
        id = "satyananda",
        title = "Classical Tantra (Satyananda)",
        scriptureReference = "Shat-Chakra-Nirupana (1577 CE)",
        crownSoundPreference = "Resonant Omkara (ॐ)",
        description = "Follows the 7 standard focal centers with classical bija sounds (LAM, VAM, RAM, YAM, HAM, OM, OM) and petal counts."
    ),
    VEDIC_UPANISHADIC(
        id = "vedic_upanishad",
        title = "Vedic Yoga Upanishad",
        scriptureReference = "Dhyanabindu & Yoga-Kundalini Upanishads",
        crownSoundPreference = "Pure Silence (Nirguna)",
        description = "Emphasizes the upward ascent of Prana through Sushumna, resting Sahasrara in silent Samadhi without sound."
    ),
    KASHMIR_SHAIVISM(
        id = "kashmir_shaivism",
        title = "Trika / Kashmir Shaivism",
        scriptureReference = "Vijnana Bhairava Tantra & Tantraloka",
        crownSoundPreference = "Subtle Spanda Vibration",
        description = "Centers on universal awareness (Chiti) and vibrating pulsation (Spanda), viewing each chakra as a gateway of Divine delight."
    )
}

/**
 * Meditation modes supported in the app.
 */
enum class MeditationMode(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val iconName: String
) {
    CHAKRA_FOCUS(
        id = "chakra_focus",
        displayName = "Chakra Focus",
        subtitle = "Contemplative visualization on a single subtle center",
        iconName = "CenterFocusStrong"
    ),
    BIJA_MANTRA(
        id = "bija_mantra",
        displayName = "Bija Mantra Chanting",
        subtitle = "Vocal or mental repetition of root seed syllables",
        iconName = "RecordVoiceOver"
    ),
    GUIDED_MEDITATION(
        id = "guided_meditation",
        displayName = "Guided Contemplation",
        subtitle = "Scriptural contemplation and step-by-step awareness",
        iconName = "SelfImprovement"
    ),
    BREATH_MEDITATION(
        id = "breath_meditation",
        displayName = "Pranayama & Breath",
        subtitle = "Harmonized visual breathing with inhale/hold/exhale cues",
        iconName = "Air"
    ),
    SILENT_MEDITATION(
        id = "silent_meditation",
        displayName = "Silent Dhyana",
        subtitle = "Uninterrupted quiet awareness with interval bells",
        iconName = "NotificationsPaused"
    ),
    MANTRA_AMBIENT(
        id = "mantra_ambient",
        displayName = "Mantra + Music",
        subtitle = "Sacred Vedic chants blended with Indian classical drone",
        iconName = "MusicNote"
    ),
    FULL_JOURNEY(
        id = "full_journey",
        displayName = "7-Chakra Ascending Journey",
        subtitle = "Guided ascent from Muladhara to Sahasrara",
        iconName = "AllInclusive"
    )
}

/**
 * Traditional Yogic Breathing (Pranayama) configurations.
 */
enum class BreathingPattern(
    val id: String,
    val title: String,
    val sanskritName: String,
    val inhaleSeconds: Int,
    val holdAfterInhaleSeconds: Int,
    val exhaleSeconds: Int,
    val holdAfterExhaleSeconds: Int,
    val explanation: String
) {
    SAMA_VRITTI(
        id = "sama_vritti",
        title = "Box Breathing (Equal Ratio)",
        sanskritName = "समवृत्ति प्राणायाम",
        inhaleSeconds = 4,
        holdAfterInhaleSeconds = 4,
        exhaleSeconds = 4,
        holdAfterExhaleSeconds = 4,
        explanation = "Equal duration for each phase. Cultivates equanimity, steady attention, and neurological balance."
    ),
    NADI_SHODHANA(
        id = "nadi_shodhana",
        title = "Nadi Shodhana Rhythm",
        sanskritName = "नाडी शोधन लय",
        inhaleSeconds = 4,
        holdAfterInhaleSeconds = 8,
        exhaleSeconds = 4,
        holdAfterExhaleSeconds = 2,
        explanation = "Traditional subtle channel purification ratio that calms mental agitation and aligns Ida and Pingala."
    ),
    VISHAMA_VRITTI(
        id = "vishama_vritti",
        title = "Relaxing 4-7-8 Breath",
        sanskritName = "विषमवृत्ति (प्रशान्त)",
        inhaleSeconds = 4,
        holdAfterInhaleSeconds = 7,
        exhaleSeconds = 8,
        holdAfterExhaleSeconds = 0,
        explanation = "Extended exhale activates parasympathetic calmness, relieving physical tension before meditation."
    ),
    PRANAVA_BREATH(
        id = "pranava_breath",
        title = "Pranava Omkara Breath",
        sanskritName = "प्रणव प्राणायाम",
        inhaleSeconds = 4,
        holdAfterInhaleSeconds = 4,
        exhaleSeconds = 6,
        holdAfterExhaleSeconds = 2,
        explanation = "Deep intake followed by resonant mental repetition of OM during the slow exhalation."
    ),
    DEEP_ABDOMINAL(
        id = "deep_abdominal",
        title = "Deep Diaphragmatic Breath",
        sanskritName = "दीर्घ प्राणायाम",
        inhaleSeconds = 5,
        holdAfterInhaleSeconds = 2,
        exhaleSeconds = 5,
        holdAfterExhaleSeconds = 2,
        explanation = "Natural soothing flow gently engaging the diaphragm for steady prana flow."
    )
}

/**
 * Ambient musical soundscapes for dhyana and chanting.
 */
enum class AmbientSoundTrack(
    val id: String,
    val displayName: String,
    val description: String,
    val audioPath: String
) {
    TANPURA(
        id = "tanpura",
        displayName = "Indian Classical Tanpura",
        description = "Traditional 4-string acoustic Sa-Pa drone for vocal alignment",
        audioPath = "audio/ambient/ambient_tanpura.mp3"
    ),
    FLUTE(
        id = "flute",
        displayName = "Meditative Bansuri Flute",
        description = "Gentle devotional bamboo flute in Raag Bhupali",
        audioPath = "audio/ambient/ambient_flute.mp3"
    ),
    SINGING_BOWLS(
        id = "singing_bowls",
        displayName = "Tibetan Singing Bowls",
        description = "Resonant multi-harmonic acoustic bronze overtone wash",
        audioPath = "audio/ambient/ambient_singing_bowls.mp3"
    ),
    TEMPLE_BELLS(
        id = "temple_bells",
        displayName = "Vedic Temple Bells & Ghanta",
        description = "Sacred reverberant brass chimes with soothing decay",
        audioPath = "audio/ambient/ambient_temple_bells.mp3"
    ),
    RAIN_NATURE(
        id = "rain_nature",
        displayName = "Sacred Rain & Nature Stream",
        description = "Peaceful mountain rainfall and gentle flowing water",
        audioPath = "audio/ambient/ambient_rain_nature.mp3"
    ),
    DEEP_DRONE(
        id = "deep_drone",
        displayName = "Deep Cosmic Grounding Drone",
        description = "108 Hz warm grounding foundation for stillness",
        audioPath = "audio/ambient/ambient_deep_drone.mp3"
    ),
    COSMIC_SPACE(
        id = "cosmic_space",
        displayName = "Celestial Shimmer & Space",
        description = "Ethereal astral ambience evoking the cosmic void",
        audioPath = "audio/ambient/ambient_cosmic_space.mp3"
    ),
    NONE(
        id = "none",
        displayName = "Pure Silence (No Music)",
        description = "Silent ambient environment for pure self-observation",
        audioPath = ""
    )
}

/**
 * Traditional Mantra entry model.
 */
data class MantraItem(
    val id: String,
    val title: String,
    val sanskrit: String,
    val transliteration: String,
    val englishMeaning: String,
    val pronunciationGuide: String,
    val recommendedCount: Int = 108,
    val associatedChakra: ChakraType?,
    val audioPath: String,
    val traditionalContext: String
)

/**
 * Dedicated Data Model for each of the 7 chakras, specifically capturing
 * Bija mantras, full Sanskrit text, transliterations, pronunciation guides,
 * audio links, and spiritual context.
 */
data class ChakraMantraData(
    val chakraType: ChakraType,
    val chakraNameSanskrit: String,
    val chakraNameEnglish: String,
    val bijaMantra: String,
    val bijaTransliteration: String,
    val sanskritText: String,
    val transliteration: String,
    val englishMeaning: String,
    val pronunciationGuide: String,
    val audioFilePath: String,
    val traditionalElement: String,
    val petals: Int,
    val presidingDeity: String,
    val traditionalSource: String
)

/**
 * Real-time playback status.
 */
data class MeditationPlayerState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentChakra: ChakraType = ChakraType.MULADHARA,
    val currentMode: MeditationMode = MeditationMode.CHAKRA_FOCUS,
    val selectedAmbient: AmbientSoundTrack = AmbientSoundTrack.TANPURA,
    val selectedBreathingPattern: BreathingPattern = BreathingPattern.SAMA_VRITTI,
    val targetDurationMinutes: Int = 15,
    val elapsedSeconds: Int = 0,
    val isLooping: Boolean = true,
    val volume: Float = 1.0f,
    val sleepTimerRemainingMinutes: Int = 0,
    val isSessionComplete: Boolean = false
)
