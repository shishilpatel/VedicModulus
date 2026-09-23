package com.example.data

import android.content.Context
import com.example.core.model.chakra.ChakraType
import com.example.core.model.chakra.MeditationMode
import com.example.data.room.MeditationSessionDao
import com.example.data.room.MeditationSessionEntity
import com.example.data.room.VedicDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class MeditationStats(
    val totalMinutes: Int = 0,
    val totalSessions: Int = 0,
    val currentStreakDays: Int = 0,
    val favoriteChakra: ChakraType = ChakraType.MULADHARA
)

class ChakraProgressRepository(
    private val sessionDao: MeditationSessionDao
) {
    val allSessions: Flow<List<MeditationSessionEntity>> = sessionDao.getAllSessions()
    val favoriteSessions: Flow<List<MeditationSessionEntity>> = sessionDao.getFavoriteSessions()

    suspend fun recordSession(
        chakraType: ChakraType,
        mode: MeditationMode,
        durationMinutes: Int,
        completedSeconds: Int,
        ambientTrack: String,
        notes: String = ""
    ): Long {
        val entity = MeditationSessionEntity(
            timestamp = System.currentTimeMillis(),
            chakraId = chakraType.id,
            mode = mode.id,
            durationMinutes = durationMinutes,
            completedSeconds = completedSeconds,
            ambientTrack = ambientTrack,
            isFavorite = false,
            notes = notes
        )
        return sessionDao.insertSession(entity)
    }

    suspend fun toggleFavorite(sessionId: Long, isFavorite: Boolean) {
        sessionDao.updateFavorite(sessionId, isFavorite)
    }

    suspend fun deleteSession(session: MeditationSessionEntity) {
        sessionDao.deleteSession(session)
    }

    fun getStats(): Flow<MeditationStats> {
        return sessionDao.getAllSessions().map { sessions ->
            if (sessions.isEmpty()) {
                MeditationStats()
            } else {
                val totalSec = sessions.sumOf { it.completedSeconds.toLong() }
                val totalMins = (totalSec / 60).toInt()
                val totalSessions = sessions.size

                // Calculate streak by distinct local dates
                val distinctDates = sessions
                    .map { Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() }
                    .distinct()
                    .sortedDescending()

                var streak = 0
                var checkDate = LocalDate.now()
                // If today has no session, check if yesterday had one
                if (distinctDates.isNotEmpty() && distinctDates.first() == checkDate) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                } else if (distinctDates.isNotEmpty() && distinctDates.first() == checkDate.minusDays(1)) {
                    checkDate = checkDate.minusDays(1)
                }

                for (d in distinctDates) {
                    if (d == checkDate) {
                        streak++
                        checkDate = checkDate.minusDays(1)
                    } else if (d.isBefore(checkDate)) {
                        break
                    }
                }

                // Favorite chakra by frequency
                val topChakraId = sessions.groupingBy { it.chakraId }.eachCount().maxByOrNull { it.value }?.key
                val favoriteChakra = ChakraType.values().firstOrNull { it.id == topChakraId } ?: ChakraType.MULADHARA

                MeditationStats(
                    totalMinutes = totalMins,
                    totalSessions = totalSessions,
                    currentStreakDays = streak,
                    favoriteChakra = favoriteChakra
                )
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ChakraProgressRepository? = null

        fun getInstance(context: Context): ChakraProgressRepository {
            return INSTANCE ?: synchronized(this) {
                val db = VedicDatabase.getDatabase(context)
                val instance = ChakraProgressRepository(db.meditationSessionDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
