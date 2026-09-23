package com.example.generator.watchface

import com.example.core.model.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Geometric vector primitive for rendering high-precision procedural smartwatch dials.
 */
sealed class VectorDialElement {
    data class CircleRing(
        val radiusRatio: Float, // 0.0 to 1.0 of watch radius
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
        val isFilled: Boolean = false
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

    /**
     * Procedurally constructs lightweight vector dial definitions for the selected style.
     */
    fun generateDialModel(
        config: WatchFaceConfiguration,
        personalizedProfile: PersonalizedProfile,
        dailyProfile: DailyVedicProfile?
    ): ProceduralWatchFaceModel {
        val elements = mutableListOf<VectorDialElement>()
        val pColor = config.primaryColorHex
        val aColor = config.accentColorHex

        // True AMOLED black canvas for all faces to maximize OLED battery life
        val bgHex = 0xFF000000

        when (config.style) {
            WatchFaceStyle.COSMIC -> {
                // Outer celestial zodiac track
                elements.add(VectorDialElement.CircleRing(0.96f, 0xFF1E293B, 1.5f))
                elements.add(VectorDialElement.CircleRing(0.85f, 0xFF0F172A, 1.0f))
                elements.add(VectorDialElement.TickMarks(60, 0.90f, 0.95f, 0xFF334155, 1.0f))
                elements.add(VectorDialElement.TickMarks(12, 0.86f, 0.95f, pColor, 2.0f))

                // Concentric planetary orbits
                elements.add(VectorDialElement.CircleRing(0.68f, 0x33FFD166, 1.0f, isDashed = true))
                elements.add(VectorDialElement.CircleRing(0.52f, 0x2238BDF8, 1.0f, isDashed = true))
                elements.add(VectorDialElement.CircleRing(0.36f, 0x33F43F5E, 1.0f, isDashed = true))

                // Sun and Moon glyphs
                elements.add(VectorDialElement.GlyphMarker(270f, 0.72f, "☉", 0xFFFFD166, 14f))
                elements.add(VectorDialElement.GlyphMarker(90f, 0.54f, "☽", 0xFFE2E8F0, 13f))
                elements.add(VectorDialElement.GlyphMarker(0f, 0.38f, personalizedProfile.natalChandraRashi.symbol, aColor, 12f))
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
                // 4 Upward Shiva Triangles (Consciousness / Fire / Upward Ascension)
                val shiva1 = listOf(0.0f to -0.48f, 0.38f to 0.28f, -0.38f to 0.28f)
                val shiva2 = listOf(0.0f to -0.38f, 0.44f to 0.18f, -0.44f to 0.18f)
                val shiva3 = listOf(0.0f to -0.26f, 0.34f to 0.36f, -0.34f to 0.36f)
                val shiva4 = listOf(0.0f to -0.16f, 0.22f to 0.24f, -0.22f to 0.24f)

                // 5 Downward Shakti Triangles (Creative Energy / Water / Manifest Creation)
                val shakti1 = listOf(0.0f to 0.48f, 0.42f to -0.22f, -0.42f to -0.22f)
                val shakti2 = listOf(0.0f to 0.38f, 0.46f to -0.12f, -0.46f to -0.12f)
                val shakti3 = listOf(0.0f to 0.28f, 0.36f to -0.32f, -0.36f to -0.32f)
                val shakti4 = listOf(0.0f to 0.18f, 0.26f to -0.20f, -0.26f to -0.20f)
                val shakti5 = listOf(0.0f to 0.10f, 0.14f to -0.10f, -0.14f to -0.10f)

                // Render 9 Triangles in Sacred Vedic Gold & Ruby Accent
                elements.add(VectorDialElement.PolygonShape(shiva1, pColor, 1.6f))
                elements.add(VectorDialElement.PolygonShape(shakti1, aColor, 1.6f))
                elements.add(VectorDialElement.PolygonShape(shiva2, pColor, 1.5f))
                elements.add(VectorDialElement.PolygonShape(shakti2, aColor, 1.5f))
                elements.add(VectorDialElement.PolygonShape(shiva3, pColor, 1.4f))
                elements.add(VectorDialElement.PolygonShape(shakti3, aColor, 1.4f))
                elements.add(VectorDialElement.PolygonShape(shiva4, pColor, 1.3f))
                elements.add(VectorDialElement.PolygonShape(shakti4, aColor, 1.3f))
                elements.add(VectorDialElement.PolygonShape(shakti5, aColor, 1.5f))

                // 6. Central Bindu (Sarva Anandamayi Chakra - The Singularity of Supreme Bliss)
                elements.add(VectorDialElement.CircleRing(0.035f, 0xFFE63946, 2.5f))
                elements.add(VectorDialElement.CircleRing(0.015f, 0xFFFFD166, 2.0f))
            }

            WatchFaceStyle.CHAKRA -> {
                // 7-Chakra Concentric Energy rings
                val chakraRadii = listOf(0.95f, 0.85f, 0.73f, 0.61f, 0.49f, 0.37f, 0.25f)
                val chakraColors = listOf(
                    0xFFE63946, // Muladhara
                    0xFFF77F00, // Svadhisthana
                    0xFFFCBF49, // Manipura
                    0xFF2A9D8F, // Anahata
                    0xFF457B9D, // Vishuddha
                    0xFF3D348B, // Ajna
                    0xFFE0AAFF  // Sahasrara
                )
                for (i in chakraRadii.indices) {
                    elements.add(VectorDialElement.CircleRing(chakraRadii[i], chakraColors[i], 1.2f))
                }
                elements.add(VectorDialElement.TickMarks(12, 0.90f, 0.96f, 0xFFFFFFFF, 1.5f))
                elements.add(VectorDialElement.GlyphMarker(270f, 0.15f, "ॐ", 0xFFFFD166, 16f))
            }

            WatchFaceStyle.VASTU -> {
                // 8 Directional Compass Wheel
                elements.add(VectorDialElement.CircleRing(0.96f, pColor, 2.0f))
                elements.add(VectorDialElement.TickMarks(8, 0.84f, 0.95f, 0xFFFFB703, 3.0f))
                elements.add(VectorDialElement.TickMarks(32, 0.90f, 0.95f, 0x55FFFFFF, 1.0f))

                // Direction markers (N, NE, E, SE, S, SW, W, NW)
                val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
                val sanskritDirs = listOf("उ", "ई", "पू", "आ", "द", "नै", "प", "वा")
                for (i in 0 until 8) {
                    val angle = (i * 45f) - 90f
                    val label = if (config.showDevanagari) sanskritDirs[i] else dirs[i]
                    elements.add(VectorDialElement.GlyphMarker(angle, 0.76f, label, 0xFFE2E8F0, 11f))
                }
                // Central Quadrant Crosshair
                elements.add(VectorDialElement.CircleRing(0.35f, aColor, 1.0f, isDashed = true))
            }

            WatchFaceStyle.PANCHANGA -> {
                // Traditional Calendar Dial with Devanagari numerals
                elements.add(VectorDialElement.CircleRing(0.96f, 0xFFD4AF37, 2.5f))
                elements.add(VectorDialElement.CircleRing(0.82f, 0x44D4AF37, 1.0f))
                elements.add(VectorDialElement.TickMarks(60, 0.88f, 0.94f, 0x88D4AF37, 1.0f))
                elements.add(VectorDialElement.TickMarks(12, 0.84f, 0.95f, 0xFFD4AF37, 2.5f))

                // Devanagari Hour Numerals (१ to १२)
                val devanagariHours = listOf("१२", "१", "२", "३", "४", "५", "६", "७", "८", "९", "१०", "११")
                for (h in 0 until 12) {
                    val angle = (h * 30f) - 90f
                    elements.add(VectorDialElement.GlyphMarker(angle, 0.70f, devanagariHours[h], 0xFFFFE082, 12f))
                }
            }

            WatchFaceStyle.MINIMAL -> {
                // Contemporary Minimalist Dial
                elements.add(VectorDialElement.TickMarks(12, 0.90f, 0.96f, 0xFFFFFFFF, 1.5f))
                elements.add(VectorDialElement.CircleRing(0.60f, 0x1FFFFFFF, 1.0f))
                // Subtle personal birth number at 6 o'clock
                elements.add(VectorDialElement.GlyphMarker(90f, 0.45f, "#${config.personalizedBirthNumber}", pColor, 13f))
            }

            WatchFaceStyle.ROYAL -> {
                // Luxury Indian Royal Heritage Motifs
                elements.add(VectorDialElement.CircleRing(0.97f, 0xFFFFD700, 3.0f))
                elements.add(VectorDialElement.CircleRing(0.91f, 0xFFB8860B, 1.0f))
                elements.add(VectorDialElement.CircleRing(0.85f, 0x66FFD700, 1.0f, isDashed = true))
                elements.add(VectorDialElement.TickMarks(12, 0.82f, 0.93f, 0xFFFFD700, 2.5f))
                elements.add(VectorDialElement.TickMarks(60, 0.88f, 0.92f, 0x55FFD700, 1.0f))
                // Saffron & Crimson accents
                elements.add(VectorDialElement.CircleRing(0.40f, 0xFFDC2626, 1.5f))
                elements.add(VectorDialElement.GlyphMarker(270f, 0.62f, "॥ ॐ ॥", 0xFFFFD700, 12f))
            }

            WatchFaceStyle.MODERN -> {
                // Samsung Galaxy Active Modern Smartwatch
                elements.add(VectorDialElement.CircleRing(0.95f, 0x3338BDF8, 2.0f))
                elements.add(VectorDialElement.TickMarks(12, 0.88f, 0.95f, 0xFF38BDF8, 2.0f))
                // Dynamic active arc
                elements.add(VectorDialElement.ArcSegment(270f, 180f, 0.90f, aColor, 3.0f))
            }
        }

        // Add dynamic daily status indicators if available
        if (dailyProfile != null) {
            // Rahu Kalam warning ring arc (if currently in Rahu Kalam)
            val now = dailyProfile.currentZonedDateTime
            val isRahuActive = !now.isBefore(dailyProfile.rahuKalam.startTime) && now.isBefore(dailyProfile.rahuKalam.endTime)
            if (isRahuActive) {
                elements.add(VectorDialElement.CircleRing(0.99f, 0xFFEF4444, 3.0f)) // Red alert ring
            }
        }

        return ProceduralWatchFaceModel(
            style = config.style,
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
