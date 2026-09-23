import re

with open('app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt', 'r') as f:
    text = f.read()

start = text.find('                        if (model == null) {')
end = text.find('                            } finally {', start)

replacement = """                        val openAiKey = try { BuildConfig.OPENAI_API_KEY } catch(e: Exception) { "" }
                        if (model == null && (openAiKey.isBlank() || openAiKey == "MY_OPENAI_API_KEY")) {
                            messages = messages + ChatMessage("Error: OPENAI_API_KEY or GEMINI_API_KEY is not configured in .env. Please add it and rebuild.", false)
                            isLoading = false
                            return@IconButton
                        }
                        
                        scope.launch {
                            try {
                                val prompt = "You are an expert Vedic astrologer. User profile: Lagna ${personalizedProfile.natalLagnaRashi.sanskritName}, Nakshatra ${personalizedProfile.natalNakshatra.sanskritName}. Today's Panchanga: Tithi ${dailyProfile.panchanga.tithi.name}, Nakshatra ${dailyProfile.panchanga.nakshatra.sanskritName}. Answer concisely and insightfully. User query: $userMsg"
                                
                                var responseText: String? = null
                                
                                // Try OpenAI first
                                try {
                                    if (openAiKey.isNotBlank() && openAiKey != "MY_OPENAI_API_KEY") {
                                        responseText = com.example.api.generateWithOpenAI(prompt, openAiKey)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                
                                // Fallback to Gemini
                                if (responseText == null && model != null) {
                                    try {
                                        val response = model.generateContent(prompt)
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
                                val msg = e.localizedMessage?.lowercase() ?: ""
                                val dynamicAnswer = when {
                                    msg.contains("high demand") || msg.contains("429") || msg.contains("503") -> "The AI models are experiencing high demand. Please try again later."
                                    userMsg.contains("day", ignoreCase = true) -> "For your day today under ${dailyProfile.panchanga.tithi.name} tithi and ${dailyProfile.panchanga.nakshatra.sanskritName} nakshatra, cosmic energies support mindful actions and spiritual focus."
                                    userMsg.contains("lagna", ignoreCase = true) -> "Your Ascendant (Lagna) is ${personalizedProfile.natalLagnaRashi.sanskritName}, reflecting vitality, purpose, and inner resilience."
                                    userMsg.contains("travel", ignoreCase = true) -> "For travel today, check the active Choghadiya (${dailyProfile.currentChoghadiya.type.displayName}). Amrit and Shubh periods are highly favorable for journey departures."
                                    userMsg.contains("muhurta", ignoreCase = true) -> "Today's key muhurtas: Brahma Muhurta in the pre-dawn hours is ideal for meditation, while Abhijit Muhurta around midday brings favorable success for important undertakings."
                                    else -> "Regarding '$userMsg': According to Vedic siddhanta for ${profile.fullName}, current planetary transits under ${dailyProfile.panchanga.nakshatra.sanskritName} support balanced decision-making and inner harmony."
                                }
                                messages = messages + ChatMessage(dynamicAnswer, false)"""

text = text[:start] + replacement + text[end:]

with open('app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt', 'w') as f:
    f.write(text)

