with open("app/src/main/java/com/example/core/model/Models.kt", "r") as f:
    content = f.read()

import re

imports = """import com.example.engines.festival.Festival
import com.example.engines.festival.Vrat
import com.example.engines.festival.PujaTimingRecommendation

/**
 * Supported Ayanamsha systems"""

content = content.replace("/**\n * Supported Ayanamsha systems", imports)

with open("app/src/main/java/com/example/core/model/Models.kt", "w") as f:
    f.write(content)
