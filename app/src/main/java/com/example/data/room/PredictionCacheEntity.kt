package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prediction_cache")
data class PredictionCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "Personalized Life Reading",
    val predictionData: String,
    val timestamp: Long = System.currentTimeMillis()
)
