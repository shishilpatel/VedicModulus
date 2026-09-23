package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressAnim = remember { Animatable(0f) }
    var currentStepText by remember { mutableStateOf("Awakening Cosmic Jyotish...") }

    // Continuous rotation for the circular cosmic loader arc
    val infiniteTransition = rememberInfiniteTransition(label = "SplashLoaderInfinite")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LoaderRotation"
    )

    // Breathing glow animation around the center loader ring
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    // Smooth, fast splash transition (1.2s total so preview never freezes)
    LaunchedEffect(Unit) {
        currentStepText = "Awakening Cosmic Jyotish..."
        progressAnim.animateTo(0.40f, animationSpec = tween(350, easing = FastOutSlowInEasing))

        currentStepText = "Aligning Planetary Coordinates & Rashis..."
        progressAnim.animateTo(0.75f, animationSpec = tween(350, easing = FastOutSlowInEasing))

        currentStepText = "Calculating Panchanga & Auspicious Muhurtas..."
        progressAnim.animateTo(1f, animationSpec = tween(350, easing = LinearEasing))

        delay(150)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("splash_screen")
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Immediate skip on tap anytime
                onSplashFinished()
            }
    ) {
        // 1. Full-bleed background artwork provided by user
        Image(
            painter = painterResource(id = R.drawable.img_splash_bg),
            contentDescription = "Vedic Cosmic Ghadi Splash Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Cosmic Vignette overlay for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )

        // 3. Center Loader Section
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val loaderDiameter = 120.dp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 68.dp)
            ) {
                // Circular Loader nested inside the center round section
                Box(
                    modifier = Modifier
                        .size(loaderDiameter)
                        .testTag("splash_center_loader"),
                    contentAlignment = Alignment.Center
                ) {
                    // Subtle ambient glow behind the loader
                    Box(
                        modifier = Modifier
                            .size(loaderDiameter * 0.95f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x55FFB703),
                                        Color(0x22FFAA00),
                                        Color.Transparent
                                    )
                                )
                            )
                            .alpha(glowAlpha)
                    )

                    // Active animated rotating loader canvas
                    Canvas(modifier = Modifier.size(loaderDiameter * 0.78f)) {
                        val strokeWidth = 5.5.dp.toPx()
                        val padding = strokeWidth / 2f
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val arcOffset = Offset(padding, padding)

                        // Base track ring (dark translucent gold)
                        drawArc(
                            color = Color(0x33FFB703),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth * 0.65f)
                        )

                        // Rotating golden gradient arc
                        rotate(degrees = rotationAngle) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x33FFB703),
                                        Color(0xFFFFB703),
                                        Color(0xFFFFF4B8),
                                        Color.White
                                    )
                                ),
                                startAngle = 0f,
                                sweepAngle = 260f,
                                useCenter = false,
                                topLeft = arcOffset,
                                size = arcSize,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }

                    // Center percentage
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${(progressAnim.value * 100).toInt()}%",
                            color = Color(0xFFFFD166),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Loading Status pill beneath the round loader
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x880F172A))
                        .padding(horizontal = 18.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = currentStepText,
                        color = Color(0xFFF1F5F9),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 4. Footer notice
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.splash_tap_enter),
                color = Color(0xCCFFFFFF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
        }
    }
}
