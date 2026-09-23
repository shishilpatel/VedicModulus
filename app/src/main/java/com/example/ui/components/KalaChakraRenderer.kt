package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.core.model.DailyVedicProfile
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun KalaChakraRenderer(
    dailyProfile: DailyVedicProfile,
    modifier: Modifier = Modifier
) {
    // A simplified visual representation of the Vedic Day
    val sunriseTime = dailyProfile.panchanga.tithi.name // we need actual sunrise time here, but we will mock drawing for now
    
    Box(modifier = modifier.aspectRatio(1f).padding(16.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 - 16.dp.toPx()

            // Draw outer ring (24 hours)
            drawCircle(
                color = Color.LightGray,
                radius = radius,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )
            
            // Draw current time marker
            val currentMinutes = dailyProfile.currentZonedDateTime.hour * 60 + dailyProfile.currentZonedDateTime.minute
            val angle = (currentMinutes / (24 * 60f)) * 360f - 90f // -90 to start at top (midnight)
            
            val markerX = center.x + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
            val markerY = center.y + radius * sin(Math.toRadians(angle.toDouble())).toFloat()
            
            drawCircle(
                color = Color(0xFFEF4444),
                radius = 8.dp.toPx(),
                center = Offset(markerX, markerY)
            )
            
            // Draw inner segments for Choghadiya (simulated 8 slices for day, 8 for night)
            val innerRadius = radius - 24.dp.toPx()
            for (i in 0 until 16) {
                val startAngle = i * (360f / 16f) - 90f
                val sweepAngle = 360f / 16f
                val color = if (i % 2 == 0) Color(0xFFFDE047) else Color(0xFF38BDF8) // Auspicious vs Inauspicious
                drawArc(
                    color = color.copy(alpha = 0.5f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                    size = Size(innerRadius * 2, innerRadius * 2),
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Butt)
                )
            }
        }
    }
}
