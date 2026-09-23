package com.example.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): AppSettingEntity?

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    fun getSettingFlow(key: String): Flow<String?>

    @Query("SELECT * FROM app_settings")
    fun getAllSettingsFlow(): Flow<List<AppSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSettings(settings: List<AppSettingEntity>)

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    suspend fun deleteSetting(key: String)

    @Query("DELETE FROM app_settings")
    suspend fun clearSettings()
}
