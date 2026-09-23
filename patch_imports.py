import re

with open("app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.material.icons.Icons", "import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Warning")

with open("app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt", "w") as f:
    f.write(content)
