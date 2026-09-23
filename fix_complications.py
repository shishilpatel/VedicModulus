import re

with open("app/src/main/java/com/example/ui/components/GalaxyWatchFaceCanvas.kt", "r") as f:
    content = f.read()

content = content.replace("personalizedProfile.todayTaraBala?.name", "dailyProfile.panchanga.nakshatra.sanskritName") # Just a fallback
content = content.replace("personalizedProfile.birthNakshatra.sanskritName", "personalizedProfile.natalNakshatra.sanskritName")
content = content.replace("Graha.MANGAL", "Graha.MANGALA")
content = content.replace("Graha.BUDH", "Graha.BUDHA")

with open("app/src/main/java/com/example/ui/components/GalaxyWatchFaceCanvas.kt", "w") as f:
    f.write(content)
