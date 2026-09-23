with open("app/src/main/java/com/example/ui/screens/tabs/PanchangaTab.kt", "r") as f:
    content = f.read()

imports = """import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.google.ai.client.generativeai.GenerativeModel
import com.example.BuildConfig"""
if "import com.example.BuildConfig" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\n" + imports)


dialog_code = """
    var showAiDialog by remember { mutableStateOf(false) }
    var aiInsight by remember { mutableStateOf("") }
    var isAiLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val model = remember {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            GenerativeModel(modelName = "gemini-1.5-flash", apiKey = apiKey)
        } else null
    }

    if (showAiDialog) {
        AlertDialog(
            onDismissRequest = { showAiDialog = false },
            title = { Text("Panchanga AI Insight", color = MaroonPrimary) },
            text = {
                if (isAiLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(aiInsight, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAiDialog = false }) { Text("Close") }
            }
        )
    }
"""
if "var showAiDialog" not in content:
    content = content.replace("    val harmonyScore = remember(personalizedProfile, p.nakshatra) {", dialog_code + "\n    val harmonyScore = remember(personalizedProfile, p.nakshatra) {")


ai_btn = """        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onDateOffsetChange(selectedDateOffset - 1) }) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous Day")
                }
                Text(
                    text = if (selectedDateOffset == 0) "Today" else "Offset: $selectedDateOffset Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaroonPrimary
                )
                IconButton(onClick = { onDateOffsetChange(selectedDateOffset + 1) }) {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next Day")
                }
            }
            
            Button(
                onClick = {
                    showAiDialog = true
                    if (aiInsight.isBlank()) {
                        if (model == null) {
                            aiInsight = "GEMINI_API_KEY not configured."
                            return@Button
                        }
                        isAiLoading = true
                        scope.launch {
                            try {
                                val prompt = "Analyze today's Panchanga for a user with Janma Nakshatra ${personalizedProfile.natalNakshatra.sanskritName}. Today's Nakshatra is ${p.nakshatra.sanskritName} and Tithi is ${p.tithi.name}. Keep it to 3 short sentences of practical advice."
                                aiInsight = model.generateContent(prompt).text ?: "Could not generate insight."
                            } catch (e: Exception) {
                                aiInsight = "Error: ${e.message}"
                            } finally {
                                isAiLoading = false
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("AI Insight", fontSize = 12.sp)
            }
        }"""

old_header = """        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onDateOffsetChange(selectedDateOffset - 1) }) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous Day")
            }
            Text(
                text = if (selectedDateOffset == 0) "Today" else "Offset: $selectedDateOffset Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaroonPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = { onDateOffsetChange(selectedDateOffset + 1) }) {
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next Day")
            }
        }"""

if "AI Insight" not in content:
    content = content.replace(old_header, ai_btn)

with open("app/src/main/java/com/example/ui/screens/tabs/PanchangaTab.kt", "w") as f:
    f.write(content)
