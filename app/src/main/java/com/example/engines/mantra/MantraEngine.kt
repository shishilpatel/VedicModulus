package com.example.engines.mantra

import com.example.core.model.Mantra
import com.example.core.model.MuhurtaDeity

class MantraEngine {
    
    private val mantras = listOf(
        Mantra(
            id = "agni_01",
            sanskrit = "ॐ अग्नये नमः",
            transliteration = "Om Agnaye Namah",
            language = "Sanskrit",
            meaning = "Salutations to Agni, the fire god.",
            associatedDeity = "Agni",
            associatedContext = "Morning Puja",
            sourceTradition = "Rig Veda"
        ),
        Mantra(
            id = "shiva_01",
            sanskrit = "ॐ नमः शिवाय",
            transliteration = "Om Namah Shivaya",
            language = "Sanskrit",
            meaning = "I bow to Shiva.",
            associatedDeity = "Shiva",
            associatedContext = "Pradosha",
            sourceTradition = "Yajur Veda"
        )
    )
    
    private val muhurtaDeities = listOf(
        MuhurtaDeity(
            deityName = "Agni",
            sanskritName = "अग्नि",
            transliteration = "Agni",
            description = "God of Fire, representing transformation and purity.",
            associatedMuhurta = "Brahma Muhurta",
            associatedMantra = getMantraById("agni_01")
        ),
        MuhurtaDeity(
            deityName = "Shiva",
            sanskritName = "शिव",
            transliteration = "Shiva",
            description = "The Auspicious One, representing consciousness.",
            associatedMuhurta = "Pradosha",
            associatedMantra = getMantraById("shiva_01")
        )
    )

    fun getMantraById(id: String): Mantra? {
        return mantras.find { it.id == id }
    }
    
    fun getDeityForMuhurta(muhurtaName: String): MuhurtaDeity? {
        return muhurtaDeities.find { it.associatedMuhurta.contains(muhurtaName, ignoreCase = true) }
    }
}
