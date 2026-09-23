package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class OpenAIApiRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<OpenAIMessage>
)

@JsonClass(generateAdapter = true)
data class OpenAIMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAIApiResponse(
    val choices: List<OpenAIChoice>? = null
)

@JsonClass(generateAdapter = true)
data class OpenAIChoice(
    val message: OpenAIMessage? = null
)

interface OpenAIApiService {
    @POST("v1/chat/completions")
    suspend fun generateContent(
        @Body request: OpenAIApiRequest
    ): OpenAIApiResponse
}

class OpenAIAuthInterceptor(private val apiKeyProvider: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${apiKeyProvider()}")
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}

object OpenAIClient {
    private const val BASE_URL = "https://api.openai.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun getService(apiKey: String): OpenAIApiService {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(OpenAIAuthInterceptor { apiKey })
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenAIApiService::class.java)
    }
}

suspend fun generateWithOpenAI(
    prompt: String,
    apiKey: String,
    systemPrompt: String? = null
): String? = withContext(Dispatchers.IO) {
    if (apiKey.isBlank() || apiKey == "MY_OPENAI_API_KEY") {
        return@withContext null
    }
    
    val messageList = mutableListOf<OpenAIMessage>()
    if (!systemPrompt.isNullOrBlank()) {
        messageList.add(OpenAIMessage(role = "system", content = systemPrompt))
    }
    messageList.add(OpenAIMessage(role = "user", content = prompt))

    val request = OpenAIApiRequest(
        messages = messageList
    )

    try {
        val response = OpenAIClient.getService(apiKey).generateContent(request)
        response.choices?.firstOrNull()?.message?.content
    } catch (e: Exception) {
        throw e
    }
}
