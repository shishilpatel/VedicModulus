package com.example.core.model.sanatan

/**
 * Subject categories of Sanatan Sanskriti knowledge.
 */
enum class SanatanCategory(val displayName: String, val sanskritName: String, val iconName: String) {
    FOUNDATION("Foundations & Vision", "सनातन संस्कृति स्वरूप", "foundation"),
    SOUL_GOD_NATURE("Atma, Ishvara & Prakriti", "आत्मा, परमात्मा एवं प्रकृति", "soul"),
    DHARMA_KARMA("Dharma & Law of Karma", "धर्म एवं कर्म सिद्धान्त", "dharma"),
    YOGA_SADHANA("Yoga & Sadhana Paths", "योग एवं साधना मार्ग", "yoga"),
    COSMIC_TIME("Brahmanda & Cosmic Time", "ब्रह्माण्ड एवं काल चक्र", "time"),
    DEVAS_CONCEPTS("Devas & Cosmic Energies", "देव तत्व एवं दैव विज्ञान", "devas"),
    SCRIPTURES_GURU("Shastras & Guru Parampara", "शास्त्र एवं गुरु-शिष्य परम्परा", "scriptures"),
    ASHRAMA_PURUSHARTHA("Purusharthas & Ashramas", "पुरुषार्थ एवं आश्रम व्यवस्था", "purushartha"),
    SAMSKARAS("16 Sacred Samskaras", "षोडश संस्कार", "samskara"),
    FESTIVALS_PRACTICES("Festivals & Observances", "पर्व, उत्सव एवं सदाचार", "festivals"),
    GLOSSARY("Vedic Sanskrit Glossary", "वैदिक शब्दकोश", "glossary")
}

/**
 * Educational topic chapter.
 */
data class SanatanTopic(
    val id: String,
    val category: SanatanCategory,
    val title: String,
    val devanagariTitle: String,
    val summary: String,
    val fullText: String,
    val keyPillars: List<String>,
    val scripturalVerses: List<ScripturalVerse>,
    val commonMisconceptions: List<String>,
    val practicalLivingTip: String
)

data class ScripturalVerse(
    val devanagari: String,
    val transliteration: String,
    val englishMeaning: String,
    val source: String
)

/**
 * Searchable glossary entry.
 */
data class SanskritGlossaryTerm(
    val id: String,
    val term: String,
    val devanagari: String,
    val pronunciation: String,
    val category: SanatanCategory,
    val rootEtymology: String,
    val conciseMeaning: String,
    val inDepthDescription: String,
    val practicalContext: String
)

/**
 * Interactive Quiz for Sanatan Sanskriti learning.
 */
data class SanatanQuiz(
    val id: String,
    val topicId: String,
    val title: String,
    val category: SanatanCategory,
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val scripturalReference: String
)
