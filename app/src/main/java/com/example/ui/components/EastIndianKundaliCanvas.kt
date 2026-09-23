package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.example.core.model.Rashi
import com.example.ui.theme.DarkText
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeAccent
import kotlin.math.hypot

/**
 * East Indian style Kundali (Bengali / Odia / Assamese / Maithili) chart canvas.
 * Fixed Signs arranged counter-clockwise with Aries (Mesha) fixed at the top chamber.
 * Lagna (Ascendant) is marked with "लग्" / "ASC" in the native's rising sign.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun EastIndianKundaliCanvas(
    chartData: KundaliChartData,
    selectedHouse: Int,
    onHouseSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // 12 Chambers mapping to Fixed Signs (Counter-clockwise from Aries at Top)
    val chamberRashis = listOf(
        Rashi.MESHA,        // 1: Top Center
        Rashi.VRISHABHA,    // 2: Top-Left Corner
        Rashi.MITHUNA,      // 3: Left-Top
        Rashi.KARKATA,      // 4: Left Center
        Rashi.SIMHA,        // 5: Left-Bottom
        Rashi.KANYA,        // 6: Bottom-Left Corner
        Rashi.TULA,         // 7: Bottom Center
        Rashi.VRISHCHIKA,   // 8: Bottom-Right Corner
        Rashi.DHANUS,       // 9: Right-Bottom
        Rashi.MAKARA,       // 10: Right Center
        Rashi.KUMBHA,       // 11: Right-Top
        Rashi.MEENA         // 12: Top-Right Corner
    )

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

                        val chamberCenters = listOf(
                            Offset(cx, h * 0.22f),            // 1 Mesha
                            Offset(w * 0.22f, h * 0.12f),     // 2 Vrishabha
                            Offset(w * 0.12f, h * 0.28f),     // 3 Mithuna
                            Offset(w * 0.24f, cy),            // 4 Karkata
                            Offset(w * 0.12f, h * 0.72f),     // 5 Simha
                            Offset(w * 0.22f, h * 0.88f),     // 6 Kanya
                            Offset(cx, h * 0.78f),            // 7 Tula
                            Offset(w * 0.78f, h * 0.88f),     // 8 Vrishchika
                            Offset(w * 0.88f, h * 0.72f),     // 9 Dhanus
                            Offset(w * 0.76f, cy),            // 10 Makara
                            Offset(w * 0.88f, h * 0.28f),     // 11 Kumbha
                            Offset(w * 0.78f, h * 0.12f)      // 12 Meena
                        )

                        // Find closest chamber to tap
                        val closestIdx = chamberCenters.indices.minByOrNull { idx ->
                            val c = chamberCenters[idx]
                            hypot((tapOffset.x - c.x).toDouble(), (tapOffset.y - c.y).toDouble())
                        } ?: 0

                        val tappedRashi = chamberRashis[closestIdx]
                        val bhava = chartData.bhavas.firstOrNull { it.rashi == tappedRashi }
                        if (bhava != null) {
                            onHouseSelected(bhava.houseNumber)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // 1. Outer Square
            drawRect(
                color = LightBorder,
                topLeft = Offset(0f, 0f),
                size = size,
                style = Stroke(width = 2.5f)
            )

            // 2. Corner to corner diagonals (X)
            drawLine(color = LightBorder, start = Offset(0f, 0f), end = Offset(w, h), strokeWidth = 2f)
            drawLine(color = LightBorder, start = Offset(0f, h), end = Offset(w, 0f), strokeWidth = 2f)

            // 3. Central horizontal and vertical cross (+)
            drawLine(color = LightBorder, start = Offset(0f, cy), end = Offset(w, cy), strokeWidth = 2f)
            drawLine(color = LightBorder, start = Offset(cx, 0f), end = Offset(cx, h), strokeWidth = 2f)

            // 4. Inner diamond connecting midpoints
            val diamondPath = Path().apply {
                moveTo(cx, 0f)
                lineTo(w, cy)
                lineTo(cx, h)
                lineTo(0f, cy)
                close()
            }
            drawPath(
                path = diamondPath,
                color = MaroonPrimary.copy(alpha = 0.85f),
                style = Stroke(width = 2.5f)
            )

            // Chamber center coordinates
            val chamberCenters = listOf(
                Offset(cx, h * 0.22f),            // 1 Mesha
                Offset(w * 0.22f, h * 0.12f),     // 2 Vrishabha
                Offset(w * 0.12f, h * 0.28f),     // 3 Mithuna
                Offset(w * 0.24f, cy),            // 4 Karkata
                Offset(w * 0.12f, h * 0.72f),     // 5 Simha
                Offset(w * 0.22f, h * 0.88f),     // 6 Kanya
                Offset(cx, h * 0.78f),            // 7 Tula
                Offset(w * 0.78f, h * 0.88f),     // 8 Vrishchika
                Offset(w * 0.88f, h * 0.72f),     // 9 Dhanus
                Offset(w * 0.76f, cy),            // 10 Makara
                Offset(w * 0.88f, h * 0.28f),     // 11 Kumbha
                Offset(w * 0.78f, h * 0.12f)      // 12 Meena
            )

            // Render each chamber
            chamberRashis.forEachIndexed { idx, rashi ->
                val center = chamberCenters[idx]
                val bhava = chartData.bhavas.firstOrNull { it.rashi == rashi }
                val isLagna = rashi == chartData.lagnaRashi
                val isSelected = bhava?.houseNumber == selectedHouse

                // Highlight circle for selected chamber
                if (isSelected) {
                    drawCircle(
                        color = OrangeAccent.copy(alpha = 0.28f),
                        radius = w * 0.11f,
                        center = center
                    )
                    drawCircle(
                        color = MaroonPrimary,
                        radius = w * 0.11f,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                } else if (isLagna) {
                    drawCircle(
                        color = MaroonPrimary.copy(alpha = 0.1f),
                        radius = w * 0.1f,
                        center = center
                    )
                }

                // Rashi Symbol & Name
                val rashiText = "${rashi.sanskritName.take(3)} ${rashi.symbol}"
                val rashiMeasured = textMeasurer.measure(
                    text = AnnotatedString(rashiText),
                    style = TextStyle(
                        color = if (isSelected) MaroonPrimary else DarkText.copy(alpha = 0.85f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                // Lagna or House Number badge
                val houseMeasured = if (bhava != null) {
                    val houseLabel = if (isLagna) "L (H1)" else "H${bhava.houseNumber}"
                    textMeasurer.measure(
                        text = AnnotatedString(houseLabel),
                        style = TextStyle(
                            color = if (isLagna) MaroonPrimary else OrangeAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else null

                // Occupying Grahas
                val grahaMeasured = if (bhava != null && bhava.occupyingGrahas.isNotEmpty()) {
                    val grahaText = bhava.occupyingGrahas.joinToString(" ") { it.shortCode() }
                    textMeasurer.measure(
                        text = AnnotatedString(grahaText),
                        style = TextStyle(
                            color = if (isSelected) MaroonPrimary else OrangeAccent,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else null

                // Dynamic vertical stacking to avoid any overlapping
                val gap = 3f
                val totalHeight = rashiMeasured.size.height +
                        (if (houseMeasured != null) houseMeasured.size.height + gap else 0f) +
                        (if (grahaMeasured != null) grahaMeasured.size.height + gap else 0f)
                var currentY = center.y - totalHeight / 2f

                drawText(
                    textLayoutResult = rashiMeasured,
                    topLeft = Offset(center.x - rashiMeasured.size.width / 2f, currentY)
                )

                if (houseMeasured != null) {
                    currentY += rashiMeasured.size.height + gap
                    drawText(
                        textLayoutResult = houseMeasured,
                        topLeft = Offset(center.x - houseMeasured.size.width / 2f, currentY)
                    )
                }

                if (grahaMeasured != null && houseMeasured != null) {
                    currentY += houseMeasured.size.height + gap
                    drawText(
                        textLayoutResult = grahaMeasured,
                        topLeft = Offset(center.x - grahaMeasured.size.width / 2f, currentY)
                    )
                }
            }
        }
    }
}
