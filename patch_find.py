import sys

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "r") as f:
    content = f.read()

content = content.replace("spashtaList.find { it.graha == Graha.CHANDRA }", "spashtaList.grahas.find { it.graha == Graha.CHANDRA }")

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "w") as f:
    f.write(content)
