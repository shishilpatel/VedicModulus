import sys

path = "app/src/main/java/com/example/ui/screens/tabs/CompatibilityMatchTab.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace("import com.example.core.model.UserProfile", "import com.example.core.model.BirthProfile")
content = content.replace("profile: UserProfile", "profile: BirthProfile")
content = content.replace("profile.name.split", "profile.fullName.split")

with open(path, "w") as f:
    f.write(content)
