package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "partner_campaigns")
data class PartnerCampaignEntity(
    @PrimaryKey val id: String,
    val advertiserName: String,
    val campaignTitle: String,
    val audioScriptFr: String,
    val targetZone: String, // e.g., "Abidjan Nord - Cocody", "Abidjan Sud - Marcory", "Yopougon"
    val category: String, // "Station Service", "Banque", "Restauration", "Télécom"
    val cpmCfa: Int = 150,
    val impressionsLogged: Int = 0,
    val isActive: Boolean = true
)
