package com.example.data.vitality

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Hourly snapshot of physical and pranic vitality telemetry synced from Wear OS.
 */
data class VitalityTelemetrySample(
    val timestamp: Long,
    val stepCount: Int,
    val heartRateBpm: Int,
    val restingHeartRateBpm: Int = 68,
    val pranicScore: Int, // 0 to 100
    val ojasIndex: Int, // 0 to 100
    val activeDoshaPeriod: String, // "Kapha (06:00-10:00)", "Pitta (10:00-14:00)", "Vata (14:00-18:00)"
    val isSattvicWindow: Boolean = false
)

/**
 * Daily aggregated Vedic Vitality Score summary.
 */
data class DailyVitalityScoreSummary(
    val date: LocalDate,
    val totalSteps: Int,
    val averageHeartRate: Int,
    val restingHeartRate: Int,
    val overallVitalityScore: Int, // 0 to 100
    val pranaScore: Int, // Energy & movement vitality
    val ojasScore: Int, // Cellular immunity & restorative calm
    val tejasScore: Int, // Metabolic vigor & heart rate resilience
    val circadianHarmonyPercent: Int, // Circadian adherence to Sattvic hours
    val vedicHealthGuidance: String,
    val hourlySamples: List<VitalityTelemetrySample> = emptyList()
)

/**
 * Periodic historical trend point for multi-day charts.
 */
data class VitalityTrendPoint(
    val dayLabel: String,
    val date: LocalDate,
    val vitalityScore: Int,
    val steps: Int,
    val avgHeartRate: Int
)
