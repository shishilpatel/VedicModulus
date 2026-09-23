package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.core.model.KundaliChartData
import com.example.ui.theme.DarkText
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeAccent
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Circular Kundali (Bhava Chakra / 360° Wheel) canvas.
 * Concentric celestial wheel displaying all 12 Bhavas, Zodiac signs, and planetary positions.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun CircularKundaliCanvas(
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
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = tapOffset.x - cx
                        val dy = tapOffset.y - cy
                        var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                        if (angleDeg < 0) angleDeg += 360.0

                        // Starting from 180° (Ascendant / 9 o'clock) counter-clockwise
                        // In screen coordinates: 180° is left, 90° is bottom, 0° is right, 270° is top.
                        // Moving counter-clockwise in screen coordinates decreases angle.
                        // Standard: House 1 from 165° to 195°
                        val relAngle = (360.0 - angleDeg + 195.0) % 360.0
                        val houseTapped = ((relAngle / 30.0).toInt() % 12) + 1
                        onHouseSelected(houseTapped.coerceIn(1, 12))
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val rMax = min(w, h) / 2f * 0.95f
            val rOuter = rMax
            val rMiddle = rMax * 0.76f
            val rInner = rMax * 0.44f
            val rCenter = rMax * 0.22f

            // 1. Concentric circles
            drawCircle(color = LightBorder, radius = rOuter, center = Offset(cx, cy), style = Stroke(width = 2.5f))
            drawCircle(color = LightBorder, radius = rMiddle, center = Offset(cx, cy), style = Stroke(width = 1.5f))
            drawCircle(color = MaroonPrimary.copy(alpha = 0.7f), radius = rInner, center = Offset(cx, cy), style = Stroke(width = 2f))
            drawCircle(color = MaroonPrimary.copy(alpha = 0.15f), radius = rCenter, center = Offset(cx, cy))

            // 2. Draw 12 Radial Spokes & Sectors
            // Ascendant (House 1) centered at 180° (left).
            // Sector for House H is centered at 180 - (H-1)*30
            for (houseNum in 1..12) {
                val centerAngleDeg = 180.0 - (houseNum - 1) * 30.0
                val startSpokeDeg = centerAngleDeg - 15.0
                val radStart = Math.toRadians(startSpokeDeg)

                // Spoke lines
                val xOuter = cx + (rOuter * cos(radStart)).toFloat()
                val yOuter = cy + (rOuter * sin(radStart)).toFloat()
                val xInner = cx + (rInner * cos(radStart)).toFloat()
                val yInner = cy + (rInner * sin(radStart)).toFloat()

                drawLine(
                    color = LightBorder,
                    start = Offset(xInner, yInner),
                    end = Offset(xOuter, yOuter),
                    strokeWidth = 1.5f
                )

                val isSelected = houseNum == selectedHouse
                val bhava = chartData.bhavas.firstOrNull { it.houseNumber == houseNum }
                val isLagna = houseNum == 1

                // Highlight arc wedge if selected
                if (isSelected) {
                    val sweep = 30f
                    val startAngle = (startSpokeDeg % 360.0).toFloat()
                    drawArc(
                        color = OrangeAccent.copy(alpha = 0.25f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(cx - rOuter, cy - rOuter),
                        size = Size(2 * rOuter, 2 * rOuter)
                    )
                }

                // Coordinates for outer ring text (Rashi) and middle ring text (House & Planets)
                val radMid = Math.toRadians(centerAngleDeg)
                val rRashi = (rOuter + rMiddle) / 2f
                val rHouse = (rMiddle + rInner) / 2f

                val xRashi = cx + (rRashi * cos(radMid)).toFloat()
                val yRashi = cy + (rRashi * sin(radMid)).toFloat()
                val xHouse = cx + (rHouse * cos(radMid)).toFloat()
                val yHouse = cy + (rHouse * sin(radMid)).toFloat()

                // Draw Rashi (Outer ring)
                if (bhava != null) {
                    val rashiSymbolText = "${bhava.rashi.symbol} ${bhava.rashi.sanskritName.take(3)}"
                    val rashiMeasured = textMeasurer.measure(
                        text = AnnotatedString(rashiSymbolText),
                        style = TextStyle(
                            color = if (isSelected) MaroonPrimary else DarkText.copy(alpha = 0.85f),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = rashiMeasured,
                        topLeft = Offset(xRashi - rashiMeasured.size.width / 2f, yRashi - rashiMeasured.size.height / 2f)
                    )

                    // Draw House badge & Planets (Middle ring)
                    val houseLabel = if (isLagna) "L(H1)" else "H$houseNum"
                    val houseMeasured = textMeasurer.measure(
                        text = AnnotatedString(houseLabel),
                        style = TextStyle(
                            color = if (isLagna) MaroonPrimary else OrangeAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    val grahaMeasured = if (bhava.occupyingGrahas.isNotEmpty()) {
                        val grahaText = bhava.occupyingGrahas.joinToString(" ") { it.shortCode() }
                        textMeasurer.measure(
                            text = AnnotatedString(grahaText),
                            style = TextStyle(
                                color = if (isSelected) MaroonPrimary else OrangeAccent,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else null

                    val secGap = 2f
                    val totalSecHeight = houseMeasured.size.height + (if (grahaMeasured != null) grahaMeasured.size.height + secGap else 0f)
                    var secY = yHouse - totalSecHeight / 2f

                    drawText(
                        textLayoutResult = houseMeasured,
                        topLeft = Offset(xHouse - houseMeasured.size.width / 2f, secY)
                    )

                    if (grahaMeasured != null) {
                        secY += houseMeasured.size.height + secGap
                        drawText(
                            textLayoutResult = grahaMeasured,
                            topLeft = Offset(xHouse - grahaMeasured.size.width / 2f, secY)
                        )
                    }
                }
            }

            // 3. Center Hub Text - Dynamically stacked to prevent overlap
            val centerTitle = "भाव चक्र"
            val centerSub = "BHAVA CHAKRA"
            val centerLagna = "Lagna: ${chartData.lagnaRashi.sanskritName} ${String.format(Locale.US, "%.1f°", chartData.lagnaDegrees)}"

            val titleMeasured = textMeasurer.measure(
                text = AnnotatedString(centerTitle),
                style = TextStyle(color = MaroonPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            )
            val subMeasured = textMeasurer.measure(
                text = AnnotatedString(centerSub),
                style = TextStyle(color = OrangeAccent, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold)
            )
            val lagnaMeasured = textMeasurer.measure(
                text = AnnotatedString(centerLagna),
                style = TextStyle(color = DarkTextSecondary, fontSize = 8.sp)
            )

            val hubGap = 4f
            val totalHubHeight = titleMeasured.size.height + subMeasured.size.height + lagnaMeasured.size.height + (2 * hubGap)
            var currentHubY = cy - totalHubHeight / 2f

            drawText(
                textLayoutResult = titleMeasured,
                topLeft = Offset(cx - titleMeasured.size.width / 2f, currentHubY)
            )
            currentHubY += titleMeasured.size.height + hubGap
            drawText(
                textLayoutResult = subMeasured,
                topLeft = Offset(cx - subMeasured.size.width / 2f, currentHubY)
            )
            currentHubY += subMeasured.size.height + hubGap
            drawText(
                textLayoutResult = lagnaMeasured,
                topLeft = Offset(cx - lagnaMeasured.size.width / 2f, currentHubY)
            )
        }
    }
}
