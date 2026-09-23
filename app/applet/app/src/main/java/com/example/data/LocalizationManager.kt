package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी"),
    GUJARATI("gu", "ગુજરાતી"),
    SANSKRIT("sa", "संस्कृतम्")
}

enum class NumeralPreference(val code: String, val displayName: String) {
    AUTOMATIC("auto", "Automatic"),
    WESTERN("western", "Western (1, 2, 3)"),
    DEVANAGARI("devanagari", "Devanagari (१, २, ३)"),
    GUJARATI("gujarati", "Gujarati (૧, ૨, ૩)")
}

enum class TerminologyMode(val code: String, val displayName: String) {
    TRADITIONAL("traditional", "Traditional Sanskrit/Devanagari"),
    ENGLISH("english", "English Translated"),
    MIXED("mixed", "Mixed / Transliterated")
}

class LocalizationManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vedic_localization_prefs", Context.MODE_PRIVATE)

    private val _currentLanguage = MutableStateFlow(
        prefs.getString("selected_language", "en")?.let { code ->
            AppLanguage.values().find { it.code == code }
        } ?: AppLanguage.ENGLISH
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _numeralPreference = MutableStateFlow(
        prefs.getString("numeral_pref", "auto")?.let { code ->
            NumeralPreference.values().find { it.code == code }
        } ?: NumeralPreference.AUTOMATIC
    )
    val numeralPreference: StateFlow<NumeralPreference> = _numeralPreference.asStateFlow()

    private val _terminologyMode = MutableStateFlow(
        prefs.getString("term_mode", "traditional")?.let { code ->
            TerminologyMode.values().find { it.code == code }
        } ?: TerminologyMode.TRADITIONAL
    )
    val terminologyMode: StateFlow<TerminologyMode> = _terminologyMode.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("selected_language", language.code).apply()
    }

    fun setNumeralPreference(pref: NumeralPreference) {
        _numeralPreference.value = pref
        prefs.edit().putString("numeral_pref", pref.code).apply()
    }

    fun setTerminologyMode(mode: TerminologyMode) {
        _terminologyMode.value = mode
        prefs.edit().putString("term_mode", mode.code).apply()
    }

    fun formatNumerals(input: String): String {
        val pref = _numeralPreference.value
        val lang = _currentLanguage.value
        val useDevanagari = pref == NumeralPreference.DEVANAGARI || (pref == NumeralPreference.AUTOMATIC && (lang == AppLanguage.HINDI || lang == AppLanguage.SANSKRIT))
        val useGujarati = pref == NumeralPreference.GUJARATI || (pref == NumeralPreference.AUTOMATIC && lang == AppLanguage.GUJARATI)

        return when {
            useDevanagari -> {
                input.map { char ->
                    when (char) {
                        '0' -> '०'; '1' -> '१'; '2' -> '२'; '3' -> '३'; '4' -> '४'
                        '5' -> '५'; '6' -> '६'; '7' -> '७'; '8' -> '८'; '9' -> '९'
                        else -> char
                    }
                }.joinToString("")
            }
            useGujarati -> {
                input.map { char ->
                    when (char) {
                        '0' -> '૦'; '1' -> '૧'; '2' -> '૨'; '3' -> '૩'; '4' -> '૪'
                        '5' -> '૫'; '6' -> '૬'; '7' -> '૭'; '8' -> '૮'; '9' -> '૯'
                        else -> char
                    }
                }.joinToString("")
            }
            else -> input
        }
    }
}
