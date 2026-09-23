import sys

path = "app/src/main/java/com/example/ui/components/GooglePlacesSearchField.kt"
with open(path, "r") as f:
    content = f.read()

replacement = """package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.api.GooglePlacesApiService
import com.example.api.OsmNominatimApiService
import com.example.core.model.LocationData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GooglePlacesSearchField(
    selectedLocation: LocationData?,
    onLocationSelected: (LocationData) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf(selectedLocation?.talukaOrTown ?: "") }
    var suggestions by remember { mutableStateOf<List<com.example.api.OsmPlace>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasSearched by remember { mutableStateOf(false) }
    
    // Manual fallback mode state
    var isManualMode by remember { mutableStateOf(false) }
    var manualCity by remember { mutableStateOf(selectedLocation?.cityName ?: "") }
    var manualLat by remember { mutableStateOf(selectedLocation?.latitude?.toString() ?: "") }
    var manualLng by remember { mutableStateOf(selectedLocation?.longitude?.toString() ?: "") }
    var manualTimezone by remember { mutableStateOf(selectedLocation?.timezoneId ?: "Asia/Kolkata") }

    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }
    
    val apiKey = BuildConfig.MAPS_API_KEY
    
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isManualMode) "Manual Coordinates" else "Search Location",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            )
            TextButton(onClick = { isManualMode = !isManualMode }) {
                Icon(if (isManualMode) Icons.Default.Search else Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isManualMode) "Use Search" else "Enter Manually")
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        if (isManualMode) {
            // Manual Mode UI
            OutlinedTextField(
                value = manualCity,
                onValueChange = { manualCity = it },
                label = { Text("City/Town Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = manualLat,
                    onValueChange = { manualLat = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = manualLng,
                    onValueChange = { manualLng = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = manualTimezone,
                onValueChange = { manualTimezone = it },
                label = { Text("Timezone ID (e.g. Asia/Kolkata)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val lat = manualLat.toDoubleOrNull()
                    val lng = manualLng.toDoubleOrNull()
                    if (lat == null || lng == null) {
                        errorMessage = "Please enter valid numerical latitude and longitude."
                    } else if (manualCity.isBlank()) {
                        errorMessage = "Please enter a city name."
                    } else {
                        errorMessage = null
                        val loc = LocationData(
                            country = "India",
                            state = "Unknown",
                            district = "Unknown",
                            talukaOrTown = manualCity,
                            cityName = manualCity,
                            latitude = lat,
                            longitude = lng,
                            timezoneId = manualTimezone,
                            officialLocationCode = "MANUAL",
                            alternateNames = listOf(manualCity)
                        )
                        onLocationSelected(loc)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Manual Coordinates")
            }
            
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            // Search Mode UI
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    hasSearched = true
                    errorMessage = null

                    searchJob?.cancel()

                    if (query.trim().length < 3) {
                        suggestions = emptyList()
                        isLoading = false
                        return@OutlinedTextField
                    }

                    searchJob = coroutineScope.launch {
                        delay(800) // Debounce 800ms to avoid rate limiting
                        isLoading = true
                        try {
                            val response = OsmNominatimApiService.service.search(query = query)
                            suggestions = response
                        } catch (e: Exception) {
                            errorMessage = "Location Search Error: ${e.message ?: "Unknown error"}. Please check network or use 'Enter Manually'."
                            suggestions = emptyList()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                placeholder = { Text("Search city, town, village (Powered by OpenStreetMap)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }

            if (errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (!isLoading && suggestions.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        suggestions.forEach { prediction ->
                            val mainText = prediction.display_name
                            val city = prediction.address?.city ?: prediction.address?.town ?: prediction.address?.village ?: mainText.substringBefore(",").trim()
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchQuery = city
                                        suggestions = emptyList()
                                        
                                        val lat = prediction.lat.toDoubleOrNull() ?: 28.6139
                                        val lng = prediction.lon.toDoubleOrNull() ?: 77.2090
                                        
                                        val locData = LocationData(
                                            country = prediction.address?.country ?: "India",
                                            state = prediction.address?.state ?: "Unknown",
                                            district = prediction.address?.state ?: "Unknown",
                                            talukaOrTown = city,
                                            cityName = city,
                                            latitude = lat,
                                            longitude = lng,
                                            timezoneId = "Asia/Kolkata", // Ideally determine timezone from lat/lng
                                            officialLocationCode = prediction.place_id?.toString() ?: "",
                                            alternateNames = listOf(city, mainText)
                                        )
                                        onLocationSelected(locData)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = city,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = mainText,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (!isLoading && hasSearched && searchQuery.trim().length >= 3 && suggestions.isEmpty() && errorMessage == null) {
                Text(
                    text = "No locations found matching '$searchQuery'",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        
        if (selectedLocation != null && errorMessage == null && !isManualMode) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Selected: ${selectedLocation.talukaOrTown}, ${selectedLocation.state} (Lat: ${String.format("%.4f", selectedLocation.latitude)}, Lng: ${String.format("%.4f", selectedLocation.longitude)})",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            )
        }
    }
}
"""

with open(path, "w") as f:
    f.write(replacement)
