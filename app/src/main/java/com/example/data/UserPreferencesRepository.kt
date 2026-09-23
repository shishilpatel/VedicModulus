package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vedic_cosmic_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_FULL_NAME = stringPreferencesKey("full_name")
        val KEY_BIRTH_DATE = stringPreferencesKey("birth_date") // YYYY-MM-DD
        val KEY_BIRTH_TIME = stringPreferencesKey("birth_time") // HH:mm
        val KEY_CITY_NAME = stringPreferencesKey("city_name")
        val KEY_STATE = stringPreferencesKey("state")
        val KEY_COUNTRY = stringPreferencesKey("country")
        val KEY_LATITUDE = doublePreferencesKey("latitude")
        val KEY_LONGITUDE = doublePreferencesKey("longitude")
        val KEY_TIMEZONE = stringPreferencesKey("timezone")
        val KEY_AYANAMSHA = stringPreferencesKey("ayanamsha")
        val KEY_CALENDAR_TRADITION = stringPreferencesKey("calendar_tradition")
        val KEY_NUMEROLOGY_SYSTEM = stringPreferencesKey("numerology_system")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_WATCH_STYLE = stringPreferencesKey("watch_style")
        val KEY_APP_THEME = stringPreferencesKey("app_theme")
        val KEY_HAS_SMART_WATCH = booleanPreferencesKey("has_smart_watch")
        val KEY_WATCH_MODEL = stringPreferencesKey("watch_model")
        val KEY_IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val KEY_IS_WATCH_SYNCED = booleanPreferencesKey("is_watch_synced")
        val KEY_WEAR_BACKGROUND_SYNC_ENABLED = booleanPreferencesKey("wear_background_sync_enabled")
        val KEY_DAILY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("daily_notifications_enabled")
        val KEY_DAILY_NOTIFICATION_TIME = stringPreferencesKey("daily_notification_time")
        val KEY_NOTIFY_FESTIVALS = booleanPreferencesKey("notify_festivals")
        val KEY_NOTIFY_MUHURTAS = booleanPreferencesKey("notify_muhurtas")
        val KEY_NOTIFY_RAHU_KAAL = booleanPreferencesKey("notify_rahu_kaal")
        val KEY_PAID_AD_FREE = booleanPreferencesKey("paid_ad_free")
        val KEY_PAID_PLAN_ID = stringPreferencesKey("paid_plan_id")
        val KEY_PURCHASE_TOKEN = stringPreferencesKey("purchase_token")
        val KEY_PURCHASE_ORDER_ID = stringPreferencesKey("purchase_order_id")
        val KEY_PURCHASE_TIME_MS = longPreferencesKey("purchase_time_ms")

        // Authentication & Role Management Keys
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_ROLE = stringPreferencesKey("user_role") // REGULAR or ASTROLOGER
        val KEY_ASTROLOGER_LICENSE = stringPreferencesKey("astrologer_license")
        val KEY_ACTIVE_CLIENT_ID = longPreferencesKey("active_client_id")
        val KEY_VASTU_LIVE_SENSOR_ENABLED = booleanPreferencesKey("vastu_live_sensor_enabled")
        val KEY_VASTU_SPACE_OPTIMIZATION_ENABLED = booleanPreferencesKey("vastu_space_optimization_enabled")
        val KEY_VASTU_SPACE_MODE = stringPreferencesKey("vastu_space_mode")
        val KEY_VASTU_SELECTED_ROOM = stringPreferencesKey("vastu_selected_room")

        fun isPaidAdFreeCached(context: Context): Boolean {
            val sp = context.getSharedPreferences("vedic_billing_prefs", Context.MODE_PRIVATE)
            return sp.getBoolean("paid_ad_free", false)
        }

        private var inMemoryVastuOptimizationEnabled: Boolean? = null

        fun isVastuSpaceOptimizationEnabledCached(context: Context): Boolean {
            inMemoryVastuOptimizationEnabled?.let { return it }
            val sp = context.getSharedPreferences("vedic_vastu_prefs", Context.MODE_PRIVATE)
            val res = if (sp.contains("vastu_space_optimization_enabled")) {
                sp.getBoolean("vastu_space_optimization_enabled", true)
            } else if (sp.contains("vastu_live_sensor_enabled")) {
                sp.getBoolean("vastu_live_sensor_enabled", true)
            } else {
                true
            }
            inMemoryVastuOptimizationEnabled = res
            return res
        }

        fun setVastuSpaceOptimizationEnabledCached(context: Context, enabled: Boolean) {
            inMemoryVastuOptimizationEnabled = enabled
            val sp = context.getSharedPreferences("vedic_vastu_prefs", Context.MODE_PRIVATE)
            sp.edit()
                .putBoolean("vastu_space_optimization_enabled", enabled)
                .putBoolean("vastu_live_sensor_enabled", enabled)
                .apply()
        }

        fun isVastuLiveSensorEnabledCached(context: Context): Boolean {
            return isVastuSpaceOptimizationEnabledCached(context)
        }

        fun setVastuLiveSensorEnabledCached(context: Context, enabled: Boolean) {
            setVastuSpaceOptimizationEnabledCached(context, enabled)
        }

        fun getVastuSpaceModeCached(context: Context): com.example.engines.vastu.VastuSpaceMode {
            val sp = context.getSharedPreferences("vedic_vastu_prefs", Context.MODE_PRIVATE)
            val modeStr = sp.getString("vastu_space_mode", com.example.engines.vastu.VastuSpaceMode.HOME.name)
            return try {
                com.example.engines.vastu.VastuSpaceMode.valueOf(modeStr ?: com.example.engines.vastu.VastuSpaceMode.HOME.name)
            } catch (e: Exception) {
                com.example.engines.vastu.VastuSpaceMode.HOME
            }
        }

        fun setVastuSpaceModeCached(context: Context, mode: com.example.engines.vastu.VastuSpaceMode) {
            val sp = context.getSharedPreferences("vedic_vastu_prefs", Context.MODE_PRIVATE)
            sp.edit().putString("vastu_space_mode", mode.name).apply()
        }

        fun getVastuSelectedRoomNameCached(context: Context): String? {
            val sp = context.getSharedPreferences("vedic_vastu_prefs", Context.MODE_PRIVATE)
            return sp.getString("vastu_selected_room", null)
        }

        fun setVastuSelectedRoomNameCached(context: Context, roomName: String) {
            val sp = context.getSharedPreferences("vedic_vastu_prefs", Context.MODE_PRIVATE)
            sp.edit().putString("vastu_selected_room", roomName).apply()
        }
    }

    // Database Synchronization Helper
    private suspend fun persistSettingToRoom(key: String, value: String) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = com.example.data.room.VedicDatabase.getDatabase(context)
                db.appSettingsDao().setSetting(
                    com.example.data.room.AppSettingEntity(
                        key = key,
                        value = value,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                android.util.Log.d("UserPreferencesRepo", "Setting '$key' preserved in SQLite Room DB: $value")
            } catch (e: Exception) {
                android.util.Log.e("UserPreferencesRepo", "Failed to preserve setting '$key' in Room DB: ${e.message}", e)
            }
        }
    }

    private suspend fun persistMultipleSettingsToRoom(settingsMap: Map<String, String>) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = com.example.data.room.VedicDatabase.getDatabase(context)
                val now = System.currentTimeMillis()
                val entities = settingsMap.map { (k, v) ->
                    com.example.data.room.AppSettingEntity(key = k, value = v, updatedAt = now)
                }
                db.appSettingsDao().setSettings(entities)
                android.util.Log.d("UserPreferencesRepo", "${entities.size} settings successfully batch preserved in SQLite Room DB.")
            } catch (e: Exception) {
                android.util.Log.e("UserPreferencesRepo", "Failed to batch preserve settings in Room DB: ${e.message}", e)
            }
        }
    }

    val vastuLiveSensorEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VASTU_LIVE_SENSOR_ENABLED] ?: isVastuLiveSensorEnabledCached(context)
    }.distinctUntilChanged()

    suspend fun saveVastuLiveSensorEnabled(enabled: Boolean) {
        setVastuLiveSensorEnabledCached(context, enabled)
        persistSettingToRoom("vastu_live_sensor_enabled", enabled.toString())
        persistSettingToRoom("vastu_space_optimization_enabled", enabled.toString())
        try {
            context.dataStore.edit { prefs ->
                prefs[KEY_VASTU_LIVE_SENSOR_ENABLED] = enabled
                prefs[KEY_VASTU_SPACE_OPTIMIZATION_ENABLED] = enabled
            }
        } catch (e: Exception) {
            // Ignore DataStore write errors so local immediate state is not broken
        }
    }

    val isPaidAdFreeFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_PAID_AD_FREE] ?: false }
    val paidPlanIdFlow: Flow<String?> = context.dataStore.data.map { it[KEY_PAID_PLAN_ID] }
    val purchaseOrderIdFlow: Flow<String?> = context.dataStore.data.map { it[KEY_PURCHASE_ORDER_ID] }
    val purchaseTimeMsFlow: Flow<Long> = context.dataStore.data.map { it[KEY_PURCHASE_TIME_MS] ?: 0L }

    suspend fun savePaidSubscription(
        planId: String,
        token: String,
        orderId: String,
        purchaseTime: Long = System.currentTimeMillis()
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PAID_AD_FREE] = true
            prefs[KEY_PAID_PLAN_ID] = planId
            prefs[KEY_PURCHASE_TOKEN] = token
            prefs[KEY_PURCHASE_ORDER_ID] = orderId
            prefs[KEY_PURCHASE_TIME_MS] = purchaseTime
        }
        val sp = context.getSharedPreferences("vedic_billing_prefs", Context.MODE_PRIVATE)
        sp.edit()
            .putBoolean("paid_ad_free", true)
            .putString("paid_plan_id", planId)
            .putString("purchase_token", token)
            .putString("purchase_order_id", orderId)
            .putLong("purchase_time_ms", purchaseTime)
            .apply()

        persistMultipleSettingsToRoom(
            mapOf(
                "paid_ad_free" to "true",
                "paid_plan_id" to planId,
                "purchase_token" to token,
                "purchase_order_id" to orderId,
                "purchase_time_ms" to purchaseTime.toString()
            )
        )
    }

    suspend fun clearPaidSubscription() {
        context.dataStore.edit { prefs ->
            prefs[KEY_PAID_AD_FREE] = false
            prefs.remove(KEY_PAID_PLAN_ID)
            prefs.remove(KEY_PURCHASE_TOKEN)
            prefs.remove(KEY_PURCHASE_ORDER_ID)
            prefs.remove(KEY_PURCHASE_TIME_MS)
        }
        val sp = context.getSharedPreferences("vedic_billing_prefs", Context.MODE_PRIVATE)
        sp.edit().clear().apply()
        persistSettingToRoom("paid_ad_free", "false")
    }

    val dailyNotificationsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_DAILY_NOTIFICATIONS_ENABLED] ?: true }
    val dailyNotificationTimeFlow: Flow<String> = context.dataStore.data.map { it[KEY_DAILY_NOTIFICATION_TIME] ?: "06:30" }
    val notifyFestivalsFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFY_FESTIVALS] ?: true }
    val notifyMuhurtasFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFY_MUHURTAS] ?: true }
    val notifyRahuKaalFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_NOTIFY_RAHU_KAAL] ?: true }
    val wearBackgroundSyncEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_WEAR_BACKGROUND_SYNC_ENABLED] ?: true }

    suspend fun updateWearBackgroundSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_WEAR_BACKGROUND_SYNC_ENABLED] = enabled }
        persistSettingToRoom("wear_background_sync_enabled", enabled.toString())
    }

    suspend fun updateDailyNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DAILY_NOTIFICATIONS_ENABLED] = enabled }
        persistSettingToRoom("daily_notifications_enabled", enabled.toString())
    }

    suspend fun updateDailyNotificationTime(time: String) {
        context.dataStore.edit { it[KEY_DAILY_NOTIFICATION_TIME] = time }
        persistSettingToRoom("daily_notification_time", time)
    }

    suspend fun updateNotifyFestivals(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFY_FESTIVALS] = enabled }
        persistSettingToRoom("notify_festivals", enabled.toString())
    }

    suspend fun updateNotifyMuhurtas(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFY_MUHURTAS] = enabled }
        persistSettingToRoom("notify_muhurtas", enabled.toString())
    }

    suspend fun updateNotifyRahuKaal(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFY_RAHU_KAAL] = enabled }
        persistSettingToRoom("notify_rahu_kaal", enabled.toString())
    }

    val birthProfileFlow: Flow<BirthProfile?> = context.dataStore.data.map { prefs ->
        val isOnboarded = prefs[KEY_IS_ONBOARDED] ?: false
        if (!isOnboarded) {
            // Default sample profile (Ahmedabad, 15 August 1990, 07:42 AM as referenced in user prompt)
            BirthProfile(
                fullName = "Example User",
                birthDate = LocalDate.of(1990, 8, 15),
                birthTime = LocalTime.of(7, 42),
                birthLocation = CityRepository.getDefaultCity(),
                ayanamsha = AyanamshaSystem.LAHIRI,
                calendarTradition = CalendarTradition.AMANTA,
                numerologySystem = NumerologySystem.CHALDEAN,
                language = LanguageDisplay.ENGLISH,
                preferredStyle = WatchFaceStyle.COSMIC
            )
        } else {
            val name = prefs[KEY_FULL_NAME] ?: "Seeker"
            val dateStr = prefs[KEY_BIRTH_DATE] ?: "1990-08-15"
            val timeStr = prefs[KEY_BIRTH_TIME] ?: "07:42"
            val cityName = prefs[KEY_CITY_NAME] ?: "Ahmedabad"
            val lat = prefs[KEY_LATITUDE]
            val lon = prefs[KEY_LONGITUDE]
            val tz = prefs[KEY_TIMEZONE]
            val state = prefs[KEY_STATE] ?: "Gujarat"
            val country = prefs[KEY_COUNTRY] ?: "India"

            val city = if (lat != null && lon != null && tz != null) {
                LocationData(
                    country = country,
                    state = state,
                    district = cityName,
                    talukaOrTown = cityName,
                    cityName = cityName,
                    latitude = lat,
                    longitude = lon,
                    timezoneId = tz
                )
            } else {
                CityRepository.CITIES.firstOrNull { it.cityName == cityName } ?: CityRepository.getDefaultCity()
            }

            val ayanamsha = try {
                AyanamshaSystem.valueOf(prefs[KEY_AYANAMSHA] ?: AyanamshaSystem.LAHIRI.name)
            } catch (e: Exception) { AyanamshaSystem.LAHIRI }

            val tradition = try {
                CalendarTradition.valueOf(prefs[KEY_CALENDAR_TRADITION] ?: CalendarTradition.AMANTA.name)
            } catch (e: Exception) { CalendarTradition.AMANTA }

            val numerology = try {
                NumerologySystem.valueOf(prefs[KEY_NUMEROLOGY_SYSTEM] ?: NumerologySystem.CHALDEAN.name)
            } catch (e: Exception) { NumerologySystem.CHALDEAN }

            val language = try {
                LanguageDisplay.valueOf(prefs[KEY_LANGUAGE] ?: LanguageDisplay.ENGLISH.name)
            } catch (e: Exception) { LanguageDisplay.ENGLISH }

            val style = try {
                WatchFaceStyle.valueOf(prefs[KEY_WATCH_STYLE] ?: WatchFaceStyle.COSMIC.name)
            } catch (e: Exception) { WatchFaceStyle.COSMIC }

            val appTheme = try {
                AppThemePalette.valueOf(prefs[KEY_APP_THEME] ?: AppThemePalette.SOLAR_GOLD.name)
            } catch (e: Exception) { AppThemePalette.SOLAR_GOLD }

            val hasSmartWatch = prefs[KEY_HAS_SMART_WATCH] ?: false
            val watchModel = prefs[KEY_WATCH_MODEL] ?: "None"

            BirthProfile(
                fullName = name,
                birthDate = LocalDate.parse(dateStr),
                birthTime = LocalTime.parse(timeStr),
                birthLocation = city,
                ayanamsha = ayanamsha,
                calendarTradition = tradition,
                numerologySystem = numerology,
                language = language,
                preferredStyle = style,
                appTheme = appTheme,
                hasSmartWatch = hasSmartWatch,
                watchModel = watchModel
            )
        }
    }.distinctUntilChanged()

    suspend fun clearBirthProfile() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_FULL_NAME)
            prefs.remove(KEY_IS_ONBOARDED)
        }
        persistSettingToRoom("is_onboarded", "false")
    }

    suspend fun saveBirthProfile(profile: BirthProfile) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FULL_NAME] = profile.fullName
            prefs[KEY_BIRTH_DATE] = profile.birthDate.toString()
            prefs[KEY_BIRTH_TIME] = profile.birthTime.toString()
            prefs[KEY_CITY_NAME] = profile.birthLocation.cityName
            prefs[KEY_STATE] = profile.birthLocation.state
            prefs[KEY_COUNTRY] = profile.birthLocation.country
            prefs[KEY_LATITUDE] = profile.birthLocation.latitude
            prefs[KEY_LONGITUDE] = profile.birthLocation.longitude
            prefs[KEY_TIMEZONE] = profile.birthLocation.timezoneId
            prefs[KEY_AYANAMSHA] = profile.ayanamsha.name
            prefs[KEY_CALENDAR_TRADITION] = profile.calendarTradition.name
            prefs[KEY_NUMEROLOGY_SYSTEM] = profile.numerologySystem.name
            prefs[KEY_LANGUAGE] = profile.language.name
            prefs[KEY_WATCH_STYLE] = profile.preferredStyle.name
            prefs[KEY_APP_THEME] = profile.appTheme.name
            prefs[KEY_HAS_SMART_WATCH] = profile.hasSmartWatch
            prefs[KEY_WATCH_MODEL] = profile.watchModel
            prefs[KEY_IS_ONBOARDED] = true
        }

        // Persist to Room UserProfileDao (Primary User Profile)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = com.example.data.room.VedicDatabase.getDatabase(context)
                val existingPrimary = db.userProfileDao().getPrimaryProfile()
                val profileEntity = com.example.data.room.UserProfileEntity.fromBirthProfile(
                    profile = profile,
                    isPrimary = true,
                    relation = "Self",
                    id = existingPrimary?.id ?: 0
                )
                if (existingPrimary != null) {
                    db.userProfileDao().updateProfile(profileEntity)
                } else {
                    db.userProfileDao().insertProfile(profileEntity)
                }
            } catch (e: Exception) {
                android.util.Log.e("UserPreferencesRepo", "Error persisting profile to UserProfileDao: ${e.message}", e)
            }
        }

        // Batch persist all settings to AppSettingsDao
        persistMultipleSettingsToRoom(
            mapOf(
                "full_name" to profile.fullName,
                "birth_date" to profile.birthDate.toString(),
                "birth_time" to profile.birthTime.toString(),
                "city_name" to profile.birthLocation.cityName,
                "state" to profile.birthLocation.state,
                "country" to profile.birthLocation.country,
                "latitude" to profile.birthLocation.latitude.toString(),
                "longitude" to profile.birthLocation.longitude.toString(),
                "timezone" to profile.birthLocation.timezoneId,
                "ayanamsha" to profile.ayanamsha.name,
                "calendar_tradition" to profile.calendarTradition.name,
                "numerology_system" to profile.numerologySystem.name,
                "language" to profile.language.name,
                "watch_style" to profile.preferredStyle.name,
                "app_theme" to profile.appTheme.name,
                "has_smart_watch" to profile.hasSmartWatch.toString(),
                "watch_model" to profile.watchModel,
                "is_onboarded" to "true"
            )
        )
    }

    suspend fun updateWatchStyle(style: WatchFaceStyle) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WATCH_STYLE] = style.name
        }
        persistSettingToRoom("watch_style", style.name)
    }

    suspend fun updateAppTheme(theme: AppThemePalette) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_THEME] = theme.name
        }
        persistSettingToRoom("app_theme", theme.name)
    }

    suspend fun setWatchSynced(synced: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_WATCH_SYNCED] = synced
        }
        persistSettingToRoom("is_watch_synced", synced.toString())
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        val lang = prefs[KEY_LANGUAGE] ?: LanguageDisplay.ENGLISH.name
        val enumVal = try {
            LanguageDisplay.valueOf(lang)
        } catch (e: Exception) {
            LanguageDisplay.ENGLISH
        }
        com.example.core.language.LanguageManager.getLanguageCode(enumVal)
    }

    suspend fun saveLanguage(languageCode: String) {
        val enumName = com.example.core.language.LanguageManager.getLanguageFromCode(languageCode).name
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = enumName
        }
        persistSettingToRoom("language", enumName)
        persistSettingToRoom("language_code", languageCode)
    }

    // Auth & Role Flows
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_LOGGED_IN] ?: false }

    val userAccountFlow: Flow<UserAccount?> = context.dataStore.data.map { prefs ->
        val isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false
        if (!isLoggedIn) {
            null
        } else {
            val email = prefs[KEY_USER_EMAIL] ?: "user@vediccosmic.org"
            val name = prefs[KEY_USER_NAME] ?: "Jyotish Seeker"
            val roleStr = prefs[KEY_USER_ROLE] ?: UserRole.REGULAR.name
            val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.REGULAR }
            val license = prefs[KEY_ASTROLOGER_LICENSE] ?: ""
            UserAccount(
                email = email,
                fullName = name,
                role = role,
                astrologerTitleOrLicense = license,
                isLoggedIn = true
            )
        }
    }

    val userRoleFlow: Flow<UserRole> = context.dataStore.data.map { prefs ->
        val roleStr = prefs[KEY_USER_ROLE] ?: UserRole.REGULAR.name
        try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.REGULAR }
    }

    val activeClientIdFlow: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_CLIENT_ID]
    }

    suspend fun saveUserAccount(account: UserAccount) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = account.isLoggedIn
            prefs[KEY_USER_EMAIL] = account.email
            prefs[KEY_USER_NAME] = account.fullName
            prefs[KEY_USER_ROLE] = account.role.name
            prefs[KEY_ASTROLOGER_LICENSE] = account.astrologerTitleOrLicense
        }
        persistMultipleSettingsToRoom(
            mapOf(
                "is_logged_in" to account.isLoggedIn.toString(),
                "user_email" to account.email,
                "user_name" to account.fullName,
                "user_role" to account.role.name,
                "astrologer_license" to account.astrologerTitleOrLicense
            )
        )
    }

    suspend fun updateUserRole(role: UserRole) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ROLE] = role.name
        }
        persistSettingToRoom("user_role", role.name)
    }

    suspend fun saveActiveClientId(clientId: Long?) {
        context.dataStore.edit { prefs ->
            if (clientId != null && clientId > 0) {
                prefs[KEY_ACTIVE_CLIENT_ID] = clientId
            } else {
                prefs.remove(KEY_ACTIVE_CLIENT_ID)
            }
        }
        persistSettingToRoom("active_client_id", (clientId ?: 0L).toString())
    }

    suspend fun signOutUser() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs.remove(KEY_USER_EMAIL)
            prefs.remove(KEY_USER_NAME)
            prefs.remove(KEY_USER_ROLE)
            prefs.remove(KEY_ASTROLOGER_LICENSE)
            prefs.remove(KEY_ACTIVE_CLIENT_ID)
        }
        persistSettingToRoom("is_logged_in", "false")
    }

    suspend fun deleteAccountAndWipeData(appContext: Context) {
        // Clear all DataStore preferences
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
        // Delete all local Room database entries
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val db = com.example.data.room.VedicDatabase.getDatabase(appContext)
                db.clearAllTables()
            } catch (e: Exception) {
                android.util.Log.e("UserPreferencesRepo", "Error clearing Room database on delete: ${e.message}", e)
            }
        }
    }
}

