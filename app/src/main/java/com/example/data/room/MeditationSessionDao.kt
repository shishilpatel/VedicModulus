package com.example.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MeditationSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MeditationSessionEntity): Long

    @Query("SELECT * FROM meditation_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<MeditationSessionEntity>>

    @Query("SELECT * FROM meditation_sessions WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteSessions(): Flow<List<MeditationSessionEntity>>

    @Query("SELECT SUM(completedSeconds) FROM meditation_sessions")
    fun getTotalSecondsMeditated(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM meditation_sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("UPDATE meditation_sessions SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Delete
    suspend fun deleteSession(session: MeditationSessionEntity)

    @Query("DELETE FROM meditation_sessions")
    suspend fun clearAll()
}
