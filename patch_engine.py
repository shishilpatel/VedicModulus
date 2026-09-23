with open("app/src/main/java/com/example/engines/personalization/VedicPersonalizationEngine.kt", "r") as f:
    content = f.read()

import re

# Add CosmicScoreEngine import
content = content.replace("import com.example.engines.vedictime.VedicTimeEngine", "import com.example.engines.vedictime.VedicTimeEngine\nimport com.example.engines.score.CosmicScoreEngine")

# Add CosmicScoreEngine to constructor
content = content.replace("private val spiritualDesignEngine: SpiritualDesignEngine = SpiritualDesignEngine()", "private val spiritualDesignEngine: SpiritualDesignEngine = SpiritualDesignEngine(),\n    private val cosmicScoreEngine: CosmicScoreEngine = CosmicScoreEngine()")

# Add personalizedProfile parameter to createDailyVedicProfile
orig_sig = """    fun createDailyVedicProfile(
        now: ZonedDateTime,
        location: LocationData,
        ayanamsha: AyanamshaSystem = AyanamshaSystem.LAHIRI,
        tradition: CalendarTradition = CalendarTradition.AMANTA
    ): DailyVedicProfile {"""
new_sig = """    fun createDailyVedicProfile(
        now: ZonedDateTime,
        location: LocationData,
        ayanamsha: AyanamshaSystem = AyanamshaSystem.LAHIRI,
        tradition: CalendarTradition = CalendarTradition.AMANTA,
        personalizedProfile: PersonalizedProfile? = null
    ): DailyVedicProfile {"""
content = content.replace(orig_sig, new_sig)

# Add cosmicScore to return DailyVedicProfile
orig_ret = """            abhijitMuhurta = abhijit,
            brahmaMuhurta = brahma,
            moonPhaseProgress = moonPhaseProgress
        )"""

new_ret = """            abhijitMuhurta = abhijit,
            brahmaMuhurta = brahma,
            moonPhaseProgress = moonPhaseProgress
        )
        
        val score = cosmicScoreEngine.calculateScore(personalizedProfile, profileWithoutScore)
        return profileWithoutScore.copy(cosmicScore = score)"""

content = content.replace("return DailyVedicProfile(", "val profileWithoutScore = DailyVedicProfile(")
content = content.replace(orig_ret, new_ret)

with open("app/src/main/java/com/example/engines/personalization/VedicPersonalizationEngine.kt", "w") as f:
    f.write(content)
