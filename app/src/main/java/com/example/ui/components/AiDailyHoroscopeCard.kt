package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.generateHoroscope
import com.example.core.model.BirthProfile
import com.example.core.model.PersonalizedProfile
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AiDailyHoroscopeCard(
    personalizedProfile: PersonalizedProfile,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var horoscopeText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val fetchHoroscope: () -> Unit = {
        isLoading = true
        coroutineScope.launch {
            val celestialData = "Lagna: ${personalizedProfile.natalLagnaRashi.name}, Moon Sign: ${personalizedProfile.natalChandraRashi.name}, Nakshatra: ${personalizedProfile.natalNakshatra.sanskritName}, Dasha: ${personalizedProfile.dashaPeriods.firstOrNull()?.lord?.name ?: "Vimshottari"}"
            val result = generateHoroscope(celestialData, currentLanguage)
            horoscopeText = result
            isLoading = false
        }
    }

    LaunchedEffect(currentLanguage, personalizedProfile) {
        fetchHoroscope()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_horoscope_card")
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
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .background(OrangeAccent.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DAILY HOROSCOPE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Personalized via Gemini & ChatGPT",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Button(
                    onClick = fetchHoroscope,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = OnMaroon),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("refresh_horoscope_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = OnMaroon,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Refresh", fontSize = 11.sp, color = OnMaroon, fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = LightBorder,
                thickness = 0.8.dp
            )

            // Readable, scrollable section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 220.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    if (isLoading && horoscopeText == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaroonPrimary)
                        }
                    } else {
                        Text(
                            text = horoscopeText ?: "Auspicious planetary transits guide your path today toward balance, wisdom, and inner clarity.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
