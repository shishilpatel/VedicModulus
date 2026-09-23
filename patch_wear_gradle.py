import re

with open("wear/build.gradle.kts", "r") as f:
    content = f.read()

deps_old = """    implementation("androidx.wear.compose:compose-foundation:1.3.0")"""

deps_new = """    implementation("androidx.wear.compose:compose-foundation:1.3.0")
    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.2.1")"""

content = content.replace(deps_old, deps_new)

with open("wear/build.gradle.kts", "w") as f:
    f.write(content)
