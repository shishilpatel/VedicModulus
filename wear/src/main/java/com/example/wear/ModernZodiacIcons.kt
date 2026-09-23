package com.example.wear

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modern, realistic vector icons for Wear OS representing the 12 Vedic Rashis.
 */
@Composable
fun WearZodiacIcon(
    signName: String,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = Color(0xFFFFD54F)
) {
    val signKey = signName.lowercase().trim()
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = this.size.width
            val h = this.size.height
            fun pX(f: Float) = f * w
            fun pY(f: Float) = f * h

            val strokeWidth = (w * 0.08f).coerceAtLeast(1.2f)
            val strokeStyle = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

            when {
                signKey.startsWith("mesh") || signKey.startsWith("arie") -> {
                    val p = Path().apply {
                        moveTo(pX(0.50f), pY(0.82f))
                        cubicTo(pX(0.48f), pY(0.45f), pX(0.35f), pY(0.20f), pX(0.20f), pY(0.20f))
                        cubicTo(pX(0.08f), pY(0.20f), pX(0.08f), pY(0.42f), pX(0.24f), pY(0.44f))

                        moveTo(pX(0.50f), pY(0.82f))
                        cubicTo(pX(0.52f), pY(0.45f), pX(0.65f), pY(0.20f), pX(0.80f), pY(0.20f))
                        cubicTo(pX(0.92f), pY(0.20f), pX(0.92f), pY(0.42f), pX(0.76f), pY(0.44f))
                    }
                    drawPath(p, tint, style = strokeStyle)
                }
                signKey.startsWith("vrish") || signKey.startsWith("taur") -> {
                    val head = Path().apply {
                        addOval(androidx.compose.ui.geometry.Rect(pX(0.26f), pY(0.46f), pX(0.74f), pY(0.90f)))
                    }
                    val horns = Path().apply {
                        moveTo(pX(0.14f), pY(0.20f))
                        cubicTo(pX(0.24f), pY(0.42f), pX(0.35f), pY(0.48f), pX(0.50f), pY(0.48f))
                        cubicTo(pX(0.65f), pY(0.48f), pX(0.76f), pY(0.42f), pX(0.86f), pY(0.20f))
                    }
                    drawPath(head, tint, style = strokeStyle)
                    drawPath(horns, tint, style = strokeStyle)
                }
                signKey.startsWith("mith") || signKey.startsWith("gemi") -> {
                    val p = Path().apply {
                        moveTo(pX(0.20f), pY(0.20f))
                        quadraticBezierTo(pX(0.50f), pY(0.28f), pX(0.80f), pY(0.20f))
                        moveTo(pX(0.35f), pY(0.24f))
                        lineTo(pX(0.35f), pY(0.76f))
                        moveTo(pX(0.65f), pY(0.24f))
                        lineTo(pX(0.65f), pY(0.76f))
                        moveTo(pX(0.20f), pY(0.80f))
                        quadraticBezierTo(pX(0.50f), pY(0.72f), pX(0.80f), pY(0.80f))
                    }
                    drawPath(p, tint, style = strokeStyle)
                }
                signKey.startsWith("kark") || signKey.startsWith("canc") -> {
                    val p1 = Path().apply {
                        moveTo(pX(0.42f), pY(0.36f))
                        cubicTo(pX(0.20f), pY(0.18f), pX(0.10f), pY(0.34f), pX(0.24f), pY(0.50f))
                        cubicTo(pX(0.38f), pY(0.62f), pX(0.68f), pY(0.38f), pX(0.84f), pY(0.38f))
                    }
                    val p2 = Path().apply {
                        moveTo(pX(0.58f), pY(0.64f))
                        cubicTo(pX(0.80f), pY(0.82f), pX(0.90f), pY(0.66f), pX(0.76f), pY(0.50f))
                        cubicTo(pX(0.62f), pY(0.38f), pX(0.32f), pY(0.62f), pX(0.16f), pY(0.62f))
                    }
                    drawPath(p1, tint, style = strokeStyle)
                    drawPath(p2, tint, style = strokeStyle)
                }
                signKey.startsWith("simh") || signKey.startsWith("leo") -> {
                    val p = Path().apply {
                        moveTo(pX(0.24f), pY(0.70f))
                        cubicTo(pX(0.10f), pY(0.50f), pX(0.24f), pY(0.22f), pX(0.46f), pY(0.22f))
                        cubicTo(pX(0.68f), pY(0.22f), pX(0.74f), pY(0.42f), pX(0.68f), pY(0.58f))
                        cubicTo(pX(0.62f), pY(0.72f), pX(0.75f), pY(0.84f), pX(0.88f), pY(0.78f))
                    }
                    drawPath(p, tint, style = strokeStyle)
                }
                signKey.startsWith("kany") || signKey.startsWith("virg") -> {
                    val p = Path().apply {
                        moveTo(pX(0.18f), pY(0.76f))
                        lineTo(pX(0.18f), pY(0.28f))
                        cubicTo(pX(0.26f), pY(0.22f), pX(0.38f), pY(0.22f), pX(0.42f), pY(0.36f))
                        lineTo(pX(0.42f), pY(0.76f))
                        moveTo(pX(0.42f), pY(0.36f))
                        cubicTo(pX(0.50f), pY(0.22f), pX(0.62f), pY(0.22f), pX(0.66f), pY(0.36f))
                        lineTo(pX(0.66f), pY(0.76f))
                        moveTo(pX(0.66f), pY(0.46f))
                        cubicTo(pX(0.82f), pY(0.32f), pX(0.92f), pY(0.50f), pX(0.74f), pY(0.75f))
                        lineTo(pX(0.86f), pY(0.86f))
                    }
                    drawPath(p, tint, style = strokeStyle)
                }
                signKey.startsWith("tula") || signKey.startsWith("libr") -> {
                    val p = Path().apply {
                        moveTo(pX(0.22f), pY(0.46f))
                        lineTo(pX(0.38f), pY(0.46f))
                        cubicTo(pX(0.38f), pY(0.24f), pX(0.62f), pY(0.24f), pX(0.62f), pY(0.46f))
                        lineTo(pX(0.78f), pY(0.46f))
                        moveTo(pX(0.16f), pY(0.72f))
                        lineTo(pX(0.84f), pY(0.72f))
                    }
                    drawPath(p, tint, style = strokeStyle)
                }
                signKey.startsWith("vrishc") || signKey.startsWith("scor") -> {
                    val p = Path().apply {
                        moveTo(pX(0.18f), pY(0.74f))
                        lineTo(pX(0.18f), pY(0.28f))
                        cubicTo(pX(0.26f), pY(0.22f), pX(0.38f), pY(0.22f), pX(0.42f), pY(0.36f))
                        lineTo(pX(0.42f), pY(0.74f))
                        moveTo(pX(0.42f), pY(0.36f))
                        cubicTo(pX(0.50f), pY(0.22f), pX(0.62f), pY(0.22f), pX(0.66f), pY(0.36f))
                        lineTo(pX(0.66f), pY(0.74f))
                        moveTo(pX(0.66f), pY(0.52f))
                        cubicTo(pX(0.78f), pY(0.44f), pX(0.88f), pY(0.52f), pX(0.86f), pY(0.70f))
                        lineTo(pX(0.92f), pY(0.64f))
                    }
                    drawPath(p, tint, style = strokeStyle)
                }
                signKey.startsWith("dhan") || signKey.startsWith("sagi") -> {
                    val p = Path().apply {
                        moveTo(pX(0.24f), pY(0.76f))
                        lineTo(pX(0.78f), pY(0.22f))
                        moveTo(pX(0.56f), pY(0.22f))
                        lineTo(pX(0.78f), pY(0.22f))
                        lineTo(pX(0.78f), pY(0.44f))
                        moveTo(pX(0.32f), pY(0.52f))
                        lineTo(pX(0.48f), pY(0.68f))
                    }
                    drawPath(p, tint, style = strokeStyle)
                }
                signKey.startsWith("maka") || signKey.startsWith("capr") -> {
                    val p = Path().apply {
                        moveTo(pX(0.18f), pY(0.28f))
                        lineTo(pX(0.34f), pY(0.76f))
                        lineTo(pX(0.50f), pY(0.34f))
                        cubicTo(pX(0.62f), pY(0.26f), pX(0.76f), pY(0.36f), pX(0.74f), pY(0.58f))
                        cubicTo(pX(0.72f), pY(0.78f), pX(0.54f), pY(0.88f), pX(0.54f), pY(0.68f))
                        cubicTo(pX(0.54f), pY(0.52f), pX(0.84f), pY(0.52f), pX(0.84f), pY(0.72f))
                    }
                    drawPath(p, tint, style = strokeStyle)
                }
                signKey.startsWith("kumb") || signKey.startsWith("aqua") -> {
                    val p = Path().apply {
                        moveTo(pX(0.16f), pY(0.38f))
                        lineTo(pX(0.32f), pY(0.24f))
                        lineTo(pX(0.50f), pY(0.38f))
                        lineTo(pX(0.68f), pY(0.24f))
                        lineTo(pX(0.84f), pY(0.38f))
                        moveTo(pX(0.16f), pY(0.66f))
                        lineTo(pX(0.32f), pY(0.52f))
                        lineTo(pX(0.50f), pY(0.66f))
                        lineTo(pX(0.68f), pY(0.52f))
                        lineTo(pX(0.84f), pY(0.66f))
                    }
                    drawPath(p, tint, style = strokeStyle)
                }
                signKey.startsWith("meen") || signKey.startsWith("pisc") -> {
                    val p = Path().apply {
                        moveTo(pX(0.34f), pY(0.18f))
                        cubicTo(pX(0.18f), pY(0.36f), pX(0.18f), pY(0.64f), pX(0.34f), pY(0.82f))
                        moveTo(pX(0.66f), pY(0.18f))
                        cubicTo(pX(0.82f), pY(0.36f), pX(0.82f), pY(0.64f), pX(0.66f), pY(0.82f))
                        moveTo(pX(0.14f), pY(0.50f))
                        lineTo(pX(0.86f), pY(0.50f))
                    }
                    drawPath(p, tint, style = strokeStyle)
                }
                else -> {
                    drawCircle(tint, radius = w * 0.35f, center = Offset(pX(0.5f), pY(0.5f)), style = strokeStyle)
                }
            }
        }
    }
}
