import re

with open('app/src/main/java/com/example/api/GeminiApiService.kt', 'r') as f:
    text = f.read()

start = text.find('suspend fun generateHoroscope(celestialData: String): String = withContext(Dispatchers.IO) {')
if start != -1:
    replacement = """suspend fun generateHoroscope(celestialData: String): String = withContext(Dispatchers.IO) {
    val openAiKey = try { BuildConfig.OPENAI_API_KEY } catch(e: Exception) { "" }
    val geminiKey = BuildConfig.GEMINI_API_KEY
    
    if ((openAiKey.isBlank() || openAiKey == "MY_OPENAI_API_KEY") && (geminiKey.isBlank() || geminiKey == "MY_GEMINI_API_KEY")) {
        return@withContext "Error: OpenAI/Gemini API Key not configured. Please add it via the AI Studio Secrets panel."
    }
    
    val prompt = "You are a Vedic astrologer. Based on the following celestial data, write a short, poetic, and insightful daily horoscope (2-3 sentences max) tailored for a watch face display. Data: $celestialData"
    
    var responseText: String? = null
    
    // Try OpenAI First
    try {
        if (openAiKey.isNotBlank() && openAiKey != "MY_OPENAI_API_KEY") {
            responseText = generateWithOpenAI(prompt, openAiKey)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    // Fallback to Gemini
    if (responseText == null && geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY") {
        try {
            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                )
            )
            val response = GeminiClient.service.generateContent(geminiKey, request)
            responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    responseText ?: "Auspicious alignment: Today's celestial transits favor inner reflection, balanced decision-making, and spiritual devotion."
}"""
    text = text[:start] + replacement
    with open('app/src/main/java/com/example/api/GeminiApiService.kt', 'w') as f:
        f.write(text)
    print("Patched generateHoroscope")
else:
    print("Could not find generateHoroscope")
