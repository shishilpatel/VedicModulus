import sys
with open("app/src/main/java/com/example/ui/components/PanchangaWidgetsGrid.kt", "r") as f:
    content = f.read()

# Let's check imports
if "import com.example.ui.theme.InauspiciousRed" not in content:
    content = content.replace("import com.example.ui.theme.AuspiciousGreen", "import com.example.ui.theme.AuspiciousGreen\nimport com.example.ui.theme.InauspiciousRed")
    
with open("app/src/main/java/com/example/ui/components/PanchangaWidgetsGrid.kt", "w") as f:
    f.write(content)
