with open("app/src/main/java/com/example/core/model/Models.kt", "r") as f:
    content = f.read()

import re

# We need to import Festival, Vrat, PujaTimingRecommendation
imports = """import java.time.ZonedDateTime
import java.time.LocalTime
import com.example.engines.festival.Festival
import com.example.engines.festival.Vrat
import com.example.engines.festival.PujaTimingRecommendation"""

content = content.replace("import java.time.ZonedDateTime\nimport java.time.LocalTime", imports)

# Add properties to DailyVedicProfile
orig_profile = """    val moonPhaseProgress: Float, // 0.0 (New Moon) to 1.0 (Full Moon) back to 0.0
    val cosmicScore: CosmicScore? = null
)"""

new_profile = """    val moonPhaseProgress: Float, // 0.0 (New Moon) to 1.0 (Full Moon) back to 0.0
    val cosmicScore: CosmicScore? = null,
    val festivals: List<Festival> = emptyList(),
    val vrats: List<Vrat> = emptyList(),
    val pujaRecommendations: List<PujaTimingRecommendation> = emptyList()
)"""

content = content.replace(orig_profile, new_profile)

with open("app/src/main/java/com/example/core/model/Models.kt", "w") as f:
    f.write(content)
