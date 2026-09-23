package com.example.ui.screens.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.core.ai.OpenAiClient
import com.example.core.model.BirthProfile
import com.example.core.model.DailyVedicProfile
import com.example.core.model.PersonalizedProfile
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

@Composable
fun LifePredictionsTab(
    profile: BirthProfile,
    personalizedProfile: PersonalizedProfile,
    dailyProfile: DailyVedicProfile,
    modifier: Modifier = Modifier
) {
    var predictionData by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    val numerology = personalizedProfile.numerology
    val lagna = personalizedProfile.natalLagnaRashi.sanskritName
    val chandra = personalizedProfile.natalChandraRashi.sanskritName
    val nakshatra = personalizedProfile.natalNakshatra.sanskritName
    
    val model = remember {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                GenerativeModel(
                    modelName = "gemini-3.5-flash",
                    apiKey = apiKey
                )
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val generatePrediction = {
        if (!OpenAiClient.isAvailable() && model == null) {
            errorMessage = "Please configure your OpenAI or Gemini API key in .env to enable AI predictions."
        } else {
            isLoading = true
            errorMessage = null
            scope.launch {
                try {
                    val prompt = """
                        You are an expert Vedic astrologer and Numerologist. 
                        User Details:
                        Name: ${profile.fullName}
                        Birth Date: ${profile.birthDate}
                        Birth Time: ${profile.birthTime}
                        Astrology: Lagna (Ascendant) is $lagna, Moon Sign is $chandra, Birth Nakshatra is $nakshatra.
                        Numerology: Life Path Number is ${numerology.lifePathNumber}, Destiny Number is ${numerology.destinyNameNumber}, Ruling Planet is ${numerology.rulingPlanet.displayName}.
                        
                        Based on these specific numerology and astrology details, provide a detailed, highly personalized reading.
                        Please structure your response cleanly into the following three sections (use simple text/bullet points, do not use complex markdown tables):
                        
                        1. Current Problems & Challenges
                        (Identify what the user might be struggling with right now based on their chart and numbers)
                        
                        2. Future Predictions
                        (Provide predictions for career, relationships, and health for the next 6-12 months)
                        
                        3. Solutions & Remedies
                        (Provide practical astrological, numerological, and lifestyle remedies/solutions for the challenges)
                        
                        Be compassionate, insightful, and highly specific to the numbers and signs provided.
                    """.trimIndent()
                    
                    predictionData = if (OpenAiClient.isAvailable()) {
                        OpenAiClient.generateContent(prompt, "You are an expert Vedic astrologer and Numerologist.")
                    } else if (model != null) {
                        val response = model.generateContent(prompt)
                        response.text ?: "Could not generate prediction. Please try again."
                    } else {
                        generateFallbackPrediction(profile, personalizedProfile)
                    }
                } catch (e: Exception) {
                    val msg = e.localizedMessage?.lowercase() ?: ""
                    if (msg.contains("high demand") || msg.contains("503") || msg.contains("unavailable") || msg.contains("overloaded")) {
                        errorMessage = "The AI model is currently experiencing high demand. Providing your personalized standard Vedic calculation instead."
                    } else {
                        errorMessage = "Providing personalized standard Vedic calculation. (${e.localizedMessage})"
                    }
                    predictionData = generateFallbackPrediction(profile, personalizedProfile)
                } finally {
                    isLoading = false
                }
            }
        }
    }
    
    LaunchedEffect(profile) {
        if (predictionData == null && !isLoading && errorMessage == null) {
            generatePrediction()
        }
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
            Column {
                Text(
                    text = "Personalized Life Predictions",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Powered by ChatGPT & Vedic Astrology",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            IconButton(onClick = { generatePrediction() }) {
                Icon(
                    imageVector = Icons.Default.Refresh, 
                    contentDescription = "Refresh Prediction",
                    tint = MaterialTheme.colorScheme.primary
                )
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
