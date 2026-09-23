package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
import com.example.core.model.Rashi
import com.example.ui.theme.DarkText
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeAccent
import java.util.Locale

/**
 * South Indian style Kundali (D1) chart canvas.
 * Fixed Zodiac signs (Rashis) arranged in a clockwise perimeter grid.
 * Houses count clockwise starting from the Lagna (Ascendant) box.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun SouthIndianKundaliCanvas(
    chartData: KundaliChartData,
    selectedHouse: Int,
    onHouseSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Mapping grid (col, row) to fixed South Indian Rashis
    val gridToRashi: Map<Pair<Int, Int>, Rashi> = mapOf(
        Pair(0, 0) to Rashi.MEENA,       // Pisces
        Pair(1, 0) to Rashi.MESHA,       // Aries
        Pair(2, 0) to Rashi.VRISHABHA,   // Taurus
        Pair(3, 0) to Rashi.MITHUNA,     // Gemini
        Pair(3, 1) to Rashi.KARKATA,     // Cancer
        Pair(3, 2) to Rashi.SIMHA,       // Leo
        Pair(3, 3) to Rashi.KANYA,       // Virgo
        Pair(2, 3) to Rashi.TULA,        // Libra
        Pair(1, 3) to Rashi.VRISHCHIKA,  // Scorpio
        Pair(0, 3) to Rashi.DHANUS,      // Sagittarius
        Pair(0, 2) to Rashi.MAKARA,      // Capricorn
        Pair(0, 1) to Rashi.KUMBHA       // Aquarius
    )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(chartData) {
                    detectTapGestures { tapOffset ->
                        val cellW = size.width / 4f
                        val cellH = size.height / 4f
                        val col = (tapOffset.x / cellW).toInt().coerceIn(0, 3)
                        val row = (tapOffset.y / cellH).toInt().coerceIn(0, 3)

                        val tappedRashi = gridToRashi[Pair(col, row)]
                        if (tappedRashi != null) {
                            val bhava = chartData.bhavas.firstOrNull { it.rashi == tappedRashi }
                            if (bhava != null) {
                                onHouseSelected(bhava.houseNumber)
                            }
                        } else {
                            // Tapped center area -> select Lagna house (House 1)
                            onHouseSelected(1)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val cellW = w / 4f
            val cellH = h / 4f

            // 1. Draw outer border
            drawRect(
                color = LightBorder,
                topLeft = Offset(0f, 0f),
                size = size,
                style = Stroke(width = 2.5f)
            )

            // 2. Draw vertical grid lines
            drawLine(color = LightBorder, start = Offset(cellW, 0f), end = Offset(cellW, h), strokeWidth = 2f)
            // Center column has gap in rows 1 & 2
            drawLine(color = LightBorder, start = Offset(2 * cellW, 0f), end = Offset(2 * cellW, cellH), strokeWidth = 2f)
            drawLine(color = LightBorder, start = Offset(2 * cellW, 3 * cellH), end = Offset(2 * cellW, h), strokeWidth = 2f)
            drawLine(color = LightBorder, start = Offset(3 * cellW, 0f), end = Offset(3 * cellW, h), strokeWidth = 2f)

            // 3. Draw horizontal grid lines
            drawLine(color = LightBorder, start = Offset(0f, cellH), end = Offset(w, cellH), strokeWidth = 2f)
            // Center row has gap in cols 1 & 2
            drawLine(color = LightBorder, start = Offset(0f, 2 * cellH), end = Offset(cellW, 2 * cellH), strokeWidth = 2f)
            drawLine(color = LightBorder, start = Offset(3 * cellW, 2 * cellH), end = Offset(w, 2 * cellH), strokeWidth = 2f)
            drawLine(color = LightBorder, start = Offset(0f, 3 * cellH), end = Offset(w, 3 * cellH), strokeWidth = 2f)

            // 4. Draw Center Box border & Title
            drawRect(
                color = MaroonPrimary.copy(alpha = 0.5f),
                topLeft = Offset(cellW, cellH),
                size = Size(2 * cellW, 2 * cellH),
                style = Stroke(width = 2f)
            )

            // Center Title Text - Dynamic Stacking to Prevent Overlap
            val centerTitle = "SOUTH INDIAN KUNDALI"
            val centerSubtitle = "Lagna: ${chartData.lagnaRashi.sanskritName} (${String.format(Locale.US, "%.1f°", chartData.lagnaDegrees)})"
            val centerDevanagari = "दक्षिण भारतीय पद्धति"

            val titleMeasured = textMeasurer.measure(
                text = AnnotatedString(centerTitle),
                style = TextStyle(color = MaroonPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )
            val devanagariMeasured = textMeasurer.measure(
                text = AnnotatedString(centerDevanagari),
                style = TextStyle(color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            )
            val subtitleMeasured = textMeasurer.measure(
                text = AnnotatedString(centerSubtitle),
                style = TextStyle(color = DarkTextSecondary, fontSize = 9.5.sp)
            )

            val centerX = w / 2f
            val centerY = h / 2f
            val centerGap = 6f
            val totalCenterHeight = titleMeasured.size.height + devanagariMeasured.size.height + subtitleMeasured.size.height + (2 * centerGap)
            var currentCenterY = centerY - totalCenterHeight / 2f

            drawText(
                textLayoutResult = titleMeasured,
                topLeft = Offset(centerX - titleMeasured.size.width / 2f, currentCenterY)
            )
            currentCenterY += titleMeasured.size.height + centerGap
            drawText(
                textLayoutResult = devanagariMeasured,
                topLeft = Offset(centerX - devanagariMeasured.size.width / 2f, currentCenterY)
            )
            currentCenterY += devanagariMeasured.size.height + centerGap
            drawText(
                textLayoutResult = subtitleMeasured,
                topLeft = Offset(centerX - subtitleMeasured.size.width / 2f, currentCenterY)
            )

            // 5. Draw 12 Perimeter Boxes
            gridToRashi.forEach { (coord, rashi) ->
                val col = coord.first
                val row = coord.second
                val boxLeft = col * cellW
                val boxTop = row * cellH

                val bhava = chartData.bhavas.firstOrNull { it.rashi == rashi }
                val houseNum = bhava?.houseNumber ?: 0
                val isLagna = rashi == chartData.lagnaRashi
                val isSelected = houseNum == selectedHouse

                // Background highlight if selected
                if (isSelected) {
                    drawRect(
                        color = OrangeAccent.copy(alpha = 0.22f),
                        topLeft = Offset(boxLeft, boxTop),
                        size = Size(cellW, cellH)
                    )
                    drawRect(
                        color = MaroonPrimary,
                        topLeft = Offset(boxLeft, boxTop),
                        size = Size(cellW, cellH),
                        style = Stroke(width = 2.5f)
                    )
                } else if (isLagna) {
                    drawRect(
                        color = MaroonPrimary.copy(alpha = 0.08f),
                        topLeft = Offset(boxLeft, boxTop),
                        size = Size(cellW, cellH)
                    )
                }

                // Top-Left: Rashi Name & Symbol
                val rashiText = "${rashi.sanskritName.take(3)} ${rashi.symbol}"
                val rashiMeasured = textMeasurer.measure(
                    text = AnnotatedString(rashiText),
                    style = TextStyle(
                        color = if (isSelected) MaroonPrimary else DarkText.copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                drawText(
                    textLayoutResult = rashiMeasured,
                    topLeft = Offset(boxLeft + 4f, boxTop + 4f)
                )

                // Top-Right: Lagna Marker or House Number
                if (isLagna) {
                    // Traditional South Indian double diagonal corner slashes
                    drawLine(
                        color = MaroonPrimary,
                        start = Offset(boxLeft + cellW - 16f, boxTop + 2f),
                        end = Offset(boxLeft + cellW - 2f, boxTop + 16f),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = MaroonPrimary,
                        start = Offset(boxLeft + cellW - 22f, boxTop + 2f),
                        end = Offset(boxLeft + cellW - 2f, boxTop + 22f),
                        strokeWidth = 2f
                    )
                    val ascMeasured = textMeasurer.measure(
                        text = AnnotatedString("ASC"),
                        style = TextStyle(color = MaroonPrimary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                    )
                    drawText(
                        textLayoutResult = ascMeasured,
                        topLeft = Offset(boxLeft + cellW - ascMeasured.size.width - 3f, boxTop + 22f)
                    )
                } else if (bhava != null) {
                    val houseLabel = "H$houseNum"
                    val houseMeasured = textMeasurer.measure(
                        text = AnnotatedString(houseLabel),
                        style = TextStyle(
                            color = OrangeAccent,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = houseMeasured,
                        topLeft = Offset(boxLeft + cellW - houseMeasured.size.width - 4f, boxTop + 4f)
                    )
                }

                // Bottom: Occupying Planets residing in this Rashi
                if (bhava != null && bhava.occupyingGrahas.isNotEmpty()) {
                    val grahaText = bhava.occupyingGrahas.joinToString(" ") { it.shortCode() }
                    val grahaMeasured = textMeasurer.measure(
                        text = AnnotatedString(grahaText),
                        style = TextStyle(
                            color = if (isSelected) MaroonPrimary else OrangeAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = grahaMeasured,
                        topLeft = Offset(
                            (boxLeft + (cellW - grahaMeasured.size.width) / 2f).coerceAtLeast(boxLeft + 2f),
                            boxTop + cellH - grahaMeasured.size.height - 4f
                        )
                    )
                }
            }
        }
    }
}
