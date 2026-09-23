package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.model.LanguageDisplay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UserPreferencesRepositoryLanguageTest {

    private lateinit var repository: UserPreferencesRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = UserPreferencesRepository(context)
    }

    @Test
    fun `saveLanguage maps code to LanguageDisplay enum string and retrieves code via languageFlow`() = runTest {
        // Default might be en if empty, let's explicitly set to hindi
        repository.saveLanguage("hi")
        
        // Wait and get the flow emission
        val emittedLanguage = repository.languageFlow.first()
        
        // "hi" should map to HINDI internally, and then languageFlow uses getLanguageCode(enum) to return "hi"
        assertEquals("hi", emittedLanguage)
        
        // Try another language
        repository.saveLanguage("gu")
        val emittedLanguage2 = repository.languageFlow.first()
        assertEquals("gu", emittedLanguage2)
    }
}
