package com.example.wear

import kotlin.math.cos
import kotlin.math.sin

enum class WatchFaceStyle(val displayName: String, val description: String) {
    COSMIC("Cosmic Orbit", "Celestial orbits and planetary tracks"),
    YANTRA("Sri Yantra", "Sacred geometry and interlocking triangles"),
    CHAKRA("7 Chakras", "Concentric energy vortex rings"),
    VASTU("Vastu Mandala", "8-Directional compass wheel"),
    PANCHANGA("Traditional", "Classical Vedic dial with Devanagari numerals"),
    MINIMAL("Minimal Swiss", "Clean minimalist high-contrast indices"),
    ROYAL("Indian Royal", "Heritage gold motifs and divine symbols"),
    MODERN("Active Modern", "Contemporary vibrant smartwatch dial")
}

sealed class VectorDialElement {
    data class CircleRing(
        val radiusRatio: Float,
        val colorHex: Long,
        val strokeWidthDp: Float,
        val isDashed: Boolean = false
    ) : VectorDialElement()

    data class TickMarks(
        val count: Int,
        val startRadiusRatio: Float,
        val endRadiusRatio: Float,
        val colorHex: Long,
        val strokeWidthDp: Float
    ) : VectorDialElement()

    data class GlyphMarker(
        val angleDeg: Float,
        val radiusRatio: Float,
        val text: String,
        val colorHex: Long,
        val fontSizeSp: Float
    ) : VectorDialElement()

    data class PolygonShape(
        val normalizedVertices: List<Pair<Float, Float>>,
        val colorHex: Long,
        val strokeWidthDp: Float,
        val isFilled: Boolean = false,
        val isDashed: Boolean = false
    ) : VectorDialElement()

    data class ArcSegment(
        val startAngleDeg: Float,
        val sweepAngleDeg: Float,
        val radiusRatio: Float,
        val colorHex: Long,
        val strokeWidthDp: Float
    ) : VectorDialElement()
}

data class ProceduralWatchFaceModel(
    val style: WatchFaceStyle,
    val backgroundFillHex: Long,
    val elements: List<VectorDialElement>,
    val hourHandColorHex: Long,
    val minuteHandColorHex: Long,
    val secondHandColorHex: Long,
    val complicationTintHex: Long,
    val centerCapColorHex: Long
)

class WatchFaceGenerator {
    fun generateDialModel(
        style: WatchFaceStyle,
        syncData: SyncData
    ): ProceduralWatchFaceModel {
        // Apply Numerology Auspicious Color Palette or Synced Custom Colors
        val palette = NumerologyColorMapper.getPaletteForLifePathNumber(syncData.destinyNumber)
        val pColor = if (syncData.primaryColorHex != 0L && syncData.primaryColorHex != 0xFFFFD166) syncData.primaryColorHex else palette.primaryColorHex
        val aColor = if (syncData.accentColorHex != 0L && syncData.accentColorHex != 0xFFF43F5E) syncData.accentColorHex else palette.accentColorHex
        val isRahuActive = syncData.isRahuActive

        val elements = mutableListOf<VectorDialElement>()
        val bgHex = 0xFF000000

        when (style) {
            WatchFaceStyle.COSMIC -> {
                elements.add(VectorDialElement.CircleRing(0.96f, 0xFF1E293B, 1.5f))
                elements.add(VectorDialElement.CircleRing(0.85f, 0xFF0F172A, 1.0f))
                elements.add(VectorDialElement.TickMarks(60, 0.90f, 0.95f, 0xFF334155, 1.0f))
                elements.add(VectorDialElement.TickMarks(12, 0.86f, 0.95f, pColor, 2.0f))
                elements.add(VectorDialElement.CircleRing(0.68f, 0x33FFD166, 1.0f, isDashed = true))
                elements.add(VectorDialElement.CircleRing(0.52f, 0x2238BDF8, 1.0f, isDashed = true))
                elements.add(VectorDialElement.CircleRing(0.36f, 0x33F43F5E, 1.0f, isDashed = true))
                elements.add(VectorDialElement.GlyphMarker(270f, 0.72f, "☉", 0xFFFFD166, 14f))
                elements.add(VectorDialElement.GlyphMarker(90f, 0.54f, "☽", 0xFFE2E8F0, 13f))
                val chandraSymbol = when (syncData.natalChandra.lowercase()) {
                    "mesha", "aries" -> "♈"
                    "vrishabha", "taurus" -> "♉"
                    "mithuna", "gemini" -> "♊"
                    "karka", "cancer" -> "♋"
                    "simha", "leo" -> "♌"
                    "kanya", "virgo" -> "♍"
                    "tula", "libra" -> "♎"
                    "vrishchika", "scorpio" -> "♏"
                    "dhanu", "sagittarius" -> "♐"
                    "makara", "capricorn" -> "♑"
                    "kumbha", "aquarius" -> "♒"
                    "meena", "pisces" -> "♓"
                    else -> "♈"
                }
                elements.add(VectorDialElement.GlyphMarker(0f, 0.38f, chandraSymbol, aColor, 12f))
            }
            WatchFaceStyle.YANTRA -> {
                // Sacred Sri Yantra (Sri Chakra) Canonical Geometry (Static, unmodified as ordained in Vedic Shastras)

                // 1. Bhupura (Trailokya Mohana Chakra) - Sacred Square Citadel with 4 Cardinal Portals
                fun generateBhupuraGate(r: Float): List<Pair<Float, Float>> {
                    val pts = mutableListOf<Pair<Float, Float>>()
                    val g = r * 0.24f
                    val w = r * 0.12f
                    // Top (North)
                    pts.add(-r to -r)
                    pts.add(-g to -r)
                    pts.add(-g to -r + w)
                    pts.add(g to -r + w)
                    pts.add(g to -r)
                    pts.add(r to -r)
                    // Right (East)
                    pts.add(r to -g)
                    pts.add(r - w to -g)
                    pts.add(r - w to g)
                    pts.add(r to g)
                    pts.add(r to r)
                    // Bottom (South)
                    pts.add(g to r)
                    pts.add(g to r - w)
                    pts.add(-g to r - w)
                    pts.add(-g to r)
                    pts.add(-r to r)
                    // Left (West)
                    pts.add(-r to g)
                    pts.add(-r + w to g)
                    pts.add(-r + w to -g)
                    pts.add(-r to -g)
                    return pts
                }
                elements.add(VectorDialElement.PolygonShape(generateBhupuraGate(0.96f), pColor, 1.8f))
                elements.add(VectorDialElement.PolygonShape(generateBhupuraGate(0.92f), 0x88FFD166, 1.0f))

                // 2. Trivritta (Three Concentric Boundary Rings)
                elements.add(VectorDialElement.CircleRing(0.86f, pColor, 1.5f))
                elements.add(VectorDialElement.CircleRing(0.83f, 0xAAFFD166, 1.0f))
                elements.add(VectorDialElement.CircleRing(0.80f, pColor, 1.5f))

                // 3. Shodasha-Dala Padma (16 Lotus Petals - Sarva Ashopaparipuraka Chakra)
                for (i in 0 until 16) {
                    val a0 = Math.toRadians((i * 22.5 - 11.25))
                    val aMid = Math.toRadians(i * 22.5)
                    val a1 = Math.toRadians((i * 22.5 + 11.25))
                    val petal = listOf(
                        (0.68f * kotlin.math.cos(a0)).toFloat() to (0.68f * kotlin.math.sin(a0)).toFloat(),
                        (0.79f * kotlin.math.cos(aMid)).toFloat() to (0.79f * kotlin.math.sin(aMid)).toFloat(),
                        (0.68f * kotlin.math.cos(a1)).toFloat() to (0.68f * kotlin.math.sin(a1)).toFloat()
                    )
                    elements.add(VectorDialElement.PolygonShape(petal, 0xBBFFD166, 1.2f))
                }
                elements.add(VectorDialElement.CircleRing(0.68f, pColor, 1.2f))

                // 4. Ashta-Dala Padma (8 Lotus Petals - Sarva Sankshobhana Chakra)
                for (i in 0 until 8) {
                    val a0 = Math.toRadians((i * 45.0 - 22.5))
                    val aMid = Math.toRadians(i * 45.0)
                    val a1 = Math.toRadians((i * 45.0 + 22.5))
                    val petal = listOf(
                        (0.54f * kotlin.math.cos(a0)).toFloat() to (0.54f * kotlin.math.sin(a0)).toFloat(),
                        (0.67f * kotlin.math.cos(aMid)).toFloat() to (0.67f * kotlin.math.sin(aMid)).toFloat(),
                        (0.54f * kotlin.math.cos(a1)).toFloat() to (0.54f * kotlin.math.sin(a1)).toFloat()
                    )
                    elements.add(VectorDialElement.PolygonShape(petal, pColor, 1.4f))
                }
                elements.add(VectorDialElement.CircleRing(0.54f, pColor, 1.5f))

                // 5. Navayoni Chakra (The 9 Authentic Interlocking Shiva & Shakti Triangles)
                val shiva1 = listOf(0.0f to -0.48f, 0.38f to 0.28f, -0.38f to 0.28f)
                val shiva2 = listOf(0.0f to -0.38f, 0.44f to 0.18f, -0.44f to 0.18f)
                val shiva3 = listOf(0.0f to -0.26f, 0.34f to 0.36f, -0.34f to 0.36f)
                val shiva4 = listOf(0.0f to -0.16f, 0.22f to 0.24f, -0.22f to 0.24f)

                val shakti1 = listOf(0.0f to 0.48f, 0.42f to -0.22f, -0.42f to -0.22f)
                val shakti2 = listOf(0.0f to 0.38f, 0.46f to -0.12f, -0.46f to -0.12f)
                val shakti3 = listOf(0.0f to 0.28f, 0.36f to -0.32f, -0.36f to -0.32f)
                val shakti4 = listOf(0.0f to 0.18f, 0.26f to -0.20f, -0.26f to -0.20f)
                val shakti5 = listOf(0.0f to 0.10f, 0.14f to -0.10f, -0.14f to -0.10f)

                elements.add(VectorDialElement.PolygonShape(shiva1, pColor, 1.6f))
                elements.add(VectorDialElement.PolygonShape(shakti1, aColor, 1.6f))
                elements.add(VectorDialElement.PolygonShape(shiva2, pColor, 1.5f))
                elements.add(VectorDialElement.PolygonShape(shakti2, aColor, 1.5f))
                elements.add(VectorDialElement.PolygonShape(shiva3, pColor, 1.4f))
                elements.add(VectorDialElement.PolygonShape(shakti3, aColor, 1.4f))
                elements.add(VectorDialElement.PolygonShape(shiva4, pColor, 1.3f))
                elements.add(VectorDialElement.PolygonShape(shakti4, aColor, 1.3f))
                elements.add(VectorDialElement.PolygonShape(shakti5, aColor, 1.5f))

                // 6. Central Bindu
                elements.add(VectorDialElement.CircleRing(0.035f, 0xFFE63946, 2.5f))
                elements.add(VectorDialElement.CircleRing(0.015f, 0xFFFFD166, 2.0f))
            }
            WatchFaceStyle.CHAKRA -> {
                val chakraRadii = listOf(0.95f, 0.85f, 0.73f, 0.61f, 0.49f, 0.37f, 0.25f)
                val chakraColors = listOf(
                    0xFFE63946, 0xFFF77F00, 0xFFFCBF49, 0xFF2A9D8F,
                    0xFF457B9D, 0xFF3D348B, 0xFFE0AAFF
                )
                for (i in chakraRadii.indices) {
                    elements.add(VectorDialElement.CircleRing(chakraRadii[i], chakraColors[i], 1.2f))
                }
                elements.add(VectorDialElement.TickMarks(12, 0.90f, 0.96f, 0xFFFFFFFF, 1.5f))
                elements.add(VectorDialElement.GlyphMarker(270f, 0.15f, "ॐ", 0xFFFFD166, 16f))
            }
            WatchFaceStyle.VASTU -> {
                elements.add(VectorDialElement.CircleRing(0.96f, pColor, 2.0f))
                elements.add(VectorDialElement.TickMarks(8, 0.84f, 0.95f, 0xFFFFB703, 3.0f))
                elements.add(VectorDialElement.TickMarks(32, 0.90f, 0.95f, 0x55FFFFFF, 1.0f))
                val sanskritDirs = listOf("उ", "ई", "पू", "आ", "द", "नै", "प", "वा")
                for (i in 0 until 8) {
                    val angle = (i * 45f) - 90f
                    elements.add(VectorDialElement.GlyphMarker(angle, 0.76f, sanskritDirs[i], 0xFFE2E8F0, 11f))
                }
                elements.add(VectorDialElement.CircleRing(0.35f, aColor, 1.0f, isDashed = true))
            }
            WatchFaceStyle.PANCHANGA -> {
                elements.add(VectorDialElement.CircleRing(0.96f, 0xFFD4AF37, 2.5f))
                elements.add(VectorDialElement.CircleRing(0.82f, 0x44D4AF37, 1.0f))
                elements.add(VectorDialElement.TickMarks(60, 0.88f, 0.94f, 0x88D4AF37, 1.0f))
                elements.add(VectorDialElement.TickMarks(12, 0.84f, 0.95f, 0xFFD4AF37, 2.5f))
                val devanagariHours = listOf("१२", "१", "२", "३", "४", "५", "६", "७", "८", "९", "१०", "११")
                for (h in 0 until 12) {
                    val angle = (h * 30f) - 90f
                    elements.add(VectorDialElement.GlyphMarker(angle, 0.70f, devanagariHours[h], 0xFFFFE082, 12f))
                }
            }
            WatchFaceStyle.MINIMAL -> {
                elements.add(VectorDialElement.TickMarks(12, 0.90f, 0.96f, 0xFFFFFFFF, 1.5f))
                elements.add(VectorDialElement.CircleRing(0.60f, 0x1FFFFFFF, 1.0f))
                elements.add(VectorDialElement.GlyphMarker(90f, 0.45f, "#${syncData.birthNumber}", pColor, 12f))
            }
            WatchFaceStyle.ROYAL -> {
                elements.add(VectorDialElement.CircleRing(0.97f, 0xFFFFD700, 3.0f))
                elements.add(VectorDialElement.CircleRing(0.91f, 0xFFB8860B, 1.0f))
                elements.add(VectorDialElement.CircleRing(0.85f, 0x66FFD700, 1.0f, isDashed = true))
                elements.add(VectorDialElement.TickMarks(12, 0.82f, 0.93f, 0xFFFFD700, 2.5f))
                elements.add(VectorDialElement.TickMarks(60, 0.88f, 0.92f, 0x55FFD700, 1.0f))
                elements.add(VectorDialElement.CircleRing(0.40f, 0xFFDC2626, 1.5f))
                elements.add(VectorDialElement.GlyphMarker(270f, 0.62f, "॥ ॐ ॥", 0xFFFFD700, 12f))
            }
            WatchFaceStyle.MODERN -> {
                elements.add(VectorDialElement.CircleRing(0.95f, 0x3338BDF8, 2.0f))
                elements.add(VectorDialElement.TickMarks(12, 0.88f, 0.95f, 0xFF38BDF8, 2.0f))
                elements.add(VectorDialElement.ArcSegment(270f, 180f, 0.90f, aColor, 3.0f))
            }
        }

        if (isRahuActive) {
            elements.add(VectorDialElement.CircleRing(0.99f, 0xFFEF4444, 3.0f))
        }

        return ProceduralWatchFaceModel(
            style = style,
            backgroundFillHex = bgHex,
            elements = elements,
            hourHandColorHex = 0xFFFFFFFF,
            minuteHandColorHex = 0xFFE2E8F0,
            secondHandColorHex = pColor,
            complicationTintHex = aColor,
            centerCapColorHex = pColor
        )
    }
}

enum class ComplicationSlotPosition(val key: String, val displayName: String) {
    TOP("top", "Top Complication"),
    BOTTOM("bottom", "Bottom Complication"),
    LEFT("left", "Left Complication"),
    RIGHT("right", "Right Complication")
}

enum class ComplicationTypeOption(
    val id: String,
    val title: String,
    val category: String,
    val icon: String,
    val description: String
) {
    VEDIC_MUHURAT("VEDIC_MUHURAT", "Auspicious Muhurat", "Vedic", "🕉️", "Live Choghadiya & Shubh timing"),
    VEDIC_TITHI("VEDIC_TITHI", "Tithi & Nakshatra", "Vedic", "🌙", "Lunar tithi and active constellation"),
    PLANETARY_HORA("PLANETARY_HORA", "Planetary Hora", "Vedic", "🪐", "Current Graha hour ruler"),
    RAHU_KAAL("RAHU_KAAL", "Rahu Kaal Alert", "Vedic", "⚡", "Inauspicious Rahu window indicator"),
    STEP_COUNT("STEP_COUNT", "Step Counter", "Fitness", "👟", "Daily step count with progress"),
    WEATHER("WEATHER", "Live Weather", "Environment", "⛅", "Current temperature & condition"),
    BATTERY("BATTERY", "Watch Battery", "System", "🔋", "Wear OS battery level percentage"),
    HEART_RATE("HEART_RATE", "Heart Rate", "Health", "❤️", "BPM pulse sensor reading"),
    SUNRISE_SUNSET("SUNRISE_SUNSET", "Surya Uday / Ast", "Astronomy", "☀️", "Solar dawn and dusk timings"),
    DATE_VARA("DATE_VARA", "Date & Vedic Vara", "Calendar", "📅", "Vedic weekday and calendar day")
}

data class SyncData(
    val watchStyle: String = "COSMIC",
    val primaryColorHex: Long = 0xFFFFD166,
    val accentColorHex: Long = 0xFFF43F5E,
    val fullName: String = "Vedic Seeker",
    val birthDate: String = "",
    val birthTime: String = "",
    val cityName: String = "Varanasi",
    val natalNakshatra: String = "Rohini",
    val natalNakshatraPada: Int = 1,
    val natalLagna: String = "Mesha",
    val natalChandra: String = "Vrishabha",
    val birthNumber: Int = 7,
    val destinyNumber: Int = 3,
    val latestPredictionTitle: String = "Vedic Life Insights",
    val latestPredictionText: String = "Align your intentions with the celestial transits today.",
    val latestPredictionTimestamp: Long = 0L,
    val currentHora: String = "Surya",
    val horaSymbol: String = "☉",
    val currentMuhurat: String = "Amrit",
    val currentMuhuratDevanagari: String = "अमृत",
    val isAuspicious: Boolean = true,
    val muhuratEndTime: String = "",
    val sunrise: String = "06:00 AM",
    val sunset: String = "06:00 PM",
    val tithi: String = "Shukla Dashami",
    val tithiDevanagari: String = "शुक्ल दशमी",
    val nakshatra: String = "Rohini",
    val currentYoga: String = "Siddha",
    val currentKarana: String = "Bava",
    val currentVara: String = "Ravivara",
    val showTithi: Boolean = true,
    val showNakshatra: Boolean = true,
    val showYoga: Boolean = true,
    val showKarana: Boolean = true,
    val showVara: Boolean = true,
    val showRahuKaal: Boolean = true,
    val showHora: Boolean = true,
    val rahuStart: String = "04:30 PM",
    val rahuEnd: String = "06:00 PM",
    val isRahuActive: Boolean = false,
    val isSafeModeEnabled: Boolean = false,
    val isLowPowerModeActive: Boolean = false,
    val isThermalThrottled: Boolean = false,
    val thermalStatusName: String = "NOMINAL",
    val topComplicationType: String = "VEDIC_MUHURAT",
    val bottomComplicationType: String = "STEP_COUNT",
    val leftComplicationType: String = "PLANETARY_HORA",
    val rightComplicationType: String = "WEATHER",
    val stepCount: Int = 5420,
    val stepGoal: Int = 10000,
    val weatherTemp: String = "28°C",
    val weatherCondition: String = "Clear Sky",
    val weatherIcon: String = "☀️",
    val batteryPercent: Int = 85,
    val heartRateBpm: Int = 72,
    val moonPhaseAssetId: String? = null,
    val isSyncedFromPhone: Boolean = false,
    val lastSyncTimestamp: Long = 0L
)

object AstroDataStore {
    val syncedData = kotlinx.coroutines.flow.MutableStateFlow(VedicCalculator.createInitialSyncData())
    private const val PREFS_NAME = "wear_vedic_sync_prefs"

    fun init(context: android.content.Context) {
        try {
            refreshSystemBattery(context)
            val sp = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            val isSynced = sp.getBoolean("is_synced", false)
            if (isSynced) {
                syncedData.value = SyncData(
                    watchStyle = sp.getString("watch_style", "COSMIC") ?: "COSMIC",
                    primaryColorHex = sp.getLong("primary_color", 0xFFFFD166),
                    accentColorHex = sp.getLong("accent_color", 0xFFF43F5E),
                    fullName = sp.getString("full_name", "Vedic Seeker") ?: "Vedic Seeker",
                    birthDate = sp.getString("birth_date", "") ?: "",
                    birthTime = sp.getString("birth_time", "") ?: "",
                    cityName = sp.getString("city_name", "Varanasi") ?: "Varanasi",
                    natalNakshatra = sp.getString("natal_nakshatra", "Rohini") ?: "Rohini",
                    natalNakshatraPada = sp.getInt("natal_nakshatra_pada", 1),
                    natalLagna = sp.getString("natal_lagna", "Mesha") ?: "Mesha",
                    natalChandra = sp.getString("natal_chandra", "Vrishabha") ?: "Vrishabha",
                    birthNumber = sp.getInt("birth_number", 7),
                    destinyNumber = sp.getInt("destiny_number", 3),
                    latestPredictionTitle = sp.getString("latest_pred_title", "Vedic Life Insights") ?: "Vedic Life Insights",
                    latestPredictionText = sp.getString("latest_pred_text", "") ?: "",
                    latestPredictionTimestamp = sp.getLong("latest_pred_time", 0L),
                    currentHora = sp.getString("current_hora", "Surya") ?: "Surya",
                    horaSymbol = sp.getString("hora_symbol", "☉") ?: "☉",
                    currentMuhurat = sp.getString("current_muhurat", "Amrit") ?: "Amrit",
                    currentMuhuratDevanagari = sp.getString("current_muhurat_dev", "अमृत") ?: "अमृत",
                    isAuspicious = sp.getBoolean("is_auspicious", true),
                    muhuratEndTime = sp.getString("muhurat_end", "") ?: "",
                    sunrise = sp.getString("sunrise", "06:00 AM") ?: "06:00 AM",
                    sunset = sp.getString("sunset", "06:00 PM") ?: "06:00 PM",
                    tithi = sp.getString("tithi", "Shukla Dashami") ?: "Shukla Dashami",
                    tithiDevanagari = sp.getString("tithi_dev", "शुक्ल दशमी") ?: "शुक्ल दशमी",
                    nakshatra = sp.getString("nakshatra", "Rohini") ?: "Rohini",
                    rahuStart = sp.getString("rahu_start", "04:30 PM") ?: "04:30 PM",
                    rahuEnd = sp.getString("rahu_end", "06:00 PM") ?: "06:00 PM",
                    isRahuActive = sp.getBoolean("is_rahu_active", false),
                    isSafeModeEnabled = sp.getBoolean("is_safe_mode", false),
                    isLowPowerModeActive = sp.getBoolean("is_low_power", false),
                    isThermalThrottled = sp.getBoolean("is_thermal_throttled", false),
                    thermalStatusName = sp.getString("thermal_status_name", "NOMINAL") ?: "NOMINAL",
                    topComplicationType = sp.getString("top_comp", "VEDIC_MUHURAT") ?: "VEDIC_MUHURAT",
                    bottomComplicationType = sp.getString("bottom_comp", "STEP_COUNT") ?: "STEP_COUNT",
                    leftComplicationType = sp.getString("left_comp", "PLANETARY_HORA") ?: "PLANETARY_HORA",
                    rightComplicationType = sp.getString("right_comp", "WEATHER") ?: "WEATHER",
                    stepCount = sp.getInt("step_count", 5420),
                    stepGoal = sp.getInt("step_goal", 10000),
                    weatherTemp = sp.getString("weather_temp", "28°C") ?: "28°C",
                    weatherCondition = sp.getString("weather_cond", "Clear Sky") ?: "Clear Sky",
                    weatherIcon = sp.getString("weather_icon", "☀️") ?: "☀️",
                    batteryPercent = sp.getInt("battery_percent", 85),
                    heartRateBpm = sp.getInt("heart_rate_bpm", 72),
                    moonPhaseAssetId = sp.getString("moon_phase", null),
                    isSyncedFromPhone = true,
                    lastSyncTimestamp = sp.getLong("last_sync_timestamp", 0L)
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("AstroDataStore", "Failed to init from SharedPreferences: ${e.message}")
        }
    }

    fun setSafeMode(context: android.content.Context, enabled: Boolean) {
        val updated = syncedData.value.copy(isSafeModeEnabled = enabled)
        updateSyncData(context, updated)
    }

    fun toggleSafeMode(context: android.content.Context): Boolean {
        val newState = !syncedData.value.isSafeModeEnabled
        setSafeMode(context, newState)
        return newState
    }

    fun setLowPowerState(
        context: android.content.Context,
        isLowPower: Boolean,
        isThermal: Boolean = false,
        thermalStatus: String = "NOMINAL"
    ) {
        val current = syncedData.value
        if (current.isLowPowerModeActive != isLowPower ||
            current.isThermalThrottled != isThermal ||
            current.thermalStatusName != thermalStatus
        ) {
            val updated = current.copy(
                isLowPowerModeActive = isLowPower,
                isThermalThrottled = isThermal,
                thermalStatusName = thermalStatus
            )
            updateSyncData(context, updated)
        }
    }

    fun toggleLowPowerMode(context: android.content.Context): Boolean {
        val newState = WearPowerManager.toggleForceLowPower(context)
        return newState
    }

    fun setComplicationSlot(context: android.content.Context, slot: ComplicationSlotPosition, type: String) {
        val current = syncedData.value
        val updated = when (slot) {
            ComplicationSlotPosition.TOP -> current.copy(topComplicationType = type)
            ComplicationSlotPosition.BOTTOM -> current.copy(bottomComplicationType = type)
            ComplicationSlotPosition.LEFT -> current.copy(leftComplicationType = type)
            ComplicationSlotPosition.RIGHT -> current.copy(rightComplicationType = type)
        }
        updateSyncData(context, updated)
    }

    fun updateStepCount(context: android.content.Context, count: Int) {
        val updated = syncedData.value.copy(stepCount = count)
        updateSyncData(context, updated)
    }

    fun updateWeather(context: android.content.Context, temp: String, condition: String, icon: String) {
        val updated = syncedData.value.copy(
            weatherTemp = temp,
            weatherCondition = condition,
            weatherIcon = icon
        )
        updateSyncData(context, updated)
    }

    fun updateHeartRate(context: android.content.Context, bpm: Int) {
        val updated = syncedData.value.copy(heartRateBpm = bpm)
        updateSyncData(context, updated)
    }

    fun refreshSystemBattery(context: android.content.Context) {
        try {
            val ifilter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, ifilter)
            val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                val batteryPct = (level * 100 / scale.toFloat()).toInt()
                val current = syncedData.value
                if (current.batteryPercent != batteryPct) {
                    syncedData.value = current.copy(batteryPercent = batteryPct)
                }
            }
        } catch (e: Exception) {
            android.util.Log.d("AstroDataStore", "Battery check: ${e.message}")
        }
    }

    fun updateSyncData(context: android.content.Context, data: SyncData) {
        syncedData.value = data
        try {
            val sp = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            sp.edit().apply {
                putBoolean("is_synced", true)
                putString("watch_style", data.watchStyle)
                putLong("primary_color", data.primaryColorHex)
                putLong("accent_color", data.accentColorHex)
                putString("full_name", data.fullName)
                putString("birth_date", data.birthDate)
                putString("birth_time", data.birthTime)
                putString("city_name", data.cityName)
                putString("natal_nakshatra", data.natalNakshatra)
                putInt("natal_nakshatra_pada", data.natalNakshatraPada)
                putString("natal_lagna", data.natalLagna)
                putString("natal_chandra", data.natalChandra)
                putInt("birth_number", data.birthNumber)
                putInt("destiny_number", data.destinyNumber)
                putString("latest_pred_title", data.latestPredictionTitle)
                putString("latest_pred_text", data.latestPredictionText)
                putLong("latest_pred_time", data.latestPredictionTimestamp)
                putString("current_hora", data.currentHora)
                putString("hora_symbol", data.horaSymbol)
                putString("current_muhurat", data.currentMuhurat)
                putString("current_muhurat_dev", data.currentMuhuratDevanagari)
                putBoolean("is_auspicious", data.isAuspicious)
                putString("muhurat_end", data.muhuratEndTime)
                putString("sunrise", data.sunrise)
                putString("sunset", data.sunset)
                putString("tithi", data.tithi)
                putString("tithi_dev", data.tithiDevanagari)
                putString("nakshatra", data.nakshatra)
                putString("rahu_start", data.rahuStart)
                putString("rahu_end", data.rahuEnd)
                putBoolean("is_rahu_active", data.isRahuActive)
                putBoolean("is_safe_mode", data.isSafeModeEnabled)
                putBoolean("is_low_power", data.isLowPowerModeActive)
                putBoolean("is_thermal_throttled", data.isThermalThrottled)
                putString("thermal_status_name", data.thermalStatusName)
                putString("top_comp", data.topComplicationType)
                putString("bottom_comp", data.bottomComplicationType)
                putString("left_comp", data.leftComplicationType)
                putString("right_comp", data.rightComplicationType)
                putInt("step_count", data.stepCount)
                putInt("step_goal", data.stepGoal)
                putString("weather_temp", data.weatherTemp)
                putString("weather_cond", data.weatherCondition)
                putString("weather_icon", data.weatherIcon)
                putInt("battery_percent", data.batteryPercent)
                putInt("heart_rate_bpm", data.heartRateBpm)
                putString("moon_phase", data.moonPhaseAssetId)
                putLong("last_sync_timestamp", data.lastSyncTimestamp)
                apply()
            }
        } catch (e: Exception) {
            android.util.Log.e("AstroDataStore", "Failed to save to SharedPreferences: ${e.message}")
        }
    }
}

