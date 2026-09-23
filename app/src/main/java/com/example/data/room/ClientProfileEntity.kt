package com.example.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "client_profiles")
data class ClientProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val astrologerEmail: String,
    val fullName: String,
    val birthDate: String, // ISO YYYY-MM-DD
    val birthTime: String, // HH:mm
    val country: String = "India",
    val state: String = "Maharashtra",
    val district: String = "Mumbai",
    val talukaOrTown: String = "Mumbai",
    val cityName: String = "Mumbai",
    val latitude: Double = 19.0760,
    val longitude: Double = 72.8777,
    val timezoneId: String = "Asia/Kolkata",
    val notes: String = "",
    val phoneOrEmail: String = "",
    val lastConsultationDate: String = "",
    val nextAppointmentDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
