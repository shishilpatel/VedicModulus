package com.example.engines.chakra

import com.example.core.model.chakra.ChakraMantraData
import com.example.core.model.chakra.ChakraType
import com.example.core.model.chakra.MantraItem

/**
 * Traditional Mantra Repository holding authenticated classical Sanskrit mantras,
 * bija seeds, transliterations, and pronunciation instructions for each of the 7 chakras.
 *
 * NOTE: Mantra texts are preserved purely in their traditional sacred Sanskrit forms
 * and kept strictly distinct from secular translated explanations.
 */
object MantraRepository {

    /**
     * Dedicated data models for each of the 7 chakras, detailing Bija mantras,
     * Sanskrit texts, pronunciation guides, links to required audio files,
     * and traditional scriptural contexts.
     */
    val CHAKRA_MANTRAS: Map<ChakraType, ChakraMantraData> = mapOf(
        ChakraType.MULADHARA to ChakraMantraData(
            chakraType = ChakraType.MULADHARA,
            chakraNameSanskrit = "मूलाधार चक्र",
            chakraNameEnglish = "Root Center (Muladhara)",
            bijaMantra = "लं",
            bijaTransliteration = "LAM",
            sanskritText = "ॐ लं पृथिव्यै नमः । लं",
            transliteration = "Om Laṁ Pṛthivyai Namaḥ | LAM",
            englishMeaning = "Salutations to the Divine Earth Element and the foundational root. Cultivates grounding, bodily steadiness, and unwavering composure.",
            pronunciationGuide = "Vibrate 'LUM' with tongue tip behind upper teeth, allowing deep nasal 'm' to hum downwards to the base of spine.",
            audioFilePath = "audio/chakras/chakra_muladhara_lam.mp3",
            traditionalElement = "Earth (Prithvi)",
            petals = 4,
            presidingDeity = "Dakini & Lord Ganesha / Brahma",
            traditionalSource = "Shat-Chakra-Nirupana Verse 4-13"
        ),
        ChakraType.SVADHISTHANA to ChakraMantraData(
            chakraType = ChakraType.SVADHISTHANA,
            chakraNameSanskrit = "स्वाधिष्ठान चक्र",
            chakraNameEnglish = "Sacral Center (Svadhisthana)",
            bijaMantra = "वं",
            bijaTransliteration = "VAM",
            sanskritText = "ॐ वं वरुणाय नमः । वं",
            transliteration = "Om Vaṁ Varuṇāya Namaḥ | VAM",
            englishMeaning = "Salutations to Varuna and the cosmic lunar waters. Cultivates emotional fluidity, sensory equilibrium, and creative vitality.",
            pronunciationGuide = "Gently touch upper teeth to lower lip for 'VUM', releasing into a soft, flowing rounded resonance.",
            audioFilePath = "audio/chakras/chakra_svadisthana_vam.mp3",
            traditionalElement = "Water (Jala / Apas)",
            petals = 6,
            presidingDeity = "Rakini & Lord Vishnu",
            traditionalSource = "Shat-Chakra-Nirupana Verse 14-18"
        ),
        ChakraType.MANIPURA to ChakraMantraData(
            chakraType = ChakraType.MANIPURA,
            chakraNameSanskrit = "मणिपूर चक्र",
            chakraNameEnglish = "Solar Plexus Center (Manipura)",
            bijaMantra = "रं",
            bijaTransliteration = "RAM",
            sanskritText = "ॐ रं अग्नये नमः । रं",
            transliteration = "Om Raṁ Agnaye Namaḥ | RAM",
            englishMeaning = "Salutations to the Sacred Fire (Agni) and the city of jewels. Cultivates willpower, metabolic vitality, and digestive radiance.",
            pronunciationGuide = "Roll the 'R' lightly from the palate, sounding 'RUM' with warmth and solar resonance focused at the navel.",
            audioFilePath = "audio/chakras/chakra_manipura_ram.mp3",
            traditionalElement = "Fire (Agni / Tejas)",
            petals = 10,
            presidingDeity = "Lakini & Lord Rudra",
            traditionalSource = "Shat-Chakra-Nirupana Verse 19-21"
        ),
        ChakraType.ANAHATA to ChakraMantraData(
            chakraType = ChakraType.ANAHATA,
            chakraNameSanskrit = "अनाहत चक्र",
            chakraNameEnglish = "Heart Center (Anahata)",
            bijaMantra = "यं",
            bijaTransliteration = "YAM",
            sanskritText = "ॐ यं वायवे नमः । यं",
            transliteration = "Om Yaṁ Vāyave Namaḥ | YAM",
            englishMeaning = "Salutations to Vayu, the divine life-breath and unstruck sound. Radiates unconditional compassion, universal love, and lightness of heart.",
            pronunciationGuide = "Sigh 'YUM' effortlessly through an open heart space without muscular constriction, resting in chest expansion.",
            audioFilePath = "audio/chakras/chakra_anahata_yam.mp3",
            traditionalElement = "Air (Vayu)",
            petals = 12,
            presidingDeity = "Kakini & Lord Isha",
            traditionalSource = "Shat-Chakra-Nirupana Verse 22-27"
        ),
        ChakraType.VISHUDDHA to ChakraMantraData(
            chakraType = ChakraType.VISHUDDHA,
            chakraNameSanskrit = "विशुद्ध चक्र",
            chakraNameEnglish = "Throat Center (Vishuddha)",
            bijaMantra = "हं",
            bijaTransliteration = "HAM",
            sanskritText = "ॐ हं आकाशाय नमः । हं",
            transliteration = "Om Haṁ Ākāśāya Namaḥ | HAM",
            englishMeaning = "Salutations to the luminous ether and the seat of Vak Shakti. Harmonizes truthful expression, clarity of voice, and receptive wisdom.",
            pronunciationGuide = "Breathe out 'HUM' through an open throat with a gentle oceanic murmur and open airy vibration.",
            audioFilePath = "audio/chakras/chakra_vishuddha_ham.mp3",
            traditionalElement = "Ether / Space (Akasha)",
            petals = 16,
            presidingDeity = "Shakini & Lord Sadashiva",
            traditionalSource = "Shat-Chakra-Nirupana Verse 28-31"
        ),
        ChakraType.AJNA to ChakraMantraData(
            chakraType = ChakraType.AJNA,
            chakraNameSanskrit = "आज्ञा चक्र",
            chakraNameEnglish = "Third Eye / Command Center (Ajna)",
            bijaMantra = "ॐ",
            bijaTransliteration = "OM / AUM",
            sanskritText = "ॐ ॐ नमः । ॐ",
            transliteration = "Om Oṁ Namaḥ | AUM",
            englishMeaning = "The primordial sound synthesis encompassing creation, preservation, and dissolution. Awakens intuitive discernment, concentration, and inner vision.",
            pronunciationGuide = "Progressive tripartite resonance: 'A' (throat) → 'U' (palate/lips) → 'M' (nasal crown) resolving into silent Turiya.",
            audioFilePath = "audio/chakras/chakra_ajna_om.mp3",
            traditionalElement = "Mind / Inner Light (Manas / Mahat)",
            petals = 2,
            presidingDeity = "Hakini & Paramashiva / Hamsa",
            traditionalSource = "Shat-Chakra-Nirupana Verse 32-38"
        ),
        ChakraType.SAHASRARA to ChakraMantraData(
            chakraType = ChakraType.SAHASRARA,
            chakraNameSanskrit = "सहस्रार चक्र",
            chakraNameEnglish = "Crown Center (Sahasrara)",
            bijaMantra = "ॐ",
            bijaTransliteration = "OM / SILENCE",
            sanskritText = "ॐ परब्रह्मणे नमः । ॐ शान्तिः",
            transliteration = "Om Parabrahmaṇe Namaḥ | Om Shāntiḥ",
            englishMeaning = "The transcendental silence beyond vibrational form; boundless pure consciousness and union of Shiva and Shakti.",
            pronunciationGuide = "Deep internal resonance of OM dissolving into pure silent absorption (Mounam / Kevala Kumbhaka).",
            audioFilePath = "audio/chakras/chakra_sahasrara_om.mp3",
            traditionalElement = "Pure Consciousness (Chaitanya)",
            petals = 1000,
            presidingDeity = "Paramashiva & Parashakti",
            traditionalSource = "Shat-Chakra-Nirupana Verse 40-49"
        )
    )

    fun getChakraMantraData(chakra: ChakraType): ChakraMantraData {
        return CHAKRA_MANTRAS[chakra] ?: CHAKRA_MANTRAS.getValue(ChakraType.MULADHARA)
    }

    val BIJA_MANTRAS: List<MantraItem> = listOf(
        MantraItem(
            id = "bija_lam",
            title = "Muladhara Bija (LAM)",
            sanskrit = "लं",
            transliteration = "LAM",
            englishMeaning = "Earth seed syllable representing groundedness, physical embodiment, and enduring stability.",
            pronunciationGuide = "Vibrate 'LUM' with tongue tip behind upper teeth, allowing deep nasal 'm' to hum downwards.",
            recommendedCount = 108,
            associatedChakra = ChakraType.MULADHARA,
            audioPath = "audio/chakras/chakra_muladhara_lam.mp3",
            traditionalContext = "Classical Shat-Chakra-Nirupana verse 4: Seed syllable of Indra's vehicle Airavata and the Prithvi tattva."
        ),
        MantraItem(
            id = "bija_vam",
            title = "Svadhisthana Bija (VAM)",
            sanskrit = "वं",
            transliteration = "VAM",
            englishMeaning = "Water seed syllable evoking emotional fluidity, receptivity, and creative vitality.",
            pronunciationGuide = "Gently touch upper teeth to lower lip for 'VUM', releasing into a soft rolling resonance.",
            recommendedCount = 108,
            associatedChakra = ChakraType.SVADHISTHANA,
            audioPath = "audio/chakras/chakra_svadisthana_vam.mp3",
            traditionalContext = "Governed by Varuna, presiding over the lunar waters and sensory equilibrium."
        ),
        MantraItem(
            id = "bija_ram",
            title = "Manipura Bija (RAM)",
            sanskrit = "रं",
            transliteration = "RAM",
            englishMeaning = "Fire seed syllable invoking willpower, inner illumination, and digestive radiance.",
            pronunciationGuide = "Roll the 'R' lightly from the palate, sounding 'RUM' with warmth focused at the navel.",
            recommendedCount = 108,
            associatedChakra = ChakraType.MANIPURA,
            audioPath = "audio/chakras/chakra_manipura_ram.mp3",
            traditionalContext = "Presided by Agni riding the ram, illuminating personal resolve and discerning action."
        ),
        MantraItem(
            id = "bija_yam",
            title = "Anahata Bija (YAM)",
            sanskrit = "यं",
            transliteration = "YAM",
            englishMeaning = "Air seed syllable radiating compassionate expansion, unconditional love, and inner lightness.",
            pronunciationGuide = "Sigh 'YUM' effortlessly through an open heart space without muscular constriction.",
            recommendedCount = 108,
            associatedChakra = ChakraType.ANAHATA,
            audioPath = "audio/chakras/chakra_anahata_yam.mp3",
            traditionalContext = "Presided by Vayu riding the antelope, representing the unburdened heart free of attachment."
        ),
        MantraItem(
            id = "bija_ham",
            title = "Vishuddha Bija (HAM)",
            sanskrit = "हं",
            transliteration = "HAM",
            englishMeaning = "Space / Ether seed syllable cultivating harmonious expression, clarity of voice, and expansive listening.",
            pronunciationGuide = "Breathe out 'HUM' through an open throat with a gentle oceanic murmur.",
            recommendedCount = 108,
            associatedChakra = ChakraType.VISHUDDHA,
            audioPath = "audio/chakras/chakra_vishuddha_ham.mp3",
            traditionalContext = "Presided by Ambara and Sadashiva, purifying sensory impressions into timeless wisdom."
        ),
        MantraItem(
            id = "bija_om",
            title = "Ajna Pranava (OM / AUM)",
            sanskrit = "ॐ",
            transliteration = "OM / AUM",
            englishMeaning = "The primordial sound synthesis encompassing creation, sustenance, and dissolution; intuitive wisdom.",
            pronunciationGuide = "Progressive resonance: 'A' (throat) → 'U' (palate/lips) → 'M' (nasal crown) → Turiya (silence).",
            recommendedCount = 108,
            associatedChakra = ChakraType.AJNA,
            audioPath = "audio/chakras/chakra_ajna_om.mp3",
            traditionalContext = "Mandukya Upanishad: The entire cosmos past, present, and future is symbolized by Omkara."
        ),
        MantraItem(
            id = "sahasrara_silence",
            title = "Sahasrara Nishpanna (Mouna & OM)",
            sanskrit = "ॐ शान्तिः (मौनम्)",
            transliteration = "OM SHANTI (MOUNAM)",
            englishMeaning = "The transcendental silence beyond vibrational form; boundless pure consciousness.",
            pronunciationGuide = "Either deep internal resonance of OM or complete silent cessation of thought (Kevala Kumbhaka).",
            recommendedCount = 21,
            associatedChakra = ChakraType.SAHASRARA,
            audioPath = "audio/chakras/chakra_sahasrara_om.mp3",
            traditionalContext = "Kashmir Shaivism: The realm of Paramashiva where all sounds dissolve back into their source."
        )
    )

    val VEDIC_MANTRAS: List<MantraItem> = listOf(
        MantraItem(
            id = "vedic_gayatri",
            title = "Rigvedic Gayatri Mantra",
            sanskrit = "ॐ भूर्भुवः स्वः तत्सवितुर्वरेण्यं भर्गो देवस्य धीमहि धियो यो नः प्रचोदयात् ॥",
            transliteration = "Om Bhur Bhuvaḥ Swaḥ Tat Savitur Vareṇyaṁ Bhargo Devasya Dhīmahi Dhiyo Yo Naḥ Prachodayāt ||",
            englishMeaning = "We meditate upon the supreme splendor of the Divine Solar Illuminator (Savitur). May that luminous grace inspire and awaken our intellect and spiritual discernment.",
            pronunciationGuide = "Chant in traditional Vedic meter (Trishtubh/Gayatri): clear enunciation of 'Savitur' and 'Prachodayat'.",
            recommendedCount = 108,
            associatedChakra = ChakraType.AJNA,
            audioPath = "audio/mantras/mantra_gayatri.mp3",
            traditionalContext = "Rigveda 3.62.10: Universally revered as the mother of the Vedas, aligning individual consciousness with cosmic light."
        ),
        MantraItem(
            id = "vedic_mahamrityunjaya",
            title = "Mahamrityunjaya Moksha Mantra",
            sanskrit = "ॐ त्र्यम्बकं यजामहे सुगन्धिं पुष्टिवर्धनम् । उर्वारुकमिव बन्धनान् मृत्योर्मुक्षीय मामृतात् ॥",
            transliteration = "Om Tryambakaṁ Yajāmahe Sugandhiṁ Puṣṭi-Vardhanam | Urvārukam-Iva Bandhanān Mṛtyor-Mukṣīya Māmṛtāt ||",
            englishMeaning = "We worship the Three-Eyed Lord (Tryambaka), fragrant and nourisher of all life. As a ripe melon separates effortlessly from its vine, may we be liberated from mortal limitation into immortality.",
            pronunciationGuide = "Emphasize steady rhythm on 'Sugandhim Pushti-Vardhanam' and resonant 'Muktshiya Mamritat'.",
            recommendedCount = 108,
            associatedChakra = ChakraType.ANAHATA,
            audioPath = "audio/mantras/mantra_mahamrityunjaya.mp3",
            traditionalContext = "Rigveda 7.59.12 (Sukta of Rishi Vashistha): Chanted for mental peace, emotional protection, and inner fearlessness."
        ),
        MantraItem(
            id = "shanti_mantra",
            title = "Vedic Shanti Mantra",
            sanskrit = "ॐ द्यौः शान्तिरन्तरिक्षं शान्तिः पृथिवी शान्तिरापः शान्तिरोषधयः शान्तिः । वनस्पतयः शान्तिर्विश्वेदेवाः शान्तिर्ब्रह्म शान्तिः सर्वं शान्तिः शान्तिरेव शान्तिः सा मा शान्तिरेधि ॥ ॐ शान्तिः शान्तिः शान्तिः ॥",
            transliteration = "Om Dyauḥ Shāntir-Antarikṣhaṁ Shāntiḥ Pṛithivī Shāntir-Āpaḥ Shāntir-Oṣhadhayaḥ Shāntiḥ | Vanaspatayaḥ Shāntir-Viśhvedevāḥ Shāntir-Brahma Shāntiḥ Sarvaṁ Shāntiḥ Shāntir-Eva Shāntiḥ Sā Mā Shāntir-Edhi || Om Shāntiḥ Shāntiḥ Shāntiḥ ||",
            englishMeaning = "May peace radiate in the celestial sky, in outer space, and upon the earth. May peace prevail in the waters, in medicinal herbs, and in the forest trees. May all divinity bring peace. May peace be upon all beings. Om Peace, Peace, Peace.",
            pronunciationGuide = "Slow, dignified cadence invoking cosmic harmony across the natural elements.",
            recommendedCount = 21,
            associatedChakra = ChakraType.SAHASRARA,
            audioPath = "audio/mantras/mantra_om_shanti.mp3",
            traditionalContext = "Yajurveda 36.17: Universal peace invocation recited at the conclusion of spiritual assemblies and meditation."
        ),
        MantraItem(
            id = "so_ham_dhyana",
            title = "So-Ham Hamsa Mantra",
            sanskrit = "सो ऽहम् (हंसः)",
            transliteration = "So'ham (Haṁsaḥ)",
            englishMeaning = "'I am That'—the unforced natural sound of breath. Inhalation sounds 'So', exhalation sounds 'Ham'.",
            pronunciationGuide = "Inhale softly hearing 'Soooo', exhale gently hearing 'Hammmm'. No vocalization needed.",
            recommendedCount = 108,
            associatedChakra = ChakraType.ANAHATA,
            audioPath = "audio/mantras/mantra_om_shanti.mp3",
            traditionalContext = "Isha Upanishad & Hatha Yoga Pradipika: Known as the Ajapa-Japa recited continuously with every breath."
        )
    )

    fun getAllMantras(): List<MantraItem> = BIJA_MANTRAS + VEDIC_MANTRAS

    fun getMantraForChakra(chakra: ChakraType): MantraItem? {
        return BIJA_MANTRAS.firstOrNull { it.associatedChakra == chakra }
    }
}
