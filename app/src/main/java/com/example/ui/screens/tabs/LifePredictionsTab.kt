package com.example.ui.screens.tabs

import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.core.model.BirthProfile
import com.example.core.model.DailyVedicProfile
import com.example.core.model.PersonalizedProfile
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.data.room.VedicDatabase
import com.example.data.room.PredictionCacheEntity
import com.example.data.VedicWearableSyncManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

@Composable
fun LifePredictionsTab(
    profile: BirthProfile,
    personalizedProfile: PersonalizedProfile,
    dailyProfile: DailyVedicProfile,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    var predictionData by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var showPastReadings by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val cacheDao = remember { VedicDatabase.getDatabase(context).predictionCacheDao() }
    
    val numerology = personalizedProfile.numerology
    val lagna = personalizedProfile.natalLagnaRashi.sanskritName
    val chandra = personalizedProfile.natalChandraRashi.sanskritName
    val nakshatra = personalizedProfile.natalNakshatra.sanskritName
    
    val model = remember(currentLanguage, profile.language) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val activeLangDisplay = com.example.core.language.LanguageManager.getLanguageFromCode(currentLanguage)
                GenerativeModel(
                    modelName = "gemini-3.5-flash",
                    apiKey = apiKey,
                    systemInstruction = com.google.ai.client.generativeai.type.content {
                        text(com.example.engines.ai.VedicKnowledgeContextProvider.buildSystemInstruction(activeLangDisplay))
                    }
                )
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val generatePrediction = {
        val openAiKey = try { BuildConfig.OPENAI_API_KEY } catch(e: Exception) { "" }
        if ((openAiKey.isBlank() || openAiKey == "MY_OPENAI_API_KEY") && model == null) {
            errorMessage = "Please configure your OPENAI_API_KEY or GEMINI_API_KEY in the Secrets panel (in .env) to enable AI predictions."
        } else {
            isLoading = true
            errorMessage = null
            scope.launch {
                val contextBlock = com.example.engines.ai.VedicKnowledgeContextProvider.buildComprehensiveContext(
                    profile = profile,
                    personalizedProfile = personalizedProfile,
                    dailyProfile = dailyProfile,
                    currentDate = java.time.LocalDate.now(),
                    userQuery = "Complete Life Predictions: Astrology, Numerology, Vastu & Remedies"
                )

                val targetLanguageName = com.example.core.language.LanguageManager.getLanguagePromptName(currentLanguage)
                val variableSelectedLanguage = com.example.core.language.LanguageManager.getVariableSelectedLanguage(currentLanguage)
                val responseDirective = "RESPONSE SHOULD BE IN $variableSelectedLanguage"

                val systemPrompt = "You are a Master Vedic Astrologer, Numerologist, and Vastu Acharya. You provide authentic Vedic astrology and spiritual insights. STRICT LANGUAGE REQUIREMENT: $responseDirective. You MUST generate the ENTIRE response exclusively in $targetLanguageName using its correct native script and natural, respectful phrasing."
                val prompt = """
                    $contextBlock
                    
                    TASK:
                    As a Master Vedic Astrologer, Numerologist, and Vastu Acharya, generate an in-depth, authentic, and highly personalized reading for ${profile.fullName} anchored in today's date (${java.time.LocalDate.now()}).
                    
                    Please structure your response cleanly into the following sections:
                    
                    1. Current Influences & Karmic Atmosphere
                    (Analyze how today's Panchanga and active Vimshottari Mahadasha intersect with their Janma Lagna, Moon sign, and Mulank/Bhagyank)
                    
                    2. 6-12 Months Life Outlook
                    (Detailed predictions for Career, Finances, Relationships, and Spiritual Vitality)
                    
                    3. Vastu Shastra & Spatial Harmony
                    (Personalized room orientation, directional energy tips for their dominant ${personalizedProfile.dominantVastuElement} element, and Brahmasthan balance)
                    
                    4. Sacred Numerology & Cosmic Remedies
                    (Actionable Vedic remedies: specific Mantra Japa, gemstone guidance, charity, and daily breath/meditation alignment)
                    
                    Maintain a compassionate, classical, and inspiring tone.
                    
                    CRITICAL LANGUAGE RULE: 
                    $responseDirective
                    You MUST generate the ENTIRE response exclusively in $targetLanguageName ($currentLanguage). Ensure the grammar, terminology, and script are highly accurate and natural for $targetLanguageName speakers.
                """.trimIndent()

                try {
                    var responseText: String? = null
                    
                    // Try OpenAI / ChatGPT first
                    try {
                        if (openAiKey.isNotBlank() && openAiKey != "MY_OPENAI_API_KEY") {
                            responseText = com.example.api.generateWithOpenAI(prompt = prompt, apiKey = openAiKey, systemPrompt = systemPrompt)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Fallback to Gemini handled below
                    }

                    // Fallback to Gemini
                    if (responseText == null && model != null) {
                        try {
                            val geminiResponse = model.generateContent(prompt)
                            responseText = geminiResponse.text
                        } catch (e: Exception) {
                            throw e
                        }
                    }

                    predictionData = responseText ?: "Could not generate prediction. Please try again."
                    if (responseText != null) {
                        val newEntity = PredictionCacheEntity(
                            title = "Reading for ${profile.fullName}",
                            predictionData = responseText
                        )
                        withContext(Dispatchers.IO) {
                            val id = cacheDao.insertPrediction(newEntity)
                            val inserted = newEntity.copy(id = id)
                            try {
                                VedicWearableSyncManager.syncProfileAndHoroscopeToWatch(
                                    context = context,
                                    profile = profile,
                                    personalizedProfile = personalizedProfile,
                                    dailyProfile = dailyProfile,
                                    latestPrediction = inserted
                                )
                            } catch (e: Exception) {
                                // Ignore non-critical wear sync error
                            }
                        }
                    }
                } catch (e: Exception) {
                    val cached = withContext(Dispatchers.IO) { cacheDao.getLatestPrediction().firstOrNull() }
                    if (cached != null) {
                        predictionData = cached.predictionData
                        errorMessage = null // Silent fallback to cache
                    } else {
                        errorMessage = null // Silently provide fallback
                        val fallback = generateFallbackPrediction(profile, personalizedProfile)
                        predictionData = fallback
                        val fallbackEntity = PredictionCacheEntity(
                            title = "Reading for ${profile.fullName}",
                            predictionData = fallback
                        )
                        withContext(Dispatchers.IO) {
                            cacheDao.insertPrediction(fallbackEntity)
                        }
                    }
                } finally {
                    isLoading = false
                }
            }
        }
    }
    
    LaunchedEffect(profile) {
        if (predictionData == null && !isLoading && errorMessage == null) {
            val cached = withContext(Dispatchers.IO) { cacheDao.getLatestPrediction().firstOrNull() }
            if (cached != null) {
                predictionData = cached.predictionData
            } else {
                generatePrediction()
            }
        }
    }

    if (showPastReadings) {
        com.example.ui.screens.PastReadingsScreen(
            onBack = { showPastReadings = false }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Personalized Life Predictions",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Powered by Vedic Astrology & Numerology",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showPastReadings = true },
                    modifier = Modifier.testTag("action_view_past_readings")
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = stringResource(R.string.past_readings_title),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { generatePrediction() },
                    modifier = Modifier.testTag("action_refresh_prediction")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh, 
                        contentDescription = "Refresh Prediction",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Analyzing cosmic alignments and numbers...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (errorMessage != null && predictionData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
        } else if (predictionData != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                if (errorMessage != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
                
                val text = predictionData ?: ""
                // Simple parsing logic: split by the numbers 1., 2., 3.
                val sections = text.split(Regex("(?=1\\.|2\\.|3\\.)"))
                
                if (sections.isEmpty() || sections.size == 1) {
                    item {
                        PredictionCard("Cosmic Reading", text)
                    }
                } else {
                    items(sections.size) { index ->
                        val sectionText = sections[index].trim()
                        if (sectionText.isNotEmpty()) {
                            val lines = sectionText.split("\n")
                            val titleRaw = lines.firstOrNull() ?: "Reading"
                            // Clean up "**1. Title**" into "Title"
                            val title = titleRaw.replace(Regex("^\\d+\\.\\s*\\*?\\*?|\\*?\\*?$"), "").trim()
                            val body = lines.drop(1).joinToString("\n").trim().replace("**", "")
                            if (body.isNotEmpty()) {
                                PredictionCard(title, body)
                            } else {
                                PredictionCard("Insight", title)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionCard(title: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

// Fallback logic for when offline or no API key
private fun generateFallbackPrediction(profile: BirthProfile, personalizedProfile: PersonalizedProfile): String {
    val lagna = personalizedProfile.natalLagnaRashi.sanskritName
    val lp = personalizedProfile.numerology.lifePathNumber
    
    return """
        1. Current Problems & Challenges
        Based on your Life Path $lp and $lagna Lagna, you may currently be experiencing internal friction between your desire for independence and the responsibilities you hold towards others. You might be facing minor obstacles in professional growth due to planetary transits causing temporary delays.
        
        2. Future Predictions
        The upcoming 6-12 months show a strong indication of resolution. Your ruling planetary energies will shift, bringing clarity to your career path. Financial stability is expected to improve. In relationships, clear communication will dissolve existing misunderstandings, leading to deeper harmony.
        
        3. Solutions & Remedies
        • Astrological: Chant the mantra for your Lagna Lord every morning.
        • Numerological: Avoid making major decisions on dates that sum up to your inimical numbers. Favor your lucky numbers (${personalizedProfile.numerology.favorableNumbers.joinToString(", ")}).
        • Lifestyle: Practice Chakra meditation focusing on grounding yourself. Wear ${personalizedProfile.numerology.favorableColors.firstOrNull() ?: "light"} colors on important days.
    """.trimIndent()
}
