import sys

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "r") as f:
    content = f.read()

content = content.replace(
"""            val spashtaList = spashtaHelper.calculateRealTimeGrahaSpashta(
                zonedDateTime = zdt,
                latitude = profile.birthLocation.latitude,
                longitude = profile.birthLocation.longitude,
                timezoneId = profile.birthLocation.timezoneId,
                ayanamsaMode = com.example.engines.astronomy.AyanamsaType.LAHIRI // Or from profile.ayanamsha
            )""",
"""            val spashtaList = spashtaHelper.calculateRealTimeGrahaSpashta(
                latitude = profile.birthLocation.latitude,
                longitude = profile.birthLocation.longitude,
                altitudeMeters = 0.0,
                dateTime = zdt
            )"""
)

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "w") as f:
    f.write(content)
