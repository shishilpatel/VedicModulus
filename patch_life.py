import re

with open('app/src/main/java/com/example/ui/screens/tabs/LifePredictionsTab.kt', 'r') as f:
    text = f.read()

# find val generatePrediction = { ... }
start = text.find('    val generatePrediction = {')
end = text.find('    LaunchedEffect(profile) {', start)

replacement = """    val generatePrediction = {
        val openAiKey = try { BuildConfig.OPENAI_API_KEY } catch(e: Exception) { "" }
        if ((openAiKey.isBlank() || openAiKey == "MY_OPENAI_API_KEY") && model == null) {
            errorMessage = "Please configure your OPENAI_API_KEY or GEMINI_API_KEY in the Secrets panel (in .env) to enable AI predictions."
        } else {
            isLoading = true
            errorMessage = null
            scope.launch {
                val prompt = \"\"\"
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
                \"\"\".trimIndent()

                try {
                    var responseText: String? = null
                    
                    // Try OpenAI first
                    try {
                        if (openAiKey.isNotBlank() && openAiKey != "MY_OPENAI_API_KEY") {
                            responseText = com.example.api.generateWithOpenAI(prompt, openAiKey)
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
                } catch (e: Exception) {
                    val msg = e.localizedMessage?.lowercase() ?: ""
                    if (msg.contains("high demand") || msg.contains("503") || msg.contains("unavailable") || msg.contains("overloaded") || msg.contains("429")) {
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
    
"""

text = text[:start] + replacement + text[end:]

with open('app/src/main/java/com/example/ui/screens/tabs/LifePredictionsTab.kt', 'w') as f:
    f.write(text)

