with open("app/src/main/java/com/example/engines/muhurta/MuhurtaEngine.kt", "r") as f:
    content = f.read()

import re
content = content.replace("class MuhurtaEngine {", """import com.example.engines.mantra.MantraEngine

class MuhurtaEngine {
    private val mantraEngine = MantraEngine()""")

content = content.replace("isAuspicious = true\n        )", "isAuspicious = true,\n            deity = mantraEngine.getDeityForMuhurta(\"Brahma Muhurta\")\n        )", 1)
content = content.replace("isAuspicious = (dayOfWeek != 3)\n        )", "isAuspicious = (dayOfWeek != 3),\n            deity = mantraEngine.getDeityForMuhurta(\"Abhijit Muhurta\")\n        )", 1)
content = content.replace("isAuspicious = true\n        )", "isAuspicious = true,\n            deity = mantraEngine.getDeityForMuhurta(\"Pradosha\")\n        )", 1) # This is Pradosha
content = content.replace("isAuspicious = true\n        )", "isAuspicious = true,\n            deity = mantraEngine.getDeityForMuhurta(\"Nishita Muhurta\")\n        )", 1) # Nishita

with open("app/src/main/java/com/example/engines/muhurta/MuhurtaEngine.kt", "w") as f:
    f.write(content)
