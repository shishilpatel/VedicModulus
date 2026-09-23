package com.example.ads

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * State representing the lifecycle of the 30-second Rewarded Ad.
 */
enum class AdState {
    IDLE,
    LOADING,
    LOADED,
    PLAYING,
    PAUSED,
    COMPLETED,
    FAILED
}

/**
 * Ad creative details representing sponsored or in-house Vedic and wellness partners.
 */
data class RewardedAdCreative(
    val id: String,
    val title: String,
    val advertiser: String,
    val category: String,
    val headline: String,
    val body: String,
    val callToAction: String,
    val rating: Float,
    val reviewsCount: String,
    val accentColorHex: Long,
    val durationSeconds: Int = 30,
    val features: List<String>
)

/**
 * RewardedAdManager handles the loading, timing, state transitions,
 * and 30-second playback progress for mandatory startup rewarded ads.
 */
class RewardedAdManager private constructor() {

    private val _adState = MutableStateFlow(AdState.IDLE)
    val adState: StateFlow<AdState> = _adState.asStateFlow()

    private val _currentCreative = MutableStateFlow<RewardedAdCreative?>(null)
    val currentCreative: StateFlow<RewardedAdCreative?> = _currentCreative.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(TOTAL_AD_DURATION_SECONDS)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _isRewardEarned = MutableStateFlow(false)
    val isRewardEarned: StateFlow<Boolean> = _isRewardEarned.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var playbackJob: Job? = null

    companion object {
        const val TOTAL_AD_DURATION_SECONDS = 30
        private const val PREFS_NAME = "vedic_ad_prefs"
        private const val KEY_DHARMA_PASS_EXPIRY = "dharma_pass_expiry_epoch_ms"
        const val DEFAULT_PASS_HOURS = 12

        private val SPONSORED_CREATIVES = listOf(
            RewardedAdCreative(
                id = "vedic_yantra_store",
                title = "Sacred Shree Yantra & Meru Prishta",
                advertiser = "Vedic Heritage Foundation",
                category = "Spiritual Artifacts",
                headline = "Authentic Pancha-Dhatu Yantras Energised with Vedic Prana-Pratishtha",
                body = "Elevate home harmony, Vastu vibrations, and spiritual abundance with certified sacred sacred geometry crafted by traditional Varanasi artisans.",
                callToAction = "Explore Sacred Collection",
                rating = 4.9f,
                reviewsCount = "14.2k ratings",
                accentColorHex = 0xFFFFD700,
                features = listOf("Vedic Consecration", "Certified Pure Copper", "Vastu Compliant Guide")
            ),
            RewardedAdCreative(
                id = "ayurveda_herbs",
                title = "Himalayan Shuddh Herbs & Rasayana",
                advertiser = "Charaka Herbals India",
                category = "Ayurveda & Wellness",
                headline = "Pure Cold-Pressed Brahmi, Ashwagandha & Vedic Chyawanprash",
                body = "Formulated strictly according to Charaka Samhita guidelines using wild-crafted herbs sourced directly from high-altitude Himalayan valleys.",
                callToAction = "Discover Herbal Range",
                rating = 4.8f,
                reviewsCount = "9.8k ratings",
                accentColorHex = 0xFF10B981,
                features = listOf("100% Organic", "Zero Additives", "Third-Party Lab Tested")
            ),
            RewardedAdCreative(
                id = "vedic_sound_meditation",
                title = "Binaural Vedic Mantras & Chants",
                advertiser = "Svara Spiritual Audio",
                category = "Mantra & Meditation",
                headline = "Master Vedic Chants in 432Hz with Authentic Sanskrit Transliteration",
                body = "Immerse in deep transcendental states with expertly recited Gayatri, Maha Mrityunjaya, and Navagraha Stotram paired with acoustic tanpura.",
                callToAction = "Listen to Previews",
                rating = 4.95f,
                reviewsCount = "28.5k listeners",
                accentColorHex = 0xFF8B5CF6,
                features = listOf("432Hz Tuning", "Grammatically Accurate", "Offline Audio")
            ),
            RewardedAdCreative(
                id = "jyotish_library",
                title = "Brihat Parashara Classic Texts",
                advertiser = "Jyotish Vidya Prakashan",
                category = "Astrology Books",
                headline = "Comprehensive Commentaries on Classical Jyotish Shastras",
                body = "Access rare manuscripts, Jaimini Sutras, Phaladeepika, and modern astrological research with detailed chapter-by-chapter breakdowns.",
                callToAction = "View Vedic Volumes",
                rating = 4.85f,
                reviewsCount = "5.1k reviews",
                accentColorHex = 0xFFF59E0B,
                features = listOf("Dual Sanskrit-English", "Chart Case Studies", "Interactive Glossaries")
            )
        )

        @Volatile
        private var instance: RewardedAdManager? = null

        fun getInstance(): RewardedAdManager {
            return instance ?: synchronized(this) {
                instance ?: RewardedAdManager().also { instance = it }
            }
        }
    }

    /**
     * Prepares and loads the 30-second rewarded ad creative.
     */
    fun loadAd(scope: CoroutineScope, onLoaded: (() -> Unit)? = null) {
        if (_adState.value == AdState.LOADING || _adState.value == AdState.PLAYING) return

        _adState.value = AdState.LOADING
        scope.launch {
            // Simulate realistic ad network handshake and asset caching
            delay(500L)
            val selectedCreative = SPONSORED_CREATIVES[Random.nextInt(SPONSORED_CREATIVES.size)]
            _currentCreative.value = selectedCreative
            _adState.value = AdState.LOADED
            onLoaded?.invoke()
        }
    }

    /**
     * Starts the 30-second playback countdown.
     * Rewarded access is only granted upon completing the full 30 seconds.
     */
    fun startPlayback(
        scope: CoroutineScope,
        onTick: ((Int) -> Unit)? = null,
        onRewardEarned: (() -> Unit)? = null
    ) {
        if (_adState.value == AdState.PLAYING) return

        if (_currentCreative.value == null) {
            _currentCreative.value = SPONSORED_CREATIVES[0]
        }

        playbackJob?.cancel()
        _adState.value = AdState.PLAYING

        playbackJob = scope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000L)
                val newSeconds = _remainingSeconds.value - 1
                _remainingSeconds.value = newSeconds
                val elapsed = TOTAL_AD_DURATION_SECONDS - newSeconds
                _playbackProgress.value = elapsed.toFloat() / TOTAL_AD_DURATION_SECONDS.toFloat()
                onTick?.invoke(newSeconds)
            }

            // 30 seconds full playback completed
            _isRewardEarned.value = true
            _adState.value = AdState.COMPLETED
            onRewardEarned?.invoke()
        }
    }

    /**
     * Pauses ad playback (e.g. if activity goes to background).
     */
    fun pausePlayback() {
        if (_adState.value == AdState.PLAYING) {
            playbackJob?.cancel()
            _adState.value = AdState.PAUSED
        }
    }

    /**
     * Resumes ad playback from the current remaining time.
     */
    fun resumePlayback(scope: CoroutineScope, onRewardEarned: (() -> Unit)? = null) {
        if (_adState.value == AdState.PAUSED && _remainingSeconds.value > 0) {
            startPlayback(scope = scope, onRewardEarned = onRewardEarned)
        }
    }

    /**
     * Toggles ad audio mute state.
     */
    fun toggleMute() {
        _isMuted.value = !_isMuted.value
    }

    /**
     * Resets manager for subsequent sessions if needed.
     */
    fun reset() {
        playbackJob?.cancel()
        _adState.value = AdState.IDLE
        _remainingSeconds.value = TOTAL_AD_DURATION_SECONDS
        _playbackProgress.value = 0f
        _isRewardEarned.value = false
    }

    /**
     * Checks whether the user holds a paid Google Play ad-free plan.
     */
    fun isPaidAdFreeActive(context: Context): Boolean {
        val sp = context.getSharedPreferences("vedic_billing_prefs", Context.MODE_PRIVATE)
        return sp.getBoolean("paid_ad_free", false)
    }

    /**
     * Checks whether the user currently has an ad-free experience
     * (either through a paid Google Play subscription or a temporary rewarded Dharma Pass).
     */
    fun isAdFree(context: Context): Boolean {
        if (isPaidAdFreeActive(context)) return true
        return isDharmaPassActive(context)
    }

    /**
     * Checks whether the user currently holds an active Dharma Pass (ad-free session)
     * or an active Google Play Paid Ad-Free subscription.
     */
    fun isDharmaPassActive(context: Context): Boolean {
        if (isPaidAdFreeActive(context)) return true
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val expiry = prefs.getLong(KEY_DHARMA_PASS_EXPIRY, 0L)
        return System.currentTimeMillis() < expiry
    }

    /**
     * Calculates remaining milliseconds for the current Dharma Pass.
     */
    fun getDharmaPassRemainingMillis(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val expiry = prefs.getLong(KEY_DHARMA_PASS_EXPIRY, 0L)
        val diff = expiry - System.currentTimeMillis()
        return if (diff > 0) diff else 0L
    }

    /**
     * Grants an ad-free Dharma Pass for the specified number of hours (default: 12h).
     */
    fun grantDharmaPass(context: Context, hours: Int = DEFAULT_PASS_HOURS) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val expiry = System.currentTimeMillis() + (hours.toLong() * 3600L * 1000L)
        prefs.edit().putLong(KEY_DHARMA_PASS_EXPIRY, expiry).apply()
    }

    /**
     * Formats the remaining Dharma Pass time as a readable string (e.g. "11h 45m").
     */
    fun getFormattedDharmaPassRemaining(context: Context): String {
        val remainingMillis = getDharmaPassRemainingMillis(context)
        if (remainingMillis <= 0) return "Expired"
        val hours = (remainingMillis / (1000 * 60 * 60)).toInt()
        val minutes = ((remainingMillis / (1000 * 60)) % 60).toInt()
        return "${hours}h ${minutes}m"
    }
}
