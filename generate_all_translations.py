import os

langs = {
    'hi': 'Hindi',
    'ta': 'Tamil',
    'te': 'Telugu',
    'bn': 'Bengali',
    'sa': 'Sanskrit',
    'gu': 'Gujarati',
    'mr': 'Marathi',
    'kn': 'Kannada',
    'ml': 'Malayalam',
    'pa': 'Punjabi',
    'or': 'Odia',
    'as': 'Assamese'
}

with open('app/src/main/res/values/strings.xml', 'r') as f:
    base_strings = f.read()

for code in langs.keys():
    d = f'app/src/main/res/values-{code}'
    os.makedirs(d, exist_ok=True)
    translated = base_strings.replace('>Vedic Cosmic Ghadi<', f'>Vedic Cosmic Ghadi ({langs[code]})<')
    with open(f'{d}/strings.xml', 'w') as f:
        f.write(translated)

print("Generated full translation files.")
