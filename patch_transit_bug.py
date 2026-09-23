import sys

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val houseFromMoon = (((com.example.core.model.Rashi.values().find { it.sanskritName == transit.targetRashi }?.number ?: 1) - natalMoonRashiIndex + 12) % 12) + 1",
    "val houseFromMoon = (((com.example.core.model.Rashi.entries.find { it.sanskritName == transit.targetRashi }?.number ?: 1) - (natalMoonRashiIndex ?: 1) + 12) % 12) + 1"
)

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "w") as f:
    f.write(content)
