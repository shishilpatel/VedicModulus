import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

bad_state_cat = "    var selectedCategory by remember { mutableStateOf(NavCategory.DASHBOARD) }"
good_state_cat = "    var selectedCategory by rememberSaveable { mutableStateOf(NavCategory.DASHBOARD) }"

bad_state_tab = "    var selectedTab by remember { mutableStateOf(NavCategory.DASHBOARD.tabs.first()) }"
good_state_tab = "    var selectedTab by rememberSaveable { mutableStateOf(NavCategory.DASHBOARD.tabs.first()) }"

content = content.replace(bad_state_cat, good_state_cat)
content = content.replace(bad_state_tab, good_state_tab)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
