package com.example

import com.example.data.VedicTerminologyRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LocalizationTest {

    @Test
    fun testTerminologyRepositoryLanguages() {
        val englishTerm = VedicTerminologyRepository.getTerm("tithi_ekadashi", "en")
        val hindiTerm = VedicTerminologyRepository.getTerm("tithi_ekadashi", "hi")
        val gujaratiTerm = VedicTerminologyRepository.getTerm("tithi_ekadashi", "gu")
        val sanskritTerm = VedicTerminologyRepository.getTerm("tithi_ekadashi", "sa")

        assertEquals("Ekadashi", englishTerm)
        assertEquals("एकादशी", hindiTerm)
        assertEquals("એકાદશી", gujaratiTerm)
        assertEquals("एकादशी", sanskritTerm)
    }

    @Test
    fun testShortLabels() {
        val label = VedicTerminologyRepository.getShortLabel("tithi_purnima", "en")
        assertNotNull(label)
    }
}
