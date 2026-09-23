package com.example.engines.vastu

import com.example.core.model.Rashi
import com.example.core.model.VastuDirectionInfo

class VastuEngine {

    companion object {
        val ASHTA_DIK = listOf(
            VastuDirectionInfo("North", "Uttara (उत्तर)", "Kubera (Lord of Wealth)", "Jala / Mercury", "#00B4D8", 0.0),
            VastuDirectionInfo("North-East", "Ishanya (ईशान्य)", "Ishana (Pure Divinity)", "Jala + Akasha", "#90E0EF", 45.0),
            VastuDirectionInfo("East", "Purva (पूर्व)", "Indra & Surya", "Agni / Vitality", "#FFB703", 90.0),
            VastuDirectionInfo("South-East", "Agneya (आग्नेय)", "Agni (Sacred Fire)", "Agni / Energy", "#E63946", 135.0),
            VastuDirectionInfo("South", "Dakshina (दक्षिण)", "Yama (Dharma & Justice)", "Prithvi / Stability", "#D90429", 180.0),
            VastuDirectionInfo("South-West", "Nairritya (नैऋत्य)", "Nairriti (Earth Anchor)", "Prithvi / Strength", "#B5838D", 225.0),
            VastuDirectionInfo("West", "Pashchima (पश्चिम)", "Varuna (Cosmic Order)", "Vayu / Intellect", "#4361EE", 270.0),
            VastuDirectionInfo("North-West", "Vayavya (वायव्य)", "Vayu (Wind / Movement)", "Vayu / Flow", "#A8DADC", 315.0)
        )
    }

    /**
     * Derives the presiding Mahabhuta (Element) and sacred directional resonance
     * based on user's Janma Rashi.
     */
    fun deriveElementalAffinity(rashi: Rashi): Pair<String, String> {
        return when (rashi) {
            Rashi.MESHA, Rashi.SIMHA, Rashi.DHANUS -> {
                "Agni (Fire Element)" to "Radiance, vitality, clarity of vision and transformative inner warmth."
            }
            Rashi.VRISHABHA, Rashi.KANYA, Rashi.MAKARA -> {
                "Prithvi (Earth Element)" to "Grounding, stability, patient endurance and material harmony."
            }
            Rashi.MITHUNA, Rashi.TULA, Rashi.KUMBHA -> {
                "Vayu (Air Element)" to "Wisdom, creative intellect, open expansiveness and communication."
            }
            Rashi.KARKATA, Rashi.VRISHCHIKA, Rashi.MEENA -> {
                "Jala (Water Element)" to "Intuitive depth, emotional purity, adaptability and healing peace."
            }
        }
    }

    fun getDirectionByAngle(degrees: Double): VastuDirectionInfo {
        val norm = ((degrees % 360.0) + 360.0) % 360.0
        val index = (((norm + 22.5) / 45.0).toInt()) % 8
        return ASHTA_DIK[index]
    }
}
