with open("app/src/main/java/com/example/core/model/Models.kt", "r") as f:
    content = f.read()

import re

imports = """import com.example.engines.reports.AscendantReport
import com.example.engines.reports.DashaPeriod"""

content = content.replace("import com.example.engines.festival.PujaTimingRecommendation", "import com.example.engines.festival.PujaTimingRecommendation\n" + imports)

orig_profile = """data class PersonalizedProfile(
    val userBirthProfile: BirthProfile,
    val natalLagnaRashi: Rashi,
    val natalSuryaRashi: Rashi,
    val natalChandraRashi: Rashi,
    val natalNakshatra: Nakshatra,
    val natalNakshatraPada: Int,
    val numerology: NumerologyProfile,
    val dominantVastuElement: String,
    val favorableColorsHex: List<Long>
)"""

new_profile = """data class PersonalizedProfile(
    val userBirthProfile: BirthProfile,
    val natalLagnaRashi: Rashi,
    val natalSuryaRashi: Rashi,
    val natalChandraRashi: Rashi,
    val natalNakshatra: Nakshatra,
    val natalNakshatraPada: Int,
    val numerology: NumerologyProfile,
    val dominantVastuElement: String,
    val favorableColorsHex: List<Long>,
    val ascendantReport: AscendantReport? = null,
    val dashaPeriods: List<DashaPeriod> = emptyList()
)"""

content = content.replace(orig_profile, new_profile)

with open("app/src/main/java/com/example/core/model/Models.kt", "w") as f:
    f.write(content)
