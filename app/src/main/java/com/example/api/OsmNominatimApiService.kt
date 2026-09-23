package com.example.api

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OsmPlace(
    val place_id: Long? = null,
    val lat: String,
    val lon: String,
    val display_name: String,
    val address: OsmAddress? = null
)

@JsonClass(generateAdapter = true)
data class OsmAddress(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postcode: String? = null
)

interface OsmNominatimApiService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressdetails: Int = 1,
        @Query("limit") limit: Int = 5
    ): List<OsmPlace>

    companion object {
        private const val BASE_URL = "https://nominatim.openstreetmap.org/"

        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "VedicAstrologyApp/1.0")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val service: OsmNominatimApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OsmNominatimApiService::class.java)
        }
    }
}
