package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PanchangaData
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeAccent

@Composable
fun TithiCalculatorCard(
    panchanga: PanchangaData,
    modifier: Modifier = Modifier
) {
    val tithi = panchanga.tithi
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VEDIC TITHI",
                        style = MaterialTheme.typography.labelMedium.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${tithi.name} (${tithi.devanagari})",
                        style = MaterialTheme.typography.titleMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaroonPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${tithi.paksha} Paksha",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = LightBorder,
                thickness = 1.dp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Lunar Day Number", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                    Text("Tithi #${tithi.number} of 30", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Calculation Precision", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary))
                    Text("High Astronomical Precision", style = MaterialTheme.typography.bodyMedium.copy(color = OrangeAccent, fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}
