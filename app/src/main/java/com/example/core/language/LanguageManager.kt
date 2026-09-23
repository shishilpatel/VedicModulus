package com.example.core.language

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import com.example.core.model.LanguageDisplay
import java.util.Locale

object LanguageManager {
    fun createLocalizedContext(originalContext: Context, languageCode: String): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(originalContext.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        val configContext = originalContext.createConfigurationContext(config)

        return object : ContextWrapper(configContext),
            ActivityResultRegistryOwner,
            OnBackPressedDispatcherOwner,
            LifecycleOwner,
            SavedStateRegistryOwner {
            
            override val activityResultRegistry: ActivityResultRegistry
                get() = (originalContext as? ActivityResultRegistryOwner)?.activityResultRegistry
                    ?: (configContext as? ActivityResultRegistryOwner)?.activityResultRegistry
                    ?: throw IllegalStateException("Context $originalContext does not implement ActivityResultRegistryOwner")
                
            override val onBackPressedDispatcher: OnBackPressedDispatcher
                get() = (originalContext as? OnBackPressedDispatcherOwner)?.onBackPressedDispatcher
                    ?: (configContext as? OnBackPressedDispatcherOwner)?.onBackPressedDispatcher
                    ?: throw IllegalStateException("Context $originalContext does not implement OnBackPressedDispatcherOwner")
                
            override val lifecycle: Lifecycle
                get() = (originalContext as? LifecycleOwner)?.lifecycle
                    ?: (configContext as? LifecycleOwner)?.lifecycle
                    ?: throw IllegalStateException("Context $originalContext does not implement LifecycleOwner")
                
            override val savedStateRegistry: SavedStateRegistry
                get() = (originalContext as? SavedStateRegistryOwner)?.savedStateRegistry
                    ?: (configContext as? SavedStateRegistryOwner)?.savedStateRegistry
                    ?: throw IllegalStateException("Context $originalContext does not implement SavedStateRegistryOwner")
        }
    }
    
    fun findActivity(context: Context): android.app.Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is android.app.Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
    
    fun getLanguageCode(language: LanguageDisplay): String {
        return when (language) {
            LanguageDisplay.ENGLISH -> "en"
            LanguageDisplay.HINDI -> "hi"
            LanguageDisplay.GUJARATI -> "gu"
            LanguageDisplay.SANSKRIT -> "sa"
            LanguageDisplay.MARATHI -> "mr"
            LanguageDisplay.TAMIL -> "ta"
            LanguageDisplay.TELUGU -> "te"
            LanguageDisplay.KANNADA -> "kn"
            LanguageDisplay.MALAYALAM -> "ml"
            LanguageDisplay.BENGALI -> "bn"
            LanguageDisplay.PUNJABI -> "pa"
            LanguageDisplay.ODIA -> "or"
            LanguageDisplay.ASSAMESE -> "as"
        }
    }

    fun getLanguageFromCode(code: String): LanguageDisplay {
        return when (code) {
            "hi" -> LanguageDisplay.HINDI
            "gu" -> LanguageDisplay.GUJARATI
            "sa" -> LanguageDisplay.SANSKRIT
            "mr" -> LanguageDisplay.MARATHI
            "ta" -> LanguageDisplay.TAMIL
            "te" -> LanguageDisplay.TELUGU
            "kn" -> LanguageDisplay.KANNADA
            "ml" -> LanguageDisplay.MALAYALAM
            "bn" -> LanguageDisplay.BENGALI
            "pa" -> LanguageDisplay.PUNJABI
            "or" -> LanguageDisplay.ODIA
            "as" -> LanguageDisplay.ASSAMESE
            "en" -> LanguageDisplay.ENGLISH
            else -> LanguageDisplay.ENGLISH
        }
    }
    
    fun getLanguageOptions(): List<Triple<String, String, String>> {
        return listOf(
            Triple("en", "English", "A"),
            Triple("hi", "हिन्दी (Hindi)", "अ"),
            Triple("gu", "ગુજરાતી (Gujarati)", "અ"),
            Triple("sa", "संस्कृतम् (Sanskrit)", "ॐ"),
            Triple("mr", "मराठी (Marathi)", "म"),
            Triple("ta", "தமிழ் (Tamil)", "அ"),
            Triple("te", "తెలుగు (Telugu)", "అ"),
            Triple("kn", "ಕನ್ನಡ (Kannada)", "ಅ"),
            Triple("ml", "മലയാളം (Malayalam)", "അ"),
            Triple("bn", "বাংলা (Bengali)", "অ"),
            Triple("pa", "ਪੰਜਾਬੀ (Punjabi)", "ੳ"),
            Triple("or", "ଓଡ଼ିଆ (Odia)", "ଅ"),
            Triple("as", "অসমীয়া (Assamese)", "অ")
        )
    }

    fun getLanguagePromptName(code: String): String {
        return when (code) {
            "hi" -> "Hindi (हिंदी)"
            "gu" -> "Gujarati (ગુજરાતી)"
            "sa" -> "Sanskrit (संस्कृतम्)"
            "mr" -> "Marathi (मराठी)"
            "ta" -> "Tamil (தமிழ்)"
            "te" -> "Telugu (తెలుగు)"
            "kn" -> "Kannada (ಕನ್ನಡ)"
            "ml" -> "Malayalam (മലയാളം)"
            "bn" -> "Bengali (বাংলা)"
            "pa" -> "Punjabi (ਪੰਜਾਬੀ)"
            "or" -> "Odia (ଓଡ଼ିଆ)"
            "as" -> "Assamese (অসমীয়া)"
            else -> "English"
        }
    }

    /**
     * Resolves the selected language to its clean standard name for AI prompts
     * (e.g., "Gujarati", "Hindi", "English", "Sanskrit", etc.).
     */
    fun getVariableSelectedLanguage(codeOrName: String?): String {
        if (codeOrName.isNullOrBlank()) return "English"
        return when (codeOrName.trim().lowercase()) {
            "hi", "hindi" -> "Hindi"
            "gu", "gujarati" -> "Gujarati"
            "sa", "sanskrit" -> "Sanskrit"
            "mr", "marathi" -> "Marathi"
            "ta", "tamil" -> "Tamil"
            "te", "telugu" -> "Telugu"
            "kn", "kannada" -> "Kannada"
            "ml", "malayalam" -> "Malayalam"
            "bn", "bengali" -> "Bengali"
            "pa", "punjabi" -> "Punjabi"
            "or", "odia" -> "Odia"
            "as", "assamese" -> "Assamese"
            "en", "english" -> "English"
            else -> {
                try {
                    val enumVal = LanguageDisplay.valueOf(codeOrName.trim().uppercase())
                    getVariableSelectedLanguage(enumVal)
                } catch (e: Exception) {
                    codeOrName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                }
            }
        }
    }

    fun getVariableSelectedLanguage(language: LanguageDisplay?): String {
        return when (language) {
            LanguageDisplay.HINDI -> "Hindi"
            LanguageDisplay.GUJARATI -> "Gujarati"
            LanguageDisplay.SANSKRIT -> "Sanskrit"
            LanguageDisplay.MARATHI -> "Marathi"
            LanguageDisplay.TAMIL -> "Tamil"
            LanguageDisplay.TELUGU -> "Telugu"
            LanguageDisplay.KANNADA -> "Kannada"
            LanguageDisplay.MALAYALAM -> "Malayalam"
            LanguageDisplay.BENGALI -> "Bengali"
            LanguageDisplay.PUNJABI -> "Punjabi"
            LanguageDisplay.ODIA -> "Odia"
            LanguageDisplay.ASSAMESE -> "Assamese"
            LanguageDisplay.ENGLISH -> "English"
            null -> "English"
        }
    }

    /**
     * Generates the standard prompt directive: "RESPONSE SHOULD BE IN %VARIABLE_SELECTED_LANGUAGE%"
     */
    fun getResponseLanguageDirective(codeOrName: String?): String {
        val selectedLang = getVariableSelectedLanguage(codeOrName)
        return "RESPONSE SHOULD BE IN $selectedLang"
    }

    fun getResponseLanguageDirective(language: LanguageDisplay?): String {
        val selectedLang = getVariableSelectedLanguage(language)
        return "RESPONSE SHOULD BE IN $selectedLang"
    }
}
