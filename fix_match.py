import re

with open("app/src/main/java/com/example/ui/screens/tabs/CompatibilityMatchTab.kt", "r") as f:
    content = f.read()

bad = """                        val myPers = personalizationEngine.createPersonalizedProfile(profile)
                        val pPers = personalizationEngine.createPersonalizedProfile(partnerBirthProfile)
                        
                        val matchRes = KundliMatchingEngine.matchKundlis("""

good = """                        val myPers = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { personalizationEngine.createPersonalizedProfile(profile) }
                        val pPers = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { personalizationEngine.createPersonalizedProfile(partnerBirthProfile) }
                        
                        val matchRes = KundliMatchingEngine.matchKundlis("""

content = content.replace(bad, good)

with open("app/src/main/java/com/example/ui/screens/tabs/CompatibilityMatchTab.kt", "w") as f:
    f.write(content)
