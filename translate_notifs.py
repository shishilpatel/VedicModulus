import os
import xml.etree.ElementTree as ET

new_strings = [
    "notif_vedic_panchang", "notif_location", "notif_tithi", "notif_nakshatra",
    "notif_yoga", "notif_karana", "notif_brahma_muhurta", "notif_abhijit_muhurta",
    "notif_rahu_kaal", "notif_festival", "notif_vrat", "notif_cosmic_energy",
    "notif_active_hora", "notif_choghadiya"
]

translations = {
    "hi": {
        "notif_vedic_panchang": "दैनिक पंचांग",
        "notif_location": "स्थान:",
        "notif_tithi": "तिथि:",
        "notif_nakshatra": "नक्षत्र:",
        "notif_yoga": "योग:",
        "notif_karana": "करण:",
        "notif_brahma_muhurta": "ब्रह्म मुहूर्त:",
        "notif_abhijit_muhurta": "अभिजित मुहूर्त:",
        "notif_rahu_kaal": "राहु काल:",
        "notif_festival": "त्यौहार:",
        "notif_vrat": "पवित्र व्रत:",
        "notif_cosmic_energy": "ब्रह्मांडीय ऊर्जा:",
        "notif_active_hora": "सक्रिय होरा:",
        "notif_choghadiya": "चौघड़िया:"
    },
    "gu": {
        "notif_vedic_panchang": "દૈનિક પંચાંગ",
        "notif_location": "સ્થાન:",
        "notif_tithi": "તિથિ:",
        "notif_nakshatra": "નક્ષત્ર:",
        "notif_yoga": "યોગ:",
        "notif_karana": "કરણ:",
        "notif_brahma_muhurta": "બ્રહ્મ મુહૂર્ત:",
        "notif_abhijit_muhurta": "અભિજિત મુહૂર્ત:",
        "notif_rahu_kaal": "રાહુ કાળ:",
        "notif_festival": "તહેવાર:",
        "notif_vrat": "વ્રત:",
        "notif_cosmic_energy": "કોસ્મિક ઊર્જા:",
        "notif_active_hora": "સક્રિય હોરા:",
        "notif_choghadiya": "ચોઘડિયા:"
    },
    "ta": {
        "notif_vedic_panchang": "தினசரி பஞ்சாங்கம்",
        "notif_location": "இடம்:",
        "notif_tithi": "திதி:",
        "notif_nakshatra": "நட்சத்திரம்:",
        "notif_yoga": "யோகம்:",
        "notif_karana": "கரணம்:",
        "notif_brahma_muhurta": "பிரம்ம முகூர்த்தம்:",
        "notif_abhijit_muhurta": "அபிஜித் முகூர்த்தம்:",
        "notif_rahu_kaal": "ராகு காலம்:",
        "notif_festival": "பண்டிகை:",
        "notif_vrat": "விரதம்:"
    },
    "te": {
        "notif_vedic_panchang": "రోజువారీ పంచాంగం",
        "notif_location": "స్థానం:",
        "notif_tithi": "తిథి:",
        "notif_nakshatra": "నక్షత్రం:",
        "notif_yoga": "యోగం:",
        "notif_karana": "కరణం:",
        "notif_brahma_muhurta": "బ్రహ్మ ముహూర్తం:",
        "notif_abhijit_muhurta": "అభిజిత్ ముహూర్తం:",
        "notif_rahu_kaal": "రాహు కాలం:",
        "notif_festival": "పండుగ:",
        "notif_vrat": "వ్రతం:"
    },
    "sa": {
        "notif_vedic_panchang": "दैनिक पञ्चाङ्गम्",
        "notif_location": "स्थानम्:",
        "notif_tithi": "तिथिः:",
        "notif_nakshatra": "नक्षत्रम्:",
        "notif_yoga": "योगः:",
        "notif_karana": "करणम्:",
        "notif_brahma_muhurta": "ब्रह्म-मुहूर्तः:",
        "notif_abhijit_muhurta": "अभिजित-मुहूर्तः:",
        "notif_rahu_kaal": "राहुकालः:",
        "notif_festival": "उत्सवः:",
        "notif_vrat": "व्रतम्:"
    }
}

base_path = "app/src/main/res/"

for lang in translations.keys():
    file_path = os.path.join(base_path, f"values-{lang}", "strings.xml")
    if not os.path.exists(file_path):
        continue
    
    tree = ET.parse(file_path)
    root = tree.getroot()
    
    trans_dict = translations.get(lang, {})
    
    for key, value in trans_dict.items():
        # check if it exists
        found = False
        for child in root:
            if child.get("name") == key:
                child.text = value
                found = True
                break
        if not found:
            el = ET.SubElement(root, "string", {"name": key})
            el.text = value
            
    tree.write(file_path, encoding="utf-8", xml_declaration=True)
    print(f"Added notifs to {file_path}")

