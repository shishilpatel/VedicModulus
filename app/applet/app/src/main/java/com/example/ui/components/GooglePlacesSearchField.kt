package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.api.GooglePlacesApiService
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
    var suggestions by remember { mutableStateOf<List<com.example.api.PlacePrediction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasSearched by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val apiKey = BuildConfig.MAPS_API_KEY

    Column(modifier = modifier.fillMaxWidth()) {
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

                if (apiKey.isBlank() || apiKey == "YOUR_MAPS_API_KEY") {
                    errorMessage = "Google Maps API Key not configured. Please add MAPS_API_KEY in AI Studio Secrets."
                    return@OutlinedTextField
                }

                searchJob = coroutineScope.launch {
                    delay(400) // Debounce 400ms
                    isLoading = true
                    try {
                        val response = GooglePlacesApiService.service.autocomplete(
                            apiKey = apiKey,
                            request = com.example.api.AutocompleteRequest(input = query)
                        )
                        suggestions = response.suggestions?.mapNotNull { it.placePrediction } ?: emptyList()
                    } catch (e: Exception) {
                        errorMessage = "Network or API error: ${e.localizedMessage ?: "Unknown error"}"
                        suggestions = emptyList()
                    } finally {
                        isLoading = false
                    }
                }
            },
            placeholder = { Text("Search city, town, village...") },
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
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onErrorContainer),
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
                        val mainText = prediction.structuredFormat?.mainText?.text ?: prediction.text?.text ?: ""
                        val secondaryText = prediction.structuredFormat?.secondaryText?.text ?: ""

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchQuery = mainText
                                    suggestions = emptyList()
                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            val details = GooglePlacesApiService.service.getPlaceDetails(
                                                placeId = prediction.placeId,
                                                apiKey = apiKey
                                            )
                                            val lat = details.location?.latitude ?: 28.6139
                                            val lng = details.location?.longitude ?: 77.2090
                                            val address = details.formattedAddress ?: secondaryText
                                            val placeName = details.displayName?.text ?: mainText

                                            val locData = LocationData(
                                                country = "India",
                                                state = secondaryText.substringAfterLast(",").trim().ifBlank { "India" },
                                                district = secondaryText.substringBefore(",").trim().ifBlank { placeName },
                                                talukaOrTown = placeName,
                                                cityName = placeName,
                                                latitude = lat,
                                                longitude = lng,
                                                timezoneId = "Asia/Kolkata",
                                                officialLocationCode = prediction.placeId,
                                                alternateNames = listOf(placeName, address)
                                            )
                                            onLocationSelected(locData)
                                        } catch (e: Exception) {
                                            errorMessage = "Failed to fetch place details: ${e.localizedMessage}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
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
                                    text = mainText,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                if (secondaryText.isNotBlank()) {
                                    Text(
                                        text = secondaryText,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
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

        if (selectedLocation != null && errorMessage == null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Selected: ${selectedLocation.talukaOrTown}, ${selectedLocation.state} (Lat: ${String.format("%.4f", selectedLocation.latitude)}, Lng: ${String.format("%.4f", selectedLocation.longitude)})",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            )
        }
    }
}
