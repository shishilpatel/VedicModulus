import re

with open("app/src/main/java/com/example/api/GeminiApiService.kt", "r") as f:
    content = f.read()

prompt_old = """    val prompt = "You are a Vedic astrologer. Based on the following celestial data, write a short, poetic, and insightful daily horoscope (2-3 sentences max) tailored for a watch face display. Data: $celestialData\""""

prompt_new = """    val prompt = "You are a Vedic astrologer. Based on the following celestial data, write a short, poetic, and insightful daily horoscope (2-3 sentences max) tailored for a watch face display. Data: $celestialData. Important: This app provides astrology, numerology, Vastu, meditation, yoga and spiritual content for informational and personal guidance purposes only. Results and interpretations are not guaranteed and should not be considered professional advice. Do not make deterministic predictions or medical claims." """

content = content.replace(prompt_old, prompt_new)

with open("app/src/main/java/com/example/api/GeminiApiService.kt", "w") as f:
    f.write(content)
