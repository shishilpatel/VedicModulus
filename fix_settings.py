import sys

path = "app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Replace Row with FlowRow for settings
content = content.replace(
    'Row(\n                        modifier = Modifier.fillMaxWidth(),\n                        horizontalArrangement = Arrangement.spacedBy(8.dp)\n                    ) {\n                        AyanamshaSystem.values().forEach { sys ->',
    'OptIn(ExperimentalLayoutApi::class)\n                    androidx.compose.foundation.layout.FlowRow(\n                        modifier = Modifier.fillMaxWidth(),\n                        horizontalArrangement = Arrangement.spacedBy(8.dp),\n                        verticalArrangement = Arrangement.spacedBy(4.dp)\n                    ) {\n                        AyanamshaSystem.values().forEach { sys ->'
)
content = content.replace(
    'Row(\n                        modifier = Modifier.fillMaxWidth(),\n                        horizontalArrangement = Arrangement.spacedBy(8.dp)\n                    ) {\n                        CalendarTradition.values().forEach { trad ->',
    'OptIn(ExperimentalLayoutApi::class)\n                    androidx.compose.foundation.layout.FlowRow(\n                        modifier = Modifier.fillMaxWidth(),\n                        horizontalArrangement = Arrangement.spacedBy(8.dp),\n                        verticalArrangement = Arrangement.spacedBy(4.dp)\n                    ) {\n                        CalendarTradition.values().forEach { trad ->'
)
content = content.replace(
    'Row(\n                        modifier = Modifier.fillMaxWidth(),\n                        horizontalArrangement = Arrangement.spacedBy(8.dp)\n                    ) {\n                        NumerologySystem.values().forEach { numSys ->',
    'OptIn(ExperimentalLayoutApi::class)\n                    androidx.compose.foundation.layout.FlowRow(\n                        modifier = Modifier.fillMaxWidth(),\n                        horizontalArrangement = Arrangement.spacedBy(8.dp),\n                        verticalArrangement = Arrangement.spacedBy(4.dp)\n                    ) {\n                        NumerologySystem.values().forEach { numSys ->'
)

with open(path, "w") as f:
    f.write(content)
