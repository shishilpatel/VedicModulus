import os

replacements = {
    # StartupRewardedAdScreen.kt
    '"Verified Vedic & Wellness Partner"': 'stringResource(R.string.startup_ad_verified)',
    '"Watch Ad to Unlock App"': 'stringResource(R.string.startup_ad_unlock)',
    '"Continue to Vedic Dashboard"': 'stringResource(R.string.startup_ad_continue)',
    '"Want to skip ads forever?"': 'stringResource(R.string.startup_ad_skip_prompt)',
    '"Ad-Free plans via Google Play Store (from ₹99/mo)"': 'stringResource(R.string.startup_ad_plans)',
    '"View Plans →"': 'stringResource(R.string.startup_ad_view_plans)',
    
    # PastReadingsScreen.kt
    '"Clear All Past Readings?"': 'stringResource(R.string.past_readings_clear_title)',
    '"Clear All"': 'stringResource(R.string.past_readings_clear_btn)',
    '"Cancel"': 'stringResource(R.string.past_readings_cancel)',
    '"Delete This Reading?"': 'stringResource(R.string.past_readings_delete_title)',
    '"Are you sure you want to remove this prediction from your local history?"': 'stringResource(R.string.past_readings_delete_desc)',
    '"Delete"': 'stringResource(R.string.past_readings_delete_btn)',
    '"Past Readings"': 'stringResource(R.string.past_readings_title)',
    '"Search past readings..."': 'stringResource(R.string.past_readings_search_hint)',
    '"No Past Readings Yet"': 'stringResource(R.string.past_readings_empty)',
    '"Every time you generate personalized life predictions, they will be securely stored in Room database so you can revisit them anytime, even when offline."': 'stringResource(R.string.past_readings_empty_desc)',
    '"Generate Reading"': 'stringResource(R.string.past_readings_generate_btn)',
    '"Vedic Reading"': 'stringResource(R.string.past_readings_vedic_reading)',
    
    # SplashScreen.kt
    '"Tap anywhere to enter"': 'stringResource(R.string.splash_tap_enter)',
    
    # OnboardingScreen.kt
    '"Your Sacred Identity"': 'stringResource(R.string.onboarding_name_title)',
    '"Your name generates your Namank (Name Number) via traditional Chaldean or Pythagorean numerology."': 'stringResource(R.string.onboarding_name_desc)',
    '"When were you born?"': 'stringResource(R.string.onboarding_birth_date_title)',
    '"Used to determine planetary positions."': 'stringResource(R.string.onboarding_birth_date_desc)',
    '"Exact time of birth"': 'stringResource(R.string.onboarding_birth_time_title)',
    '"Crucial for exact Ascendant (Lagna) calculation."': 'stringResource(R.string.onboarding_birth_time_desc)',
    '"Birth Location"': 'stringResource(R.string.onboarding_location_title)',
    '"Search and select city, town, village, or locality via Google Places for accurate coordinates."': 'stringResource(R.string.onboarding_location_desc)',
    '"Vedic Tradition Preferences"': 'stringResource(R.string.onboarding_tradition_title)',
    '"Honoring diverse regional lineages across Northern, Southern, and Western India."': 'stringResource(R.string.onboarding_tradition_desc)',
    '"Smartwatch Integration"': 'stringResource(R.string.onboarding_smartwatch_title)',
    '"Do you have a smartwatch? If yes, please select your supported Wear OS model."': 'stringResource(R.string.onboarding_smartwatch_desc)',
    '"Supported Wear OS Models"': 'stringResource(R.string.onboarding_wear_os_models)',
    '"Compatible across all Wear OS brands and manufacturers."': 'stringResource(R.string.onboarding_wear_os_desc)',
    '"Choose Watch Face Visual Style"': 'stringResource(R.string.onboarding_watch_face_title)',
    '"Select your procedural dial aesthetic. You can freely switch anytime from the watch or phone."': 'stringResource(R.string.onboarding_watch_face_desc)',
    '"Your Personalized Watch Face"': 'stringResource(R.string.onboarding_personalized_watch_face)',
    '"Live Wear OS Smartwatch Procedural Simulation"': 'stringResource(R.string.onboarding_procedural_simulation)',
    '"Astrological Resonance Summary"': 'stringResource(R.string.onboarding_resonance_summary)',
    
    # CompatibilityMatchTab.kt
    '"Kundli Match & Synergy"': 'stringResource(R.string.kundli_match_synergy)',
    '"Compare your chart with a partner to check Ashtakoota compatibility."': 'stringResource(R.string.kundli_match_desc)',
    '"Partner\'s Name"': 'stringResource(R.string.partner_name)',
    '"Partner Date of Birth"': 'stringResource(R.string.partner_dob)',
    '"Partner Time of Birth"': 'stringResource(R.string.partner_tob)',
    '"Partner\'s Birth Location"': 'stringResource(R.string.partner_location)',
    '"OK"': 'stringResource(R.string.btn_ok)',
    
    # ChoghadiyaHoraTab.kt
    '"ACTIVE PLANETARY HORA"': 'stringResource(R.string.active_planetary_hora)',
    
    # PanchangaTab.kt
    '"Panchanga AI Insight"': 'stringResource(R.string.panchanga_ai_insight)',
    '"Close"': 'stringResource(R.string.btn_close)',
    '"Export Dainik Panchang (PDF)"': 'stringResource(R.string.export_dainik_panchang)',
    '"PERSONAL COSMIC HARMONY"': 'stringResource(R.string.personal_cosmic_harmony)',
    '"Surya Transit & Solar Events"': 'stringResource(R.string.surya_transit_events)',
    '"The Five Limbs of Time (Panchanga)"': 'stringResource(R.string.five_limbs_panchanga)',
    '"Calendrical Eras & Seasons"': 'stringResource(R.string.calendrical_eras_seasons)',
    
    # VedicAITab.kt
    '"Ask about Vastu, Astrology, Numbers, Muhurta..."': 'stringResource(R.string.ask_ai_hint)',
    
    # VedicCalendarTab.kt
    '"Calculating Vedic ephemeris..."': 'stringResource(R.string.calculating_ephemeris)',
    '"Sync Panchang to Android Calendar"': 'stringResource(R.string.sync_panchang_calendar)',
    '"Export Brahma Muhurta, Rahu Kaal, and auspicious festivals directly into your device calendar."': 'stringResource(R.string.sync_panchang_desc)',
    '"Export .ics"': 'stringResource(R.string.export_ics)',
    '"+ Brahma Muhurta"': 'stringResource(R.string.add_brahma_muhurta)',
    '"Festivals & Observances on this Day"': 'stringResource(R.string.festivals_observances_day)',
    '"No recorded major festivals or vrats for this month."': 'stringResource(R.string.no_festivals_month)',
    
    # DailyHighlightsTab.kt
    '"A traditional Vedic-inspired summary"': 'stringResource(R.string.vedic_inspired_summary)'
}

complex_replacements = {
    '"Step $step of 8"': 'stringResource(R.string.onboarding_step_fmt, step)',
    '"Hora of ${hora.rulingGraha.displayName} (${hora.rulingGraha.sanskritName})"': 'stringResource(R.string.hora_of_fmt, hora.rulingGraha.displayName, hora.rulingGraha.sanskritName)',
    '"Hora #${hora.horaNumber}: ${hora.rulingGraha.displayName}"': 'stringResource(R.string.hora_num_fmt, hora.horaNumber, hora.rulingGraha.displayName)',
    '"Tarabala: ${harmonyScore.taraType.displayName}"': 'stringResource(R.string.tarabala_fmt, harmonyScore.taraType.displayName)',
    '"Janma Star: ${personalizedProfile.natalNakshatra.sanskritName}"': 'stringResource(R.string.janma_star_fmt, personalizedProfile.natalNakshatra.sanskritName)',
    '"Transit Star: ${p.nakshatra.sanskritName}"': 'stringResource(R.string.transit_star_fmt, p.nakshatra.sanskritName)',
    '"Nakshatra: ${profile.panchanga.nakshatra.name} (Pada ${profile.panchanga.nakshatraPada})"': 'stringResource(R.string.nakshatra_pada_fmt, profile.panchanga.nakshatra.name, profile.panchanga.nakshatraPada)',
    '"Details for ${selectedDate.format(DateTimeFormatter.ofPattern(\\"dd MMMM yyyy\\"))}"': 'stringResource(R.string.details_for_fmt, selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))',
    '"${vrat.name} (Vrat)"': 'stringResource(R.string.vrat_fmt, vrat.name)',
    '"Festivals & Events in ${currentMonth.format(DateTimeFormatter.ofPattern(\\"MMMM yyyy\\"))}"': 'stringResource(R.string.festivals_in_month_fmt, currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")))',
    '"${(progressAnim.value * 100).toInt()}%"': '"${(progressAnim.value * 100).toInt()}%"', # Keep as is, it's just a number
    '"${pastReadings.size} cached reading${if (pastReadings.size == 1) \\"\\" else \\"s\\"} in local storage"': 'if (pastReadings.size == 1) "${pastReadings.size} cached reading in local storage" else "${pastReadings.size} cached readings in local storage"', # A bit complex, we can handle it later or ignore
}

import glob

for root, dirs, files in os.walk("app/src/main/java/com/example/ui"):
    for file in files:
        if file.endswith(".kt"):
            filepath = os.path.join(root, file)
            with open(filepath, 'r') as f:
                content = f.read()
            
            modified = False
            for k, v in replacements.items():
                if k in content:
                    content = content.replace(k, v)
                    modified = True
                    
            for k, v in complex_replacements.items():
                if k in content:
                    content = content.replace(k, v)
                    modified = True
                    
            if modified:
                # Add import if missing
                if "import androidx.compose.ui.res.stringResource" not in content and "stringResource" in content:
                    # Find a good place to insert import
                    import_idx = content.find("import ")
                    if import_idx != -1:
                        content = content[:import_idx] + "import androidx.compose.ui.res.stringResource\nimport com.example.R\n" + content[import_idx:]
                with open(filepath, 'w') as f:
                    f.write(content)
                print(f"Patched {filepath}")

