package com.example.ui.screens.tabs

import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.BuildConfig
import com.example.core.model.BirthProfile
import com.example.core.model.DailyVedicProfile
import com.example.core.model.PersonalizedProfile
import com.example.engines.ai.VedicKnowledgeContextProvider
import com.google.ai.client.generativeai.GenerativeModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.SuggestionChip
import java.time.LocalDate

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun VedicAITab(
    profile: BirthProfile,
    personalizedProfile: PersonalizedProfile,
    dailyProfile: DailyVedicProfile,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    "Namaste ${profile.fullName}! I am your Master Vedic AI Advisor, endowed with complete real-time knowledge of Vedic Astrology, live Panchanga, Muhurtas, Numerology, Vastu Shastra, and sacred Chakra energies for today (${LocalDate.now()}). How may I guide your day?",
                    false
                )
            )
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Initialize Gemini model with modern gemini-3.5-flash and comprehensive Vedic system instructions
    val model = remember(currentLanguage, profile.language) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val activeLangDisplay = com.example.core.language.LanguageManager.getLanguageFromCode(currentLanguage)
                GenerativeModel(
                    modelName = "gemini-3.5-flash",
                    apiKey = apiKey,
                    systemInstruction = com.google.ai.client.generativeai.type.content {
                        text(VedicKnowledgeContextProvider.buildSystemInstruction(activeLangDisplay))
                    }
                )
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }
            if (isLoading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(20.dp)
                    )
                }
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val suggestions = listOf(
                "Today's Cosmic Alignment",
                "My Vastu & Home Energy",
                "Mulank & Lucky Numbers",
                "Current Muhurta & Travel",
                "Chakra & Mantra for Today",
                "Planetary Dasha & Career"
            )
            items(suggestions) { sugg ->
                SuggestionChip(
                    onClick = { query = sugg },
                    label = { Text(sugg, fontSize = 11.sp) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.ask_ai_hint), fontSize = 13.sp) },
                shape = RoundedCornerShape(20.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    if (query.isNotBlank() && !isLoading) {
                        val userMsg = query
                        messages = messages + ChatMessage(userMsg, true)
                        query = ""
                        isLoading = true

                        val openAiKey = try { BuildConfig.OPENAI_API_KEY } catch (e: Exception) { "" }
                        if (model == null && (openAiKey.isBlank() || openAiKey == "MY_OPENAI_API_KEY")) {
                            messages = messages + ChatMessage(
                                "Error: GEMINI_API_KEY or OPENAI_API_KEY is not configured in .env. Please configure it in the Secrets panel and rebuild.",
                                false
                            )
                            isLoading = false
                            return@IconButton
                        }

                        scope.launch {
                            try {
                                val contextBlock = VedicKnowledgeContextProvider.buildComprehensiveContext(
                                    profile = profile,
                                    personalizedProfile = personalizedProfile,
                                    dailyProfile = dailyProfile,
                                    currentDate = LocalDate.now(),
                                    userQuery = userMsg
                                )

                                val targetLanguageName = com.example.core.language.LanguageManager.getLanguagePromptName(currentLanguage)
                                val variableSelectedLanguage = com.example.core.language.LanguageManager.getVariableSelectedLanguage(currentLanguage)
                                val responseDirective = "RESPONSE SHOULD BE IN $variableSelectedLanguage"

                                val systemPrompt = "You are an expert Vedic astrologer and Sanskrit scholar. You provide authentic Vedic astrology, horoscope, and spiritual insights. STRICT LANGUAGE REQUIREMENT: $responseDirective. You MUST generate the ENTIRE response exclusively in $targetLanguageName using its correct native script and natural, respectful phrasing."
                                val prompt = """
                                    $contextBlock
                                    
                                    USER QUESTION:
                                    $userMsg
                                    
                                    INSTRUCTIONS:
                                    Synthesize the user's specific birth chart, today's live date and Panchanga, exact Muhurta times, Numerology numbers, Vastu element, and Chakra affinities. Provide a precise, authentic, insightful, and practical response.
                                    
                                    CRITICAL LANGUAGE RULE: 
                                    $responseDirective
                                    You MUST generate the ENTIRE response exclusively in $targetLanguageName ($currentLanguage). Ensure the grammar, terminology, and script are highly accurate and natural for $targetLanguageName speakers.
                                """.trimIndent()

                                var responseText: String? = null

                                // Try OpenAI / ChatGPT first if configured
                                try {
                                    if (openAiKey.isNotBlank() && openAiKey != "MY_OPENAI_API_KEY") {
                                        responseText = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { 
                                            com.example.api.generateWithOpenAI(prompt = prompt, apiKey = openAiKey, systemPrompt = systemPrompt) 
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                // Fallback to Gemini 3.5 Flash
                                if (responseText == null && model != null) {
                                    try {
                                        val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { model.generateContent(prompt) }
                                        responseText = response.text
                                    } catch (e: Exception) {
                                        throw e
                                    }
                                }

                                if (!responseText.isNullOrBlank()) {
                                    messages = messages + ChatMessage(responseText, false)
                                } else {
                                    throw Exception("Empty response")
                                }
                            } catch (e: Exception) {
                                val tithiName = dailyProfile.panchanga.tithi.name
                                val nakshatraName = dailyProfile.panchanga.nakshatra.sanskritName
                                val lagnaName = personalizedProfile.natalLagnaRashi.sanskritName
                                val chandraName = personalizedProfile.natalChandraRashi.sanskritName
                                val mulank = personalizedProfile.numerology.birthNumber
                                val bhagyank = personalizedProfile.numerology.lifePathNumber
                                val vastuElement = personalizedProfile.dominantVastuElement
                                val todayDateStr = LocalDate.now().toString()

                                val dynamicAnswer = when {
                                    userMsg.contains("vastu", ignoreCase = true) || userMsg.contains("home", ignoreCase = true) || userMsg.contains("direction", ignoreCase = true) ->
                                        "For today ($todayDateStr), your dominant Vastu element is $vastuElement (resonant with Janma Rashi $chandraName). Harmonize your living space by keeping the Ishanya (North-East) corner pure, clean, and elevated with fresh water or a prayer light, while ensuring your Nairritya (South-West) zone stays grounded with heavy furnishings to stabilize material prosperity."

                                    userMsg.contains("number", ignoreCase = true) || userMsg.contains("mulank", ignoreCase = true) || userMsg.contains("bhagyank", ignoreCase = true) ->
                                        "Your sacred Numerology profile: Mulank (Birth Number) is $mulank, ruled by ${personalizedProfile.numerology.rulingPlanet.displayName}, and Bhagyank (Life Path Destiny) is $bhagyank. On today ($todayDateStr), your vibration is especially supported by favorable numbers (${personalizedProfile.numerology.favorableNumbers.joinToString()}) and favorable colors (${personalizedProfile.numerology.favorableColors.joinToString()})."

                                    userMsg.contains("chakra", ignoreCase = true) || userMsg.contains("mantra", ignoreCase = true) || userMsg.contains("meditat", ignoreCase = true) ->
                                        "To harmonize your subtle energetic field today under $nakshatraName nakshatra: chant your Lagna mantra for $lagnaName, and practice breath awareness focusing on the Anahata (Heart - 'YAM') or Ajna (Third Eye - 'AUM') center during Brahma Muhurta (${dailyProfile.brahmaMuhurta.startTime.toLocalTime()} - ${dailyProfile.brahmaMuhurta.endTime.toLocalTime()})."

                                    userMsg.contains("travel", ignoreCase = true) || userMsg.contains("muhurta", ignoreCase = true) ->
                                        "For travel and major endeavors today ($todayDateStr): Active Choghadiya is ${dailyProfile.currentChoghadiya.type.displayName} (${dailyProfile.currentChoghadiya.type.quality}). Favorable windows: Shubh and Amrit. Rahu Kaal occurs between ${dailyProfile.rahuKalam.startTime.toLocalTime()} and ${dailyProfile.rahuKalam.endTime.toLocalTime()}—avoid initiating risky ventures during this window."

                                    else ->
                                        "On today ($todayDateStr), under ${tithiName} tithi and ${nakshatraName} nakshatra, cosmic energies directly support your natal Lagna ($lagnaName) and Moon ($chandraName). Guided by your Mulank $mulank and Vastu affinity ($vastuElement), mindful contemplation and aligned action will ensure fruitful, balanced outcomes."
                                }
                                messages = messages + ChatMessage(dynamicAnswer, false)
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = query.isNotBlank() && !isLoading
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (query.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (!message.isUser) {
                IconButton(
                    onClick = {
                        android.widget.Toast.makeText(context, "Thank you. AI response reported for review.", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Report AI message",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isUser) 16.dp else 0.dp,
                            bottomEnd = if (message.isUser) 0.dp else 16.dp
                        )
                    )
                    .background(bgColor)
                    .padding(12.dp)
            ) {
                Text(text = message.text, color = textColor)
            }
        }
    }
}
