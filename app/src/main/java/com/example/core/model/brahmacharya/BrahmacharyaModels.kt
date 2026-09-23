package com.example.core.model.brahmacharya

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Target audience life stages for tailored Brahmacharya guidance.
 */
enum class AudienceCategory(val displayName: String, val subtitle: String, val focusArea: String) {
    STUDENT(
        "Student / Vidyarthi",
        "Memory, Concentration & Academic Excellence",
        "Channel creative energy into deep focus (Dharana), memory retention (Smriti), and sharp intellect (Buddhi)."
    ),
    UNMARRIED(
        "Unmarried Aspirant",
        "Self-Mastery, Ojas & Emotional Balance",
        "Sublimate physical vitality into spiritual vigor (Ojas/Tejas), creative pursuits, physical fitness, and noble character."
    ),
    MARRIED(
        "Married / Grihastha",
        "Dharmic Restraint, Sacred Union & Harmony",
        "Maintain mutual reverence, marital fidelity, seasonal restraint (Ritu-Kala discipline), and balanced householder life."
    )
}

/**
 * Sacred commitments and duration for Brahmacharya Vrata.
 */
enum class SankalpaDuration(val days: Int, val title: String, val sanskritName: String, val badgeTitle: String) {
    DAYS_7(7, "7-Day Foundation", "Saptaha Vrata (सप्ताह व्रत)", "Ojas Initiate"),
    DAYS_21(21, "21-Day Transformation", "Samskara Vrata (संस्कार व्रत)", "Vira Sadhaka"),
    DAYS_40(40, "40-Day Mandala", "Mandala Siddhi (मण्डल सिद्धि)", "Mandala Siddha"),
    DAYS_108(108, "108-Day Mahavrata", "Mahavrata (महाव्रत)", "Brahmacharya Achiever")
}

/**
 * Core pillar of Brahmacharya practice.
 */
data class BrahmacharyaPillar(
    val id: String,
    val title: String,
    val sanskritTitle: String,
    val subtitle: String,
    val summary: String,
    val detailedText: String,
    val keyPractices: List<String>,
    val scripturalQuote: String,
    val quoteSource: String,
    val audienceNotes: Map<AudienceCategory, String> = emptyMap()
)

/**
 * Interactive practical exercise or yogic technique.
 */
data class BrahmacharyaExercise(
    val id: String,
    val title: String,
    val sanskritName: String,
    val type: ExerciseType,
    val durationMinutes: Int,
    val steps: List<String>,
    val benefits: List<String>,
    val precautions: String
)

enum class ExerciseType(val label: String) {
    PRANAYAMA("Pranayama (Breath Control)"),
    ASANA("Yogic Asana (Posture)"),
    MUDRA_BANDHA("Mudra & Bandha (Energy Lock)"),
    MEDITATION("Dhyana (Mental Grounding)"),
    EMERGENCY_SOS("SOS Urge Deflector")
}

/**
 * Daily habit in Brahmacharya routine.
 */
data class BrahmacharyaDailyHabit(
    val id: String,
    val title: String,
    val sanskritName: String,
    val description: String,
    val timing: String,
    val points: Int = 10
)

/**
 * Daily reflection entry.
 */
data class BrahmacharyaJournalEntry(
    val dateString: String,
    val mentalClarityScore: Int, // 1 to 5
    val triggersOvercome: List<String>,
    val note: String,
    val gratitudeNote: String
)
