package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vtc_trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val passengerName: String,
    val pickupPoint: String,
    val dropoffPoints: String, // Comma separated dropoff stops
    val totalDistanceKm: Double,
    val estimatedDurationMinutes: Int,
    val fareCfa: Int,
    val status: String, // "EN_COURS", "TERMINE", "RESERVE"
    val timestamp: Long = System.currentTimeMillis()
)
