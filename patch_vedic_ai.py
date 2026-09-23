import re

with open("app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt", "r") as f:
    content = f.read()

model_old = """    val model = remember {
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
    }"""

model_new = """    val model = remember {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                GenerativeModel(
                    modelName = "gemini-3.5-flash",
                    apiKey = apiKey,
                    systemInstruction = com.google.ai.client.generativeai.type.content {
                        text("You are a Vedic AI Assistant. Important: This app provides astrology, numerology, Vastu, meditation, yoga and spiritual content for informational and personal guidance purposes only. Results and interpretations are not guaranteed and should not be considered professional advice. Do not make deterministic predictions or medical claims.")
                    }
                )
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }"""

content = content.replace(model_old, model_new)

with open("app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt", "w") as f:
    f.write(content)
