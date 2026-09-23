package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.BirthProfile
import com.example.engines.numerology.NumerologyEngine
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.LightBorder
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeAccent
import java.time.ZonedDateTime

@Composable
fun DailyNumerologyCard(
    birthProfile: BirthProfile,
    currentDate: ZonedDateTime,
    modifier: Modifier = Modifier
) {
    val engine = remember { NumerologyEngine() }

    val numerologyProfile = remember(birthProfile) {
        engine.calculateNumerology(birthProfile.birthDate, birthProfile.fullName, birthProfile.numerologySystem)
    }
    
    val lifePathNumber = numerologyProfile.lifePathNumber

    val dailyPersonalNumber = remember(birthProfile.birthDate, currentDate) {
        // Daily Personal Number = reduce(Birth Day + Birth Month + Current Year + Current Month + Current Day)
        val sum = birthProfile.birthDate.dayOfMonth + 
                  birthProfile.birthDate.monthValue + 
                  currentDate.year + 
                  currentDate.monthValue + 
                  currentDate.dayOfMonth
        engine.reduceToSingleDigit(sum)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LightBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "DAILY NUMEROLOGY",
                style = MaterialTheme.typography.labelSmall.copy(color = OrangeAccent, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Life Path Number
                NumerologyNumberBadge(
                    label = "Life Path Number",
                    number = lifePathNumber,
                    color = MaroonPrimary
                )
                
                // Daily Personal Number
                NumerologyNumberBadge(
                    label = "Daily Personal Number",
                    number = dailyPersonalNumber,
                    color = OrangeAccent
                )
            }
        }
    }
}

@Composable
private fun NumerologyNumberBadge(label: String, number: Int, color: Color) {
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
            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
        )
    }
}
