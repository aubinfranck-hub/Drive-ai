package com.example.data

import com.example.data.local.AdLogEntity
import com.example.data.local.DriveDao
import com.example.data.local.PartnerCampaignEntity
import com.example.data.local.TripEntity
import com.example.data.remote.GeminiClient
import com.example.data.rules.AdEngine
import com.example.data.rules.AdEvaluationResult
import kotlinx.coroutines.flow.Flow

class DriveRepository(private val dao: DriveDao) {

    val adLogs: Flow<List<AdLogEntity>> = dao.getAllAdLogs()
    val vtcTrips: Flow<List<TripEntity>> = dao.getAllTrips()
    val partnerCampaigns: Flow<List<PartnerCampaignEntity>> = dao.getAllCampaigns()

    private val adEngine = AdEngine()

    suspend fun seedInitialDataIfNeeded() {
        // Initial Partner Campaigns
        val initialCampaigns = listOf(
            PartnerCampaignEntity(
                id = "camp_petroci_01",
                advertiserName = "Petroci Côte d'Ivoire",
                campaignTitle = "Super Carburant - Offre Marcory & Plateau",
                audioScriptFr = "Faites le plein chez Petroci Marcory Boulevard VGE ! Bénéficiez d'un contrôle de pression pneu offert pour toute course VTC aujourd'hui.",
                targetZone = "Abidjan Sud - Marcory",
                category = "Station Service",
                cpmCfa = 200,
                impressionsLogged = 18,
                isActive = true
            ),
            PartnerCampaignEntity(
                id = "camp_ecobank_02",
                advertiserName = "Ecobank CI",
                campaignTitle = "Compte Express VTC - Zero Frais",
                audioScriptFr = "Chauffeurs VTC, ouvrez votre compte Ecobank Express en 3 minutes et recevez vos encaissements instantanément sans frais de virement.",
                targetZone = "Abidjan Plateau",
                category = "Banque",
                cpmCfa = 250,
                impressionsLogged = 34,
                isActive = true
            ),
            PartnerCampaignEntity(
                id = "camp_orange_03",
                advertiserName = "Orange Money CI",
                campaignTitle = "Paiement Course par QR Code",
                audioScriptFr = "Acceptez Orange Money directement dans votre véhicule avec le QR Code Drive AI. Rapidité, sécurité et bonus de 500 FCFA sur votre 10ème course.",
                targetZone = "Abidjan Nord - Cocody",
                category = "Télécom",
                cpmCfa = 180,
                impressionsLogged = 42,
                isActive = true
            ),
            PartnerCampaignEntity(
                id = "camp_total_04",
                advertiserName = "TotalEnergies Abidjan",
                campaignTitle = "Vidange Express Yopougon & Cocody",
                audioScriptFr = "TotalEnergies vous accueille à Yopougon Keneya : Vidange Quartz réalisée en 15 minutes chrono. Gardez votre moteur VTC au top !",
                targetZone = "Yopougon",
                category = "Station Service",
                cpmCfa = 220,
                impressionsLogged = 25,
                isActive = true
            )
        )
        dao.insertCampaigns(initialCampaigns)

        // Initial Sample VTC Trips
        val initialTrips = listOf(
            TripEntity(
                passengerName = "Aubin K. (Client VIP)",
                pickupPoint = "Aéroport Félix Houphouët-Boigny",
                dropoffPoints = "Hôtel Ivoire Cocody, Plateau CCIA",
                totalDistanceKm = 18.5,
                estimatedDurationMinutes = 32,
                fareCfa = 8500,
                status = "EN_COURS"
            ),
            TripEntity(
                passengerName = "Marie-Claire D.",
                pickupPoint = "Marcory Zone 4 (Rue du 7 Décembre)",
                dropoffPoints = "Palais de Justice Plateau",
                totalDistanceKm = 7.2,
                estimatedDurationMinutes = 18,
                fareCfa = 3500,
                status = "TERMINE"
            ),
            TripEntity(
                passengerName = "Koffi B. (Multi-arrêts)",
                pickupPoint = "Yopougon Bel Air",
                dropoffPoints = "Adjamé Liberté ➔ Cocody Saint-Jean",
                totalDistanceKm = 14.1,
                estimatedDurationMinutes = 28,
                fareCfa = 6000,
                status = "TERMINE"
            )
        )
        for (trip in initialTrips) {
            dao.insertTrip(trip)
        }
    }

    fun evaluateAdRules(
        speedKmH: Double,
        secondsToNextManeuver: Int,
        secondsSinceTripEnded: Int?,
        currentTimeMs: Long = System.currentTimeMillis(),
        availableCampaigns: List<PartnerCampaignEntity>,
        currentZone: String
    ): AdEvaluationResult {
        return adEngine.evaluateAdTrigger(
            speedKmH = speedKmH,
            secondsToNextManeuver = secondsToNextManeuver,
            secondsSinceTripEnded = secondsSinceTripEnded,
            currentTimeMs = currentTimeMs,
            availableCampaigns = availableCampaigns,
            currentZone = currentZone
        )
    }

    suspend fun recordAdImpression(campaign: PartnerCampaignEntity, lat: Double, lng: Double, triggerType: String) {
        val now = System.currentTimeMillis()
        adEngine.setLastAdTimestamp(now)

        // Log anonymized ad play
        val log = AdLogEntity(
            campaignId = campaign.id,
            advertiserName = campaign.advertiserName,
            targetZone = campaign.targetZone,
            timestamp = now,
            anonymizedLat = lat,
            anonymizedLng = lng,
            vehicleSpeedKmH = 0.0,
            triggerType = triggerType,
            durationSeconds = 8,
            cpmValueCfa = campaign.cpmCfa
        )
        dao.insertAdLog(log)
        dao.incrementCampaignImpression(campaign.id)
    }

    suspend fun addNewTrip(passengerName: String, pickup: String, dropoffs: String, distanceKm: Double, fareCfa: Int) {
        val trip = TripEntity(
            passengerName = passengerName,
            pickupPoint = pickup,
            dropoffPoints = dropoffs,
            totalDistanceKm = distanceKm,
            estimatedDurationMinutes = (distanceKm * 2.2).toInt(),
            fareCfa = fareCfa,
            status = "RESERVE"
        )
        dao.insertTrip(trip)
    }

    suspend fun updateTripState(tripId: Long, newStatus: String) {
        dao.updateTripStatus(tripId, newStatus)
    }

    suspend fun askGeminiCopilot(prompt: String, currentStreet: String, isVtcMode: Boolean): String {
        return GeminiClient.queryCopilot(prompt, currentStreet, isVtcMode)
    }
}
