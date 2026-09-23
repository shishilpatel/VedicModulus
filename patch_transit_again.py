import sys

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "r") as f:
    content = f.read()

content = content.replace(
"""            val spashtaList = spashtaHelper.calculateGrahaSpashta(
                date = profile.birthDate,
                time = profile.birthTime,
                latitude = profile.birthLocation.latitude,
                longitude = profile.birthLocation.longitude,
                timezoneId = profile.birthLocation.timezoneId
            )""",
"""            val zdt = ZonedDateTime.of(profile.birthDate, profile.birthTime, java.time.ZoneId.of(profile.birthLocation.timezoneId))
            val spashtaList = spashtaHelper.calculateRealTimeGrahaSpashta(
                zonedDateTime = zdt,
                latitude = profile.birthLocation.latitude,
                longitude = profile.birthLocation.longitude,
                timezoneId = profile.birthLocation.timezoneId,
                ayanamsaMode = com.example.engines.astronomy.AyanamsaType.LAHIRI // Or from profile.ayanamsha
            )"""
)

with open("app/src/main/java/com/example/ui/components/TransitCalendarCard.kt", "w") as f:
    f.write(content)
