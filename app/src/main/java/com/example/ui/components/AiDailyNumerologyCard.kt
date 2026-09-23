package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.api.generateWithOpenAI
import com.example.core.model.BirthProfile
import com.example.engines.numerology.NumerologyEngine
import com.example.ui.theme.*
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

@Composable
fun AiDailyNumerologyCard(
    birthProfile: BirthProfile,
    currentDate: ZonedDateTime = ZonedDateTime.now(),
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val numerologyEngine = remember { NumerologyEngine() }

    val numerologyProfile = remember(birthProfile) {
        numerologyEngine.calculateNumerology(birthProfile.birthDate, birthProfile.fullName, birthProfile.numerologySystem)
    }

    val lifePath = numerologyProfile.lifePathNumber
    val moolank = numerologyProfile.birthNumber
    val dailyNumber = remember(birthProfile.birthDate, currentDate) {
        val sum = birthProfile.birthDate.dayOfMonth + birthProfile.birthDate.monthValue + currentDate.year + currentDate.monthValue + currentDate.dayOfMonth
        numerologyEngine.reduceToSingleDigit(sum)
    }

    var numerologyReading by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val fetchNumerologyReading: () -> Unit = {
        isLoading = true
        coroutineScope.launch {
            val targetLanguageName = com.example.core.language.LanguageManager.getLanguagePromptName(currentLanguage)
            val variableSelectedLanguage = com.example.core.language.LanguageManager.getVariableSelectedLanguage(currentLanguage)
            val responseDirective = "RESPONSE SHOULD BE IN $variableSelectedLanguage"

            val systemPrompt = "You are an expert master numerologist specializing in Vedic Sankhya Shastra. STRICT LANGUAGE REQUIREMENT: $responseDirective. You MUST generate the ENTIRE response exclusively in $targetLanguageName using its authentic script and natural vocabulary."
            val prompt = """
                Provide a concise, inspiring, and insightful Daily Numerology reading for:
                - Name: ${birthProfile.fullName}
                - Life Path Number: $lifePath
                - Psychic/Birth Number (Moolank): $moolank
                - Today's Personal Vibration Number: $dailyNumber
                
                Provide 2-3 short paragraphs covering:
                1. The unique numerical energy governing today.
                2. Practical guidance for decision-making, professional focus, and personal harmony.
                3. A lucky daily affirmation.
                
                Tone: Uplifting, clear, and spiritual.
                CRITICAL LANGUAGE RULE: 
                $responseDirective
                Respond 100% in $targetLanguageName ($currentLanguage).
            """.trimIndent()

            val apiKey = BuildConfig.GEMINI_API_KEY
            val openAiKey = try { BuildConfig.OPENAI_API_KEY } catch (_: Exception) { "" }

            var result: String? = null

            // Try OpenAI / ChatGPT first
            if (openAiKey.isNotBlank() && openAiKey != "MY_OPENAI_API_KEY") {
                try {
                    result = withContext(Dispatchers.IO) { 
                        generateWithOpenAI(prompt = prompt, apiKey = openAiKey, systemPrompt = systemPrompt) 
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Fallback to Gemini
            if (result.isNullOrBlank() && apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    val model = GenerativeModel(modelName = "gemini-3.5-flash", apiKey = apiKey)
                    val response = withContext(Dispatchers.IO) { model.generateContent(prompt) }
                    result = response.text
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Default fallback
            if (result.isNullOrBlank()) {
                result = "Today's personal vibration number is $dailyNumber, governed by Life Path $lifePath. Focus your energy on creative alignment, mindful discipline, and purposeful action."
            }

            numerologyReading = result
            isLoading = false
        }
    }

    LaunchedEffect(currentLanguage, birthProfile) {
        fetchNumerologyReading()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaroonPrimary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_numerology_card")
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
                            .background(MaroonPrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaroonPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DAILY NUMEROLOGY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Life Path $lifePath • Daily Vibration $dailyNumber",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Button(
                    onClick = fetchNumerologyReading,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = OnMaroon),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("refresh_numerology_button")
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
                    if (isLoading && numerologyReading == null) {
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
                            text = numerologyReading ?: "Today's vibration number $dailyNumber amplifies your soul foundation ($moolank). Embrace balanced action and focused inner growth.",
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
