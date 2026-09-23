with open("app/build.gradle.kts", "r") as f:
    content = f.read()

if "play-services-wearable" not in content:
    content = content.replace('implementation(libs.retrofit)', 'implementation(libs.retrofit)\n  implementation("com.google.android.gms:play-services-wearable:18.2.0")')
    
with open("app/build.gradle.kts", "w") as f:
    f.write(content)
