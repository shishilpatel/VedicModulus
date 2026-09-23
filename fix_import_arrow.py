import os
path = "app/src/main/java/com/example/ui/screens/StartupRewardedAdScreen.kt"
with open(path, "r") as f:
    content = f.read()

if "import androidx.compose.material.icons.automirrored.filled.ArrowForward" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.automirrored.filled.ArrowForward")

# Wait, let me also check if `Icons.Filled.ArrowForward` was used somewhere in the same file.
if "Icons.Filled.ArrowForward" in content:
    content = content.replace("Icons.Filled.ArrowForward", "Icons.AutoMirrored.Filled.ArrowForward")

with open(path, "w") as f:
    f.write(content)
