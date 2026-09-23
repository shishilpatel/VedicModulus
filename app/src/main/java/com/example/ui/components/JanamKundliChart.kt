package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Graha
import com.example.core.model.Rashi
import com.example.engines.astronomy.GrahaSpashta

enum class ChartStyle {
    NORTH_INDIAN, SOUTH_INDIAN
}

@Composable
fun JanamKundliChart(
    spashtasList: List<GrahaSpashta>,
    lagnaRashi: Rashi,
    style: ChartStyle = ChartStyle.NORTH_INDIAN,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            .padding(2.dp)
    ) {
        if (style == ChartStyle.NORTH_INDIAN) {
            NorthIndianChart(spashtasList, lagnaRashi, Modifier.fillMaxSize())
        } else {
            // South Indian chart implementation (placeholder)
            Text("South Indian chart not implemented yet", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun NorthIndianChart(
    spashtasList: List<GrahaSpashta>,
    lagnaRashi: Rashi,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val textColor = MaterialTheme.colorScheme.onSurface
    val rashiColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    // Map houses to planets
    // In North Indian chart, House 1 is always the top center diamond.
    // House 1 contains the Lagna Rashi.
    // House N contains Rashi = (LagnaRashi + N - 1) % 12 (1-indexed)
    val houseToPlanets = mutableMapOf<Int, MutableList<GrahaSpashta>>()
    for (i in 1..12) {
        houseToPlanets[i] = mutableListOf()
    }
    
    val lagnaNum = lagnaRashi.number
    
    // Assign planets to houses based on their Rashi
    spashtasList.forEach { spashta ->
        // Find which house this rashi falls into
        val rashiNum = spashta.rashi.number
        val houseNum = ((rashiNum - lagnaNum + 12) % 12) + 1
        houseToPlanets[houseNum]?.add(spashta)
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw main diagonal lines
        drawLine(lineColor, start = Offset(0f, 0f), end = Offset(w, h), strokeWidth = 2.dp.toPx())
        drawLine(lineColor, start = Offset(0f, h), end = Offset(w, 0f), strokeWidth = 2.dp.toPx())
        
        // Draw inner diamond
        val diamondPath = Path().apply {
            moveTo(w / 2, 0f)
            lineTo(w, h / 2)
            lineTo(w / 2, h)
            lineTo(0f, h / 2)
            close()
        }
        drawPath(diamondPath, color = lineColor, style = Stroke(width = 2.dp.toPx()))

        // Centers for the 12 houses (approximate, for drawing text)
        val centers = mapOf(
            1 to Offset(w / 2, h / 4),
            2 to Offset(w / 4.5f, h / 7.5f),
            3 to Offset(w / 7.5f, h / 4.5f),
            4 to Offset(w / 4, h / 2),
            5 to Offset(w / 7.5f, h * 0.76f),
            6 to Offset(w / 4.5f, h * 0.86f),
            7 to Offset(w / 2, h * 0.75f),
            8 to Offset(w * 0.77f, h * 0.86f),
            9 to Offset(w * 0.86f, h * 0.76f),
            10 to Offset(w * 0.75f, h / 2),
            11 to Offset(w * 0.86f, h / 4.5f),
            12 to Offset(w * 0.77f, h / 7.5f)
        )

        val rashiNumberCenters = mapOf(
            1 to Offset(w / 2, h / 4 + h * 0.15f), // bottom of H1
            2 to Offset(w / 4.5f + w * 0.05f, h / 7.5f + h * 0.05f),
            3 to Offset(w / 7.5f + w * 0.05f, h / 4.5f + h * 0.05f),
            4 to Offset(w / 4 + w * 0.15f, h / 2),
            5 to Offset(w / 7.5f + w * 0.05f, h * 0.76f - h * 0.05f),
            6 to Offset(w / 4.5f + w * 0.05f, h * 0.86f - h * 0.05f),
            7 to Offset(w / 2, h * 0.75f - h * 0.15f), // top of H7
            8 to Offset(w * 0.77f - w * 0.05f, h * 0.86f - h * 0.05f),
            9 to Offset(w * 0.86f - w * 0.05f, h * 0.76f - h * 0.05f),
            10 to Offset(w * 0.75f - w * 0.15f, h / 2),
            11 to Offset(w * 0.86f - w * 0.05f, h / 4.5f + h * 0.05f),
            12 to Offset(w * 0.77f - w * 0.05f, h / 7.5f + h * 0.05f)
        )

        // Draw planets and Rashi numbers in each house
        for (i in 1..12) {
            val rashiNum = ((lagnaNum + i - 2) % 12) + 1
            
            // Draw Rashi Number
            val rashiText = rashiNum.toString()
            val rashiLayoutResult = textMeasurer.measure(
                text = rashiText,
                style = TextStyle(fontSize = 10.sp, color = rashiColor, fontWeight = FontWeight.Bold)
            )
            val rPos = rashiNumberCenters[i] ?: Offset.Zero
            drawText(
                textLayoutResult = rashiLayoutResult,
                topLeft = Offset(rPos.x - rashiLayoutResult.size.width / 2, rPos.y - rashiLayoutResult.size.height / 2)
            )

            // Draw Planets
            val planets = houseToPlanets[i] ?: emptyList()
            if (planets.isNotEmpty()) {
                val pStr = planets.joinToString(" ") { 
                    it.graha.displayName.take(2) + if (it.isRetrograde) "(R)" else ""
                }
                val layoutResult = textMeasurer.measure(
                    text = pStr,
                    style = TextStyle(fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Medium)
                )
                val pos = centers[i] ?: Offset.Zero
                drawText(
                    textLayoutResult = layoutResult,
                    topLeft = Offset(pos.x - layoutResult.size.width / 2, pos.y - layoutResult.size.height / 2)
                )
            }
        }
    }
}
