package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engines.chakra.BreathingPhase
import com.example.engines.chakra.BreathingState
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MaroonPrimary
import kotlin.math.min

@Composable
fun BreathingGuideVisualizer(
    breathingState: BreathingState,
    modifier: Modifier = Modifier
) {
    val phaseColor = when (breathingState.phase) {
        BreathingPhase.INHALE -> Color(0xFF00ACC1) // Expanding Cyan
        BreathingPhase.HOLD_IN -> GoldPrimary // Full Gold
        BreathingPhase.EXHALE -> Color(0xFFFB8C00) // Grounding Amber
        BreathingPhase.HOLD_OUT -> Color(0xFF8E24AA) // Restful Violet
    }

    // Dynamic scale computation based on phase
    val dynamicFraction = when (breathingState.phase) {
        BreathingPhase.INHALE -> 0.45f + (0.55f * breathingState.phaseProgress)
        BreathingPhase.HOLD_IN -> 1.0f
        BreathingPhase.EXHALE -> 1.0f - (0.55f * breathingState.phaseProgress)
        BreathingPhase.HOLD_OUT -> 0.45f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = min(size.width, size.height) / 2f * 0.85f
            val currentR = maxR * dynamicFraction

            // Outer Guide Ring (Max breath boundary)
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = maxR,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Inner Boundary Ring (Min breath boundary)
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = maxR * 0.45f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Expanding / Contracting Gradient Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        phaseColor.copy(alpha = 0.55f),
                        phaseColor.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentR
                ),
                radius = currentR,
                center = center
            )

            // Active Breathing Perimeter Ring
            drawCircle(
                color = phaseColor,
                radius = currentR,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Central Information
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${breathingState.secondsRemainingInPhase}s",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = phaseColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = breathingState.phase.label,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = breathingState.phase.sanskritLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.Normal
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = CircleShape
            ) {
                Text(
                    text = "Cycle #${breathingState.cycleCount}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
