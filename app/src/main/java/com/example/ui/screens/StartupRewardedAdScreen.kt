package com.example.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdState
import com.example.ads.RewardedAdManager
import com.example.billing.PlayBillingManager
import com.example.ui.components.PlayBillingPaywallDialog
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

/**
 * Startup screen that requires the user to watch a full 30-second rewarded ad
 * before unlocking access and navigating to the main app dashboard,
 * or allows upgrading to a paid Ad-Free plan via Google Play Store.
 */
@Composable
fun StartupRewardedAdScreen(
    onAdFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adManager = remember { RewardedAdManager.getInstance() }
    val billingManager = remember { PlayBillingManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    val billingStatus by billingManager.billingStatus.collectAsState()
    var showPaywallDialog by remember { mutableStateOf(false) }

    // If user has or activates a paid ad-free plan via Google Play, immediately finish ad screen!
    LaunchedEffect(billingStatus.isAdFree) {
        if (billingStatus.isAdFree) {
            onAdFinished()
        }
    }

    val adState by adManager.adState.collectAsState()
    val creative by adManager.currentCreative.collectAsState()
    val remainingSeconds by adManager.remainingSeconds.collectAsState()
    val playbackProgress by adManager.playbackProgress.collectAsState()
    val isRewardEarned by adManager.isRewardEarned.collectAsState()
    val isMuted by adManager.isMuted.collectAsState()

    // Smooth animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = playbackProgress,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "ad_progress"
    )

    var hasUserOptedInToAd by remember { mutableStateOf(false) }

    // Load ad on startup, but do not auto-start playback
    LaunchedEffect(Unit) {
        if (adState == AdState.IDLE) {
            adManager.loadAd(scope) {}
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("startup_rewarded_ad_screen"),
        containerColor = Color(0xFF090D16) // Deep celestial dark canvas
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Top Linear Countdown Progress Bar
            if (hasUserOptedInToAd) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .testTag("ad_progress_bar"),
                    color = if (isRewardEarned) Color(0xFF10B981) else Color(0xFFFFB703),
                    trackColor = Color(0x33334155)
                )
            }

            // 2. Header Bar: Sponsored Tag, Sound Mute Button & Live Countdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sponsored Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0x44FFB703), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Sponsor",
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SPONSORED REWARD",
                        color = Color(0xFFFFB703),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ad-Free Google Play Button
                    Surface(
                        onClick = { showPaywallDialog = true },
                        color = Color(0xFF78350F),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFB703)),
                        modifier = Modifier.testTag("startup_go_ad_free_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Ad-Free",
                                tint = Color(0xFFFFB703),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Remove Ads",
                                color = Color(0xFFFFB703),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Mute / Unmute Toggle
                    IconButton(
                        onClick = { adManager.toggleMute() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Timer Pill
                    Surface(
                        color = if (isRewardEarned) Color(0xFF065F46) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isRewardEarned) Color(0xFF10B981) else Color(0x6694A3B8)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            if (!isRewardEarned) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFFFFB703)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reward in ${remainingSeconds}s",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reward Granted",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Ad Main Showcase Card
            creative?.let { ad ->
                val accentColor = Color(ad.accentColorHex)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("ad_creative_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF131B2E)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(accentColor.copy(alpha = 0.6f), Color(0x221E293B))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Category Pill & Rating
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ad.category.uppercase(),
                                color = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFB703),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${ad.rating} (${ad.reviewsCount})",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Advertiser Avatar & Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accentColor.copy(alpha = 0.2f))
                                    .border(1.dp, accentColor, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ad.advertiser,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Sponsor",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.startup_ad_verified),
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Headline
                        Text(
                            text = ad.headline,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Body Description
                        Text(
                            text = ad.body,
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Feature Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ad.features.forEach { feature ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "✓ $feature",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Interactive Sponsor CTA Pill
                        OutlinedButton(
                            onClick = { /* In-app sponsor exploration */ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = accentColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ad.callToAction,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // 4. Mandatory 30-second Completion Gateway Notice & Proceed Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Info Explainer
                Text(
                    text = if (!isRewardEarned) {
                        "⏳ Complete this 30-second ad to unlock your dashboard and activate a 12-Hour Ad-Free Dharma Pass."
                    } else {
                        "✨ Ad completed! 12-Hour Dharma Pass activated — enjoy uninterrupted ad-free calculations and guidance."
                    },
                    color = if (isRewardEarned) Color(0xFF34D399) else Color(0xFF94A3B8),
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Unlock / Proceed Button
                Button(
                    onClick = {
                        if (!hasUserOptedInToAd) {
                            hasUserOptedInToAd = true
                            adManager.startPlayback(scope)
                        } else if (isRewardEarned) {
                            onAdFinished()
                        }
                    },
                    enabled = !hasUserOptedInToAd || isRewardEarned,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("proceed_to_app_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        disabledContainerColor = Color(0xFF1E293B),
                        contentColor = Color.White,
                        disabledContentColor = Color(0xFF64748B)
                    )
                ) {
                    if (!hasUserOptedInToAd) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Ad",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.startup_ad_unlock),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (isRewardEarned) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.startup_ad_continue),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Enter Dashboard",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Playing Ad (${remainingSeconds}s remaining)...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional Google Play Ad-Free Upgrade Banner
                Surface(
                    onClick = { showPaywallDialog = true },
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0x66FFB703)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("startup_play_store_plan_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFFFB703),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.startup_ad_skip_prompt),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.startup_ad_plans),
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.startup_ad_view_plans),
                            color = Color(0xFFFFB703),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showPaywallDialog) {
        PlayBillingPaywallDialog(
            onDismiss = { showPaywallDialog = false }
        )
    }
}
