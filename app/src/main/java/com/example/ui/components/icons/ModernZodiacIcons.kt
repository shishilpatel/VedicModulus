package com.example.ui.components.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.model.Rashi

/**
 * Modern, high-craft realistic vector icons for all 12 Zodiac signs (द्वादश राशि).
 * Rendered using precision vector paths and gradients with high-DPI scaling.
 */
@Composable
fun ModernZodiacIcon(
    rashi: Rashi,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.Unspecified,
    isRealistic: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "${rashi.westernName} (${rashi.sanskritName})" }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRealisticZodiac(
                rashi = rashi,
                tint = tint,
                isRealistic = isRealistic
            )
        }
    }
}

private fun DrawScope.drawRealisticZodiac(
    rashi: Rashi,
    tint: Color,
    isRealistic: Boolean
) {
    val w = size.width
    val h = size.height

    fun pX(fraction: Float): Float = fraction * w
    fun pY(fraction: Float): Float = fraction * h

    val (primaryColor, accentColor) = if (tint != Color.Unspecified) {
        tint to tint.copy(alpha = 0.75f)
    } else when (rashi) {
        Rashi.MESHA -> Color(0xFFD97706) to Color(0xFFDC2626) // Aries: Fire Gold & Crimson
        Rashi.VRISHABHA -> Color(0xFF059669) to Color(0xFFD4AF37) // Taurus: Earth Emerald & Gold
        Rashi.MITHUNA -> Color(0xFF0284C7) to Color(0xFF38BDF8) // Gemini: Celestial Sky Blue
        Rashi.KARKATA -> Color(0xFF06B6D4) to Color(0xFFE2E8F0) // Cancer: Lunar Cyan & Silver
        Rashi.SIMHA -> Color(0xFFEA580C) to Color(0xFFF59E0B) // Leo: Sun Amber & Gold
        Rashi.KANYA -> Color(0xFF10B981) to Color(0xFFFBBF24) // Virgo: Earth Jade & Wheat
        Rashi.TULA -> Color(0xFFD4AF37) to Color(0xFF6366F1) // Libra: Golden Balance & Indigo
        Rashi.VRISHCHIKA -> Color(0xFFB91C1C) to Color(0xFF991B1B) // Scorpio: Deep Ruby & Crimson
        Rashi.DHANUS -> Color(0xFFF97316) to Color(0xFFFACC15) // Sagittarius: Saffron Arrow
        Rashi.MAKARA -> Color(0xFF475569) to Color(0xFF92400E) // Capricorn: Deep Slate & Bronze
        Rashi.KUMBHA -> Color(0xFF0EA5E9) to Color(0xFF6366F1) // Aquarius: Flowing Electric Blue
        Rashi.MEENA -> Color(0xFF8B5CF6) to Color(0xFF14B8A6) // Pisces: Mystic Violet & Sea Teal
    }

    val strokeWidth = (w * 0.075f).coerceAtLeast(1.5f)
    val strokeStyle = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (rashi) {
        Rashi.MESHA -> {
            // Aries: Realistic curved Ram Horns with crown apex
            val path = Path().apply {
                // Left Horn spiral
                moveTo(pX(0.50f), pY(0.82f))
                cubicTo(pX(0.48f), pY(0.45f), pX(0.35f), pY(0.20f), pX(0.20f), pY(0.20f))
                cubicTo(pX(0.08f), pY(0.20f), pX(0.08f), pY(0.42f), pX(0.24f), pY(0.44f))

                // Right Horn spiral
                moveTo(pX(0.50f), pY(0.82f))
                cubicTo(pX(0.52f), pY(0.45f), pX(0.65f), pY(0.20f), pX(0.80f), pY(0.20f))
                cubicTo(pX(0.92f), pY(0.20f), pX(0.92f), pY(0.42f), pX(0.76f), pY(0.44f))

                // Center crown ridge
                moveTo(pX(0.50f), pY(0.82f))
                lineTo(pX(0.50f), pY(0.40f))
            }
            drawPath(path, primaryColor, style = strokeStyle)
            drawCircle(accentColor, radius = w * 0.08f, center = Offset(pX(0.50f), pY(0.24f)))
        }

        Rashi.VRISHABHA -> {
            // Taurus: The Mighty Bull with sweeping crescent horns and solar disk
            val headPath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(pX(0.24f), pY(0.44f), pX(0.76f), pY(0.92f)))
            }
            drawPath(headPath, primaryColor, style = strokeStyle)

            val horns = Path().apply {
                moveTo(pX(0.12f), pY(0.18f))
                cubicTo(pX(0.22f), pY(0.42f), pX(0.35f), pY(0.50f), pX(0.50f), pY(0.50f))
                cubicTo(pX(0.65f), pY(0.50f), pX(0.78f), pY(0.42f), pX(0.88f), pY(0.18f))
            }
            drawPath(horns, accentColor, style = strokeStyle)
            drawCircle(accentColor, radius = w * 0.06f, center = Offset(pX(0.50f), pY(0.68f)))
        }

        Rashi.MITHUNA -> {
            // Gemini: Twin pillars crowned with cosmic celestial arches
            val path = Path().apply {
                // Top arch
                moveTo(pX(0.18f), pY(0.18f))
                quadraticBezierTo(pX(0.50f), pY(0.28f), pX(0.82f), pY(0.18f))

                // Left twin pillar
                moveTo(pX(0.35f), pY(0.22f))
                lineTo(pX(0.35f), pY(0.78f))

                // Right twin pillar
                moveTo(pX(0.65f), pY(0.22f))
                lineTo(pX(0.65f), pY(0.78f))

                // Bottom arch
                moveTo(pX(0.18f), pY(0.82f))
                quadraticBezierTo(pX(0.50f), pY(0.72f), pX(0.82f), pY(0.82f))
            }
            drawPath(path, primaryColor, style = strokeStyle)
            // Twin star diadems
            drawCircle(accentColor, radius = w * 0.05f, center = Offset(pX(0.35f), pY(0.50f)))
            drawCircle(accentColor, radius = w * 0.05f, center = Offset(pX(0.65f), pY(0.50f)))
        }

        Rashi.KARKATA -> {
            // Cancer: Sculpted interlocking Crab Pincers
            val leftClaw = Path().apply {
                moveTo(pX(0.44f), pY(0.38f))
                cubicTo(pX(0.24f), pY(0.18f), pX(0.08f), pY(0.34f), pX(0.24f), pY(0.50f))
                cubicTo(pX(0.40f), pY(0.62f), pX(0.68f), pY(0.38f), pX(0.84f), pY(0.38f))
            }
            val rightClaw = Path().apply {
                moveTo(pX(0.56f), pY(0.62f))
                cubicTo(pX(0.76f), pY(0.82f), pX(0.92f), pY(0.66f), pX(0.76f), pY(0.50f))
                cubicTo(pX(0.60f), pY(0.38f), pX(0.32f), pY(0.62f), pX(0.16f), pY(0.62f))
            }
            drawPath(leftClaw, primaryColor, style = strokeStyle)
            drawPath(rightClaw, accentColor, style = strokeStyle)
            drawCircle(primaryColor, radius = w * 0.06f, center = Offset(pX(0.24f), pY(0.38f)))
            drawCircle(accentColor, radius = w * 0.06f, center = Offset(pX(0.76f), pY(0.62f)))
        }

        Rashi.SIMHA -> {
            // Leo: Majestic Lion Mane with solar crest & sweeping tail
            val mane = Path().apply {
                moveTo(pX(0.24f), pY(0.70f))
                cubicTo(pX(0.10f), pY(0.52f), pX(0.24f), pY(0.24f), pX(0.46f), pY(0.24f))
                cubicTo(pX(0.68f), pY(0.24f), pX(0.74f), pY(0.42f), pX(0.68f), pY(0.58f))
                cubicTo(pX(0.62f), pY(0.72f), pX(0.75f), pY(0.84f), pX(0.88f), pY(0.78f))
            }
            drawPath(mane, primaryColor, style = strokeStyle)
            // Lion radiant sun badge
            drawCircle(accentColor, radius = w * 0.09f, center = Offset(pX(0.38f), pY(0.42f)))
        }

        Rashi.KANYA -> {
            // Virgo: The Maiden with Wheat Sheaf and angelic wing loop
            val maiden = Path().apply {
                // Triple fold maiden monogram
                moveTo(pX(0.18f), pY(0.78f))
                lineTo(pX(0.18f), pY(0.28f))
                cubicTo(pX(0.26f), pY(0.22f), pX(0.38f), pY(0.22f), pX(0.42f), pY(0.36f))
                lineTo(pX(0.42f), pY(0.78f))

                moveTo(pX(0.42f), pY(0.36f))
                cubicTo(pX(0.50f), pY(0.22f), pX(0.62f), pY(0.22f), pX(0.66f), pY(0.36f))
                lineTo(pX(0.66f), pY(0.78f))

                // Wing & grain flourish
                moveTo(pX(0.66f), pY(0.46f))
                cubicTo(pX(0.82f), pY(0.32f), pX(0.92f), pY(0.50f), pX(0.74f), pY(0.75f))
                lineTo(pX(0.88f), pY(0.88f))
            }
            drawPath(maiden, primaryColor, style = strokeStyle)
            drawCircle(accentColor, radius = w * 0.05f, center = Offset(pX(0.78f), pY(0.48f)))
        }

        Rashi.TULA -> {
            // Libra: Balanced Celestial Beam Scales with suspended pans
            val beam = Path().apply {
                // Upper balance fulcrum crest
                moveTo(pX(0.22f), pY(0.46f))
                lineTo(pX(0.38f), pY(0.46f))
                cubicTo(pX(0.38f), pY(0.24f), pX(0.62f), pY(0.24f), pX(0.62f), pY(0.46f))
                lineTo(pX(0.78f), pY(0.46f))

                // Lower base plate
                moveTo(pX(0.15f), pY(0.72f))
                lineTo(pX(0.85f), pY(0.72f))
            }
            drawPath(beam, primaryColor, style = strokeStyle)
            // Left pan suspension
            drawLine(accentColor, Offset(pX(0.28f), pY(0.46f)), Offset(pX(0.20f), pY(0.62f)), strokeWidth = strokeWidth * 0.7f)
            drawLine(accentColor, Offset(pX(0.28f), pY(0.46f)), Offset(pX(0.36f), pY(0.62f)), strokeWidth = strokeWidth * 0.7f)
            // Right pan suspension
            drawLine(accentColor, Offset(pX(0.72f), pY(0.46f)), Offset(pX(0.64f), pY(0.62f)), strokeWidth = strokeWidth * 0.7f)
            drawLine(accentColor, Offset(pX(0.72f), pY(0.46f)), Offset(pX(0.80f), pY(0.62f)), strokeWidth = strokeWidth * 0.7f)
        }

        Rashi.VRISHCHIKA -> {
            // Scorpio: Articulated Scorpion with Venom Stinger Barb
            val scorpion = Path().apply {
                moveTo(pX(0.18f), pY(0.74f))
                lineTo(pX(0.18f), pY(0.28f))
                cubicTo(pX(0.26f), pY(0.22f), pX(0.38f), pY(0.22f), pX(0.42f), pY(0.36f))
                lineTo(pX(0.42f), pY(0.74f))

                moveTo(pX(0.42f), pY(0.36f))
                cubicTo(pX(0.50f), pY(0.22f), pX(0.62f), pY(0.22f), pX(0.66f), pY(0.36f))
                lineTo(pX(0.66f), pY(0.74f))

                // High-arching Stinger tail
                moveTo(pX(0.66f), pY(0.52f))
                cubicTo(pX(0.78f), pY(0.44f), pX(0.88f), pY(0.52f), pX(0.86f), pY(0.70f))
                // Pointed barb arrow
                lineTo(pX(0.92f), pY(0.64f))
            }
            drawPath(scorpion, primaryColor, style = strokeStyle)
            // Arrow stinger barb
            val arrow = Path().apply {
                moveTo(pX(0.86f), pY(0.70f))
                lineTo(pX(0.96f), pY(0.72f))
                lineTo(pX(0.92f), pY(0.60f))
            }
            drawPath(arrow, accentColor, style = strokeStyle)
        }

        Rashi.DHANUS -> {
            // Sagittarius: Taut Recurve Archer Bow & Golden Arrow
            val arrowPath = Path().apply {
                // Diagonal Arrow Shaft
                moveTo(pX(0.24f), pY(0.76f))
                lineTo(pX(0.78f), pY(0.22f))

                // Arrow Head
                moveTo(pX(0.56f), pY(0.22f))
                lineTo(pX(0.78f), pY(0.22f))
                lineTo(pX(0.78f), pY(0.44f))

                // Bowstring crossbar
                moveTo(pX(0.32f), pY(0.52f))
                lineTo(pX(0.48f), pY(0.68f))
            }
            drawPath(arrowPath, primaryColor, style = strokeStyle)

            // Recurved Bow Arc
            val bowPath = Path().apply {
                moveTo(pX(0.20f), pY(0.40f))
                cubicTo(pX(0.36f), pY(0.56f), pX(0.44f), pY(0.64f), pX(0.60f), pY(0.80f))
            }
            drawPath(bowPath, accentColor, style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        Rashi.MAKARA -> {
            // Capricorn: Mythic Horned Sea-Goat with twisting aquatic tail
            val goat = Path().apply {
                // Horn & Head loop
                moveTo(pX(0.18f), pY(0.28f))
                lineTo(pX(0.34f), pY(0.76f))
                lineTo(pX(0.50f), pY(0.34f))
                // Downward fish tail loop
                cubicTo(pX(0.62f), pY(0.26f), pX(0.76f), pY(0.36f), pX(0.74f), pY(0.58f))
                cubicTo(pX(0.72f), pY(0.78f), pX(0.54f), pY(0.88f), pX(0.54f), pY(0.68f))
                cubicTo(pX(0.54f), pY(0.52f), pX(0.84f), pY(0.52f), pX(0.84f), pY(0.72f))
            }
            drawPath(goat, primaryColor, style = strokeStyle)
            drawCircle(accentColor, radius = w * 0.06f, center = Offset(pX(0.34f), pY(0.76f)))
        }

        Rashi.KUMBHA -> {
            // Aquarius: Dual Cascading Serpentine Waves of Sacred Water
            val wave1 = Path().apply {
                moveTo(pX(0.14f), pY(0.38f))
                lineTo(pX(0.32f), pY(0.24f))
                lineTo(pX(0.50f), pY(0.38f))
                lineTo(pX(0.68f), pY(0.24f))
                lineTo(pX(0.86f), pY(0.38f))
            }
            val wave2 = Path().apply {
                moveTo(pX(0.14f), pY(0.68f))
                lineTo(pX(0.32f), pY(0.54f))
                lineTo(pX(0.50f), pY(0.68f))
                lineTo(pX(0.68f), pY(0.54f))
                lineTo(pX(0.86f), pY(0.68f))
            }
            drawPath(wave1, primaryColor, style = strokeStyle)
            drawPath(wave2, accentColor, style = strokeStyle)
            // Water droplets
            drawCircle(primaryColor, radius = w * 0.04f, center = Offset(pX(0.50f), pY(0.16f)))
            drawCircle(accentColor, radius = w * 0.04f, center = Offset(pX(0.50f), pY(0.84f)))
        }

        Rashi.MEENA -> {
            // Pisces: Two Intertwined Golden Fish linked by a sacred cord
            val fishLeft = Path().apply {
                moveTo(pX(0.34f), pY(0.18f))
                cubicTo(pX(0.18f), pY(0.36f), pX(0.18f), pY(0.64f), pX(0.34f), pY(0.82f))
            }
            val fishRight = Path().apply {
                moveTo(pX(0.66f), pY(0.18f))
                cubicTo(pX(0.82f), pY(0.36f), pX(0.82f), pY(0.64f), pX(0.66f), pY(0.82f))
            }
            val cord = Path().apply {
                moveTo(pX(0.14f), pY(0.50f))
                lineTo(pX(0.86f), pY(0.50f))
            }
            drawPath(fishLeft, primaryColor, style = strokeStyle)
            drawPath(fishRight, accentColor, style = strokeStyle)
            drawPath(cord, primaryColor, style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            // Fish eye beads
            drawCircle(primaryColor, radius = w * 0.05f, center = Offset(pX(0.24f), pY(0.50f)))
            drawCircle(accentColor, radius = w * 0.05f, center = Offset(pX(0.76f), pY(0.50f)))
        }
    }
}
