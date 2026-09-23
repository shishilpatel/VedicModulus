import os

filepath = "app/src/main/java/com/example/workers/DailyVedicNotificationManager.kt"
with open(filepath, 'r') as f:
    content = f.read()

import_str = """import java.util.Locale
import android.content.res.Configuration
import com.example.core.model.LanguageDisplay"""

if "java.util.Locale" not in content:
    content = content.replace("import java.util.concurrent.TimeUnit", "import java.util.concurrent.TimeUnit\n" + import_str)

new_code = """
    fun buildAndPostNotification(
        context: Context,
        birthProfile: BirthProfile,
        dailyProfile: DailyVedicProfile,
        notifyFestivals: Boolean = true,
        notifyMuhurtas: Boolean = true,
        notifyRahuKaal: Boolean = true,
        notificationId: Int = 2001
    ) {
        val langCode = when (birthProfile.language) {
            LanguageDisplay.HINDI -> "hi"
            LanguageDisplay.GUJARATI -> "gu"
            LanguageDisplay.TAMIL -> "ta"
            LanguageDisplay.TELUGU -> "te"
            LanguageDisplay.BENGALI -> "bn"
            LanguageDisplay.SANSKRIT -> "sa"
            else -> "en"
        }
        val locale = Locale(langCode)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)

        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        val cityName = birthProfile.birthLocation.cityName
        val tithiName = dailyProfile.panchanga.tithi.name
        val nakshatraName = dailyProfile.panchanga.nakshatra.name
        val festival = if (notifyFestivals) dailyProfile.festivals.firstOrNull() else null
        val vrat = if (notifyFestivals) dailyProfile.vrats.firstOrNull() else null

        val brahmaStart = dailyProfile.brahmaMuhurta.startTime.format(timeFormatter)
        val brahmaEnd = dailyProfile.brahmaMuhurta.endTime.format(timeFormatter)
        val abhijitText = dailyProfile.abhijitMuhurta?.let {
            "${it.startTime.format(timeFormatter)} - ${it.endTime.format(timeFormatter)}"
        } ?: "N/A"
        val rahuStart = dailyProfile.rahuKalam.startTime.format(timeFormatter)
        val rahuEnd = dailyProfile.rahuKalam.endTime.format(timeFormatter)

        val strVedicPanchang = localizedContext.getString(R.string.notif_vedic_panchang)
        val strLocation = localizedContext.getString(R.string.notif_location)
        val strTithi = localizedContext.getString(R.string.notif_tithi)
        val strNakshatra = localizedContext.getString(R.string.notif_nakshatra)
        val strYoga = localizedContext.getString(R.string.notif_yoga)
        val strKarana = localizedContext.getString(R.string.notif_karana)
        val strBrahmaMuhurta = localizedContext.getString(R.string.notif_brahma_muhurta)
        val strAbhijitMuhurta = localizedContext.getString(R.string.notif_abhijit_muhurta)
        val strRahuKaal = localizedContext.getString(R.string.notif_rahu_kaal)
        val strFestival = localizedContext.getString(R.string.notif_festival)
        val strVrat = localizedContext.getString(R.string.notif_vrat)
        val strCosmicEnergy = localizedContext.getString(R.string.notif_cosmic_energy)
        val strActiveHora = localizedContext.getString(R.string.notif_active_hora)
        val strChoghadiya = localizedContext.getString(R.string.notif_choghadiya)

        // Formulate Title
        val title = when {
            festival != null -> "🎉 ${festival.name} • $cityName $strVedicPanchang"
            vrat != null -> "🙏 ${vrat.name} • $cityName $strVedicPanchang"
            else -> "🕉 $strVedicPanchang • $cityName"
        }

        // Formulate Short Content
        val shortContent = when {
            festival != null -> "${festival.name}: $tithiName, $nakshatraName. $strBrahmaMuhurta $brahmaStart"
            vrat != null -> "${vrat.name}: $tithiName, $nakshatraName. $strBrahmaMuhurta $brahmaStart"
            else -> "$tithiName • $nakshatraName | $strBrahmaMuhurta $brahmaStart - $brahmaEnd"
        }

        // Formulate Expanded Body
        val expandedBuilder = StringBuilder()
        expandedBuilder.append("📍 $strLocation ${birthProfile.birthLocation.cityName}, ${birthProfile.birthLocation.state}\\n")
        expandedBuilder.append("🌕 $strTithi $tithiName (${dailyProfile.panchanga.tithi.paksha.name})\\n")
        expandedBuilder.append("⭐ $strNakshatra $nakshatraName (Pada ${dailyProfile.panchanga.nakshatraPada})\\n")
        expandedBuilder.append("🌿 $strYoga ${dailyProfile.panchanga.yoga} | $strKarana ${dailyProfile.panchanga.karana}\\n\\n")

        if (notifyMuhurtas) {
            expandedBuilder.append("✨ $strBrahmaMuhurta $brahmaStart - $brahmaEnd\\n")
            if (dailyProfile.abhijitMuhurta != null) {
                expandedBuilder.append("🌟 $strAbhijitMuhurta $abhijitText\\n")
            }
        }

        if (notifyRahuKaal) {
            expandedBuilder.append("⚠️ $strRahuKaal $rahuStart - $rahuEnd\\n")
        }

        if (festival != null) {
            expandedBuilder.append("\\n🎉 $strFestival ${festival.name}\\n${festival.description}\\n")
        }
        if (vrat != null) {
            expandedBuilder.append("\\n🙏 $strVrat ${vrat.name}\\n${vrat.description}\\n")
        }

        if (dailyProfile.cosmicScore != null) {
            val score = dailyProfile.cosmicScore
            expandedBuilder.append("$strCosmicEnergy ${score.overall}/100 • Luck: ${score.luck} • Calm: ${score.calm}\\n")
        }

        val activeHora = dailyProfile.currentHora.rulingGraha.displayName
        val activeChoghadiyaText = "${dailyProfile.currentChoghadiya.type.displayName} (${dailyProfile.currentChoghadiya.type.quality})"
        
        expandedBuilder.append("🪐 $strActiveHora $activeHora | $strChoghadiya $activeChoghadiyaText")
"""

start_str = "    fun buildAndPostNotification("
end_str = "        val launchIntent = Intent(context, MainActivity::class.java).apply {"

content = content[:content.find(start_str)] + new_code + "\n" + content[content.find(end_str):]

with open(filepath, 'w') as f:
    f.write(content)
