import re

with open('app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt', 'r') as f:
    text = f.read()
if 'Regarding' in text:
    text = text.replace("Regarding '$userMsg': ", "")
    with open('app/src/main/java/com/example/ui/screens/tabs/VedicAITab.kt', 'w') as f:
        f.write(text)
