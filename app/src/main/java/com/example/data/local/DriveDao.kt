package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DriveDao {

    // Ad Logs
    @Query("SELECT * FROM ad_logs ORDER BY timestamp DESC")
    fun getAllAdLogs(): Flow<List<AdLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdLog(log: AdLogEntity)

    // VTC Trips
    @Query("SELECT * FROM vtc_trips ORDER BY timestamp DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Query("UPDATE vtc_trips SET status = :newStatus WHERE id = :tripId")
    suspend fun updateTripStatus(tripId: Long, newStatus: String)

    // Partner Campaigns
    @Query("SELECT * FROM partner_campaigns")
    fun getAllCampaigns(): Flow<List<PartnerCampaignEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaigns(campaigns: List<PartnerCampaignEntity>)

    @Query("UPDATE partner_campaigns SET impressionsLogged = impressionsLogged + 1 WHERE id = :campaignId")
    suspend fun incrementCampaignImpression(campaignId: String)
}
