import re

with open("app/src/main/java/com/example/ui/screens/StartupRewardedAdScreen.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.automirrored.filled.ArrowForward", "Icons.Default.ArrowForward")

with open("app/src/main/java/com/example/ui/screens/StartupRewardedAdScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt", "r") as f:
    content = f.read()

content = content.replace("Icons.Default.Warning", "Icons.Default.Warning\n")
if "import androidx.compose.material.icons.filled.Warning" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.filled.Warning")

with open("app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt", "w") as f:
    f.write(content)
