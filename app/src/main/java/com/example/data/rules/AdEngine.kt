package com.example.data.rules

import com.example.data.local.PartnerCampaignEntity

enum class AdConditionRule(val description: String) {
    SPEED_ZERO("Vitesse = 0 km/h (arrêt complet)"),
    NO_CRITICAL_MANEUVER("Aucune instruction GPS critique dans les 15 secondes"),
    TIME_INTERVAL_PASSED("Au moins 3 minutes écoulées depuis la dernière pub"),
    TRIP_NOT_JUST_ENDED("Le trajet n'est pas terminé depuis moins de 5 secondes")
}

data class AdEvaluationResult(
    val canTrigger: Boolean,
    val failedRules: List<AdConditionRule> = emptyList(),
    val candidateCampaign: PartnerCampaignEntity? = null,
    val triggerType: String = "STOP_ZERO_KMH",
    val explanationFr: String
)

class AdEngine {

    private var lastAdTimestampMs: Long = 0L
    private val minAdIntervalMs: Long = 3 * 60 * 1000L // 3 minutes = 180,000 ms

    fun setLastAdTimestamp(timestampMs: Long) {
        lastAdTimestampMs = timestampMs
    }

    /**
     * Evaluates strict V1 ad trigger conditions.
     * @param speedKmH Current vehicle speed in km/h. MUST be 0.0 for trigger.
     * @param secondsToNextManeuver Seconds until next critical GPS turn instruction. MUST be > 15.
     * @param secondsSinceTripEnded Seconds elapsed since current trip ended. MUST be > 5 (or trip active).
     * @param currentTimeMs Current system timestamp in milliseconds.
     * @param availableCampaigns Active campaigns in current geographic zone.
     * @param currentZone Zone name (e.g. "Abidjan Nord - Cocody")
     */
    fun evaluateAdTrigger(
        speedKmH: Double,
        secondsToNextManeuver: Int,
        secondsSinceTripEnded: Int?,
        currentTimeMs: Long = System.currentTimeMillis(),
        availableCampaigns: List<PartnerCampaignEntity>,
        currentZone: String
    ): AdEvaluationResult {
        val failed = mutableListOf<AdConditionRule>()

        // 1. Speed check (Must be strictly 0 km/h)
        if (speedKmH > 0.05) {
            failed.add(AdConditionRule.SPEED_ZERO)
        }

        // 2. No critical maneuver in next 15 seconds
        if (secondsToNextManeuver <= 15) {
            failed.add(AdConditionRule.NO_CRITICAL_MANEUVER)
        }

        // 3. At least 3 minutes since last ad
        val timeSinceLastAdMs = currentTimeMs - lastAdTimestampMs
        if (lastAdTimestampMs > 0 && timeSinceLastAdMs < minAdIntervalMs) {
            failed.add(AdConditionRule.TIME_INTERVAL_PASSED)
        }

        // 4. Trip not ended within last 5 seconds
        if (secondsSinceTripEnded != null && secondsSinceTripEnded in 0..5) {
            failed.add(AdConditionRule.TRIP_NOT_JUST_ENDED)
        }

        if (failed.isNotEmpty()) {
            val failureMessages = failed.joinToString(" • ") { it.description }
            return AdEvaluationResult(
                canTrigger = false,
                failedRules = failed,
                explanationFr = "Publicité bloquée : $failureMessages"
            )
        }

        // Filter campaigns for matching or global zone
        val matchingCampaigns = availableCampaigns.filter {
            it.isActive && (it.targetZone == currentZone || it.targetZone.contains("Abidjan") || it.targetZone == "Global")
        }

        val selectedCampaign = matchingCampaigns.randomOrNull() ?: availableCampaigns.firstOrNull()

        val triggerType = if (secondsToNextManeuver > 60) "RED_LIGHT_STOP" else "TRAFFIC_JAM_STOP"

        return AdEvaluationResult(
            canTrigger = selectedCampaign != null,
            candidateCampaign = selectedCampaign,
            triggerType = triggerType,
            explanationFr = if (selectedCampaign != null)
                "Conditions V1 validées (Vitesse=0, pas de virage imminent, intervalle > 3m). Diffusion pub contextuelle : ${selectedCampaign.advertiserName}"
            else "Aucune campagne active disponible dans la zone $currentZone"
        )
    }
}
