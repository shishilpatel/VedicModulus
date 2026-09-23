with open("app/src/main/java/com/example/ui/screens/tabs/VedicCalendarTab.kt", "r") as f:
    content = f.read()

orig_grid = """                                        isSelected -> OrangeAccent
                                        isToday -> OrangeAccent.copy(alpha = 0.2f)
                                        else -> Color.Transparent"""

new_grid = """                                        isSelected -> OrangeAccent
                                        isToday -> OrangeAccent.copy(alpha = 0.3f)
                                        else -> Color.Transparent"""

# we need a way to show if a day is personally auspicious. Since generating daily profiles for the whole month is expensive, we can just add a placeholder or simple logic for "Personal Auspiciousness" if we want to simulate it, or just keep it simple.

content = content.replace(orig_grid, new_grid)

with open("app/src/main/java/com/example/ui/screens/tabs/VedicCalendarTab.kt", "w") as f:
    f.write(content)
