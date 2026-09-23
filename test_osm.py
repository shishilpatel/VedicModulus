import urllib.request
import json

req = urllib.request.Request(
    'https://nominatim.openstreetmap.org/search?q=Delhi&format=json&limit=5',
    headers={'User-Agent': 'VedicAstrologyApp/1.0'}
)
with urllib.request.urlopen(req) as response:
    data = json.loads(response.read().decode())
    for item in data:
        print(item['display_name'], item['lat'], item['lon'])
