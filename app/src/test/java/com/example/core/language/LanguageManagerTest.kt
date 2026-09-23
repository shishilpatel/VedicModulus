package com.example.core.language

import android.os.Build
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.MainActivity
import com.example.core.model.LanguageDisplay
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33]) // using 33 for general stability
class LanguageManagerTest {

    @Test
    fun `getLanguageCode returns correct string code for LanguageDisplay enums`() {
        assertEquals("en", LanguageManager.getLanguageCode(LanguageDisplay.ENGLISH))
        assertEquals("hi", LanguageManager.getLanguageCode(LanguageDisplay.HINDI))
        assertEquals("gu", LanguageManager.getLanguageCode(LanguageDisplay.GUJARATI))
        assertEquals("ta", LanguageManager.getLanguageCode(LanguageDisplay.TAMIL))
        assertEquals("te", LanguageManager.getLanguageCode(LanguageDisplay.TELUGU))
        assertEquals("bn", LanguageManager.getLanguageCode(LanguageDisplay.BENGALI))
        assertEquals("sa", LanguageManager.getLanguageCode(LanguageDisplay.SANSKRIT))
    }

    @Test
    fun `getLanguageFromCode returns correct LanguageDisplay enum for string codes`() {
        assertEquals(LanguageDisplay.ENGLISH, LanguageManager.getLanguageFromCode("en"))
        assertEquals(LanguageDisplay.HINDI, LanguageManager.getLanguageFromCode("hi"))
        assertEquals(LanguageDisplay.GUJARATI, LanguageManager.getLanguageFromCode("gu"))
        assertEquals(LanguageDisplay.TAMIL, LanguageManager.getLanguageFromCode("ta"))
        assertEquals(LanguageDisplay.TELUGU, LanguageManager.getLanguageFromCode("te"))
        assertEquals(LanguageDisplay.BENGALI, LanguageManager.getLanguageFromCode("bn"))
        assertEquals(LanguageDisplay.SANSKRIT, LanguageManager.getLanguageFromCode("sa"))
    }

    @Test
    fun `getLanguageFromCode defaults to ENGLISH for unknown codes`() {
        assertEquals(LanguageDisplay.ENGLISH, LanguageManager.getLanguageFromCode("unknown"))
        assertEquals(LanguageDisplay.ENGLISH, LanguageManager.getLanguageFromCode(""))
    }

    @Test
    fun `getLanguageOptions returns all 13 supported language options`() {
        val options = LanguageManager.getLanguageOptions()
        assertEquals(13, options.size)
        // Verify a couple of known mappings in the options list
        val englishOption = options.find { it.first == "en" }
        assertEquals("English", englishOption?.second)
        
        val hindiOption = options.find { it.first == "hi" }
        assertEquals("हिन्दी (Hindi)", hindiOption?.second)
    }

    @Test
    fun `createLocalizedContext returns context with correct Locale`() {
        org.robolectric.Robolectric.buildActivity(androidx.activity.ComponentActivity::class.java).use { controller ->
            val activity = controller.setup().get()
            
            // Apply Hindi locale
            val localizedContext = LanguageManager.createLocalizedContext(activity, "hi")
            
            // Verify context configuration has the correct locale
            val locale = localizedContext.resources.configuration.locales.get(0)
            assertEquals("hi", locale.language)
        }
    }

}
