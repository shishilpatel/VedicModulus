package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.example.ui.theme.DarkText
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import com.example.core.model.VastuDirectionInfo
import com.example.engines.vastu.VastuEngine
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun VastuCompassCanvas(
    rotationAngle: Float,
    selectedDirection: VastuDirectionInfo,
    onDirectionSelected: (VastuDirectionInfo) -> Unit,
    onRotate: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val directions = remember { VastuEngine.ASHTA_DIK }
    
    val measuredLabels = remember(directions) {
        directions.associate { dir ->
            val label = when (dir.directionName) {
                "North" -> "N"
                "North-East" -> "NE"
                "East" -> "E"
                "South-East" -> "SE"
                "South" -> "S"
                "South-West" -> "SW"
                "West" -> "W"
                "North-West" -> "NW"
                else -> dir.directionName.take(2)
            }
            dir to textMeasurer.measure(
                text = AnnotatedString(label),
                style = TextStyle(fontSize = 13.sp)
            )
        }
    }

    val measuredSanskrit = remember(directions) {
        directions.associate { dir ->
            val sanskritShort = dir.sanskritName.substringBefore(" ").take(4)
            dir to textMeasurer.measure(
                text = AnnotatedString(sanskritShort),
                style = TextStyle(fontSize = 9.sp)
            )
        }
    }

    val omMeasured = remember {
        textMeasurer.measure(
            text = AnnotatedString("ॐ"),
            style = TextStyle(fontSize = 20.sp)
        )
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    val vastuEngine = VastuEngine()
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Drag horizontally rotates the compass
                        onRotate(rotationAngle + dragAmount.x * 0.4f)
                    }
                }
                .pointerInput(Unit) {
                    val vastuEngine = VastuEngine()
                    detectTapGestures { offset ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = offset.x - cx
                        val dy = offset.y - cy
                        // Calculate angle in degrees from North (0° pointing up)
                        var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 90.0
                        if (angleDeg < 0) angleDeg += 360.0
                        // Account for compass rotation
                        val effectiveAngle = (angleDeg - rotationAngle + 360.0) % 360.0
                        val dir = vastuEngine.getDirectionByAngle(effectiveAngle)
                        onDirectionSelected(dir)
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = min(cx, cy) * 0.92f

            // Outer dial rim
            drawCircle(
                color = LightBorder,
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 3f)
            )

            drawCircle(
                color = OffWhiteSurface.copy(alpha = 0.6f),
                radius = radius,
                center = Offset(cx, cy),
                style = Fill
            )

            // Inner subtle concentric circles
            drawCircle(
                color = LightBorder.copy(alpha = 0.5f),
                radius = radius * 0.65f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )

            drawCircle(
                color = LightBorder.copy(alpha = 0.5f),
                radius = radius * 0.35f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )

            // Draw 8 Ashta Dik segments with sector rays
            directions.forEach { dir ->
                // Direction angle relative to compass rotation
                // In standard math: 0 deg North is at angle -90 deg from positive x axis
                val dirAngleRad = Math.toRadians(dir.degrees - 90.0 + rotationAngle)
                val x1 = cx + (radius * 0.35f) * cos(dirAngleRad).toFloat()
                val y1 = cy + (radius * 0.35f) * sin(dirAngleRad).toFloat()
                val x2 = cx + radius * cos(dirAngleRad).toFloat()
                val y2 = cy + radius * sin(dirAngleRad).toFloat()

                // Radial divider ray
                val rayAngleRad = Math.toRadians(dir.degrees - 90.0 + 22.5 + rotationAngle)
                val rx2 = cx + radius * cos(rayAngleRad).toFloat()
                val ry2 = cy + radius * sin(rayAngleRad).toFloat()
                drawLine(
                    color = LightBorder.copy(alpha = 0.6f),
                    start = Offset(cx, cy),
                    end = Offset(rx2, ry2),
                    strokeWidth = 1f
                )

                // Highlight segment if selected
                val isSelected = dir.directionName == selectedDirection.directionName
                if (isSelected) {
                    val highlightPath = Path().apply {
                        moveTo(cx, cy)
                        val startRad = Math.toRadians(dir.degrees - 90.0 - 22.5 + rotationAngle)
                        val endRad = Math.toRadians(dir.degrees - 90.0 + 22.5 + rotationAngle)
                        arcTo(
                            rect = androidx.compose.ui.geometry.Rect(cx - radius, cy - radius, cx + radius, cy + radius),
                            startAngleDegrees = (dir.degrees.toFloat() - 90f - 22.5f + rotationAngle),
                            sweepAngleDegrees = 45f,
                            forceMoveTo = false
                        )
                        close()
                    }
                    drawPath(
                        path = highlightPath,
                        color = MaroonPrimary.copy(alpha = 0.22f)
                    )
                }

                // Direction label (e.g. "NE", "E", "Ishanya")
                val textRadius = radius * 0.80f
                val tx = cx + textRadius * cos(dirAngleRad).toFloat()
                val ty = cy + textRadius * sin(dirAngleRad).toFloat()

                val measured = measuredLabels[dir]
                if (measured != null) {
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(tx - measured.size.width / 2f, ty - measured.size.height / 2f),
                        color = if (isSelected) MaroonPrimary else DarkText
                    )
                }

                // Sanskrit short name below label
                val subRadius = radius * 0.52f
                val sx = cx + subRadius * cos(dirAngleRad).toFloat()
                val sy = cy + subRadius * sin(dirAngleRad).toFloat()
                val subMeasured = measuredSanskrit[dir]
                if (subMeasured != null) {
                    drawText(
                        textLayoutResult = subMeasured,
                        topLeft = Offset(sx - subMeasured.size.width / 2f, sy - subMeasured.size.height / 2f),
                        color = if (isSelected) OrangeAccent else GrayText
                    )
                }
            }

            // Brahmasthana (Center Sacred Space)
            drawCircle(
                color = WhiteBackground,
                radius = radius * 0.22f,
                center = Offset(cx, cy),
                style = Fill
            )
            drawCircle(
                color = MaroonPrimary,
                radius = radius * 0.22f,
                center = Offset(cx, cy),
                style = Stroke(width = 2f)
            )

            drawText(
                textLayoutResult = omMeasured,
                topLeft = Offset(cx - omMeasured.size.width / 2f, cy - omMeasured.size.height / 2f),
                color = MaroonPrimary
            )

            // Top North pointer arrow (Indicates current facing direction)
            val needlePath = Path().apply {
                moveTo(cx, cy - radius * 0.35f)
                lineTo(cx - 10f, cy - radius * 0.95f)
                lineTo(cx, cy - radius * 1.05f)
                lineTo(cx + 10f, cy - radius * 0.95f)
                close()
            }
            drawPath(
                path = needlePath,
                color = InauspiciousRed,
                style = Fill
            )
        }
    }
}
