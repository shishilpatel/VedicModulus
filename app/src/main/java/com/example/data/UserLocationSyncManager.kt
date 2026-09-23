package com.example.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.core.model.LocationData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

/**
 * State of user location and timezone synchronization.
 */
sealed class LocationSyncState {
    data object Idle : LocationSyncState()
    data object Locating : LocationSyncState()
    data class Synced(
        val location: LocationData,
        val zoneId: ZoneId,
        val isGpsLive: Boolean,
        val syncTimestamp: Long = System.currentTimeMillis(),
        val syncMessage: String
    ) : LocationSyncState()
    data class Error(val message: String) : LocationSyncState()
}

/**
 * Service to synchronize user's current GPS location and timezone for real-time
 * Vedic calculations (Graha Spashta, Panchanga, Ghadi/Pala, Muhurtas).
 */
class UserLocationSyncManager private constructor() {

    private val _syncState = MutableStateFlow<LocationSyncState>(LocationSyncState.Idle)
    val syncState: StateFlow<LocationSyncState> = _syncState.asStateFlow()

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    /**
     * Synchronizes the user's location and timezone using device GPS / Network location provider.
     * Falls back to fallbackLocation if permission is denied or device GPS is unavailable.
     */
    suspend fun syncUserLocationAndTimezone(
        context: Context,
        fallbackLocation: LocationData? = null
    ): LocationSyncState = withContext(Dispatchers.IO) {
        _syncState.value = LocationSyncState.Locating

        val defaultTz = try {
            ZoneId.systemDefault()
        } catch (e: Exception) {
            ZoneId.of("Asia/Kolkata")
        }

        val defaultFallback = fallbackLocation ?: CityRepository.getDefaultCity()

        if (!hasLocationPermission(context)) {
            val state = LocationSyncState.Synced(
                location = defaultFallback,
                zoneId = try { ZoneId.of(defaultFallback.timezoneId) } catch (e: Exception) { defaultTz },
                isGpsLive = false,
                syncMessage = "Location permission not granted. Using profile location: ${defaultFallback.cityName} (${defaultFallback.timezoneId})"
            )
            _syncState.value = state
            return@withContext state
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            val cancellationTokenSource = CancellationTokenSource()
            // Request high accuracy current location with timeout token
            val location: Location? = try {
                kotlinx.coroutines.withTimeoutOrNull(2000L) {
                    try {
                        fusedClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            cancellationTokenSource.token
                        ).await()
                    } catch (e: Exception) {
                        null
                    }
                } ?: kotlinx.coroutines.withTimeoutOrNull(1000L) {
                    try {
                        fusedClient.lastLocation.await()
                    } catch (ex: Exception) {
                        null
                    }
                }
            } catch (e: SecurityException) {
                null
            } catch (e: Exception) {
                null
            }

            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude

                // Determine TimeZone: device's active local timezone reflects current cell/GPS network
                val activeZoneId = try {
                    ZoneId.systemDefault()
                } catch (e: Exception) {
                    ZoneId.of(TimeZone.getDefault().id)
                }

                // Reverse geocode to find user-friendly city and state with timeout
                val resolvedLocation = reverseGeocode(context, lat, lon, activeZoneId.id)

                val state = LocationSyncState.Synced(
                    location = resolvedLocation,
                    zoneId = activeZoneId,
                    isGpsLive = true,
                    syncMessage = "Synced with GPS: ${resolvedLocation.cityName}, ${resolvedLocation.state} [${activeZoneId.id}]"
                )
                _syncState.value = state
                state
            } else {
                val state = LocationSyncState.Synced(
                    location = defaultFallback,
                    zoneId = try { ZoneId.of(defaultFallback.timezoneId) } catch (e: Exception) { defaultTz },
                    isGpsLive = false,
                    syncMessage = "GPS signal awaiting fix. Using registered location: ${defaultFallback.cityName}"
                )
                _syncState.value = state
                state
            }
        } catch (e: Exception) {
            val state = LocationSyncState.Synced(
                location = defaultFallback,
                zoneId = try { ZoneId.of(defaultFallback.timezoneId) } catch (ex: Exception) { defaultTz },
                isGpsLive = false,
                syncMessage = "Location sync error (${e.localizedMessage}). Defaulting to ${defaultFallback.cityName}"
            )
            _syncState.value = state
            state
        }
    }

    private suspend fun reverseGeocode(
        context: Context,
        latitude: Double,
        longitude: Double,
        timezoneId: String
    ): LocationData = withContext(Dispatchers.IO) {
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = kotlinx.coroutines.withTimeoutOrNull(1500L) {
                    try {
                        geocoder.getFromLocation(latitude, longitude, 1)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Current Location"
                    val state = addr.adminArea ?: ""
                    val country = addr.countryName ?: "India"
                    val subLocality = addr.subLocality ?: city

                    return@withContext LocationData(
                        country = country,
                        state = state,
                        district = addr.subAdminArea ?: state,
                        talukaOrTown = subLocality,
                        cityName = city,
                        latitude = latitude,
                        longitude = longitude,
                        timezoneId = timezoneId
                    )
                }
            }
        } catch (e: Exception) {
            // Fallback to nearest city in repository
        }

        // Find closest city in CityRepository
        val nearest = findNearestCity(latitude, longitude)
        if (nearest != null) {
            nearest.copy(latitude = latitude, longitude = longitude, timezoneId = timezoneId)
        } else {
            LocationData(
                country = "India",
                state = "Synced Location",
                district = "Local",
                talukaOrTown = "Current Place",
                cityName = "Local (${String.format(Locale.US, "%.2f, %.2f", latitude, longitude)})",
                latitude = latitude,
                longitude = longitude,
                timezoneId = timezoneId
            )
        }
    }

    private fun findNearestCity(lat: Double, lon: Double): LocationData? {
        return CityRepository.CITIES.minByOrNull { city ->
            val dLat = Math.toRadians(city.latitude - lat)
            val dLon = Math.toRadians(city.longitude - lon)
            val a = sin(dLat / 2).pow(2.0) +
                    cos(Math.toRadians(lat)) * cos(Math.toRadians(city.latitude)) *
                    sin(dLon / 2).pow(2.0)
            2 * atan2(sqrt(a), sqrt(1 - a))
        }
    }

    companion object {
        @Volatile
        private var instance: UserLocationSyncManager? = null

        fun getInstance(): UserLocationSyncManager {
            return instance ?: synchronized(this) {
                instance ?: UserLocationSyncManager().also { instance = it }
            }
        }
    }
}
