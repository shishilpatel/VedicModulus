import sys

path = "app/src/main/java/com/example/ui/screens/UserProfileSettingsScreen.kt"
with open(path, "r") as f:
    lines = f.read().split('\n')

start = -1
for i, line in enumerate(lines):
    if "fun UserProfileSettingsScreen(" in line:
        start = i
        break

if start != -1:
    print('\n'.join(lines[start:start+120]))
