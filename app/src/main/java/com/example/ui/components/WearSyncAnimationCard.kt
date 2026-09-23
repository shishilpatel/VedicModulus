package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SyncState {
    IDLE, SYNCING, SUCCESS, ERROR
}

@Composable
fun WearSyncAnimationCard(
    onPerformSync: suspend () -> Boolean,
    modifier: Modifier = Modifier
) {
    var syncState by remember { mutableStateOf(SyncState.IDLE) }
    val scope = rememberCoroutineScope()

    // Infinite rotation for syncing celestial rings animation (only active during sync)
    val infiniteTransition = rememberInfiniteTransition(label = "WearSyncInfinite")
    val rotationAngle by if (syncState == SyncState.SYNCING) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "SyncRotation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (syncState) {
                SyncState.SUCCESS -> AuspiciousGreen.copy(alpha = 0.08f)
                SyncState.SYNCING -> MaroonPrimary.copy(alpha = 0.06f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when (syncState) {
                SyncState.SUCCESS -> AuspiciousGreen.copy(alpha = 0.5f)
                SyncState.SYNCING -> MaroonPrimary.copy(alpha = 0.5f)
                else -> MaroonPrimary.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (syncState) {
                SyncState.IDLE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = null,
                                tint = MaroonPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Wear OS Data Layer Sync",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = MaroonPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Sync Kundli & Complications",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = GrayText,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            syncState = SyncState.SYNCING
                            scope.launch {
                                val success = onPerformSync()
                                syncState = if (success) SyncState.SUCCESS else SyncState.ERROR
                                if (success) {
                                    delay(4000L)
                                    syncState = SyncState.IDLE
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sync_watch_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaroonPrimary,
                            contentColor = OnMaroon
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sync Complications & Watch Data",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            softWrap = false
                        )
                    }
                }

                SyncState.SYNCING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(56.dp)
                        ) {
                            // Rotating planetary orbit ring animation
                            Canvas(modifier = Modifier.size(52.dp)) {
                                drawCircle(
                                    color = MaroonPrimary.copy(alpha = 0.2f),
                                    radius = size.minDimension / 2f,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                                drawArc(
                                    color = OrangeAccent,
                                    startAngle = rotationAngle,
                                    sweepAngle = 110f,
                                    useCenter = false,
                                    style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = "Syncing",
                                tint = MaroonPrimary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(rotationAngle)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Transmitting Planetary & Vedic Data...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Updating Wear OS watch face complications & DataLayer",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GrayText,
                                fontSize = 11.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                SyncState.SUCCESS -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AuspiciousGreen.copy(alpha = 0.2f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = AuspiciousGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Successfully Synced to Wear OS Watch!",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = AuspiciousGreen,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Watch face complications and transit data updated successfully via DataClient.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GrayText,
                                fontSize = 11.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                SyncState.ERROR -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sync Timed Out or Wear OS Disconnected",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { syncState = SyncState.IDLE },
                            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Try Again", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
