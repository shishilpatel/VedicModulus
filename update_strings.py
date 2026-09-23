import os
import xml.etree.ElementTree as ET

strings = {
    "startup_ad_verified": "Verified Vedic & Wellness Partner",
    "startup_ad_unlock": "Watch Ad to Unlock App",
    "startup_ad_continue": "Continue to Vedic Dashboard",
    "startup_ad_skip_prompt": "Want to skip ads forever?",
    "startup_ad_plans": "Ad-Free plans via Google Play Store (from ₹99/mo)",
    "startup_ad_view_plans": "View Plans →",
    "past_readings_clear_title": "Clear All Past Readings?",
    "past_readings_clear_btn": "Clear All",
    "past_readings_cancel": "Cancel",
    "past_readings_delete_title": "Delete This Reading?",
    "past_readings_delete_desc": "Are you sure you want to remove this prediction from your local history?",
    "past_readings_delete_btn": "Delete",
    "past_readings_title": "Past Readings",
    "past_readings_search_hint": "Search past readings...",
    "past_readings_empty": "No Past Readings Yet",
    "past_readings_empty_desc": "Every time you generate personalized life predictions, they will be securely stored in Room database so you can revisit them anytime, even when offline.",
    "past_readings_generate_btn": "Generate Reading",
    "past_readings_vedic_reading": "Vedic Reading",
    "splash_tap_enter": "Tap anywhere to enter",
    "onboarding_name_title": "Your Sacred Identity",
    "onboarding_name_desc": "Your name generates your Namank (Name Number) via traditional Chaldean or Pythagorean numerology.",
    "onboarding_birth_date_title": "When were you born?",
    "onboarding_birth_date_desc": "Used to determine planetary positions.",
    "onboarding_birth_time_title": "Exact time of birth",
    "onboarding_birth_time_desc": "Crucial for exact Ascendant (Lagna) calculation.",
    "onboarding_location_title": "Birth Location",
    "onboarding_location_desc": "Search and select city, town, village, or locality via Google Places for accurate coordinates.",
    "onboarding_tradition_title": "Vedic Tradition Preferences",
    "onboarding_tradition_desc": "Honoring diverse regional lineages across Northern, Southern, and Western India.",
    "onboarding_smartwatch_title": "Smartwatch Integration",
    "onboarding_smartwatch_desc": "Do you have a smartwatch? If yes, please select your supported Wear OS model.",
    "onboarding_wear_os_models": "Supported Wear OS Models",
    "onboarding_wear_os_desc": "Compatible across all Wear OS brands and manufacturers.",
    "onboarding_watch_face_title": "Choose Watch Face Visual Style",
    "onboarding_watch_face_desc": "Select your procedural dial aesthetic. You can freely switch anytime from the watch or phone.",
    "onboarding_personalized_watch_face": "Your Personalized Watch Face",
    "onboarding_procedural_simulation": "Live Wear OS Smartwatch Procedural Simulation",
    "onboarding_resonance_summary": "Astrological Resonance Summary",
    "kundli_match_synergy": "Kundli Match & Synergy",
    "kundli_match_desc": "Compare your chart with a partner to check Ashtakoota compatibility.",
    "partner_name": "Partner's Name",
    "partner_dob": "Partner Date of Birth",
    "partner_tob": "Partner Time of Birth",
    "partner_location": "Partner's Birth Location",
    "btn_ok": "OK",
    "active_planetary_hora": "ACTIVE PLANETARY HORA",
    "panchanga_ai_insight": "Panchanga AI Insight",
    "btn_close": "Close",
    "export_dainik_panchang": "Export Dainik Panchang (PDF)",
    "personal_cosmic_harmony": "PERSONAL COSMIC HARMONY",
    "surya_transit_events": "Surya Transit & Solar Events",
    "five_limbs_panchanga": "The Five Limbs of Time (Panchanga)",
    "calendrical_eras_seasons": "Calendrical Eras & Seasons",
    "ask_ai_hint": "Ask about Vastu, Astrology, Numbers, Muhurta...",
    "calculating_ephemeris": "Calculating Vedic ephemeris...",
    "sync_panchang_calendar": "Sync Panchang to Android Calendar",
    "sync_panchang_desc": "Export Brahma Muhurta, Rahu Kaal, and auspicious festivals directly into your device calendar.",
    "export_ics": "Export .ics",
    "add_brahma_muhurta": "+ Brahma Muhurta",
    "festivals_observances_day": "Festivals & Observances on this Day",
    "no_festivals_month": "No recorded major festivals or vrats for this month.",
    "vedic_inspired_summary": "A traditional Vedic-inspired summary"
}

# Assuming only English for now, we'll append to default strings.xml.
# In a real comprehensive app, we'd add localized dicts as well.
tree = ET.parse("app/src/main/res/values/strings.xml")
root = tree.getroot()

existing_names = {child.get("name") for child in root if child.tag == "string"}

for key, val in strings.items():
    if key not in existing_names:
        el = ET.SubElement(root, "string", {"name": key})
        el.text = val

# Also, there are string formats needed:
formats = {
    "onboarding_step_fmt": "Step %1$d of 8",
    "hora_of_fmt": "Hora of %1$s (%2$s)",
    "hora_num_fmt": "Hora #%1$d: %2$s",
    "tarabala_fmt": "Tarabala: %1$s",
    "janma_star_fmt": "Janma Star: %1$s",
    "transit_star_fmt": "Transit Star: %1$s",
    "nakshatra_pada_fmt": "Nakshatra: %1$s (Pada %2$s)",
    "details_for_fmt": "Details for %1$s",
    "vrat_fmt": "%1$s (Vrat)",
    "festivals_in_month_fmt": "Festivals & Events in %1$s"
}
for key, val in formats.items():
    if key not in existing_names:
        el = ET.SubElement(root, "string", {"name": key})
        el.text = val

# Now let's try to parse the localized files and add translations
# For this script we will just add the English texts as placeholders for the other languages,
# then we'll run a translation dictionary pass on it.

tree.write("app/src/main/res/values/strings.xml", encoding="utf-8", xml_declaration=True)

