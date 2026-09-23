import re

with open("app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt", "r") as f:
    content = f.read()

if "import androidx.compose.ui.unit.sp" not in content:
    content = content.replace("import androidx.compose.ui.unit.dp", "import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp")

with open("app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt", "w") as f:
    f.write(content)
