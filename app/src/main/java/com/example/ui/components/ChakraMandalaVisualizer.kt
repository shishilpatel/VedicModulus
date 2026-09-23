package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.chakra.ChakraType
import com.example.ui.theme.GoldPrimary
import kotlin.math.*

@Composable
fun ChakraMandalaVisualizer(
    chakra: ChakraType,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    breatheProgress: Float? = null // Optional external breathing progress
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ChakraMandalaAnim")

    // Slow, serene rotation (60s for full circle) - peaceful, no rapid motion
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SlowMandalaRotation"
    )

    // Gentle ambient pulse (4s cycle)
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GentleChakraPulse"
    )

    val activeScale = breatheProgress?.let { 0.85f + (it * 0.3f) } ?: if (isPlaying) ambientPulse else 1.0f
    val textMeasurer = rememberTextMeasurer()

    val chakraColor = chakra.composeColor

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp)
            .height(300.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = min(size.width, size.height) / 2f * 0.88f
        val pulseRadius = maxRadius * activeScale

        // 1. Subtle Outer Cosmic Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    chakraColor.copy(alpha = 0.35f),
                    chakraColor.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = center,
                radius = pulseRadius * 1.25f
            ),
            radius = pulseRadius * 1.25f,
            center = center
        )

        // 2. Outer Sacred Ring with Gold accents
        drawCircle(
            color = GoldPrimary.copy(alpha = 0.6f),
            radius = pulseRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = chakraColor.copy(alpha = 0.4f),
            radius = pulseRadius * 0.95f,
            center = center,
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        )

        // 3. Petals (Traditional geometry matching petal count)
        val petalCount = if (chakra.petals > 32) 32 else chakra.petals
        val petalBaseRadius = pulseRadius * 0.72f
        val petalTipRadius = pulseRadius * 0.92f
        val angleStep = (2 * PI / petalCount).toFloat()

        for (i in 0 until petalCount) {
            val petalAngle = (i * angleStep) + (rotationAngle * PI.toFloat() / 180f)
            val tipX = center.x + petalTipRadius * cos(petalAngle)
            val tipY = center.y + petalTipRadius * sin(petalAngle)

            val leftAngle = petalAngle - (angleStep * 0.35f)
            val rightAngle = petalAngle + (angleStep * 0.35f)
            val leftX = center.x + petalBaseRadius * cos(leftAngle)
            val leftY = center.y + petalBaseRadius * sin(leftAngle)
            val rightX = center.x + petalBaseRadius * cos(rightAngle)
            val rightY = center.y + petalBaseRadius * sin(rightAngle)

            val petalPath = Path().apply {
                moveTo(leftX, leftY)
                quadraticTo(
                    center.x + pulseRadius * 0.85f * cos(petalAngle - 0.1f),
                    center.y + pulseRadius * 0.85f * sin(petalAngle - 0.1f),
                    tipX, tipY
                )
                quadraticTo(
                    center.x + pulseRadius * 0.85f * cos(petalAngle + 0.1f),
                    center.y + pulseRadius * 0.85f * sin(petalAngle + 0.1f),
                    rightX, rightY
                )
                close()
            }

            drawPath(
                path = petalPath,
                brush = Brush.radialGradient(
                    colors = listOf(chakraColor.copy(alpha = 0.7f), chakraColor.copy(alpha = 0.25f)),
                    center = center,
                    radius = pulseRadius
                )
            )
            drawPath(
                path = petalPath,
                color = GoldPrimary.copy(alpha = 0.45f),
                style = Stroke(width = 1.2.dp.toPx())
            )
        }

        // 4. Inner Geometric Core based on traditional element
        val innerRadius = pulseRadius * 0.52f
        drawInnerTattvaGeometry(chakra, center, innerRadius, chakraColor)

        // 5. Central Hub with Golden Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1A1A24),
                    Color(0xFF0F0F16)
                ),
                center = center,
                radius = innerRadius * 0.65f
            ),
            radius = innerRadius * 0.65f,
            center = center
        )
        drawCircle(
            color = GoldPrimary,
            radius = innerRadius * 0.65f,
            center = center,
            style = Stroke(width = 1.8.dp.toPx())
        )

        // 6. Center Bija Syllable in Devanagari
        val bijaText = chakra.bijaMantra
        val bijaLayout = textMeasurer.measure(
            text = bijaText,
            style = TextStyle(
                color = GoldPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )
        drawText(
            textLayoutResult = bijaLayout,
            topLeft = Offset(
                center.x - bijaLayout.size.width / 2f,
                center.y - bijaLayout.size.height / 2f
            )
        )
    }
}

private fun DrawScope.drawInnerTattvaGeometry(
    chakra: ChakraType,
    center: Offset,
    radius: Float,
    chakraColor: Color
) {
    when (chakra) {
        ChakraType.MULADHARA -> {
            // Earth: Yellow Prithvi Square (Chaturasra)
            val halfSide = radius * 0.7f
            val squarePath = Path().apply {
                moveTo(center.x - halfSide, center.y - halfSide)
                lineTo(center.x + halfSide, center.y - halfSide)
                lineTo(center.x + halfSide, center.y + halfSide)
                lineTo(center.x - halfSide, center.y + halfSide)
                close()
            }
            drawPath(path = squarePath, color = Color(0xFFFDD835).copy(alpha = 0.35f))
            drawPath(path = squarePath, color = GoldPrimary, style = Stroke(width = 1.5.dp.toPx()))
        }
        ChakraType.SVADHISTHANA -> {
            // Water: Silver Crescent Moon (Ardha-Chandra)
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = radius * 0.75f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        ChakraType.MANIPURA -> {
            // Fire: Inverted Red/Gold Triangle (Trikona)
            val triPath = Path().apply {
                val r = radius * 0.8f
                moveTo(center.x, center.y + r) // Pointing down
                lineTo(center.x - r * 0.866f, center.y - r * 0.5f)
                lineTo(center.x + r * 0.866f, center.y - r * 0.5f)
                close()
            }
            drawPath(path = triPath, color = Color(0xFFE53935).copy(alpha = 0.3f))
            drawPath(path = triPath, color = GoldPrimary, style = Stroke(width = 1.5.dp.toPx()))
        }
        ChakraType.ANAHATA -> {
            // Air: Hexagram (Shatkona - Two interlocking triangles)
            val r = radius * 0.75f
            val upTri = Path().apply {
                moveTo(center.x, center.y - r)
                lineTo(center.x - r * 0.866f, center.y + r * 0.5f)
                lineTo(center.x + r * 0.866f, center.y + r * 0.5f)
                close()
            }
            val downTri = Path().apply {
                moveTo(center.x, center.y + r)
                lineTo(center.x - r * 0.866f, center.y - r * 0.5f)
                lineTo(center.x + r * 0.866f, center.y - r * 0.5f)
                close()
            }
            drawPath(path = upTri, color = Color(0xFF43A047).copy(alpha = 0.25f))
            drawPath(path = upTri, color = GoldPrimary.copy(alpha = 0.7f), style = Stroke(width = 1.2.dp.toPx()))
            drawPath(path = downTri, color = Color(0xFF43A047).copy(alpha = 0.25f))
            drawPath(path = downTri, color = GoldPrimary.copy(alpha = 0.7f), style = Stroke(width = 1.2.dp.toPx()))
        }
        ChakraType.VISHUDDHA -> {
            // Space: Pure White/Sky Circle (Vritta)
            drawCircle(
                color = Color(0xFF00ACC1).copy(alpha = 0.3f),
                radius = radius * 0.75f,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = radius * 0.75f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
        ChakraType.AJNA -> {
            // Mind/Intuition: Winged 2-petaled Circle
            drawCircle(
                color = Color(0xFF3949AB).copy(alpha = 0.35f),
                radius = radius * 0.7f,
                center = center
            )
            drawCircle(
                color = GoldPrimary,
                radius = radius * 0.7f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
        ChakraType.SAHASRARA -> {
            // Crown: Concentric Radiance
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8E24AA).copy(alpha = 0.5f), Color.White.copy(alpha = 0.2f), Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = radius * 0.8f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}
