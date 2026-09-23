import re

# 1. Fix VedicRealtimeSpashtaCard.kt
with open("app/src/main/java/com/example/ui/components/VedicRealtimeSpashtaCard.kt", "r") as f:
    content = f.read()

content = content.replace(
    "var report by remember { mutableStateOf<RealTimeGrahaSpashtaReport?>(null) }",
    "val reportState = remember { mutableStateOf<RealTimeGrahaSpashtaReport?>(null) }\n    val report = reportState.value"
)
content = content.replace("report = kotlinx", "reportState.value = kotlinx")
content = content.replace("report!!.", "report.")

with open("app/src/main/java/com/example/ui/components/VedicRealtimeSpashtaCard.kt", "w") as f:
    f.write(content)


# 2. Fix KundaliTab.kt
with open("app/src/main/java/com/example/ui/screens/tabs/KundaliTab.kt", "r") as f:
    content = f.read()

content = content.replace(
    "var natalSummary by remember { mutableStateOf<com.example.engines.jyotisha.JyotishaEngine.NatalChartSummary?>(null) }",
    "val natalSummaryState = remember { mutableStateOf<com.example.engines.jyotisha.JyotishaEngine.NatalChartSummary?>(null) }\n    val natalSummary = natalSummaryState.value"
)
content = content.replace(
    "var kundaliData by remember { mutableStateOf<com.example.core.model.KundaliChartData?>(null) }",
    "val kundaliDataState = remember { mutableStateOf<com.example.core.model.KundaliChartData?>(null) }\n    val kundaliData = kundaliDataState.value"
)
content = content.replace("natalSummary = summary", "natalSummaryState.value = summary")
content = content.replace("kundaliData = kotlinx", "kundaliDataState.value = kotlinx")
content = content.replace("natalSummary!!.", "natalSummary.")
content = content.replace("kundaliData!!.", "kundaliData.")

with open("app/src/main/java/com/example/ui/screens/tabs/KundaliTab.kt", "w") as f:
    f.write(content)


# 3. Fix VedicCurrentDateCalendarComponent.kt
with open("app/src/main/java/com/example/ui/components/VedicCurrentDateCalendarComponent.kt", "r") as f:
    content = f.read()

content = content.replace(
    "var panchangaData by remember { mutableStateOf<com.example.engines.panchanga.PanchangaResult?>(null) }",
    "val panchangaDataState = remember { mutableStateOf<com.example.engines.panchanga.PanchangaData?>(null) }\n    val panchangaData = panchangaDataState.value"
)
content = content.replace("panchangaData = kotlinx", "panchangaDataState.value = kotlinx")
content = content.replace("panchangaData!!.", "panchangaData.")

with open("app/src/main/java/com/example/ui/components/VedicCurrentDateCalendarComponent.kt", "w") as f:
    f.write(content)

