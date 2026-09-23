package com.example.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyPanchangDao {
    @Query("SELECT * FROM daily_panchang_cache WHERE dateString = :dateStr")
    suspend fun getPanchang(dateStr: String): DailyPanchangEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPanchang(entity: DailyPanchangEntity)

    @Query("DELETE FROM daily_panchang_cache WHERE updatedAt < :olderThanTimestamp")
    suspend fun clearOldCache(olderThanTimestamp: Long)
}
