import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

bad_onback = "onBack = { selectedTab = DashboardTab.DAILY_HIGHLIGHTS },"
good_onback = """onBack = {
                            selectedCategory = NavCategory.DASHBOARD
                            selectedTab = DashboardTab.DAILY_HIGHLIGHTS
                        },"""

content = content.replace(bad_onback, good_onback)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
