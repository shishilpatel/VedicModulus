import sys

path = "app/src/main/java/com/example/ui/screens/DashboardScreen.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace(
    "val dailyResult = remember(activeLocation, profile.ayanamsha, profile.calendarTradition, currentTime.minute, selectedDateOffset) {",
    "val dailyResult = remember(activeLocation, profile, personalizedResult, currentTime.minute, selectedDateOffset) {"
)

with open(path, "w") as f:
    f.write(content)
