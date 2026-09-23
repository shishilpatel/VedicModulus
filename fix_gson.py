import sys

path = "app/src/main/java/com/example/ui/screens/tabs/CompatibilityMatchTab.kt"
with open(path, "r") as f:
    content = f.read()

content = content.replace("import com.google.gson.Gson\n", "")

replacement = """
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("kundali_match_prefs", Context.MODE_PRIVATE)
    val scope = rememberCoroutineScope()
    
    var partnerName by remember { mutableStateOf(prefs.getString("p_name", "") ?: "") }
    var pDay by remember { mutableStateOf(prefs.getString("p_day", "01") ?: "01") }
    var pMonth by remember { mutableStateOf(prefs.getString("p_month", "01") ?: "01") }
    var pYear by remember { mutableStateOf(prefs.getString("p_year", "1990") ?: "1990") }
    var pHour by remember { mutableStateOf(prefs.getString("p_hour", "12") ?: "12") }
    var pMinute by remember { mutableStateOf(prefs.getString("p_minute", "00") ?: "00") }
    
    val savedLat = prefs.getString("p_loc_lat", null)
    var pLocation by remember { 
        mutableStateOf<LocationData?>(
            if (savedLat != null) {
                try {
                    LocationData(
                        country = prefs.getString("p_loc_country", "India") ?: "India",
                        state = prefs.getString("p_loc_state", "Unknown") ?: "Unknown",
                        district = prefs.getString("p_loc_district", "Unknown") ?: "Unknown",
                        talukaOrTown = prefs.getString("p_loc_town", "Unknown") ?: "Unknown",
                        cityName = prefs.getString("p_loc_city", "Unknown") ?: "Unknown",
                        latitude = savedLat.toDouble(),
                        longitude = prefs.getString("p_loc_lng", "0.0")?.toDouble() ?: 0.0,
                        timezoneId = prefs.getString("p_loc_tz", "Asia/Kolkata") ?: "Asia/Kolkata",
                        officialLocationCode = prefs.getString("p_loc_code", "") ?: "",
                        alternateNames = emptyList()
                    )
                } catch(e:Exception) { null }
            } else {
                null
            }
        ) 
    }
    
    var isCalculating by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Helper to save to prefs
    fun savePrefs() {
        prefs.edit().apply {
            putString("p_name", partnerName)
            putString("p_day", pDay)
            putString("p_month", pMonth)
            putString("p_year", pYear)
            putString("p_hour", pHour)
            putString("p_minute", pMinute)
            if (pLocation != null) {
                putString("p_loc_country", pLocation!!.country)
                putString("p_loc_state", pLocation!!.state)
                putString("p_loc_district", pLocation!!.district)
                putString("p_loc_town", pLocation!!.talukaOrTown)
                putString("p_loc_city", pLocation!!.cityName)
                putString("p_loc_lat", pLocation!!.latitude.toString())
                putString("p_loc_lng", pLocation!!.longitude.toString())
                putString("p_loc_tz", pLocation!!.timezoneId)
                putString("p_loc_code", pLocation!!.officialLocationCode)
            }
        }.apply()
    }
"""

import re
pattern = r"val context = LocalContext\.current.*?fun savePrefs\(\) \{.*?\.apply\(\)\n    \}"
content = re.sub(pattern, replacement.strip(), content, flags=re.DOTALL)

with open(path, "w") as f:
    f.write(content)
