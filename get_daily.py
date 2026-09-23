import sys

path = "app/src/main/java/com/example/ui/screens/tabs/DailyHighlightsTab.kt"
with open(path, "r") as f:
    content = f.read()

lines = content.split('\n')
start = -1
end = -1
for i, line in enumerate(lines):
    if "fun DailyHighlightsTab(" in line:
        start = i
    if start != -1 and "private fun ScoreCircle(" in line:
        end = i
        break

if start != -1 and end != -1:
    print('\n'.join(lines[start:start+150]))
