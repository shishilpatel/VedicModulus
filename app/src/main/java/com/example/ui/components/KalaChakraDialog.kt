package com.example.ui.components

import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.DailyVedicProfile
import com.example.ui.theme.*

@Composable
fun KalaChakraDialog(
    dailyProfile: DailyVedicProfile,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_close), color = MaroonPrimary, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "KalaChakra Visualization",
                    style = MaterialTheme.typography.titleLarge.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Vedic 24-Hour Planetary & Choghadiya Wheel",
                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // KalaChakra Wheel Renderer
                KalaChakraRenderer(
                    dailyProfile = dailyProfile,
                    modifier = Modifier.size(260.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, LightBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Wheel Legend",
                            style = MaterialTheme.typography.labelMedium.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Red Indicator: Current Time Position\n• Yellow Slices: Auspicious Choghadiya / Hora\n• Blue Slices: Neutral / Variable Segments\n• Center: Sacred Solar-Lunar Meridian",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}
