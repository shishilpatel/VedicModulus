package com.example.engines.spiritual

import kotlin.math.cos
import kotlin.math.sin

data class ChakraInfo(
    val name: String,
    val sanskritName: String,
    val seedMantra: String,
    val petals: Int,
    val colorHex: Long,
    val meaning: String
)

data class SacredPolygon(
    val points: List<Pair<Float, Float>>, // Normalized (-1f..1f)
    val colorHex: Long,
    val strokeWidth: Float
)

class SpiritualDesignEngine {

    companion object {
        val CHAKRAS = listOf(
            ChakraInfo("Root", "Muladhara (मूलाधार)", "LAM", 4, 0xFFE63946, "Stability, Grounding, Vital Base"),
            ChakraInfo("Sacral", "Svadhisthana (स्वाधिष्ठान)", "VAM", 6, 0xFFF77F00, "Creativity, Flow, Transformation"),
            ChakraInfo("Solar Plexus", "Manipura (मणिपूर)", "RAM", 10, 0xFFFCBF49, "Willpower, Inner Radiance, Courage"),
            ChakraInfo("Heart", "Anahata (अनाहत)", "YAM", 12, 0xFF2A9D8F, "Unconditional Love, Harmony, Compassion"),
            ChakraInfo("Throat", "Vishuddha (विशुद्ध)", "HAM", 16, 0xFF457B9D, "Truth, Expression, Resonant Voice"),
            ChakraInfo("Third Eye", "Ajna (आज्ञा)", "OM", 2, 0xFF3D348B, "Intuition, Perception, Inner Light"),
            ChakraInfo("Crown", "Sahasrara (सहस्रार)", "AUM", 1000, 0xFFE0AAFF, "Cosmic Unity, Pure Consciousness")
        )
    }

    /**
     * Generates normalized points for concentric geometric star polygons (Yantra triangles & mandalas).
     */
    fun generateYantraGeometry(petals: Int, radiusScale: Float, rotationDeg: Float = 0f): SacredPolygon {
        val points = mutableListOf<Pair<Float, Float>>()
        val rotRad = Math.toRadians(rotationDeg.toDouble())
        val step = (2.0 * Math.PI) / petals

        for (i in 0 until petals) {
            val angle = i * step + rotRad
            val x = (radiusScale * cos(angle)).toFloat()
            val y = (radiusScale * sin(angle)).toFloat()
            points.add(x to y)
        }

        return SacredPolygon(
            points = points,
            colorHex = 0xFFFFD166,
            strokeWidth = 1.5f
        )
    }
}
