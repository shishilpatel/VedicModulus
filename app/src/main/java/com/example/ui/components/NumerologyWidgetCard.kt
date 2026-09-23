package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.NumerologyProfile
import com.example.ui.theme.*

@Composable
fun NumerologyWidgetCard(
    numerologyProfile: NumerologyProfile,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SACRED NUMEROLOGY",
                style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Psychic Number (Moolank)
                NumerologyBadge(
                    label = "Moolank (Psychic)",
                    number = numerologyProfile.birthNumber,
                    color = MaroonPrimary
                )
                
                // Destiny Number (Bhagyank)
                NumerologyBadge(
                    label = "Bhagyank (Destiny)",
                    number = numerologyProfile.lifePathNumber,
                    color = OrangeAccent
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Ruling Planet: ${numerologyProfile.rulingPlanet.name}",
                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun NumerologyBadge(label: String, number: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(alpha = 0.15f), shape = CircleShape)
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(color = color, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(color = DarkTextSecondary)
        )
    }
}
