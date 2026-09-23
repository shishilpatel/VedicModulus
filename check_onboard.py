with open("app/src/main/java/com/example/ui/screens/OnboardingScreen.kt", "r") as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    if "personalizedResult" in line or "dailyResult" in line or "watchConfig" in line:
        print(f"{i+1}: {line.strip()}")
