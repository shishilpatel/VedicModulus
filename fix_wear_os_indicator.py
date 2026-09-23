import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# Add import
if "import kotlinx.coroutines.tasks.await" not in content:
    content = content.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\nimport kotlinx.coroutines.tasks.await")

if "import androidx.compose.material.icons.filled.Watch" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Watch", "import androidx.compose.material.icons.filled.Watch\nimport androidx.compose.material.icons.filled.WatchOff")

# Add state
state_block = """    var isWearOsConnected by remember { mutableStateOf(false) }
    LaunchedEffect(refreshTrigger) {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            isWearOsConnected = nodes.isNotEmpty()
        } catch (e: Exception) {
            isWearOsConnected = false
        }
    }

    Scaffold("""

content = content.replace("    Scaffold(", state_block)

# Add icon in TopAppBar
icon_block = """                    // Wear OS Connection Indicator
                    if (profile.hasSmartWatch) {
                        IconButton(onClick = { 
                            scope.launch { 
                                try {
                                    val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                                    isWearOsConnected = nodes.isNotEmpty()
                                } catch (e: Exception) {
                                    isWearOsConnected = false
                                }
                            } 
                        }) {
                            Icon(
                                imageVector = if (isWearOsConnected) Icons.Default.Watch else Icons.Default.WatchOff,
                                contentDescription = if (isWearOsConnected) "Wear OS Connected" else "Wear OS Disconnected",
                                tint = if (isWearOsConnected) AuspiciousGreen else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Google Play Ad-Free Status Button / Badge"""

content = content.replace("                    // Google Play Ad-Free Status Button / Badge", icon_block)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
