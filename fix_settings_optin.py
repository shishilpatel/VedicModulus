import sys

path = "app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace("OptIn(ExperimentalLayoutApi::class)", "@kotlin.OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)")

with open(path, "w") as f:
    f.write(content)
