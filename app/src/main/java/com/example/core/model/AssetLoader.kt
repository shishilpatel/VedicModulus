package com.example.core.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import java.lang.ref.SoftReference
import java.util.concurrent.ConcurrentHashMap

/**
 * AssetLoader utility object providing lazy loading, caching, and resource reference management
 * optimized for memory efficiency in Wear OS and mobile environments.
 * Prevents simultaneous heavy loading and garbage collection pressure.
 */
object AssetLoader {

    private val bitmapCache = ConcurrentHashMap<String, SoftReference<Bitmap>>()

    /**
     * Fetches the drawable resource ID for a given Vedic asset ID from [VedicAssetRegistry].
     */
    @DrawableRes
    fun getAssetDrawableRes(assetId: String): Int {
        val asset = VedicAssetRegistry.getAsset(assetId)
        return asset?.drawableResId ?: android.R.drawable.ic_menu_compass
    }

    /**
     * Lazily loads and caches a bitmap asset with memory-efficient configuration.
     * Soft references allow the garbage collector to reclaim memory when needed on Wear OS.
     */
    fun loadLazyBitmap(context: Context, assetId: String): Bitmap? {
        bitmapCache[assetId]?.get()?.let { bitmap ->
            if (!bitmap.isRecycled) {
                return bitmap
            }
        }

        val resId = getAssetDrawableRes(assetId)
        return try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565 // Optimized memory footprint for Wear OS
            }
            val bitmap = BitmapFactory.decodeResource(context.resources, resId, options)
            if (bitmap != null) {
                bitmapCache[assetId] = SoftReference(bitmap)
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clears the asset cache to free up memory when transitioning or entering low memory states.
     */
    fun clearCache() {
        bitmapCache.values.forEach { it.get()?.recycle() }
        bitmapCache.clear()
    }
}
