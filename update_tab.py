import sys

path = "app/src/main/java/com/example/ui/screens/tabs/CompatibilityMatchTab.kt"
with open(path, "r") as f:
    content = f.read()

replacement = """package com.example.ui.screens.tabs

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.core.model.BirthProfile
import com.example.core.model.LocationData
import com.example.engines.personalization.VedicPersonalizationEngine
import com.example.engines.jyotisha.KundliMatchingEngine
import com.example.ui.theme.MaroonPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.components.GooglePlacesSearchField
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.gson.Gson

@Composable
fun CompatibilityMatchTab(
    profile: BirthProfile,
    personalizationEngine: VedicPersonalizationEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("kundali_match_prefs", Context.MODE_PRIVATE)
    val gson = Gson()
    val scope = rememberCoroutineScope()
    
    var partnerName by remember { mutableStateOf(prefs.getString("p_name", "") ?: "") }
    var pDay by remember { mutableStateOf(prefs.getString("p_day", "01") ?: "01") }
    var pMonth by remember { mutableStateOf(prefs.getString("p_month", "01") ?: "01") }
    var pYear by remember { mutableStateOf(prefs.getString("p_year", "1990") ?: "1990") }
    var pHour by remember { mutableStateOf(prefs.getString("p_hour", "12") ?: "12") }
    var pMinute by remember { mutableStateOf(prefs.getString("p_minute", "00") ?: "00") }
    
    val savedLocJson = prefs.getString("p_location", null)
    var pLocation by remember { 
        mutableStateOf<LocationData?>(
            if (savedLocJson != null) {
                try { gson.fromJson(savedLocJson, LocationData::class.java) } catch(e:Exception) { null }
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
                putString("p_location", gson.toJson(pLocation))
            }
        }.apply()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Kundli Match & Synergy",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaroonPrimary
            )
        )
        Text(
            text = "Compare your chart with a partner to check Ashtakoota compatibility.",
            style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextSecondary)
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = partnerName,
            onValueChange = { partnerName = it; savePrefs() },
            label = { Text("Partner's Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Text("Partner's Date of Birth", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = pDay,
                onValueChange = { if(it.length <= 2) { pDay = it; savePrefs() } },
                label = { Text("DD") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = pMonth,
                onValueChange = { if(it.length <= 2) { pMonth = it; savePrefs() } },
                label = { Text("MM") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = pYear,
                onValueChange = { if(it.length <= 4) { pYear = it; savePrefs() } },
                label = { Text("YYYY") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1.2f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Text("Partner's Time of Birth", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = pHour,
                onValueChange = { if(it.length <= 2) { pHour = it; savePrefs() } },
                label = { Text("HH (24h)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = pMinute,
                onValueChange = { if(it.length <= 2) { pMinute = it; savePrefs() } },
                label = { Text("MM") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Partner's Birth Location", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(8.dp))
        
        GooglePlacesSearchField(
            selectedLocation = pLocation,
            onLocationSelected = { loc ->
                pLocation = loc
                savePrefs()
            }
        )
            
        Spacer(modifier = Modifier.height(24.dp))
        
        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }
            
        Button(
            onClick = {
                val y = pYear.toIntOrNull()
                val m = pMonth.toIntOrNull()
                val d = pDay.toIntOrNull()
                val hh = pHour.toIntOrNull()
                val mm = pMinute.toIntOrNull()
                
                if (y == null || m == null || d == null || hh == null || mm == null) {
                    errorMessage = "Please enter valid numerical date and time."
                    return@Button
                }
                
                val maxDays = try { java.time.YearMonth.of(y, m).lengthOfMonth() } catch(e:Exception) { 31 }
                if (m !in 1..12 || d !in 1..maxDays) {
                    errorMessage = "Invalid date. Please check month and day."
                    return@Button
                }
                if (hh !in 0..23 || mm !in 0..59) {
                    errorMessage = "Invalid time. Hours must be 0-23, Minutes 0-59."
                    return@Button
                }
                
                if (pLocation == null) {
                    errorMessage = "Please select the partner's birth location (or enter coordinates manually)."
                    return@Button
                }
                
                errorMessage = null
                isCalculating = true
                
                scope.launch {
                    try {
                        val pDate = LocalDate.of(y, m, d)
                        val pTime = LocalTime.of(hh, mm)
                        val partnerBirthProfile = profile.copy(
                            fullName = if (partnerName.isBlank()) "Partner" else partnerName,
                            birthDate = pDate,
                            birthTime = pTime,
                            birthLocation = pLocation!!
                        )
                        
                        val myPers = personalizationEngine.createPersonalizedProfile(profile)
                        val pPers = personalizationEngine.createPersonalizedProfile(partnerBirthProfile)
                        
                        val matchRes = KundliMatchingEngine.matchKundlis(
                            boyRashi = myPers.natalChandraRashi,
                            boyNak = myPers.natalNakshatra,
                            boyPada = myPers.natalNakshatraPada,
                            girlRashi = pPers.natalChandraRashi,
                            girlNak = pPers.natalNakshatra,
                            girlPada = pPers.natalNakshatraPada,
                            boyLagna = myPers.natalLagnaRashi,
                            girlLagna = pPers.natalLagnaRashi
                        )
                        
                        val scoreText = matchRes.scores.joinToString("\\n") { "${it.kootaName}: ${it.pointsObtained}/${it.maxPoints}" }
                        
                        val myName = profile.fullName.split(" ").firstOrNull() ?: "You"
                        resultText = "Ashtakoota Match Score for $myName & ${partnerBirthProfile.fullName}:\\n\\n" +
                            "Score: ${matchRes.totalPoints} / 36 (${matchRes.compatibilityPercentage}%)\\n\\n" +
                            scoreText + "\\n\\n" +
                            "Manglik Compatibility: ${matchRes.manglikCompatibilityVerdict}\\n\\n" +
                            "Overall Compatibility: ${matchRes.ratingCategory}. ${matchRes.overallInterpretation}"
                    } catch (e: Exception) {
                        errorMessage = "Calculation Error: ${e.message}"
                    } finally {
                        isCalculating = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCalculating
        ) {
            Text(if (isCalculating) "Calculating Stars..." else "Calculate Match")
        }

        if (resultText.isNotBlank()) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
"""

with open(path, "w") as f:
    f.write(replacement)
