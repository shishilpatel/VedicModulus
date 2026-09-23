package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DarkText
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.core.model.Graha
import com.example.core.model.KundaliChartData
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeAccent

@OptIn(ExperimentalTextApi::class)
@Composable
fun NorthIndianKundaliCanvas(
    chartData: KundaliChartData,
    selectedHouse: Int,
    onHouseSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(chartData) {
                    detectTapGestures { tapOffset ->
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val cy = h / 2f

                        // Approximate which house was tapped based on angle and distance from center
                        val dx = tapOffset.x - cx
                        val dy = tapOffset.y - cy
                        val angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble()))
                        val distNorm = Math.hypot(dx.toDouble(), dy.toDouble()) / (w / 2.0)

                        val houseTapped = when {
                            // Center Kendra houses
                            distNorm < 0.55 -> {
                                when {
                                    angle in -135.0..-45.0 -> 1   // Top Kendra
                                    angle in 45.0..135.0 -> 7    // Bottom Kendra
                                    angle in -45.0..45.0 -> 10   // Right Kendra
                                    else -> 4                    // Left Kendra
                                }
                            }
                            // Outer corners
                            else -> {
                                when {
                                    angle in -180.0..-135.0 -> if (tapOffset.y < cy) 2 else 3
                                    angle in -135.0..-90.0 -> 12
                                    angle in -90.0..-45.0 -> 11
                                    angle in -45.0..0.0 -> 10
                                    angle in 0.0..45.0 -> 9
                                    angle in 45.0..90.0 -> 8
                                    angle in 90.0..135.0 -> 6
                                    else -> 5
                                }
                            }
                        }
                        onHouseSelected(houseTapped.coerceIn(1, 12))
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // 1. Background square border
            drawRect(
                color = LightBorder,
                topLeft = Offset(0f, 0f),
                size = size,
                style = Stroke(width = 2.5f)
            )

            // 2. Corner to Corner Diagonals (X)
            drawLine(
                color = LightBorder,
                start = Offset(0f, 0f),
                end = Offset(w, h),
                strokeWidth = 2f
            )
            drawLine(
                color = LightBorder,
                start = Offset(0f, h),
                end = Offset(w, 0f),
                strokeWidth = 2f
            )

            // 3. Inner Diamond connecting midpoints
            val diamondPath = Path().apply {
                moveTo(cx, 0f)
                lineTo(w, cy)
                lineTo(cx, h)
                lineTo(0f, cy)
                close()
            }
            drawPath(
                path = diamondPath,
                color = MaroonPrimary.copy(alpha = 0.8f),
                style = Stroke(width = 2.5f)
            )

            // House center coordinate approximations
            val houseCenters = mapOf(
                1 to Offset(cx, h * 0.26f),
                2 to Offset(w * 0.25f, h * 0.12f),
                3 to Offset(w * 0.12f, h * 0.25f),
                4 to Offset(w * 0.26f, cy),
                5 to Offset(w * 0.12f, h * 0.75f),
                6 to Offset(w * 0.25f, h * 0.88f),
                7 to Offset(cx, h * 0.74f),
                8 to Offset(w * 0.75f, h * 0.88f),
                9 to Offset(w * 0.88f, h * 0.75f),
                10 to Offset(w * 0.74f, cy),
                11 to Offset(w * 0.88f, h * 0.25f),
                12 to Offset(w * 0.75f, h * 0.12f)
            )

            // Draw each house's Rashi number and Graha occupants
            chartData.bhavas.forEach { bhava ->
                val center = houseCenters[bhava.houseNumber] ?: Offset(cx, cy)
                val isSelected = bhava.houseNumber == selectedHouse

                // Highlight circle for selected house
                if (isSelected) {
                    drawCircle(
                        color = MaroonPrimary.copy(alpha = 0.22f),
                        radius = w * 0.12f,
                        center = center
                    )
                }

                val isLagna = bhava.houseNumber == 1
                val rashiText = if (isLagna) "Asc • ${bhava.rashi.number}" else "${bhava.rashi.number}"
                val rashiMeasured = textMeasurer.measure(
                    text = AnnotatedString(rashiText),
                    style = TextStyle(
                        color = if (isSelected) MaroonPrimary else if (isLagna) OrangeAccent else DarkText.copy(alpha = 0.8f),
                        fontSize = if (isLagna) 9.sp else 11.sp,
                        fontWeight = if (isLagna) FontWeight.Bold else FontWeight.SemiBold
                    )
                )

                val grahaMeasured = if (bhava.occupyingGrahas.isNotEmpty()) {
                    val grahaText = bhava.occupyingGrahas.joinToString(" ") { it.shortCode() }
                    textMeasurer.measure(
                        text = AnnotatedString(grahaText),
                        style = TextStyle(
                            color = OrangeAccent,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else null

                val gap = 4f
                val totalHeight = rashiMeasured.size.height + (if (grahaMeasured != null) grahaMeasured.size.height + gap else 0f)
                var currentY = center.y - totalHeight / 2f

                drawText(
                    textLayoutResult = rashiMeasured,
                    topLeft = Offset(center.x - rashiMeasured.size.width / 2f, currentY)
                )

                if (grahaMeasured != null) {
                    currentY += rashiMeasured.size.height + gap
                    drawText(
                        textLayoutResult = grahaMeasured,
                        topLeft = Offset(center.x - grahaMeasured.size.width / 2f, currentY)
                    )
                }
            }
        }
    }
}
