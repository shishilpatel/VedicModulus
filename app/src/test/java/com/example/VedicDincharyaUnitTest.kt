package com.example

import com.example.core.model.*
import com.example.core.model.dincharya.*
import com.example.data.CityRepository
import com.example.engines.dincharya.VedicDincharyaEngine
import com.example.engines.personalization.VedicPersonalizationEngine
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class VedicDincharyaUnitTest {

    private fun createSampleDailyProfile(): DailyVedicProfile {
        val now = ZonedDateTime.of(2026, 9, 20, 7, 0, 0, 0, ZoneId.of("Asia/Kolkata"))
        val location = CityRepository.getDefaultCity()
        val personalizationEngine = VedicPersonalizationEngine()
        return personalizationEngine.createDailyVedicProfile(now, location)
    }

    @Test
    fun testScheduleGeneration() {
        val dailyProfile = createSampleDailyProfile()
        val schedule = VedicDincharyaEngine.generateSchedule(
            dailyProfile = dailyProfile,
            personalizedProfile = null,
            birthProfile = null,
            selectedGoal = DincharyaGoal.SPIRITUAL_GROWTH,
            completedIds = setOf("act_brahma_muhurta"),
            targetDate = LocalDate.of(2026, 9, 20)
        )

        assertNotNull(schedule)
        assertTrue(schedule.activities.isNotEmpty())
        assertEquals(1, schedule.completedCount)
        assertTrue(schedule.totalCount >= 10)
        assertTrue(schedule.completionPercentage > 0f)

        // Check Brahma Muhurta activity
        val brahmaAct = schedule.activities.firstOrNull { it.id == "act_brahma_muhurta" }
        assertNotNull(brahmaAct)
        assertTrue(brahmaAct!!.isCompleted)
        assertEquals(dailyProfile.brahmaMuhurta.startTime, brahmaAct.startTime)
        assertEquals(dailyProfile.brahmaMuhurta.endTime, brahmaAct.endTime)

        // Check Meals are provided
        assertTrue(schedule.meals.isNotEmpty())
    }

    @Test
    fun testGoalCustomization() {
        val dailyProfile = createSampleDailyProfile()
        val spiritualSchedule = VedicDincharyaEngine.generateSchedule(
            dailyProfile = dailyProfile,
            personalizedProfile = null,
            birthProfile = null,
            selectedGoal = DincharyaGoal.SPIRITUAL_GROWTH,
            completedIds = emptySet(),
            targetDate = LocalDate.of(2026, 9, 20)
        )

        val vitalitySchedule = VedicDincharyaEngine.generateSchedule(
            dailyProfile = dailyProfile,
            personalizedProfile = null,
            birthProfile = null,
            selectedGoal = DincharyaGoal.VITALITY_HEALTH,
            completedIds = emptySet(),
            targetDate = LocalDate.of(2026, 9, 20)
        )

        val spiritualRecommended = spiritualSchedule.activities.filter { it.isRecommendedForGoal }
        val vitalityRecommended = vitalitySchedule.activities.filter { it.isRecommendedForGoal }

        assertTrue(spiritualRecommended.isNotEmpty())
        assertTrue(vitalityRecommended.isNotEmpty())
    }
}

