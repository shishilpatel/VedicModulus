package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

suspend fun generateHoroscope(celestialData: String, languageCode: String = "en"): String = withContext(Dispatchers.IO) {
    val openAiKey = try { BuildConfig.OPENAI_API_KEY } catch(e: Exception) { "" }
    val geminiKey = BuildConfig.GEMINI_API_KEY
    
    if ((openAiKey.isBlank() || openAiKey == "MY_OPENAI_API_KEY") && (geminiKey.isBlank() || geminiKey == "MY_GEMINI_API_KEY")) {
        return@withContext "Error: OpenAI/Gemini API Key not configured. Please add it via the AI Studio Secrets panel."
    }
    
    val targetLanguageName = com.example.core.language.LanguageManager.getLanguagePromptName(languageCode)
    val variableSelectedLanguage = com.example.core.language.LanguageManager.getVariableSelectedLanguage(languageCode)
    val responseDirective = "RESPONSE SHOULD BE IN $variableSelectedLanguage"
    
    val systemPrompt = "You are an expert Vedic astrologer and Sanskrit scholar. You provide authentic Vedic astrology, horoscope, and spiritual insights. STRICT LANGUAGE REQUIREMENT: $responseDirective. You MUST generate the ENTIRE response exclusively in $targetLanguageName using its correct native script and natural, respectful phrasing."
    val prompt = "Based on the following celestial data, write a short, poetic, and insightful daily horoscope (2-3 sentences max) tailored for a smartwatch display. Data: $celestialData. Important: This content is for spiritual guidance and personal wellness only. Do not make deterministic or medical claims. MANDATORY: $responseDirective. Respond completely in $targetLanguageName."
    
    var responseText: String? = null
    
    // Try OpenAI / ChatGPT First
    try {
        if (openAiKey.isNotBlank() && openAiKey != "MY_OPENAI_API_KEY") {
            responseText = generateWithOpenAI(prompt = prompt, apiKey = openAiKey, systemPrompt = systemPrompt)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    // Fallback to Gemini
    if (responseText == null && geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY") {
        try {
            val request = GenerateContentRequest(
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                )
            )
            val response = GeminiClient.service.generateContent(geminiKey, request)
            responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    responseText ?: "Auspicious alignment: Today's celestial transits favor inner reflection, balanced decision-making, and spiritual devotion."
}

suspend fun generateVedicAiChatResponse(
    userPrompt: String,
    languageCode: String = "en",
    contextData: String = ""
): String = withContext(Dispatchers.IO) {
    val openAiKey = try { BuildConfig.OPENAI_API_KEY } catch(e: Exception) { "" }
    val geminiKey = BuildConfig.GEMINI_API_KEY
    
    if ((openAiKey.isBlank() || openAiKey == "MY_OPENAI_API_KEY") && (geminiKey.isBlank() || geminiKey == "MY_GEMINI_API_KEY")) {
        return@withContext "Error: OpenAI/Gemini API Key not configured. Please add it via the AI Studio Secrets panel."
    }
    
    val targetLanguageName = com.example.core.language.LanguageManager.getLanguagePromptName(languageCode)
    val variableSelectedLanguage = com.example.core.language.LanguageManager.getVariableSelectedLanguage(languageCode)
    val responseDirective = "RESPONSE SHOULD BE IN $variableSelectedLanguage"
    
    val systemPrompt = "You are a wise and compassionate Vedic Rishi and Astrologer Assistant. You answer questions on Jyotisha (Vedic Astrology), Panchanga, Kundali, Dincharya daily routines, Mantras, Chakras, Vastu Shastra, and Numerology based on classical Vedic traditions. Maintain a respectful, uplifting, and sattvic tone. Important disclaimer: All astrological interpretations are traditional guidance for spiritual wellness, not medical, financial, or deterministic fatalistic predictions. CRITICAL LANGUAGE MANDATE: $responseDirective. You MUST write your entire response exclusively in $targetLanguageName ($languageCode) using its authentic script and vocabulary. Do not mix English words unless technical astrological terms require transliteration."
    
    val fullPrompt = if (contextData.isNotBlank()) {
        "User Vedic Profile & Astronomical Context: $contextData\n\nUser Question: $userPrompt\n\n$responseDirective\nRemember: Answer fully in $targetLanguageName."
    } else {
        "$userPrompt\n\n$responseDirective\nRemember: Answer fully in $targetLanguageName."
    }
    
    var responseText: String? = null
    
    // Try OpenAI / ChatGPT first
    try {
        if (openAiKey.isNotBlank() && openAiKey != "MY_OPENAI_API_KEY") {
            responseText = generateWithOpenAI(prompt = fullPrompt, apiKey = openAiKey, systemPrompt = systemPrompt)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    // Fallback to Gemini
    if (responseText == null && geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY") {
        try {
            val request = GenerateContentRequest(
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                contents = listOf(
                    Content(parts = listOf(Part(text = fullPrompt)))
                )
            )
            val response = GeminiClient.service.generateContent(geminiKey, request)
            responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    responseText ?: "Hari Om. Unable to receive cosmic response at this moment. Please check your internet connection or API keys."
}

suspend fun generateAstrologicalCalculationAiInterpretation(
    calculationDetails: String,
    astrologicalDomain: String,
    languageCode: String = "en",
    userContext: String = ""
): String = withContext(Dispatchers.IO) {
    val openAiKey = try { BuildConfig.OPENAI_API_KEY } catch(e: Exception) { "" }
    val geminiKey = BuildConfig.GEMINI_API_KEY
    
    if ((openAiKey.isBlank() || openAiKey == "MY_OPENAI_API_KEY") && (geminiKey.isBlank() || geminiKey == "MY_GEMINI_API_KEY")) {
        return@withContext "Error: OpenAI/Gemini API Key not configured. Please add it via the AI Studio Secrets panel."
    }
    
    val targetLanguageName = com.example.core.language.LanguageManager.getLanguagePromptName(languageCode)
    val variableSelectedLanguage = com.example.core.language.LanguageManager.getVariableSelectedLanguage(languageCode)
    val responseDirective = "RESPONSE SHOULD BE IN $variableSelectedLanguage"
    
    val systemPrompt = """
        You are a Master Vedic Astrologer, Sanskrit Scholar, and Shastra Acharya. 
        You specialize in classical Jyotisha (Vedic Astrology), Ashtakoota Kundli Milan, Dainik Panchanga, Sankhya Shastra (Numerology), and Vastu Shastra.
        You take the precise mathematical and astronomical calculation details generated by the computational ephemeris engine and synthesize them into an authentic, deeply insightful, uplifting, and practical reading.
        STRICT LANGUAGE REQUIREMENT: $responseDirective.
        You MUST generate the ENTIRE response exclusively in $targetLanguageName ($languageCode) using its authentic script, correct regional terminology, and respectful tone. Do NOT answer in English unless the requested language is English.
        Important: Vedic astrological guidance is for spiritual contemplation and personal insight. Never make deterministic, fear-inducing, or fatalistic claims.
    """.trimIndent()
    
    val fullPrompt = """
        $responseDirective
        Astrological Domain: $astrologicalDomain
        
        Calculated Technical Details from Engine:
        $calculationDetails
        
        ${if (userContext.isNotBlank()) "User Profile / Seeker Context:\n$userContext\n" else ""}
        Task:
        1. Review the calculated technical data provided above.
        2. Synthesize these exact calculations into an authentic traditional interpretation.
        3. Explain the classical significance, strengths, areas of balance, and practical spiritual remedies (mantras, mindfulness, charities).
        4. Maintain a supportive, serene, and empowering tone.
        
        CRITICAL REQUIREMENT:
        $responseDirective
        The entire interpretation MUST be written 100% in $targetLanguageName ($languageCode).
    """.trimIndent()
    
    var responseText: String? = null
    
    // Try OpenAI / ChatGPT first
    try {
        if (openAiKey.isNotBlank() && openAiKey != "MY_OPENAI_API_KEY") {
            responseText = generateWithOpenAI(prompt = fullPrompt, apiKey = openAiKey, systemPrompt = systemPrompt)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    // Fallback to Gemini
    if (responseText == null && geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY") {
        try {
            val request = GenerateContentRequest(
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                contents = listOf(
                    Content(parts = listOf(Part(text = fullPrompt)))
                )
            )
            val response = GeminiClient.service.generateContent(geminiKey, request)
            responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    responseText ?: "Hari Om. Unable to generate astrological interpretation at this time. Please check your network connection or API key."
}