package com.example.core.model.dincharya

import com.example.core.model.Graha
import com.example.core.model.Mantra
import com.example.core.model.Nakshatra
import com.example.core.model.TimingWindow
import java.time.LocalDate
import java.time.ZonedDateTime

/**
 * User-selected personalized aspiration for tailoring Vedic Dincharya recommendations.
 */
enum class DincharyaGoal(
    val title: String,
    val sanskrit: String,
    val description: String,
    val iconEmoji: String
) {
    SPIRITUAL_GROWTH(
        title = "Spiritual Sadhana & Moksha",
        sanskrit = "मोक्ष साधनम्",
        description = "Deep meditation, Brahma Muhurta awakening, sacred japa, and Sandhya Vandana focus.",
        iconEmoji = "🕉"
    ),
    MENTAL_PEACE(
        title = "Mental Peace & Stress Relief",
        sanskrit = "शान्ति एवं सत्व",
        description = "Nadi Shodhana pranayama, gentle dhyana, grounding Ayurvedic routines, and evening calm.",
        iconEmoji = "🧘"
    ),
    VITALITY_HEALTH(
        title = "Physical Agni & Vitality",
        sanskrit = "ओजस् एवं बलम्",
        description = "Surya Namaskar, Din-Abhyanga, digestive fire alignment, and active solar routines.",
        iconEmoji = "⚡"
    ),
    FOCUS_PRODUCTIVITY(
        title = "Mindful Focus & Success",
        sanskrit = "धारणा एवं कर्म",
        description = "Choghadiya-timed deep work blocks, mental clarity rituals, and balanced afternoon pacing.",
        iconEmoji = "🎯"
    ),
    AYURVEDIC_BALANCE(
        title = "Tridosha Harmony & Longevity",
        sanskrit = "दोष साम्यम् एवं दीर्घायु",
        description = "Circadian alignment with Vata-Pitta-Kapha hours, Mitahara dining, and Ratricharya rest.",
        iconEmoji = "🌿"
    )
}

/**
 * Category of daily practice.
 */
enum class DincharyaCategory(
    val title: String,
    val sanskrit: String,
    val colorHex: Long
) {
    PURIFICATION(
        title = "Purification & Cleansing",
        sanskrit = "शौच एवं स्नान",
        colorHex = 0xFF0284C7 // Sky Blue
    ),
    MEDITATION_DHYANA(
        title = "Meditation & Dhyana",
        sanskrit = "ध्यानम् एवं मन्त्र",
        colorHex = 0xFF7C3AED // Deep Violet
    ),
    YOGA_PRANAYAMA(
        title = "Yoga & Pranayama",
        sanskrit = "योग एवं प्राणायाम",
        colorHex = 0xFFD97706 // Amber Gold
    ),
    VEDIC_PRAYER(
        title = "Sandhya & Vedic Prayer",
        sanskrit = "त्रिसन्ध्या वन्दनम्",
        colorHex = 0xFFE11D48 // Rose
    ),
    AYURVEDA_NUTRITION(
        title = "Ayurvedic Nutrition & Agni",
        sanskrit = "आहार एवं जठराग्नि",
        colorHex = 0xFF059669 // Emerald Green
    ),
    MINDFUL_WORK(
        title = "Mindful Work & Muhurta",
        sanskrit = "शुभ कर्म एवं मुहूर्त",
        colorHex = 0xFF2563EB // Royal Blue
    ),
    BEDTIME_REST(
        title = "Night Rest & Ratricharya",
        sanskrit = "रात्रिचर्या एवं निद्रा",
        colorHex = 0xFF4F46E5 // Indigo
    )
}

/**
 * 8 Diurnal Phases of the Vedic Day based on astronomical Sun & Sandhya markers.
 */
enum class DincharyaTimePhase(
    val title: String,
    val sanskrit: String,
    val timeDescription: String
) {
    BRAHMA_MUHURTA(
        title = "Brahma Muhurta (Pre-Dawn)",
        sanskrit = "ब्राह्म मुहूर्त",
        timeDescription = "Approx 96 to 48 mins before sunrise"
    ),
    PRATAH_SANDHYA(
        title = "Dawn & Sunrise (Pratah)",
        sanskrit = "प्रातः सन्ध्या",
        timeDescription = "Sunrise window (Kapha hour begins)"
    ),
    PURVAHNA(
        title = "Morning Work (Purvahna)",
        sanskrit = "पूर्वाह्ण",
        timeDescription = "Mid-morning focus (Active energy)"
    ),
    MADHYAHNA(
        title = "Solar Noon (Madhyahna)",
        sanskrit = "मध्याह्न",
        timeDescription = "Peak solar heat (Pitta hour & Abhijit Muhurta)"
    ),
    APARAHNA(
        title = "Afternoon (Aparahna)",
        sanskrit = "अपराह्ण",
        timeDescription = "Late afternoon (Vata transition hour)"
    ),
    SAYAM_SANDHYA(
        title = "Dusk & Sunset (Sayam Sandhya)",
        sanskrit = "सायं सन्ध्या",
        timeDescription = "Sunset twilight window (Kapha return)"
    ),
    PRADOSHA(
        title = "Early Evening (Pradosha)",
        sanskrit = "प्रदोष काल",
        timeDescription = "Post-sunset relaxation and light dinner"
    ),
    RATRI_CHARYA(
        title = "Night Rest (Ratricharya)",
        sanskrit = "रात्रिचर्या",
        timeDescription = "Bedtime preparation, gratitude, and rejuvenating sleep"
    )
}

/**
 * Individual dynamic activity item in the daily Dincharya schedule.
 */
data class DincharyaActivity(
    val id: String,
    val title: String,
    val sanskritTitle: String,
    val category: DincharyaCategory,
    val phase: DincharyaTimePhase,
    val startTime: ZonedDateTime,
    val endTime: ZonedDateTime,
    val isTimeSensitive: Boolean,
    val muhurtaTag: String? = null,
    val description: String,
    val instructions: List<String>,
    val mantra: Mantra? = null,
    val ayurvedicPrinciple: String,
    val goalBenefitTag: String,
    val isRecommendedForGoal: Boolean = false,
    val isCompleted: Boolean = false,
    val cautionNote: String? = null
)

/**
 * Ayurvedic meal and hydration suggestion.
 */
data class DincharyaMealRecommendation(
    val mealType: String,
    val sanskritName: String,
    val suggestedTime: String,
    val principle: String,
    val recommendedFoods: List<String>,
    val avoidFoods: List<String>,
    val agniStatus: String
)

/**
 * Complete day's personalized Dincharya plan.
 */
data class DincharyaDailySchedule(
    val date: LocalDate,
    val locationName: String,
    val selectedGoal: DincharyaGoal,
    val rulingVaraGraha: Graha,
    val nakshatra: Nakshatra,
    val sunrise: ZonedDateTime,
    val sunset: ZonedDateTime,
    val solarNoon: ZonedDateTime,
    val brahmaMuhurta: TimingWindow,
    val abhijitMuhurta: TimingWindow?,
    val rahuKalam: TimingWindow,
    val yamaganda: TimingWindow,
    val gulikaKalam: TimingWindow,
    val activities: List<DincharyaActivity>,
    val meals: List<DincharyaMealRecommendation>,
    val completionPercentage: Float = 0f,
    val completedCount: Int = 0,
    val totalCount: Int = 0
)
