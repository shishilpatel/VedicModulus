import sys
with open("app/src/main/java/com/example/ui/components/PanchangaWidgetsGrid.kt", "r") as f:
    content = f.read()
content = content.replace("SuccessGreen", "AuspiciousGreen")
content = content.replace("Color(0xFFE57373)", "InauspiciousRed")
content = content.replace("Color(0xFFD32F2F)", "InauspiciousRed")
content = content.replace("import com.example.ui.theme.SuccessGreen", "import com.example.ui.theme.AuspiciousGreen\nimport com.example.ui.theme.InauspiciousRed")
with open("app/src/main/java/com/example/ui/components/PanchangaWidgetsGrid.kt", "w") as f:
    f.write(content)
