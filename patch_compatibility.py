import sys
import re

path = "app/src/main/java/com/example/ui/screens/tabs/CompatibilityMatchTab.kt"
with open(path, "r") as f:
    content = f.read()

# Add experimental annotation if missing
if "@OptIn(ExperimentalMaterial3Api::class)" not in content:
    content = content.replace("fun CompatibilityMatchTab(", "@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun CompatibilityMatchTab(", 1)
    content = content.replace("@Composable\n@OptIn(ExperimentalMaterial3Api::class)\n@Composable", "@OptIn(ExperimentalMaterial3Api::class)\n@Composable")

# We will need to replace the Date row and Time row with Pickers.
# In my previous response I might have used text fields.
# But it's risky to regex without seeing it.
