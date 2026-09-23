import sys
import re

path = "app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt"
with open(path, "r") as f:
    content = f.read()

start_marker = "val prompt = \"As an expert Vedic astrologer, generate a personalized"
end_marker = "isGeneratingHoroscope = false\n                            }"

# Find the indices
start_idx = content.find(start_marker)
end_idx = content.find(end_marker, start_idx) + len(end_marker)

if start_idx != -1 and end_idx != -1:
    target = content[start_idx:end_idx]
    
    replacement = """val prompt = "As an expert Vedic astrologer, generate a personalized daily horoscope prediction for ${personalizedProfile.userBirthProfile.fullName}, born under Lagna ${personalizedProfile.natalLagnaRashi.sanskritName} and Nakshatra ${personalizedProfile.natalNakshatra.sanskritName}. Today's Panchanga Tithi is ${dailyProfile.panchanga.tithi.name} and Nakshatra is ${dailyProfile.panchanga.nakshatra.sanskritName}. Provide a 3-paragraph inspiring, traditional astrological guidance."

                            try {
                                var responseText = ""
                                // Try Gemini
                                if (geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY") {
                                    try {
                                        val model = GenerativeModel(
                                            modelName = "gemini-1.5-flash",
                                            apiKey = geminiKey
                                        )
                                        val response = model.generateContent(prompt)
                                        responseText += "✨ GEMINI INSIGHT:\n${response.text}\n\n"
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                // Try OpenAI 
                                if (openAiKey.isNotBlank() && openAiKey != "MY_OPENAI_API_KEY") {
                                    try {
                                        val openAiResponse = com.example.api.generateWithOpenAI(prompt, openAiKey)
                                        responseText += "🤖 CHATGPT INSIGHT:\n${openAiResponse}\n\n"
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                aiHoroscope = if (responseText.isNotBlank()) responseText else "Cosmic Guidance for ${personalizedProfile.userBirthProfile.fullName}: Under ${dailyProfile.panchanga.tithi.name} tithi and your Lagna ${personalizedProfile.natalLagnaRashi.sanskritName}, today brings auspicious breakthroughs in career and inner alignment. Focus on meditation during Brahma Muhurta and channel your energies wisely."
                            } catch (e: Exception) {
                                aiHoroscope = "Failed to generate horoscope."
                            } finally {
                                isGeneratingHoroscope = false
                            }"""
                            
    content = content[:start_idx] + replacement + content[end_idx:]
    with open(path, "w") as f:
        f.write(content)
    print("Patched Horoscope")
else:
    print("Not found")

