package com.example.ui.screens.tabs

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.AstrologerProfile
import com.example.core.model.AvailabilityStatus
import com.example.core.model.UserAccount
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MaroonPrimary

private val GoldAccent = GoldPrimary

/**
 * Astrologer Registration, Consultation Rates Definition, and Live Availability Status Management Tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstrologerRatesAndStatusTab(
    userAccount: UserAccount,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Local state for astrologer registration profile
    var astrologerProfile by remember {
        mutableStateOf(
            AstrologerProfile(
                fullName = userAccount.fullName.ifBlank { "Acharya Vikramaditya Shastri" },
                phoneOrEmail = userAccount.email,
                titleOrLicense = userAccount.astrologerTitleOrLicense.ifBlank { "Jyotish Ratna & Vastu Shastracharya" }
            )
        )
    }

    var hourlyRate by remember { mutableFloatStateOf(astrologerProfile.hourlyConsultationRateInr.toFloat()) }
    var perQueryRate by remember { mutableFloatStateOf(astrologerProfile.perQuestionRateInr.toFloat()) }
    var reportRate by remember { mutableFloatStateOf(astrologerProfile.detailedReportRateInr.toFloat()) }

    var selectedStatus by remember { mutableStateOf(astrologerProfile.availabilityStatus) }
    var isAcceptingQueries by remember { mutableStateOf(astrologerProfile.isAvailableForConsultation) }

    var experienceYears by remember { mutableIntStateOf(astrologerProfile.experienceYears) }
    var bioText by remember { mutableStateOf(astrologerProfile.bio) }
    var titleText by remember { mutableStateOf(astrologerProfile.titleOrLicense) }

    val specializationsList = remember {
        listOf("Vedic Astrology", "Janam Kundali", "Vastu Shastra", "Kundali Matching", "Prashna Kundali", "Numerology", "KP System", "Nadi Astrology")
    }
    var selectedSpecs by remember { mutableStateOf(astrologerProfile.specializations.toSet()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1115)),
            border = BorderStroke(1.dp, MaroonPrimary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaroonPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = astrologerProfile.fullName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Scholar",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = titleText,
                        color = GoldAccent,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${experienceYears} Years Exp • ⭐ ${astrologerProfile.rating} (${astrologerProfile.reviewCount} Reviews)",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.5.sp
                    )
                }
            }
        }

        // Live Availability Status Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Consultation Availability Status",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Live Status Badge Indicator
                    Surface(
                        color = Color(selectedStatus.badgeColorHex).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(selectedStatus.badgeColorHex)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(selectedStatus.badgeColorHex))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedStatus.name,
                                color = Color(selectedStatus.badgeColorHex),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Three Status Toggle Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AvailabilityStatus.values().forEach { status ->
                        val isSelected = selectedStatus == status
                        val statusColor = Color(status.badgeColorHex)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) statusColor.copy(alpha = 0.25f) else Color(0xFF0F172A))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) statusColor else Color(0xFF334155),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedStatus = status }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = status.name,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Accept 1-on-1 Client Queries",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Receive notifications when users request ₹199 consultations",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.5.sp
                        )
                    }

                    Switch(
                        checked = isAcceptingQueries,
                        onCheckedChange = { isAcceptingQueries = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF0F172A),
                            checkedTrackColor = GoldAccent
                        )
                    )
                }
            }
        }

        // Consultation Rates Definition Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CurrencyRupee,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set Your Consultation Rates (₹ INR)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hourly Rate Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Hourly Consultation Rate:", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        Text("₹${hourlyRate.toInt()} / hour", color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = hourlyRate,
                        onValueChange = { hourlyRate = it },
                        valueRange = 199f..2499f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldAccent,
                            activeTrackColor = GoldAccent,
                            inactiveTrackColor = Color(0xFF334155)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Per Question Written Query Rate Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Per Written Question Query Rate:", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        Text("₹${perQueryRate.toInt()} / query", color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = perQueryRate,
                        onValueChange = { perQueryRate = it },
                        valueRange = 99f..999f,
                        steps = 17,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldAccent,
                            activeTrackColor = GoldAccent,
                            inactiveTrackColor = Color(0xFF334155)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 30-Page Custom Kundali PDF Report Rate
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("30-Page Custom Verified Life Report:", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        Text("₹${reportRate.toInt()} / report", color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = reportRate,
                        onValueChange = { reportRate = it },
                        valueRange = 299f..1999f,
                        steps = 16,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldAccent,
                            activeTrackColor = GoldAccent,
                            inactiveTrackColor = Color(0xFF334155)
                        )
                    )
                }
            }
        }

        // Astrologer Profile Registration & Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Astrologer Professional Details",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Title / Scholastic License") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bioText,
                    onValueChange = { bioText = it },
                    label = { Text("Scholar Bio & Lineage Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Specialization Fields:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OptInFlowRow(
                    specializations = specializationsList,
                    selected = selectedSpecs,
                    onToggle = { spec ->
                        selectedSpecs = if (selectedSpecs.contains(spec)) {
                            selectedSpecs - spec
                        } else {
                            selectedSpecs + spec
                        }
                    }
                )
            }
        }

        // Save & Publish Profile Button
        Button(
            onClick = {
                astrologerProfile = astrologerProfile.copy(
                    titleOrLicense = titleText,
                    bio = bioText,
                    hourlyConsultationRateInr = hourlyRate.toDouble(),
                    perQuestionRateInr = perQueryRate.toDouble(),
                    detailedReportRateInr = reportRate.toDouble(),
                    availabilityStatus = selectedStatus,
                    isAvailableForConsultation = isAcceptingQueries,
                    specializations = selectedSpecs.toList()
                )
                Toast.makeText(context, "Astrologer Profile & Rates Saved!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_astrologer_rates_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF0F172A))
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Profile & Consultation Rates", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OptInFlowRow(
    specializations: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        specializations.chunked(3).forEach { rowSpecs ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowSpecs.forEach { spec ->
                    val isSelected = selected.contains(spec)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggle(spec) },
                        label = { Text(spec, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaroonPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}
