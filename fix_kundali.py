with open("app/src/main/java/com/example/ui/screens/tabs/KundaliTab.kt", "r") as f:
    content = f.read()

bad = """    val natalSummary = remember(profile) {
        jyotishaEngine.calculateNatalChart(
            birthDate = profile.birthDate,
            birthTime = profile.birthTime,
            location = profile.birthLocation,
            ayanamsha = profile.ayanamsha
        )
    }

    val kundaliData = remember(natalSummary) {
        jyotishaEngine.generateKundaliChart(natalSummary)
    }"""

good = """    var natalSummary by remember { mutableStateOf<com.example.engines.jyotisha.JyotishaEngine.NatalChartSummary?>(null) }
    var kundaliData by remember { mutableStateOf<com.example.core.model.KundaliChartData?>(null) }
    
    LaunchedEffect(profile) {
        val summary = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            jyotishaEngine.calculateNatalChart(
                birthDate = profile.birthDate,
                birthTime = profile.birthTime,
                location = profile.birthLocation,
                ayanamsha = profile.ayanamsha
            )
        }
        natalSummary = summary
        kundaliData = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            jyotishaEngine.generateKundaliChart(summary)
        }
    }

    if (natalSummary == null || kundaliData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaroonPrimary)
        }
        return
    }"""

content = content.replace(bad, good)
content = content.replace("natalSummary.", "natalSummary!!.")
content = content.replace("kundaliData.", "kundaliData!!.")
content = content.replace("val planets = kundaliData.planets", "val planets = kundaliData!!.planets")
content = content.replace("val ascendantInfo = natalSummary.lagnaRashi", "val ascendantInfo = natalSummary!!.lagnaRashi")
with open("app/src/main/java/com/example/ui/screens/tabs/KundaliTab.kt", "w") as f:
    f.write(content)
