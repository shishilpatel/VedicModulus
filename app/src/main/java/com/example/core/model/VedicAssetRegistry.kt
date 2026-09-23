package com.example.core.model

import androidx.annotation.DrawableRes
import com.example.R

/**
 * Central asset registry for application graphical symbols, Yantras, Navagrahas,
 * sacred geometry patterns, and watch face themes.
 */
object VedicAssetRegistry {
    
    enum class AssetCategory {
        YANTRA,
        NAVAGRAHA,
        ZODIAC,
        VASTU,
        CHAKRA,
        BACKGROUND
    }

    data class VedicAsset(
        val id: String,
        val title: String,
        val category: AssetCategory,
        @DrawableRes val drawableResId: Int,
        val description: String
    )

    // Specific static constants requested by the user and additional sacred assets
    const val SHREE_YANTRA_GOLD = "shree_yantra_gold"
    const val NAVAGRAHA_SURYA = "navagraha_surya"
    const val NAVAGRAHA_CHANDRA = "navagraha_chandra"
    const val NAVAGRAHA_MANGALA = "navagraha_mangala"
    const val NAVAGRAHA_BUDHA = "navagraha_budha"
    const val NAVAGRAHA_GURU = "navagraha_guru"
    const val NAVAGRAHA_SHUKRA = "navagraha_shukra"
    const val NAVAGRAHA_SHANI = "navagraha_shani"
    const val NAVAGRAHA_RAHU = "navagraha_rahu"
    const val NAVAGRAHA_KETU = "navagraha_ketu"
    const val KALA_CHAKRA_WHEEL = "kala_chakra_wheel"
    const val VASTU_PURUSHA_MANDALA = "vastu_purusha_mandala"
    const val SRI_MAHA_MRITYUNJAYA_YANTRA = "sri_maha_mrityunjaya_yantra"
    const val KUBERA_YANTRA = "kubera_yantra"
    const val LAKSHMI_YANTRA = "lakshmi_yantra"
    const val DURGA_YANTRA = "durga_yantra"

    // Moon Phases
    const val MOON_PHASE_NEW = "moon_phase_new"
    const val MOON_PHASE_WAXING_CRESCENT = "moon_phase_waxing_crescent"
    const val MOON_PHASE_FIRST_QUARTER = "moon_phase_first_quarter"
    const val MOON_PHASE_WAXING_GIBBOUS = "moon_phase_waxing_gibbous"
    const val MOON_PHASE_FULL = "moon_phase_full"
    const val MOON_PHASE_WANING_GIBBOUS = "moon_phase_waning_gibbous"
    const val MOON_PHASE_LAST_QUARTER = "moon_phase_last_quarter"
    const val MOON_PHASE_WANING_CRESCENT = "moon_phase_waning_crescent"

    val registry: Map<String, VedicAsset> = mapOf(
        SHREE_YANTRA_GOLD to VedicAsset(
            id = SHREE_YANTRA_GOLD,
            title = "Shree Yantra (Gold Edition)",
            category = AssetCategory.YANTRA,
            drawableResId = android.R.drawable.ic_menu_compass,
            description = "The supreme king of yantras, manifesting cosmic wealth, harmony, and spiritual ascent."
        ),
        NAVAGRAHA_SURYA to VedicAsset(
            id = NAVAGRAHA_SURYA,
            title = "Surya (Sun - Ravi)",
            category = AssetCategory.NAVAGRAHA,
            drawableResId = android.R.drawable.ic_menu_day,
            description = "Ruling planet of vitality, soul purpose, leadership, and divine illumination."
        ),
        NAVAGRAHA_CHANDRA to VedicAsset(
            id = NAVAGRAHA_CHANDRA,
            title = "Chandra (Moon - Soma)",
            category = AssetCategory.NAVAGRAHA,
            drawableResId = android.R.drawable.ic_menu_mylocation,
            description = "Ruling planet of mind, emotions, intuition, and psychological rhythms."
        ),
        NAVAGRAHA_MANGALA to VedicAsset(
            id = NAVAGRAHA_MANGALA,
            title = "Mangala (Mars)",
            category = AssetCategory.NAVAGRAHA,
            drawableResId = android.R.drawable.ic_menu_manage,
            description = "Planet of energy, courage, action, and protective vitality."
        ),
        NAVAGRAHA_BUDHA to VedicAsset(
            id = NAVAGRAHA_BUDHA,
            title = "Budha (Mercury)",
            category = AssetCategory.NAVAGRAHA,
            drawableResId = android.R.drawable.ic_menu_edit,
            description = "Planet of intellect, communication, commerce, and analytical wisdom."
        ),
        NAVAGRAHA_GURU to VedicAsset(
            id = NAVAGRAHA_GURU,
            title = "Guru / Brihaspati (Jupiter)",
            category = AssetCategory.NAVAGRAHA,
            drawableResId = android.R.drawable.ic_menu_agenda,
            description = "The divine teacher of expansion, higher wisdom, grace, and auspicious fortune."
        ),
        NAVAGRAHA_SHUKRA to VedicAsset(
            id = NAVAGRAHA_SHUKRA,
            title = "Shukra (Venus)",
            category = AssetCategory.NAVAGRAHA,
            drawableResId = android.R.drawable.ic_menu_gallery,
            description = "Planet of aesthetic beauty, devotion, love, harmony, and material luxury."
        ),
        NAVAGRAHA_SHANI to VedicAsset(
            id = NAVAGRAHA_SHANI,
            title = "Shani (Saturn)",
            category = AssetCategory.NAVAGRAHA,
            drawableResId = android.R.drawable.ic_menu_recent_history,
            description = "The great karmic arbiter of discipline, perseverance, and spiritual maturity."
        ),
        NAVAGRAHA_RAHU to VedicAsset(
            id = NAVAGRAHA_RAHU,
            title = "Rahu (North Node)",
            category = AssetCategory.NAVAGRAHA,
            drawableResId = android.R.drawable.ic_menu_sort_by_size,
            description = "Celestial node of material ambition, worldly innovation, and intense focus."
        ),
        NAVAGRAHA_KETU to VedicAsset(
            id = NAVAGRAHA_KETU,
            title = "Ketu (South Node)",
            category = AssetCategory.NAVAGRAHA,
            drawableResId = android.R.drawable.ic_menu_view,
            description = "Celestial node of liberation, spiritual detachment, and occult insight."
        ),
        KALA_CHAKRA_WHEEL to VedicAsset(
            id = KALA_CHAKRA_WHEEL,
            title = "Kala Chakra Cosmic Wheel",
            category = AssetCategory.CHAKRA,
            drawableResId = android.R.drawable.ic_menu_compass,
            description = "Wheel of time linking cosmic cycles, nakshatras, and planetary hours."
        ),
        VASTU_PURUSHA_MANDALA to VedicAsset(
            id = VASTU_PURUSHA_MANDALA,
            title = "Vastu Purusha Mandala",
            category = AssetCategory.VASTU,
            drawableResId = android.R.drawable.ic_menu_compass,
            description = "Sacred architectural grid aligning human habitat with cosmic directions and elements."
        ),
        MOON_PHASE_NEW to VedicAsset(
            id = MOON_PHASE_NEW,
            title = "New Moon (Amavasya)",
            category = AssetCategory.BACKGROUND,
            drawableResId = R.drawable.ic_moon_phase_new,
            description = "New moon phase image"
        ),
        MOON_PHASE_WAXING_CRESCENT to VedicAsset(
            id = MOON_PHASE_WAXING_CRESCENT,
            title = "Waxing Crescent",
            category = AssetCategory.BACKGROUND,
            drawableResId = R.drawable.ic_moon_phase_waxing_crescent,
            description = "Waxing crescent moon phase image"
        ),
        MOON_PHASE_FIRST_QUARTER to VedicAsset(
            id = MOON_PHASE_FIRST_QUARTER,
            title = "First Quarter",
            category = AssetCategory.BACKGROUND,
            drawableResId = R.drawable.ic_moon_phase_first_quarter,
            description = "First quarter moon phase image"
        ),
        MOON_PHASE_WAXING_GIBBOUS to VedicAsset(
            id = MOON_PHASE_WAXING_GIBBOUS,
            title = "Waxing Gibbous",
            category = AssetCategory.BACKGROUND,
            drawableResId = R.drawable.ic_moon_phase_waxing_gibbous,
            description = "Waxing gibbous moon phase image"
        ),
        MOON_PHASE_FULL to VedicAsset(
            id = MOON_PHASE_FULL,
            title = "Full Moon (Purnima)",
            category = AssetCategory.BACKGROUND,
            drawableResId = R.drawable.ic_moon_phase_full,
            description = "Full moon phase image"
        ),
        MOON_PHASE_WANING_GIBBOUS to VedicAsset(
            id = MOON_PHASE_WANING_GIBBOUS,
            title = "Waning Gibbous",
            category = AssetCategory.BACKGROUND,
            drawableResId = R.drawable.ic_moon_phase_waning_gibbous,
            description = "Waning gibbous moon phase image"
        ),
        MOON_PHASE_LAST_QUARTER to VedicAsset(
            id = MOON_PHASE_LAST_QUARTER,
            title = "Last Quarter",
            category = AssetCategory.BACKGROUND,
            drawableResId = R.drawable.ic_moon_phase_last_quarter,
            description = "Last quarter moon phase image"
        ),
        MOON_PHASE_WANING_CRESCENT to VedicAsset(
            id = MOON_PHASE_WANING_CRESCENT,
            title = "Waning Crescent",
            category = AssetCategory.BACKGROUND,
            drawableResId = R.drawable.ic_moon_phase_waning_crescent,
            description = "Waning crescent moon phase image"
        )
    )

    fun getAsset(assetId: String): VedicAsset? = registry[assetId]

    fun getAssetsByCategory(category: AssetCategory): List<VedicAsset> {
        return registry.values.filter { it.category == category }
    }
}
