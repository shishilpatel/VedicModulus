package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun VastuCalibrationDialog(
    onDismiss: () -> Unit
) {
    var calibrationStep by remember { mutableStateOf(1) }
    var isCalibrating by remember { mutableStateOf(false) }
    var calibrationComplete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Vastu Compass Calibration",
                style = MaterialTheme.typography.titleMedium.copy(color = MaroonPrimary, fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaroonPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (calibrationComplete) "✓" else "∞",
                        fontSize = 36.sp,
                        color = if (calibrationComplete) AuspiciousGreen else MaroonPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!calibrationComplete) {
                    Text(
                        text = "Step $calibrationStep of 3: Figure-8 Device Motion",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaroonPrimary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (calibrationStep) {
                            1 -> "Hold your phone flat and wave it in a smooth figure-8 motion through the air to align magnetometer sensors."
                            2 -> "Rotate your device slowly 360 degrees around the pitch and roll axes facing True North."
                            else -> "Keep steady for final sensor filtering and magnetic declination calculation."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Calibration Successful!",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = AuspiciousGreen)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your device magnetometer is now perfectly calibrated for precise Vastu directional calculations.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            if (!calibrationComplete) {
                Button(
                    onClick = {
                        isCalibrating = true
                        if (calibrationStep < 3) {
                            calibrationStep++
                            isCalibrating = false
                        } else {
                            calibrationComplete = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White)
                ) {
                    Text(if (calibrationStep < 3) "Next Step" else "Complete Calibration", color = Color.White)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AuspiciousGreen, contentColor = Color.White)
                ) {
                    Text("Start Using Compass", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!calibrationComplete) {
                TextButton(onClick = onDismiss) {
                    Text("Skip", color = DarkTextSecondary)
                }
            }
        }
    )
}
