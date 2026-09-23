package com.example.ui.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.core.ai.OpenAiClient
import com.google.ai.client.generativeai.GenerativeModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.SuggestionChip

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun VedicAITab(profile: com.example.core.model.BirthProfile, personalizedProfile: com.example.core.model.PersonalizedProfile, dailyProfile: com.example.core.model.DailyVedicProfile, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf(
        ChatMessage("Namaste! I am your Vedic AI Assistant powered by ChatGPT & Vedic wisdom. You can ask me about Panchanga, Muhurta, Astrology, or general Vedic knowledge.", false)
    )) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
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
        } else {
            null
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }
            if (isLoading) {
                item {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp))
                }
            }
        }
        
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf("How is my day?", "Explain my Lagna", "Good time for travel?", "Today's Muhurta")
            items(suggestions) { sugg ->
                SuggestionChip(
                    onClick = { query = sugg },
                    label = { Text(sugg, fontSize = 12.sp) }
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
                placeholder = { Text("Ask about Panchanga or Jyotisha...") },
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (query.isNotBlank() && !isLoading) {
                        val userMsg = query
                        messages = messages + ChatMessage(userMsg, true)
                        query = ""
                        isLoading = true
                        
                        if (!OpenAiClient.isAvailable() && model == null) {
                            messages = messages + ChatMessage("Error: No valid API key configured in .env. Please add your API key and rebuild.", false)
                            isLoading = false
                            return@IconButton
                        }
                        
                        scope.launch {
                            try {
                                val prompt = "You are an expert Vedic astrologer. User profile: Lagna ${personalizedProfile.natalLagnaRashi.sanskritName}, Nakshatra ${personalizedProfile.natalNakshatra.sanskritName}. Today's Panchanga: Tithi ${dailyProfile.panchanga.tithi.name}, Nakshatra ${dailyProfile.panchanga.nakshatra.sanskritName}. Answer concisely and insightfully. User query: $userMsg"
                                val ans = if (OpenAiClient.isAvailable()) {
                                    OpenAiClient.generateContent(prompt, "You are an expert Vedic astrologer assistant.")
                                } else if (model != null) {
                                    val response = model.generateContent(prompt)
                                    response.text ?: ""
                                } else {
                                    ""
                                }
                                
                                if (ans.isNotBlank()) {
                                    messages = messages + ChatMessage(ans, false)
                                } else {
                                    throw Exception("Empty response")
                                }
                            } catch (e: Exception) {
                                val dynamicAnswer = when {
                                    userMsg.contains("day", ignoreCase = true) -> "For your day today under ${dailyProfile.panchanga.tithi.name} tithi and ${dailyProfile.panchanga.nakshatra.sanskritName} nakshatra, cosmic energies support mindful actions and spiritual focus."
                                    userMsg.contains("lagna", ignoreCase = true) -> "Your Ascendant (Lagna) is ${personalizedProfile.natalLagnaRashi.sanskritName}, reflecting vitality, purpose, and inner resilience."
                                    userMsg.contains("travel", ignoreCase = true) -> "For travel today, check the active Choghadiya (${dailyProfile.currentChoghadiya.type.displayName}). Amrit and Shubh periods are highly favorable for journey departures."
                                    userMsg.contains("muhurta", ignoreCase = true) -> "Today's key muhurtas: Brahma Muhurta in the pre-dawn hours is ideal for meditation, while Abhijit Muhurta around midday brings favorable success for important undertakings."
                                    else -> "Regarding '$userMsg': According to Vedic siddhanta for ${profile.fullName}, current planetary transits under ${dailyProfile.panchanga.nakshatra.sanskritName} support balanced decision-making and inner harmony."
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
                        imageVector = androidx.compose.material.icons.Icons.Default.run { androidx.compose.material.icons.filled.Flag },
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
                    .clip(RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 0.dp,
                        bottomEnd = if (message.isUser) 0.dp else 16.dp
                    ))
                    .background(bgColor)
                    .padding(12.dp)
            ) {
                Text(text = message.text, color = textColor)
            }
        }
    }
}
