package com.example.core.language

import android.util.LruCache
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Service providing offline and on-device translation using Google ML Kit Translate (17.0.3).
 */
object MLKitTranslationService {

    // Cache translators by "source_target" key
    private val translators = ConcurrentHashMap<String, Translator>()
    
    // In-memory LRU translation cache for fast UI updates
    private val translationCache = LruCache<String, String>(500)

    /**
     * Maps our app's language codes to ML Kit TranslateLanguage constants if available.
     */
    fun mapCodeToMLKit(code: String): String? {
        return when (code.lowercase()) {
            "en" -> TranslateLanguage.ENGLISH
            "hi" -> TranslateLanguage.HINDI
            "gu" -> TranslateLanguage.GUJARATI
            "mr" -> TranslateLanguage.MARATHI
            "bn" -> TranslateLanguage.BENGALI
            "ta" -> TranslateLanguage.TAMIL
            "te" -> TranslateLanguage.TELUGU
            "kn" -> TranslateLanguage.KANNADA
            "ur" -> TranslateLanguage.URDU
            else -> {
                // Check if ML Kit recognizes the BCP-47 tag directly
                val fromTag = TranslateLanguage.fromLanguageTag(code)
                fromTag ?: when (code.lowercase()) {
                    "sa" -> TranslateLanguage.HINDI // Sanskrit root fallback for Devanagari ML Kit
                    "pa" -> TranslateLanguage.HINDI // Punjabi phonetic fallback
                    "or" -> TranslateLanguage.BENGALI // Odia script/syntax fallback
                    "as" -> TranslateLanguage.BENGALI // Assamese script/syntax fallback
                    else -> null
                }
            }
        }
    }

    /**
     * Translates a given text asynchronously using ML Kit on-device model.
     *
     * @param text The input text to translate.
     * @param targetLanguageCode Target language code (e.g. "hi", "gu", "ta", etc.).
     * @param sourceLanguageCode Source language code (defaults to "en").
     * @return Translated text, or original text if source equals target or translation is unavailable.
     */
    suspend fun translateText(
        text: String,
        targetLanguageCode: String,
        sourceLanguageCode: String = "en"
    ): String = withContext(Dispatchers.IO) {
        if (text.isBlank() || targetLanguageCode.equals(sourceLanguageCode, ignoreCase = true)) {
            return@withContext text
        }

        val cacheKey = "${sourceLanguageCode}_${targetLanguageCode}_$text"
        translationCache.get(cacheKey)?.let { return@withContext it }

        val sourceLang = mapCodeToMLKit(sourceLanguageCode) ?: TranslateLanguage.ENGLISH
        val targetLang = mapCodeToMLKit(targetLanguageCode) ?: return@withContext text

        val translatorKey = "${sourceLang}_$targetLang"

        val translator = translators.getOrPut(translatorKey) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()
            Translation.getClient(options)
        }

        try {
            // Download model if required (can work on cellular or Wi-Fi)
            val conditions = DownloadConditions.Builder().build()
            Tasks.await(translator.downloadModelIfNeeded(conditions))

            val result = Tasks.await(translator.translate(text))
            if (!result.isNullOrBlank()) {
                translationCache.put(cacheKey, result)
                return@withContext result
            }
            text
        } catch (e: Exception) {
            e.printStackTrace()
            text
        }
    }

    /**
     * Pre-downloads the model for a target language in the background so translations are instant.
     */
    suspend fun prewarmLanguageModel(targetLanguageCode: String, sourceLanguageCode: String = "en") = withContext(Dispatchers.IO) {
        val sourceLang = mapCodeToMLKit(sourceLanguageCode) ?: TranslateLanguage.ENGLISH
        val targetLang = mapCodeToMLKit(targetLanguageCode) ?: return@withContext

        val translatorKey = "${sourceLang}_$targetLang"
        val translator = translators.getOrPut(translatorKey) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()
            Translation.getClient(options)
        }

        try {
            val conditions = DownloadConditions.Builder().build()
            Tasks.await(translator.downloadModelIfNeeded(conditions))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Cleans up all cached translators.
     */
    fun closeAll() {
        translators.values.forEach {
            try {
                it.close()
            } catch (_: Exception) {}
        }
        translators.clear()
        translationCache.evictAll()
    }
}
