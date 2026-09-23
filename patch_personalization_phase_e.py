with open("app/src/main/java/com/example/engines/personalization/VedicPersonalizationEngine.kt", "r") as f:
    content = f.read()

import re

# Add imports
imports = """import com.example.engines.vedictime.VedicTimeEngine
import com.example.engines.score.CosmicScoreEngine
import com.example.engines.festival.HinduFestivalEngine
import com.example.engines.festival.VratEngine
import com.example.engines.festival.PujaTimingEngine
import com.example.engines.festival.Festival
import com.example.engines.festival.Vrat
import com.example.engines.festival.PujaTimingRecommendation"""

content = content.replace("import com.example.engines.vedictime.VedicTimeEngine\nimport com.example.engines.score.CosmicScoreEngine", imports)

# Add engines to constructor
orig_cons = "private val cosmicScoreEngine: CosmicScoreEngine = CosmicScoreEngine()"
new_cons = """private val cosmicScoreEngine: CosmicScoreEngine = CosmicScoreEngine(),
    private val hinduFestivalEngine: HinduFestivalEngine = HinduFestivalEngine(),
    private val vratEngine: VratEngine = VratEngine(),
    private val pujaTimingEngine: PujaTimingEngine = PujaTimingEngine()"""

content = content.replace(orig_cons, new_cons)

with open("app/src/main/java/com/example/engines/personalization/VedicPersonalizationEngine.kt", "w") as f:
    f.write(content)
