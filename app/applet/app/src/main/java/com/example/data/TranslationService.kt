package com.example.data

data class TranslationResult(
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val isCached: Boolean
)

interface TranslationService {
    suspend fun translate(text: String, sourceLanguage: String?, targetLanguage: String): TranslationResult
}

class BundledAndCloudTranslationService(
    private val localizationManager: LocalizationManager
) : TranslationService {
    
    private val translationCache = mutableMapOf<String, String>()

    override suspend fun translate(text: String, sourceLanguage: String?, targetLanguage: String): TranslationResult {
        val cacheKey = "${sourceLanguage ?: "auto"}_${targetLanguage}_$text"
        if (translationCache.containsKey(cacheKey)) {
            return TranslationResult(
                translatedText = translationCache[cacheKey]!!,
                sourceLanguage = sourceLanguage ?: "en",
                targetLanguage = targetLanguage,
                isCached = true
            )
        }

        // Check Vedic Terminology repository first for controlled terms
        val term = VedicTerminologyRepository.getTerm(text, targetLanguage)
        if (term != text) {
            translationCache[cacheKey] = term
            return TranslationResult(term, sourceLanguage ?: "en", targetLanguage, true)
        }

        // Fallback / Bundled translation simulation or passthrough
        val translated = text
        translationCache[cacheKey] = translated
        return TranslationResult(
            translatedText = translated,
            sourceLanguage = sourceLanguage ?: "en",
            targetLanguage = targetLanguage,
            isCached = false
        )
    }
}
