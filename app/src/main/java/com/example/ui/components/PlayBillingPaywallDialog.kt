package com.example.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.billing.PlanTier
import com.example.billing.TargetBillingPlatform
import com.example.billing.UnifiedBillingManager
import com.example.billing.VedicPaidPlan
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MaroonPrimary

private val VedicDarkBackground = Color(0xFF160A0D)
private val VedicCardBackground = Color(0xFF231015)
private val VedicSurfaceSecondary = Color(0xFF1B0C10)
private val GoldAccent = GoldPrimary

/**
 * Refactored Subscription Plan Selection UI Component with scalable typography,
 * adaptive spacing across screen sizes, and Vedic-themed visual hierarchy.
 */
@Composable
fun PlayBillingPaywallDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { com.example.core.language.LanguageManager.findActivity(context) }
    val unifiedManager = remember { UnifiedBillingManager.getInstance(context) }

    val billingStatus by unifiedManager.billingStatus.collectAsState()
    val plans by unifiedManager.plans.collectAsState()
    val isProcessing by unifiedManager.isProcessing.collectAsState()
    val lastMessage by unifiedManager.lastMessage.collectAsState()
    val targetPlatform by unifiedManager.activePlatform.collectAsState()

    var activeTabIsAstrologer by remember { mutableStateOf(false) }

    val displayedPlans = remember(plans, activeTabIsAstrologer) {
        if (!activeTabIsAstrologer) {
            plans.filter { it.tier == PlanTier.MONTHLY || it.tier == PlanTier.ANNUAL }
        } else {
            plans.filter { it.tier == PlanTier.ASTROLOGER_PRO_MONTHLY || it.tier == PlanTier.ASTROLOGER_PRO_ANNUAL }
        }
    }

    var selectedPlan by remember(displayedPlans) {
        mutableStateOf(displayedPlans.find { it.isPopular } ?: displayedPlans.firstOrNull() ?: plans.first())
    }

    LaunchedEffect(lastMessage) {
        lastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isProcessing,
            dismissOnClickOutside = !isProcessing
        )
    ) {
        BoxWithConstraints(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val screenWidth = maxWidth
            val cardMaxWidth = if (screenWidth > 600.dp) 480.dp else 390.dp
            val adaptivePadding = if (screenWidth < 360.dp) 10.dp else 14.dp

            Card(
                modifier = Modifier
                    .widthIn(max = cardMaxWidth)
                    .fillMaxWidth(if (screenWidth > 600.dp) 0.75f else 0.92f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("play_billing_paywall_dialog"),
                colors = CardDefaults.cardColors(containerColor = VedicDarkBackground),
                border = BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(GoldAccent.copy(alpha = 0.5f), MaroonPrimary.copy(alpha = 0.3f))
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(adaptivePadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar with Vedic Branding & Dismiss Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(MaroonPrimary, Color(0xFF38141C))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.vedic_sacred_pass_title),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.vedic_sacred_pass_subtitle),
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(VedicSurfaceSecondary)
                                .testTag("close_paywall_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.btn_close),
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Cross-Platform Billing Platform Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF13080A))
                            .border(1.dp, Color(0xFF381B23), RoundedCornerShape(8.dp))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (targetPlatform == TargetBillingPlatform.GOOGLE_PLAY) Color(0xFF0284C7) else Color.Transparent)
                                .clickable { unifiedManager.setTargetPlatform(TargetBillingPlatform.GOOGLE_PLAY) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Shop, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.platform_google_play),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (targetPlatform == TargetBillingPlatform.APPLE_STOREKIT) Color(0xFF10B981) else Color.Transparent)
                                .clickable { unifiedManager.setTargetPlatform(TargetBillingPlatform.APPLE_STOREKIT) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.platform_apple_storekit),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Plan Type Tab Switcher (User Plans vs Astrologer Pro)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(VedicSurfaceSecondary)
                            .border(1.dp, Color(0xFF2A151B), RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!activeTabIsAstrologer) MaroonPrimary else Color.Transparent)
                                .clickable { activeTabIsAstrologer = false }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.billing_user_plans),
                                color = if (!activeTabIsAstrologer) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (activeTabIsAstrologer) MaroonPrimary else Color.Transparent)
                                .clickable { activeTabIsAstrologer = true }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.billing_astrologer_pro),
                                color = if (activeTabIsAstrologer) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Plan Cards List
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        displayedPlans.forEach { plan ->
                            val isSelected = selectedPlan.planId == plan.planId
                            ScalablePlanOptionCard(
                                plan = plan,
                                isSelected = isSelected,
                                onSelect = { selectedPlan = plan }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary Purchase Action Button
                    Button(
                        onClick = {
                            unifiedManager.launchPurchaseFlow(activity, selectedPlan)
                        },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("pay_via_billing_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = Color(0xFF0F172A),
                            disabledContainerColor = Color(0xFF334155),
                            disabledContentColor = Color(0xFF94A3B8)
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF0F172A)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedPlan.trialDays > 0) {
                                        stringResource(R.string.billing_btn_start_trial, selectedPlan.trialDays, selectedPlan.formattedPrice)
                                    } else {
                                        stringResource(R.string.billing_btn_subscribe, selectedPlan.formattedPrice)
                                    },
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Sandbox Test Simulation Button
                    OutlinedButton(
                        onClick = {
                            unifiedManager.launchPurchaseFlow(null, selectedPlan)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("simulate_purchase_button"),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.billing_btn_simulate, selectedPlan.formattedPrice),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.billing_disclaimer_fmt, selectedPlan.formattedPrice, selectedPlan.billingCycleText),
                        color = Color(0xFF94A3B8),
                        fontSize = 8.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Scalable Plan Option Card with proportioned typography, badges, and adaptive paddings.
 */
@Composable
private fun ScalablePlanOptionCard(
    plan: VedicPaidPlan,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) GoldAccent else Color(0xFF2D161C)
    val bgColor = if (isSelected) VedicCardBackground else VedicSurfaceSecondary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(if (isSelected) 1.2.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onSelect() }
            .testTag("plan_card_${plan.tier.name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect() },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = GoldAccent,
                            unselectedColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = plan.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Proportioned Free Trial Badge
                            plan.trialBadge?.let { trialText ->
                                Surface(
                                    color = Color(0xFF064E3B),
                                    shape = RoundedCornerShape(3.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = trialText,
                                        color = Color(0xFF34D399),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            // Proportioned Savings Badge
                            plan.savingsBadge?.let { savings ->
                                Surface(
                                    color = Color(0xFF78350F).copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(3.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = savings,
                                        color = Color(0xFFFBBF24),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = plan.formattedPrice,
                        color = GoldAccent,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = " ${plan.billingCycleText}",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp
                    )
                }
            }

            // Compact Expandable Benefits when selected
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    HorizontalDivider(
                        color = Color(0xFF2D161C),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    plan.benefits.take(3).forEach { benefit ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = benefit,
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
