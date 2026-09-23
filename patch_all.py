import sys

def patch_onboarding():
    path = "app/src/main/java/com/example/ui/screens/OnboardingScreen.kt"
    with open(path, "r") as f:
        content = f.read()

    target = """                        onClick = {
                            if (step < 8) {
                                step++
                            } else {
                                onComplete(currentProfile)
                            }
                        },"""
    replacement = """                        onClick = {
                            if (step == 6 && !hasSmartWatch) {
                                onComplete(currentProfile)
                            } else if (step < 8) {
                                step++
                            } else {
                                onComplete(currentProfile)
                            }
                        },"""
    if target in content:
        content = content.replace(target, replacement)
        with open(path, "w") as f:
            f.write(content)
        print("Patched OnboardingScreen.kt")
    else:
        print("Target not found in OnboardingScreen.kt")

def patch_daily_tab():
    path = "app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt"
    with open(path, "r") as f:
        content = f.read()

    # Replacing the single AI generation with dual AI generation
    target = """                            val prompt = "As an expert Vedic astrologer, generate a personalized daily horoscope prediction for ${personalizedProfile.userBirthProfile.fullName}, born under Lagna ${personalizedProfile.natalLagnaRashi.sanskritName} and Nakshatra ${personalizedProfile.natalNakshatra.sanskritName}. Today's Panchanga Tithi is ${dailyProfile.panchanga.tithi.name} and Nakshatra is ${dailyProfile.panchanga.nakshatra.sanskritName}. Provide a 3-paragraph inspiring, traditional astrological guidance."
                            try {
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
                                if (responseText == null && geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY") {
                                    try {
                                        val model = GenerativeModel(
                                            modelName = "gemini-3.5-flash",
                                            apiKey = geminiKey
                                        )
                                        val response = model.generateContent(prompt)
                                        responseText = response.text
                                    } catch (e: Exception) {
                                        throw e
                                    }
                                }
                                aiHoroscope = responseText ?: "No horoscope generated."
                            } catch (e: Exception) {
                                aiHoroscope = "Cosmic Guidance for ${personalizedProfile.userBirthProfile.fullName}: Under ${dailyProfile.panchanga.tithi.name} tithi and your Lagna ${personalizedProfile.natalLagnaRashi.sanskritName}, today brings auspicious breakthroughs in career and inner alignment. Focus on meditation during Brahma Muhurta and channel your energies wisely."
                            } finally {
                                isGeneratingHoroscope = false
                            }"""

    replacement = """                            val prompt = "As an expert Vedic astrologer, generate a personalized daily horoscope prediction for ${personalizedProfile.userBirthProfile.fullName}, born under Lagna ${personalizedProfile.natalLagnaRashi.sanskritName} and Nakshatra ${personalizedProfile.natalNakshatra.sanskritName}. Today's Panchanga Tithi is ${dailyProfile.panchanga.tithi.name} and Nakshatra is ${dailyProfile.panchanga.nakshatra.sanskritName}. Provide a 3-paragraph inspiring, traditional astrological guidance."
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
                                        responseText += "Gemini Insight:\n${response.text}\n\n"
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                // Try OpenAI 
                                if (openAiKey.isNotBlank() && openAiKey != "MY_OPENAI_API_KEY") {
                                    try {
                                        val openAiResponse = com.example.api.generateWithOpenAI(prompt, openAiKey)
                                        responseText += "ChatGPT Insight:\n${openAiResponse}\n\n"
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                aiHoroscope = if (responseText.isNotBlank()) responseText else "No horoscope generated (keys missing or failed)."
                            } catch (e: Exception) {
                                aiHoroscope = "Cosmic Guidance for ${personalizedProfile.userBirthProfile.fullName}: Under ${dailyProfile.panchanga.tithi.name} tithi and your Lagna ${personalizedProfile.natalLagnaRashi.sanskritName}, today brings auspicious breakthroughs in career and inner alignment. Focus on meditation during Brahma Muhurta and channel your energies wisely."
                            } finally {
                                isGeneratingHoroscope = false
                            }"""
    if target in content:
        content = content.replace(target, replacement)
        with open(path, "w") as f:
            f.write(content)
        print("Patched DailyHighlightsTab.kt Horoscope section")
    else:
        print("Horoscope target not found in DailyHighlightsTab.kt")

patch_onboarding()
patch_daily_tab()
