with open("app/src/main/java/com/example/core/model/Models.kt", "r") as f:
    content = f.read()

import re

# Insert CosmicScore before DailyVedicProfile
cosmic_score = """
data class CosmicScore(
    val luck: Int,
    val energy: Int,
    val calm: Int,
    val overall: Int
)

data class DailyVedicProfile("""

content = content.replace("data class DailyVedicProfile(", cosmic_score)

# Add cosmicScore to DailyVedicProfile
profile = """    val brahmaMuhurta: TimingWindow,
    val moonPhaseProgress: Float, // 0.0 (New Moon) to 1.0 (Full Moon) back to 0.0
    val cosmicScore: CosmicScore? = null
)"""

content = re.sub(r"    val brahmaMuhurta: TimingWindow,\n    val moonPhaseProgress: Float // 0.0 \(New Moon\) to 1.0 \(Full Moon\) back to 0.0\n\)", profile, content)

with open("app/src/main/java/com/example/core/model/Models.kt", "w") as f:
    f.write(content)
