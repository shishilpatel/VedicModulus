package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engines.numerology.NumerologyGrid
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeAccent

@Composable
fun NumerologyGridView(
    grid: NumerologyGrid,
    modifier: Modifier = Modifier,
    accentColor: Color = MaroonPrimary
) {
    Column(
        modifier = modifier
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = grid.title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontSize = 14.sp
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // 3x3 Grid Box that fits within any screen width
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(1.dp, Color(0xFF9CA3AF))
        ) {
            grid.cells.forEach { row ->
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    row.forEach { cellValue ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(0.5.dp, Color(0xFFE5E7EB))
                                .background(if (cellValue.isNotEmpty()) Color(0xFFFFFBEB) else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cellValue.isNotEmpty()) {
                                Text(
                                    text = cellValue,
                                    fontSize = if (cellValue.length > 2) 13.sp else 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (cellValue.length > 1) OrangeAccent else Color(0xFF1F2937)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
