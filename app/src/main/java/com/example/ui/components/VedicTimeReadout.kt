package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.DailyVedicProfile
import com.example.engines.astronomy.VedicTimeCalculator
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.DarkTextSecondary
import kotlinx.coroutines.delay

@Composable
fun VedicTimeReadout(
    dailyProfile: DailyVedicProfile,
    currentTime: java.time.LocalTime? = null,
    currentTimeProvider: (() -> java.time.LocalTime)? = null,
    modifier: Modifier = Modifier
) {
    val zone = remember(dailyProfile.currentZonedDateTime) { dailyProfile.currentZonedDateTime.zone }
    val currentProviderState = rememberUpdatedState(currentTimeProvider)
    var currentLocalTime by remember {
        mutableStateOf(currentTime ?: currentProviderState.value?.invoke() ?: java.time.LocalTime.now(zone))
    }

    if (currentTime != null) {
        currentLocalTime = currentTime
    } else {
        LaunchedEffect(zone) {
            while (true) {
                currentLocalTime = currentProviderState.value?.invoke() ?: java.time.LocalTime.now(zone)
                val nowMs = System.currentTimeMillis()
                val remaining = 1000L - (nowMs % 1000L)
                kotlinx.coroutines.delay(if (remaining <= 50L) 1000L else remaining)
            }
        }
    }

    val zdt = remember(dailyProfile.currentZonedDateTime, currentLocalTime, zone) {
        java.time.ZonedDateTime.of(dailyProfile.currentZonedDateTime.toLocalDate(), currentLocalTime, zone)
    }
    val vedicTimeData = remember(zdt, dailyProfile.panchanga.sunrise, dailyProfile.panchanga.sunset) {
        VedicTimeCalculator.calculateVedicTime(
            currentTime = zdt,
            sunrise = dailyProfile.panchanga.sunrise,
            sunset = dailyProfile.panchanga.sunset
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaroonPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Vedic Time (Ishta Kaala)",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaroonPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                TimeBlock(value = vedicTimeData.ghadi.toString(), label = "Ghati")
                Text(
                    text = ":",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                TimeBlock(value = vedicTimeData.pala.toString().padStart(2, '0'), label = "Pala")
                Text(
                    text = ":",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
                TimeBlock(value = vedicTimeData.vipala.toString().padStart(2, '0'), label = "Vipala")
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = vedicTimeData.devanagariGhadiPala,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = OrangeAccent,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.05f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = vedicTimeData.prahara.englishName,
                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSecondary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (vedicTimeData.isDayTime) OrangeAccent else Color.Blue.copy(alpha = 0.6f))
                )
            }
        }
    }
}

@Composable
private fun TimeBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = DarkTextSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        )
    }
}
