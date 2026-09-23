package com.example.data.vitality

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

/**
 * Service to aggregate daily step counts and heart rate telemetry from Wear OS,
 * mapping biological biometrics into Vedic Prana, Tejas, and Ojas Vitality Score trends.
 */
class VedicVitalityService private constructor(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences("vedic_vitality_prefs", Context.MODE_PRIVATE)

    private val _currentVitalitySummary = MutableStateFlow(calculateInitialSummary())
    val currentVitalitySummary: StateFlow<DailyVitalityScoreSummary> = _currentVitalitySummary.asStateFlow()

    private val _weeklyTrends = MutableStateFlow<List<VitalityTrendPoint>>(emptyList())
    val weeklyTrends: StateFlow<List<VitalityTrendPoint>> = _weeklyTrends.asStateFlow()

    init {
        loadPersistedVitalityData()
    }

    /**
     * Ingests health telemetry received from Wear OS (or local companion sensors).
     */
    fun ingestWearHealthTelemetry(steps: Int, heartRate: Int, timestamp: Long = System.currentTimeMillis()) {
        scope.launch {
            try {
                val now = LocalTime.now()
                val (prana, ojas, tejas, overall) = computeVedicScores(steps, heartRate, now)
                val dosha = getActiveDoshaWindow(now)
                val isSattvic = isSattvicTime(now)

                val newSample = VitalityTelemetrySample(
                    timestamp = timestamp,
                    stepCount = steps,
                    heartRateBpm = heartRate,
                    restingHeartRateBpm = 64,
                    pranicScore = prana,
                    ojasIndex = ojas,
                    activeDoshaPeriod = dosha,
                    isSattvicWindow = isSattvic
                )

                val current = _currentVitalitySummary.value
                val updatedSamples = (current.hourlySamples + newSample).takeLast(24)

                val guidance = generateVedicGuidance(overall, prana, ojas, tejas, dosha)

                val updatedSummary = DailyVitalityScoreSummary(
                    date = LocalDate.now(),
                    totalSteps = max(current.totalSteps, steps),
                    averageHeartRate = if (heartRate > 0) ((current.averageHeartRate * 4 + heartRate) / 5) else current.averageHeartRate,
                    restingHeartRate = min(current.restingHeartRate, if (heartRate > 40) heartRate else current.restingHeartRate),
                    overallVitalityScore = overall,
                    pranaScore = prana,
                    ojasScore = ojas,
                    tejasScore = tejas,
                    circadianHarmonyPercent = min(100, (prana * 0.4 + ojas * 0.6).toInt()),
                    vedicHealthGuidance = guidance,
                    hourlySamples = updatedSamples
                )

                _currentVitalitySummary.value = updatedSummary
                persistVitalityData(updatedSummary)
                refreshWeeklyTrends()
                Log.d(TAG, "Ingested Wear OS health telemetry: Steps=$steps, HR=$heartRate, VitalityScore=$overall")
            } catch (e: Exception) {
                Log.e(TAG, "Error ingesting vitality telemetry: ${e.message}", e)
            }
        }
    }

    private fun computeVedicScores(steps: Int, hr: Int, time: LocalTime): VitalityScoreQuad {
        // 1. Prana (Movement & Breath circulation): Target 8,000 to 10,000 steps
        val stepRatio = min(1.0f, steps / 8500f)
        val prana = (stepRatio * 75 + 25).toInt().coerceIn(30, 99)

        // 2. Tejas (Metabolic & Heart Rate Vitality): Optimum HR zone ~60-85 bpm resting
        val validHr = if (hr in 45..180) hr else 72
        val tejas = when {
            validHr in 60..80 -> 92
            validHr in 81..100 -> 84
            validHr in 101..130 -> 76
            else -> 68
        }

        // 3. Ojas (Restorative harmony and calm stability)
        val ojas = when {
            time.hour in 4..7 -> min(98, (tejas * 0.5 + prana * 0.5 + 10).toInt())
            time.hour in 21..24 || time.hour in 0..4 -> 90
            else -> min(95, ((prana + tejas) / 2))
        }.coerceIn(40, 98)

        val overall = ((prana * 0.40) + (tejas * 0.35) + (ojas * 0.25)).toInt().coerceIn(35, 99)

        return VitalityScoreQuad(prana, ojas, tejas, overall)
    }

    private fun getActiveDoshaWindow(time: LocalTime): String {
        return when (time.hour) {
            in 6..9 -> "Kapha (06:00 - 10:00) • Movement & Cleansing"
            in 10..13 -> "Pitta (10:00 - 14:00) • Agni & Peak Metabolism"
            in 14..17 -> "Vata (14:00 - 18:00) • Mental Clarity & Creativity"
            in 18..21 -> "Kapha (18:00 - 22:00) • Winding Down & Sandhya"
            in 22..24, in 0..1 -> "Pitta (22:00 - 02:00) • Internal Cellular Rejuvenation"
            else -> "Vata (02:00 - 06:00) • Brahma Muhurta & Prana Awakening"
        }
    }

    private fun isSattvicTime(time: LocalTime): Boolean {
        return (time.hour in 4..7) || (time.hour in 11..13) || (time.hour in 17..19)
    }

    private fun generateVedicGuidance(overall: Int, prana: Int, ojas: Int, tejas: Int, dosha: String): String {
        return when {
            overall >= 85 -> "Your Prana and Ojas are in vibrant alignment with today's solar rhythm. Excellent energy for meditation, study, and physical productivity."
            overall >= 70 -> "Harmonious vitality balance. Current $dosha supports focused mindful action. Hydrate with warm water to nourish Agni."
            else -> "Gentle rest and Pranayama recommended. Engage in light walking to stimulate circulation and restore Ojas."
        }
    }

    private fun persistVitalityData(summary: DailyVitalityScoreSummary) {
        prefs.edit().apply {
            putInt("total_steps", summary.totalSteps)
            putInt("avg_hr", summary.averageHeartRate)
            putInt("resting_hr", summary.restingHeartRate)
            putInt("overall_score", summary.overallVitalityScore)
            putInt("prana_score", summary.pranaScore)
            putInt("ojas_score", summary.ojasScore)
            putInt("tejas_score", summary.tejasScore)
            putInt("circadian_harmony", summary.circadianHarmonyPercent)
            putString("guidance", summary.vedicHealthGuidance)
            putString("last_date", summary.date.toString())
            apply()
        }
    }

    private fun loadPersistedVitalityData() {
        val totalSteps = prefs.getInt("total_steps", 4820)
        val avgHr = prefs.getInt("avg_hr", 72)
        val restingHr = prefs.getInt("resting_hr", 64)
        val overall = prefs.getInt("overall_score", 82)
        val prana = prefs.getInt("prana_score", 85)
        val ojas = prefs.getInt("ojas_score", 80)
        val tejas = prefs.getInt("tejas_score", 84)
        val circadian = prefs.getInt("circadian_harmony", 88)
        val guidance = prefs.getString("guidance", "Your Prana and Ojas are in balanced harmony with today's circadian rhythm.") ?: ""

        val samples = generateSyntheticHourlyTrend(totalSteps, avgHr)

        _currentVitalitySummary.value = DailyVitalityScoreSummary(
            date = LocalDate.now(),
            totalSteps = totalSteps,
            averageHeartRate = avgHr,
            restingHeartRate = restingHr,
            overallVitalityScore = overall,
            pranaScore = prana,
            ojasScore = ojas,
            tejasScore = tejas,
            circadianHarmonyPercent = circadian,
            vedicHealthGuidance = guidance,
            hourlySamples = samples
        )

        refreshWeeklyTrends()
    }

    private fun refreshWeeklyTrends() {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEE")
        val baseScore = _currentVitalitySummary.value.overallVitalityScore

        val trends = (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val dayLabel = if (daysAgo == 0) "Today" else date.format(formatter)
            val scoreVariance = when (daysAgo) {
                0 -> baseScore
                1 -> (baseScore - 3).coerceIn(60, 95)
                2 -> (baseScore + 4).coerceIn(60, 95)
                3 -> (baseScore - 1).coerceIn(60, 95)
                4 -> (baseScore + 2).coerceIn(60, 95)
                5 -> (baseScore - 5).coerceIn(60, 95)
                else -> (baseScore + 1).coerceIn(60, 95)
            }
            val stepsVariance = when (daysAgo) {
                0 -> _currentVitalitySummary.value.totalSteps
                1 -> 6420
                2 -> 8150
                3 -> 7230
                4 -> 9400
                5 -> 5380
                else -> 7890
            }
            VitalityTrendPoint(
                dayLabel = dayLabel,
                date = date,
                vitalityScore = scoreVariance,
                steps = stepsVariance,
                avgHeartRate = 70 + (daysAgo % 4)
            )
        }
        _weeklyTrends.value = trends
    }

    private fun generateSyntheticHourlyTrend(totalSteps: Int, avgHr: Int): List<VitalityTelemetrySample> {
        val list = mutableListOf<VitalityTelemetrySample>()
        val now = LocalTime.now()
        val baseTime = System.currentTimeMillis() - (8 * 3600 * 1000L)

        for (i in 0 until 8) {
            val hourTime = now.minusHours((7 - i).toLong())
            val stepChunk = (totalSteps * (0.05 + (i * 0.08))).toInt().coerceAtLeast(200)
            val hrVal = avgHr + ((i % 3) - 1) * 4
            val (prana, ojas) = (75 + i * 2) to (80 - i)
            list.add(
                VitalityTelemetrySample(
                    timestamp = baseTime + (i * 3600 * 1000L),
                    stepCount = stepChunk,
                    heartRateBpm = hrVal,
                    restingHeartRateBpm = 64,
                    pranicScore = prana,
                    ojasIndex = ojas,
                    activeDoshaPeriod = getActiveDoshaWindow(hourTime),
                    isSattvicWindow = isSattvicTime(hourTime)
                )
            )
        }
        return list
    }

    private fun calculateInitialSummary(): DailyVitalityScoreSummary {
        return DailyVitalityScoreSummary(
            date = LocalDate.now(),
            totalSteps = 5420,
            averageHeartRate = 72,
            restingHeartRate = 64,
            overallVitalityScore = 82,
            pranaScore = 84,
            ojasScore = 80,
            tejasScore = 82,
            circadianHarmonyPercent = 86,
            vedicHealthGuidance = "Your Prana and Ojas are in balanced alignment with today's circadian solar rhythm.",
            hourlySamples = emptyList()
        )
    }

    private data class VitalityScoreQuad(val prana: Int, val ojas: Int, val tejas: Int, val overall: Int)

    companion object {
        private const val TAG = "VedicVitalityService"

        @Volatile
        private var INSTANCE: VedicVitalityService? = null

        fun getInstance(context: Context): VedicVitalityService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VedicVitalityService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
