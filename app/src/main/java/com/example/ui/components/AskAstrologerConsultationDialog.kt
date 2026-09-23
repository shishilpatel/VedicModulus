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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.billing.PlanTier
import com.example.billing.PlayBillingManager
import com.example.billing.VedicPaidPlan
import com.example.core.model.BirthProfile
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.GoldPrimary

private val GoldAccent = GoldPrimary

/**
 * Dialog enabling users to submit 1-on-1 written queries for ₹199
 * or order a verified 30-Page Custom Kundali & Vastu Audit PDF report for ₹499.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskAstrologerConsultationDialog(
    birthProfile: BirthProfile,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { com.example.core.language.LanguageManager.findActivity(context) }
    val billingManager = remember { PlayBillingManager.getInstance(context) }

    val plans by billingManager.plans.collectAsState()
    val isProcessing by billingManager.isProcessingPurchase.collectAsState()
    val lastMessage by billingManager.lastBillingMessage.collectAsState()

    var selectedCategory by remember { mutableStateOf("Career & Wealth") }
    var userQuestionText by remember { mutableStateOf("") }
    var isReportOptionSelected by remember { mutableStateOf(false) } // false = Query (₹199), true = Report (₹499)

    val queryPlan = remember(plans) {
        plans.find { it.tier == PlanTier.ASTROLOGER_QUERY } ?: VedicPaidPlan.DEFAULT_PLANS[2]
    }
    val reportPlan = remember(plans) {
        plans.find { it.tier == PlanTier.ASTROLOGER_REPORT } ?: VedicPaidPlan.DEFAULT_PLANS[3]
    }

    val activePlan = if (isReportOptionSelected) reportPlan else queryPlan

    LaunchedEffect(lastMessage) {
        lastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            billingManager.clearLastMessage()
        }
    }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            dismissOnBackPress = !isProcessing,
            dismissOnClickOutside = !isProcessing
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("ask_astrologer_consultation_dialog"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(
                1.5.dp,
                Brush.verticalGradient(listOf(Color(0xFFFFB703), Color(0xFF800000)))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaroonPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ask Verified Astrologer",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "1-on-1 Written Query & Full Life Audits",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option Toggle Card (₹199 vs ₹499)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isReportOptionSelected) MaroonPrimary else Color.Transparent)
                            .clickable { isReportOptionSelected = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Written Query",
                                color = if (!isReportOptionSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹199 / question",
                                color = if (!isReportOptionSelected) GoldAccent else Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isReportOptionSelected) MaroonPrimary else Color.Transparent)
                            .clickable { isReportOptionSelected = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Full Life Report",
                                color = if (isReportOptionSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹499 / 30-Page PDF",
                                color = if (isReportOptionSelected) GoldAccent else Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isReportOptionSelected) {
                    // Category Selection Chips
                    Text(
                        text = "SELECT QUERY TOPIC",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val categories = listOf("Career & Wealth", "Marriage & Compatibility", "Health & Energy", "Vastu & Remedies", "General Advice")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaroonPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = userQuestionText,
                        onValueChange = { userQuestionText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        placeholder = { Text("Describe your query or situation in detail for the astrologer...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF131B2E),
                            unfocusedContainerColor = Color(0xFF131B2E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    // Report Overview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("30-Page Hand-Verified Life Audit", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Janam Kundali & D9 Navamsha Divisional Breakdown", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("• 10-Year Vimshottari Dasha & Gochar Transit Timeline", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("• Room-by-Room Directional Vastu Audit & Remedies", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text("• Astrologer Signed Certification PDF delivered in-app", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile Summary Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("For: ${birthProfile.fullName}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${birthProfile.birthDate} at ${birthProfile.birthTime} • ${birthProfile.birthLocation.cityName}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Order Button
                Button(
                    onClick = {
                        activity?.let { act ->
                            billingManager.launchBillingFlow(act, activePlan)
                        } ?: run {
                            billingManager.simulateGooglePlayPurchase(activePlan)
                        }
                    },
                    enabled = !isProcessing && (isReportOptionSelected || userQuestionText.isNotBlank()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = Color(0xFF0F172A),
                        disabledContainerColor = Color(0xFF334155),
                        disabledContentColor = Color(0xFF94A3B8)
                    )
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF0F172A))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Submit for ${activePlan.formattedPrice}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sandbox Simulator Button
                OutlinedButton(
                    onClick = {
                        billingManager.simulateGooglePlayPurchase(activePlan)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Simulate Order (${activePlan.formattedPrice} Sandbox Test)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
