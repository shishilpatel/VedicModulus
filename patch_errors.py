import re

with open("app/src/main/java/com/example/ui/screens/StartupRewardedAdScreen.kt", "r") as f:
    content = f.read()

# I used Icons.AutoMirrored.Filled.ArrowForward without importing AutoMirrored.
content = content.replace("Icons.AutoMirrored.Filled.ArrowForward", "androidx.compose.material.icons.automirrored.filled.ArrowForward")

with open("app/src/main/java/com/example/ui/screens/StartupRewardedAdScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt", "r") as f:
    content = f.read()

content = content.replace("Icons.Default.Flag", "Icons.Default.Warning")
content = content.replace("import androidx.compose.material.icons.filled.Flag\n", "")

with open("app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt", "w") as f:
    f.write(content)
