package com.example.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionCacheDao {
    @Query("SELECT * FROM prediction_cache ORDER BY timestamp DESC")
    fun getAllPastPredictions(): Flow<List<PredictionCacheEntity>>
    
    @Query("SELECT * FROM prediction_cache ORDER BY timestamp DESC LIMIT 1")
    fun getLatestPrediction(): Flow<PredictionCacheEntity?>

    @Query("SELECT * FROM prediction_cache ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPredictionDirect(): PredictionCacheEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionCacheEntity): Long

    @Query("DELETE FROM prediction_cache WHERE id = :id")
    suspend fun deletePrediction(id: Long)

    @Query("DELETE FROM prediction_cache")
    suspend fun clearAll()
}
