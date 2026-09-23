import sys

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "r") as f:
    content = f.read()

content = content.replace("?.rashi?.index", "?.rashi?.number")
content = content.replace("transit.targetRashi.index", "transit.targetRashi.number")

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "w") as f:
    f.write(content)
