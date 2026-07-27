package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ad_logs")
data class AdLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val campaignId: String,
    val advertiserName: String,
    val targetZone: String,
    val timestamp: Long = System.currentTimeMillis(),
    val anonymizedLat: Double,
    val anonymizedLng: Double,
    val vehicleSpeedKmH: Double = 0.0,
    val triggerType: String = "STOP_ZERO_KMH", // extensible trigger_type
    val durationSeconds: Int = 8,
    val cpmValueCfa: Int = 150
)
