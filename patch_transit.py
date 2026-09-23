import sys

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "r") as f:
    content = f.read()

content = content.replace("transit.targetRashi.number", "(com.example.core.model.Rashi.values().find { it.sanskritName == transit.targetRashi }?.number ?: 1)")

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "w") as f:
    f.write(content)
