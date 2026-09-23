import os
import xml.etree.ElementTree as ET

translations = {
    "hi": {
        "app_name": "वैदिक कॉस्मिक घड़ी",
        "onboarding_title": "वैदिक कॉस्मिक घड़ी",
        "onboarding_subtitle": "अपनी व्यक्तिगत ज्योतिषीय प्रोफ़ाइल प्रारंभ करें",
        "birth_date": "जन्म तिथि",
        "birth_time": "जन्म समय",
        "birth_location": "जन्म स्थान",
        "calculate_profile": "प्रोफ़ाइल जनरेट करें",
        "tab_daily_highlights": "दैनिक मुख्य बातें",
        "tab_panchanga": "पंचांग",
        "tab_muhurtas": "मुहूर्त",
        "tab_choghadiya_hora": "चौघड़िया और होरा",
        "tab_vedic_calendar": "वैदिक कैलेंडर",
        "tab_kundali_chart": "कुंडली चार्ट",
        "tab_numerology": "अंक ज्योतिष",
        "cat_dashboard": "डैशबोर्ड",
        "yes_i_do": "हाँ",
        "no_phone_only": "नहीं (केवल फोन)",
        "tab_life_predictions": "जीवन की भविष्यवाणियां",
        "tab_compatibility_match": "कुंडली मिलान",
        "tab_profile_settings": "प्रोफ़ाइल सेटिंग्स",
        "active_now": "अभी सक्रिय है",
        "active_hora": "सक्रिय होरा",
        "day_choghadiya": "दिन का चौघड़िया",
        "night_choghadiya": "रात का चौघड़िया",
        "tab_vastu_compass": "वास्तु कम्पास"
    },
    "gu": {
        "app_name": "વૈદિક કોસ્મિક ઘડિયાળ",
        "onboarding_title": "વૈદિક કોસ્મિક ઘડિયાળ",
        "onboarding_subtitle": "તમારી વ્યક્તિગત જ્યોતિષીય પ્રોફાઇલ શરૂ કરો",
        "birth_date": "જન્મ તારીખ",
        "birth_time": "જન્મ સમય",
        "birth_location": "જન્મ સ્થળ",
        "calculate_profile": "પ્રોફાઇલ બનાવો",
        "tab_daily_highlights": "દૈનિક હાઇલાઇટ્સ",
        "tab_panchanga": "પંચાંગ",
        "tab_muhurtas": "મુહૂર્ત",
        "tab_choghadiya_hora": "ચોઘડિયા અને હોરા",
        "tab_vedic_calendar": "વૈદિક કેલેન્ડર",
        "tab_kundali_chart": "કુંડળી ચાર્ટ",
        "tab_numerology": "અંકશાસ્ત્ર",
        "cat_dashboard": "ડેશબોર્ડ",
        "yes_i_do": "હા",
        "no_phone_only": "ના (ફક્ત ફોન)",
        "tab_life_predictions": "જીવનની આગાહીઓ",
        "tab_compatibility_match": "કુંડળી મેળ",
        "tab_profile_settings": "પ્રોફાઇલ સેટિંગ્સ",
        "active_now": "હવે સક્રિય છે",
        "active_hora": "સક્રિય હોરા",
        "day_choghadiya": "દિવસનું ચોઘડિયું",
        "night_choghadiya": "રાતનું ચોઘડિયું",
        "tab_vastu_compass": "વાસ્તુ હોકાયંત્ર"
    },
    "ta": {
        "app_name": "வேத காஸ்மிக் கடிகாரம்",
        "birth_date": "பிறந்த தேதி",
        "birth_time": "பிறந்த நேரம்",
        "tab_panchanga": "பஞ்சாங்கம்",
        "tab_muhurtas": "முகூர்த்தங்கள்",
        "yes_i_do": "ஆம்",
        "no_phone_only": "இல்லை",
        "tab_life_predictions": "வாழ்க்கை கணிப்புகள்",
        "tab_compatibility_match": "பொருத்தம்",
        "tab_profile_settings": "சுயவிவர அமைப்புகள்"
    },
    "te": {
        "app_name": "వేద కాస్మిక్ గడియారం",
        "birth_date": "పుట్టిన తేదీ",
        "birth_time": "పుట్టిన సమయం",
        "tab_panchanga": "పంచాంగం",
        "tab_muhurtas": "ముహూర్తాలు",
        "yes_i_do": "అవును",
        "no_phone_only": "లేదు",
        "tab_life_predictions": "జీవిత అంచనాలు",
        "tab_compatibility_match": "పొంతన",
        "tab_profile_settings": "ప్రొఫైల్ సెట్టింగులు"
    },
    "sa": {
        "app_name": "वैदिक-घटी",
        "onboarding_title": "वैदिक-घटी",
        "birth_date": "जन्मदिनम्",
        "birth_time": "जन्मसमयः",
        "birth_location": "जन्मस्थानम्",
        "calculate_profile": "आरम्भम् कुरु",
        "tab_panchanga": "पञ्चाङ्गम्",
        "tab_muhurtas": "मुहूर्ताः",
        "tab_choghadiya_hora": "होरा",
        "tab_kundali_chart": "कुण्डली",
        "yes_i_do": "आम्",
        "no_phone_only": "न",
        "tab_life_predictions": "भविष्यवाणी",
        "tab_compatibility_match": "मेलापकम्",
        "tab_profile_settings": "व्यवस्थाः"
    }
}

base_path = "app/src/main/res/"

def update_xml(lang):
    file_path = os.path.join(base_path, f"values-{lang}", "strings.xml")
    if not os.path.exists(file_path):
        return
    
    tree = ET.parse(file_path)
    root = tree.getroot()
    
    trans_dict = translations.get(lang, {})
    if not trans_dict:
        return
        
    for child in root:
        name = child.get("name")
        if name in trans_dict:
            child.text = trans_dict[name]
            
    tree.write(file_path, encoding="utf-8", xml_declaration=True)
    print(f"Updated {file_path}")

for l in translations.keys():
    update_xml(l)

