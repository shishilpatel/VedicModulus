package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meditation_sessions")
data class MeditationSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val chakraId: String,
    val mode: String,
    val durationMinutes: Int,
    val completedSeconds: Int,
    val ambientTrack: String,
    val isFavorite: Boolean = false,
    val notes: String = ""
)
