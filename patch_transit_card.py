import sys

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "r") as f:
    content = f.read()

content = content.replace("profile.birthCity.latitude", "profile.birthLocation.latitude")
content = content.replace("profile.birthCity.longitude", "profile.birthLocation.longitude")
content = content.replace("profile.birthCity.timezoneId", "profile.birthLocation.timezoneId")

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "w") as f:
    f.write(content)
