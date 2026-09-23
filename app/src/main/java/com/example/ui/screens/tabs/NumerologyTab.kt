package com.example.ui.screens.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.PersonalizedProfile
import com.example.engines.numerology.AdvancedNumerologyEngine
import com.example.engines.numerology.NumerologyEngine
import com.example.ui.components.NumerologyGridView
import com.example.ui.components.VedicDatePickerField
import com.example.ui.theme.*
import java.time.LocalDate

@Composable
fun NumerologyTab(
    personalizedProfile: PersonalizedProfile,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val birthDate = personalizedProfile.userBirthProfile.birthDate
    val fullName = personalizedProfile.userBirthProfile.fullName
    val num = personalizedProfile.numerology
    val kingNumber = num.birthNumber
    val queenNumber = num.lifePathNumber

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview & Grids, 1: Mobile Numerology, 2: Dasha Timeline, 3: Number Predictions

    // User editable inputs for calculator testing
    var activeDate by remember { mutableStateOf(birthDate) }
    var testName by remember { mutableStateOf(fullName) }
    var testMobile by remember { mutableStateOf("9913951731") } // default from PDF example

    val activeKing = remember(activeDate) {
        NumerologyEngine().reduceToSingleDigit(activeDate.dayOfMonth)
    }
    val activeQueen = remember(activeDate) {
        val sum = activeDate.dayOfMonth + activeDate.monthValue + activeDate.year
        NumerologyEngine().reduceToSingleDigit(sum)
    }

    // Grids
    val loshuGrid = remember(activeDate, activeKing, activeQueen) {
        AdvancedNumerologyEngine.generateLoshuGrid(activeDate, activeKing, activeQueen)
    }
    val vedicGrid = remember(activeDate, activeKing, activeQueen) {
        AdvancedNumerologyEngine.generateVedicGrid(activeDate, activeKing, activeQueen)
    }

    // Predictions
    val (numberMeanings, combinations) = remember(activeDate, activeKing, activeQueen) {
        AdvancedNumerologyEngine.generateDetailedPredictions(activeDate, activeKing, activeQueen)
    }

    // Mobile Analysis
    val mobileDetails = remember(testMobile, activeKing, activeQueen) {
        AdvancedNumerologyEngine.analyzeMobileNumber(testMobile, activeKing, activeQueen)
    }

    // Dasha Timeline
    val dashaTimeline = remember(activeDate, activeKing) {
        AdvancedNumerologyEngine.generateDashaTimeline(activeDate, activeKing)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // Top Header Banner
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = "Vedic & Lo Shu Numerology",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                )
                Text(
                    text = "Detailed predictions from Date of Birth, Mobile & Sacred Grids",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = MaroonPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("✨ AI Analysis", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Grids & Overview", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Mobile Analysis", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Combinations & Meanings", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("Dasha Timeline", fontSize = 12.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        // Tab Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> NumerologyAiTab(
                    personalizedProfile = personalizedProfile,
                    currentLanguage = currentLanguage,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    GridsOverviewTab(
                        activeDate = activeDate,
                        king = activeKing,
                        queen = activeQueen,
                        loshuGrid = loshuGrid,
                        vedicGrid = vedicGrid,
                        num = num,
                        onDateChange = { activeDate = it }
                    )
                }
                2 -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    MobileAnalysisTab(
                        details = mobileDetails,
                        mobileInput = testMobile,
                        onMobileChange = { testMobile = it }
                    )
                }
                3 -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    CombinationsPredictionsTab(
                        numberMeanings = numberMeanings,
                        combinations = combinations
                    )
                }
                4 -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    DashaTimelineTab(
                        timeline = dashaTimeline,
                        birthDate = activeDate,
                        king = activeKing
                    )
                }
            }
        }
    }
}

@Composable
private fun GridsOverviewTab(
    activeDate: LocalDate,
    king: Int,
    queen: Int,
    loshuGrid: com.example.engines.numerology.NumerologyGrid,
    vedicGrid: com.example.engines.numerology.NumerologyGrid,
    num: com.example.core.model.NumerologyProfile,
    onDateChange: (LocalDate) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Date & Core Numbers Banner (like PDF Page 1)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "%02d-%02d-%04d".format(activeDate.dayOfMonth, activeDate.monthValue, activeDate.year),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaroonPrimary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "King (Mulank) : $king",
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = Color(0xFFE0E7FF),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "Queen (Bhagyank) : $queen",
                            color = Color(0xFF3730A3),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Side-by-Side Grids: Loshu Grid & Vedic Grid (Exact PDF Page 1 layout)
        Text(
            text = "Sacred Numerology Grids",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NumerologyGridView(
                grid = loshuGrid,
                modifier = Modifier.weight(1f),
                accentColor = MaroonPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            NumerologyGridView(
                grid = vedicGrid,
                modifier = Modifier.weight(1f),
                accentColor = Color(0xFF1D4ED8)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Planetary Correspondence & Auspicious Guidance
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Cosmic Ruling Influences",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                InfoRow("King Number Planet", "${num.rulingPlanet.displayName} (${num.rulingPlanet.sanskritName})")
                InfoRow("Friendly Numbers", num.favorableNumbers.joinToString())
                InfoRow("Auspicious Colors", num.favorableColors.joinToString())
                InfoRow("Favorable Days", num.favorableDays.joinToString())

                Spacer(modifier = Modifier.height(10.dp))

                // Numerology Ruling God Mantra (Centered Alignment)
                val (deity, mantra) = when (num.birthNumber) {
                    1 -> "Lord Surya" to "ॐ सूर्याय नमः"
                    2 -> "Lord Shiva / Chandra" to "ॐ सों सोमाय नमः"
                    3 -> "Lord Vishnu / Brihaspati" to "ॐ बृं बृहस्पतये नमः"
                    4 -> "Lord Ganesha / Rahu" to "ॐ रां राहवे नमः"
                    5 -> "Lord Vishnu / Budha" to "ॐ बुं बुधाय नमः"
                    6 -> "Goddess Lakshmi / Shukra" to "ॐ शुं शुक्राय नमः"
                    7 -> "Lord Ganesha / Ketu" to "ॐ कें केतवे नमः"
                    8 -> "Lord Shani / Hanuman" to "ॐ शं शनैश्चराय नमः"
                    9 -> "Lord Hanuman / Mangal" to "ॐ अं अंगारकाय नमः"
                    else -> "Lord Surya" to "ॐ सूर्याय नमः"
                }

                Surface(
                    color = MaroonPrimary.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RULING DEITY: $deity",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mantra,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sacred Japa for Birth Number ${num.birthNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 9.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive DOB Modifier (for testing any birth date immediately)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, LightBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Calculate for another Birth Date",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaroonPrimary
                )
                VedicDatePickerField(
                    date = activeDate,
                    onDateChange = onDateChange,
                    label = "Birth Date (Tap to select)",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "numerology_test_date_picker"
                )
            }
        }
    }
}

@Composable
private fun MobileAnalysisTab(
    details: com.example.engines.numerology.MobileNumerologyDetails,
    mobileInput: String,
    onMobileChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Input Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Advance Mobile Numerology",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                )
                Text(
                    text = "Enter your mobile number to evaluate compound vibration, pairs, and predictions.",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = mobileInput,
                    onValueChange = onMobileChange,
                    label = { Text("Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaroonPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Analysis Table matching PDF Page 2
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Mobile Number Prediction Details",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                TableRowView("Your Mobile Number", details.mobileNumber)
                TableRowView("Your Mobile Number Compound", "Compound : ${details.compoundSum}")
                TableRowView("Your Mobile Number Total", "Total : ${details.singleDigitTotal}\n\n${details.interpretation}")
                TableRowView("Recommended Mobile Total", details.recommendedTotals)
                TableRowView("Lucky Colours", details.luckyColours)
                TableRowView("Un-Lucky Colours", details.unLuckyColours)
                TableRowView("Lucky Numbers", details.luckyNumbers.joinToString())
                TableRowView("Un-Lucky Numbers", details.unLuckyNumbers.joinToString())
                TableRowView("Neutral Numbers", details.neutralNumbers.joinToString())
                TableRowView("Missing Numbers", details.missingNumbers.joinToString(" - "))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mobile Pairs Combinations (PDF Page 3)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Mobile Pairs Combinations",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaroonPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                details.pairsAnalysis.forEach { pairItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pairItem.pair,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1F2937)
                        )
                        Surface(
                            color = when (pairItem.nature) {
                                "Benefic" -> Color(0xFFD1FAE5)
                                "Malefic" -> Color(0xFFFEE2E2)
                                else -> Color(0xFFF3F4F6)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = pairItem.nature,
                                color = when (pairItem.nature) {
                                    "Benefic" -> Color(0xFF065F46)
                                    "Malefic" -> Color(0xFF991B1B)
                                    else -> Color(0xFF4B5563)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 0.5.dp)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Key Behavioral Predictions",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                details.predictions.forEach { pred ->
                    Row(modifier = Modifier.padding(vertical = 3.dp)) {
                        Text("• ", color = MaroonPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            text = pred,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFF374151)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CombinationsPredictionsTab(
    numberMeanings: List<com.example.engines.numerology.NumberMeaningItem>,
    combinations: List<com.example.engines.numerology.CombinationItem>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Number Repetitions & Core Traits",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaroonPrimary
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        numberMeanings.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFF4B5563)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Chart Number Combinations",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D4ED8)
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        combinations.forEach { comb ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = comb.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E40AF)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = comb.description,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFF374151)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashaTimelineTab(
    timeline: List<com.example.engines.numerology.NumerologyDashaPeriod>,
    birthDate: LocalDate,
    king: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Vedic Numerology Dasha System",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = "Lifelong planetary periods calculated from your birth date and King number ($king).",
                    fontSize = 11.sp,
                    color = Color(0xFFB45309)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        timeline.forEach { period ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (period.isCurrent) Color(0xFFFFFBEB) else Color.White
                ),
                border = BorderStroke(
                    if (period.isCurrent) 1.5.dp else 1.dp,
                    if (period.isCurrent) OrangeAccent else Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dasha is : ${period.dateRangeStr}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (period.isCurrent) OrangeAccent else Color(0xFF1F2937)
                        )
                        if (period.isCurrent) {
                            Surface(
                                color = OrangeAccent,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE NOW",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Planet : ${period.planetName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaroonPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = period.prediction,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFF4B5563)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF6B7280), fontSize = 13.sp)
        Text(text = value, color = Color(0xFF111827), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun TableRowView(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF374151),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color(0xFF111827),
                modifier = Modifier.weight(1.3f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 0.5.dp)
    }
}
