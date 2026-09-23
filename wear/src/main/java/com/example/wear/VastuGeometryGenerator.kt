package com.example.wear

import kotlin.math.cos
import kotlin.math.sin

object VastuGeometryGenerator {
    
    /**
     * Generates a 9x9 Vastu Purusha Mandala (Paramasayika Mandala).
     * @param radius The radius of the watch face.
     * @param rotationDegrees Rotation based on numerology/birth details.
     * @param primaryColor Color of the primary grid lines.
     * @param accentColor Color of the accented Brahmasthan (center).
     */
    fun generate9x9Mandala(
        radius: Float,
        rotationDegrees: Float,
        primaryColor: Long,
        accentColor: Long
    ): List<VectorDialElement> {
        val elements = mutableListOf<VectorDialElement>()
        val rotationRad = Math.toRadians(rotationDegrees.toDouble()).toFloat()
        
        // Define the outer boundary of the grid
        val gridRadius = 0.85f // Fit within the circular dial
        val squareSize = gridRadius * 2
        val cellSize = squareSize / 9f
        
        // Helper to rotate points
        fun rotatePoint(x: Float, y: Float): Pair<Float, Float> {
            val rx = x * cos(rotationRad) - y * sin(rotationRad)
            val ry = x * sin(rotationRad) + y * cos(rotationRad)
            return Pair(rx, ry)
        }

        // Draw 10 vertical and 10 horizontal lines to form the 9x9 grid
        for (i in 0..9) {
            // Vertical lines
            val vx = -gridRadius + (i * cellSize)
            val vp1 = rotatePoint(vx, -gridRadius)
            val vp2 = rotatePoint(vx, gridRadius)
            elements.add(VectorDialElement.PolygonShape(
                listOf(vp1, vp2),
                primaryColor,
                if (i == 0 || i == 9) 2.0f else 0.5f,
                isFilled = false
            ))
            
            // Horizontal lines
            val hy = -gridRadius + (i * cellSize)
            val hp1 = rotatePoint(-gridRadius, hy)
            val hp2 = rotatePoint(gridRadius, hy)
            elements.add(VectorDialElement.PolygonShape(
                listOf(hp1, hp2),
                primaryColor,
                if (i == 0 || i == 9) 2.0f else 0.5f,
                isFilled = false
            ))
        }

        // Highlight Brahmasthan (central 3x3 grid)
        val brahmaStart = -gridRadius + (3 * cellSize)
        val brahmaEnd = -gridRadius + (6 * cellSize)
        
        val bp1 = rotatePoint(brahmaStart, brahmaStart)
        val bp2 = rotatePoint(brahmaEnd, brahmaStart)
        val bp3 = rotatePoint(brahmaEnd, brahmaEnd)
        val bp4 = rotatePoint(brahmaStart, brahmaEnd)
        
        elements.add(VectorDialElement.PolygonShape(
            listOf(bp1, bp2, bp3, bp4),
            accentColor,
            2.0f,
            isFilled = false
        ))
        
        // Add Golden Ratio based concentric circles
        val goldenRatio = 1.6180339887f
        elements.add(VectorDialElement.CircleRing(
            radiusRatio = gridRadius / goldenRatio,
            colorHex = accentColor,
            strokeWidthDp = 1.0f,
            isDashed = true
        ))

        return elements
    }
}
