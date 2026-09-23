package com.example

import com.example.core.model.brahmacharya.*
import com.example.core.model.sanatan.*
import com.example.ui.screens.DashboardTab
import com.example.ui.screens.NavCategory
import org.junit.Assert.*
import org.junit.Test

class BrahmacharyaAndSanatanTest {

    @Test
    fun testSadhanaTabOrder() {
        val sadhanaTabs = NavCategory.DHYANA.tabs
        assertEquals(4, sadhanaTabs.size)
        assertEquals(DashboardTab.VEDIC_DINCHARYA, sadhanaTabs[0])
        assertEquals(DashboardTab.BRAHMACHARYA, sadhanaTabs[1])
        assertEquals(DashboardTab.SANATAN_SANSKRITI, sadhanaTabs[2])
        assertEquals(DashboardTab.CHAKRA_MEDITATION, sadhanaTabs[3])
    }

    @Test
    fun testBrahmacharyaContentIntegrity() {
        assertFalse(BrahmacharyaContent.PILLARS.isEmpty())
        assertFalse(BrahmacharyaContent.EXERCISES.isEmpty())
        assertFalse(BrahmacharyaContent.DAILY_HABITS.isEmpty())

        // Verify audience coverage
        BrahmacharyaContent.PILLARS.forEach { pillar ->
            assertTrue(pillar.title.isNotBlank())
            assertTrue(pillar.sanskritTitle.isNotBlank())
            assertTrue(pillar.detailedText.isNotBlank())
            assertTrue(pillar.keyPractices.isNotEmpty())
            assertTrue(pillar.scripturalQuote.isNotBlank())
            assertTrue(pillar.quoteSource.isNotBlank())
            assertTrue(pillar.audienceNotes.containsKey(AudienceCategory.STUDENT))
            assertTrue(pillar.audienceNotes.containsKey(AudienceCategory.UNMARRIED))
            assertTrue(pillar.audienceNotes.containsKey(AudienceCategory.MARRIED))
        }

        // Verify emergency SOS exercise is present
        val sosExercise = BrahmacharyaContent.EXERCISES.find { it.type == ExerciseType.EMERGENCY_SOS }
        assertNotNull(sosExercise)
        assertEquals("ex_sos_shield", sosExercise?.id)
    }

    @Test
    fun testSanatanSanskritiContentIntegrity() {
        assertFalse(SanatanSanskritiContent.TOPICS.isEmpty())
        assertFalse(SanatanSanskritiContent.GLOSSARY.isEmpty())
        assertFalse(SanatanSanskritiContent.QUIZZES.isEmpty())

        // Ensure all key categories have topics
        val coveredCategories = SanatanSanskritiContent.TOPICS.map { it.category }.toSet()
        assertTrue(coveredCategories.contains(SanatanCategory.FOUNDATION))
        assertTrue(coveredCategories.contains(SanatanCategory.SOUL_GOD_NATURE))
        assertTrue(coveredCategories.contains(SanatanCategory.DHARMA_KARMA))
        assertTrue(coveredCategories.contains(SanatanCategory.YOGA_SADHANA))
        assertTrue(coveredCategories.contains(SanatanCategory.COSMIC_TIME))
        assertTrue(coveredCategories.contains(SanatanCategory.DEVAS_CONCEPTS))
        assertTrue(coveredCategories.contains(SanatanCategory.SCRIPTURES_GURU))
        assertTrue(coveredCategories.contains(SanatanCategory.ASHRAMA_PURUSHARTHA))
        assertTrue(coveredCategories.contains(SanatanCategory.SAMSKARAS))
        assertTrue(coveredCategories.contains(SanatanCategory.FESTIVALS_PRACTICES))

        // Ensure all quizzes have valid options and correct answers
        SanatanSanskritiContent.QUIZZES.forEach { quiz ->
            assertTrue(quiz.questions.isNotEmpty())
            quiz.questions.forEach { q ->
                assertTrue(q.question.isNotBlank())
                assertTrue(q.options.size >= 3)
                assertTrue(q.correctIndex in 0 until q.options.size)
                assertTrue(q.explanation.isNotBlank())
                assertTrue(q.scripturalReference.isNotBlank())
            }
        }
    }
}
