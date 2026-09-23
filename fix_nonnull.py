import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

content = content.replace("currentPersonalizedResult", "currentPersonalizedResult!!")
content = content.replace("currentDailyResult", "currentDailyResult!!")

# Wait, but currentPersonalizedResult!! == null is invalid syntax!
content = content.replace("currentDailyResult!! == null", "currentDailyResult == null")
content = content.replace("currentPersonalizedResult!! == null", "currentPersonalizedResult == null")

# Let's just fix the assignment!
content = content.replace("val currentPersonalizedResult!! = personalizedResult", "val currentPersonalizedResult = personalizedResult")
content = content.replace("val currentDailyResult!! = dailyResult", "val currentDailyResult = dailyResult")

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)
