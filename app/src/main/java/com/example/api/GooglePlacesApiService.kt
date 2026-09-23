package com.example.api

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class AutocompleteRequest(
    val input: String,
    val includedRegionCodes: List<String> = listOf("in")
)

@JsonClass(generateAdapter = true)
data class AutocompleteResponse(
    val suggestions: List<SuggestionItem>? = null
)

@JsonClass(generateAdapter = true)
data class SuggestionItem(
    val placePrediction: PlacePrediction? = null
)

@JsonClass(generateAdapter = true)
data class PlacePrediction(
    val placeId: String,
    val text: LocalizedText? = null,
    val structuredFormat: StructuredFormat? = null
)

@JsonClass(generateAdapter = true)
data class LocalizedText(
    val text: String
)

@JsonClass(generateAdapter = true)
data class StructuredFormat(
    val mainText: LocalizedText? = null,
    val secondaryText: LocalizedText? = null
)

@JsonClass(generateAdapter = true)
data class PlaceDetailsResponse(
    val id: String? = null,
    val displayName: LocalizedText? = null,
    val formattedAddress: String? = null,
    val location: LatLngLocation? = null
)

@JsonClass(generateAdapter = true)
data class LatLngLocation(
    val latitude: Double,
    val longitude: Double
)

interface GooglePlacesApiService {
    @POST("v1/places:autocomplete")
    suspend fun autocomplete(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String = "suggestions.placePrediction.placeId,suggestions.placePrediction.text.text,suggestions.placePrediction.structuredFormat",
        @Body request: AutocompleteRequest
    ): AutocompleteResponse

    @GET("v1/places/{placeId}")
    suspend fun getPlaceDetails(
        @Path("placeId") placeId: String,
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String = "id,displayName,formattedAddress,location"
    ): PlaceDetailsResponse

    companion object {
        private const val BASE_URL = "https://places.googleapis.com/"
        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val service: GooglePlacesApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(GooglePlacesApiService::class.java)
        }
    }
}
