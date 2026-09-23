import sys

with open("app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt", "r") as f:
    content = f.read()

target = """    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {"""

replacement = """    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ==========================================
        // PERSONAL GREETING
        // ==========================================
        Text(
            text = "Namaste, ${personalizedProfile.userBirthProfile.fullName.split(" ").firstOrNull() ?: ""}",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaroonPrimary
            )
        )
        Text(
            text = "Welcome to your daily Vedic insights.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DarkTextSecondary
            )
        )
        Spacer(modifier = Modifier.height(16.dp))"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt", "w") as f:
        f.write(content)
    print("Patched DailyHighlightsTab.kt")
else:
    print("Target not found in DailyHighlightsTab.kt")

