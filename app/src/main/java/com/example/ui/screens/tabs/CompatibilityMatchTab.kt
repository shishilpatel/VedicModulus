@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens.tabs

import androidx.compose.ui.res.stringResource
import com.example.R
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import com.example.ui.theme.LightBorder
import com.example.ui.theme.OrangeAccent

@Composable
fun CompatibilityMatchTab(
    profile: BirthProfile,
    personalizationEngine: VedicPersonalizationEngine,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.kundli_match_synergy),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaroonPrimary
            )
        )
        Text(
            text = stringResource(R.string.kundli_match_desc),
            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = partnerName,
            onValueChange = { partnerName = it; savePrefs() },
            label = { Text(stringResource(R.string.partner_name), fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = try { 
                java.time.LocalDate.of(pYear.toInt(), pMonth.toInt(), pDay.toInt())
                    .atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
            } catch(e:Exception) { null }
        )
        
        val timeState = rememberTimePickerState(
            initialHour = pHour.toIntOrNull() ?: 12,
            initialMinute = pMinute.toIntOrNull() ?: 0,
            is24Hour = false
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(stringResource(R.string.partner_dob), style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${pDay.padStart(2, '0')} / ${pMonth.padStart(2, '0')} / $pYear",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
            
            OutlinedCard(
                onClick = { showTimePicker = true },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(stringResource(R.string.partner_tob), style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${pHour.padStart(2, '0')}:${pMinute.padStart(2, '0')}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
        
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        dateState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
                            pYear = date.year.toString()
                            pMonth = date.monthValue.toString()
                            pDay = date.dayOfMonth.toString()
                            savePrefs()
                        }
                        showDatePicker = false
                    }) { Text(stringResource(R.string.btn_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.past_readings_cancel)) }
                }
            ) {
                DatePicker(state = dateState)
            }
        }
        
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pHour = timeState.hour.toString()
                        pMinute = timeState.minute.toString()
                        savePrefs()
                        showTimePicker = false
                    }) { Text(stringResource(R.string.btn_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.past_readings_cancel)) }
                },
                text = {
                    TimePicker(state = timeState)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        Text(stringResource(R.string.partner_location), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(4.dp))
        
        GooglePlacesSearchField(
            selectedLocation = pLocation,
            onLocationSelected = { loc ->
                pLocation = loc
                savePrefs()
            }
        )
            
        Spacer(modifier = Modifier.height(12.dp))
        
        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(6.dp))
        }
            
        var aiInterpretation by remember { mutableStateOf<String?>(null) }
        var isTranslatingWithAi by remember { mutableStateOf(false) }

        val errValidDateTime = stringResource(R.string.err_valid_datetime)
        val errInvalidDate = stringResource(R.string.err_invalid_date)
        val errInvalidTime = stringResource(R.string.err_invalid_time)
        val errSelectLoc = stringResource(R.string.err_select_location)

        Button(
            onClick = {
                val y = pYear.toIntOrNull()
                val m = pMonth.toIntOrNull()
                val d = pDay.toIntOrNull()
                val hh = pHour.toIntOrNull()
                val mm = pMinute.toIntOrNull()
                
                if (y == null || m == null || d == null || hh == null || mm == null) {
                    errorMessage = errValidDateTime
                    return@Button
                }
                
                val maxDays = try { java.time.YearMonth.of(y, m).lengthOfMonth() } catch(e:Exception) { 31 }
                if (m !in 1..12 || d !in 1..maxDays) {
                    errorMessage = errInvalidDate
                    return@Button
                }
                if (hh !in 0..23 || mm !in 0..59) {
                    errorMessage = errInvalidTime
                    return@Button
                }
                
                if (pLocation == null) {
                    errorMessage = errSelectLoc
                    return@Button
                }
                
                errorMessage = null
                isCalculating = true
                aiInterpretation = null
                
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
                        
                        val myPers = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { personalizationEngine.createPersonalizedProfile(profile) }
                        val pPers = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { personalizationEngine.createPersonalizedProfile(partnerBirthProfile) }
                        
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
                        
                        val scoreText = matchRes.scores.joinToString("\n") { "${it.kootaName}: ${it.pointsObtained}/${it.maxPoints}" }
                        
                        val myName = profile.fullName.split(" ").firstOrNull() ?: "You"
                        resultText = "Ashtakoota Match Score for $myName & ${partnerBirthProfile.fullName}:\n\n" +
                            "Score: ${matchRes.totalPoints} / 36 (${matchRes.compatibilityPercentage}%)\n\n" +
                            scoreText + "\n\n" +
                            "Manglik Compatibility: ${matchRes.manglikCompatibilityVerdict}\n\n" +
                            "Overall Compatibility: ${matchRes.ratingCategory}. ${matchRes.overallInterpretation}"

                        // Step 2: Pass generated details to AI for translated, rich cultural interpretation
                        isTranslatingWithAi = true
                        aiInterpretation = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            com.example.engines.ai.AstrologicalCalculationAiBridge.interpretCompatibility(
                                matchResult = matchRes,
                                seekerName = myName,
                                partnerName = partnerBirthProfile.fullName,
                                seekerMoon = "${myPers.natalChandraRashi.westernName} / ${myPers.natalChandraRashi.sanskritName} (${myPers.natalNakshatra.sanskritName})",
                                partnerMoon = "${pPers.natalChandraRashi.westernName} / ${pPers.natalChandraRashi.sanskritName} (${pPers.natalNakshatra.sanskritName})",
                                currentLanguage = currentLanguage
                            )
                        }
                    } catch (e: Exception) {
                        errorMessage = "Calculation Error: ${e.message}"
                    } finally {
                        isCalculating = false
                        isTranslatingWithAi = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary, contentColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCalculating
        ) {
            Text(if (isCalculating) stringResource(R.string.calculating_stars) else stringResource(R.string.btn_calculate_match), color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (resultText.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Translated & Synthesized Compatibility Report
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.ai_compatibility_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaroonPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (isTranslatingWithAi) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaroonPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isTranslatingWithAi) {
                        val languagePromptName = com.example.core.language.LanguageManager.getLanguagePromptName(currentLanguage)
                        Text(
                            text = stringResource(R.string.translating_compatibility, languagePromptName),
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSecondary)
                        )
                    } else if (aiInterpretation != null) {
                        Text(
                            text = aiInterpretation!!,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp)
                        )
                    }
                }
            }
        }
    }
}
