package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DriveRepository
import com.example.data.gps.GpsSimulator
import com.example.data.gps.NavigationState
import com.example.data.local.AdLogEntity
import com.example.data.local.DriveDatabase
import com.example.data.local.PartnerCampaignEntity
import com.example.data.local.TripEntity
import com.example.data.rules.AdEvaluationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    GPS_DRIVE,
    VTC_CHAUFFEUR,
    ADMIN_PARTENAIRES
}

data class CopilotUiState(
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val lastUserQuery: String? = null,
    val lastCopilotAnswer: String? = null,
    val showVoiceOverlay: Boolean = false,
    val audioWaveIntensity: Float = 0f
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DriveDatabase.getDatabase(application)
    private val repository = DriveRepository(db.driveDao())
    val gpsSimulator = GpsSimulator()

    val navigationState: StateFlow<NavigationState> = gpsSimulator.navigationState

    val adLogs: StateFlow<List<AdLogEntity>> = repository.adLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vtcTrips: StateFlow<List<TripEntity>> = repository.vtcTrips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val partnerCampaigns: StateFlow<List<PartnerCampaignEntity>> = repository.partnerCampaigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(AppTab.GPS_DRIVE)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _isDegradedNetwork = MutableStateFlow(false)
    val isDegradedNetwork: StateFlow<Boolean> = _isDegradedNetwork.asStateFlow()

    private val _copilotState = MutableStateFlow(CopilotUiState())
    val copilotState: StateFlow<CopilotUiState> = _copilotState.asStateFlow()

    private val _activeAudioAd = MutableStateFlow<PartnerCampaignEntity?>(null)
    val activeAudioAd: StateFlow<PartnerCampaignEntity?> = _activeAudioAd.asStateFlow()

    private val _adCountdownSeconds = MutableStateFlow(0)
    val adCountdownSeconds: StateFlow<Int> = _adCountdownSeconds.asStateFlow()

    val adEvaluation: StateFlow<AdEvaluationResult> = combine(
        navigationState,
        partnerCampaigns
    ) { nav, campaigns ->
        repository.evaluateAdRules(
            speedKmH = nav.speedKmH,
            secondsToNextManeuver = nav.secondsToNextManeuver,
            secondsSinceTripEnded = nav.secondsSinceTripEnded,
            availableCampaigns = campaigns,
            currentZone = nav.currentZone
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AdEvaluationResult(canTrigger = false, explanationFr = "Initialisation du moteur publicitaire...")
    )

    private var adPlaybackJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            gpsSimulator.startSimulation()
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun toggleDegradedNetwork() {
        _isDegradedNetwork.value = !_isDegradedNetwork.value
    }

    fun triggerAudioAdManualOrAuto() {
        val evaluation = adEvaluation.value
        val campaign = evaluation.candidateCampaign
        if (evaluation.canTrigger && campaign != null && adPlaybackJob?.isActive != true) {
            startAudioAdPlayback(campaign, evaluation.triggerType)
        }
    }

    private fun startAudioAdPlayback(campaign: PartnerCampaignEntity, triggerType: String) {
        adPlaybackJob?.cancel()
        adPlaybackJob = viewModelScope.launch {
            _activeAudioAd.value = campaign
            _adCountdownSeconds.value = 8

            // Record anonymized log immediately
            val nav = navigationState.value
            repository.recordAdImpression(campaign, nav.lat, nav.lng, triggerType)

            while (_adCountdownSeconds.value > 0) {
                delay(1000)
                _adCountdownSeconds.value -= 1
            }

            _activeAudioAd.value = null
        }
    }

    fun dismissAudioAd() {
        adPlaybackJob?.cancel()
        _activeAudioAd.value = null
    }

    fun toggleVoiceCopilotListening() {
        val current = _copilotState.value
        if (current.isListening) {
            _copilotState.value = current.copy(isListening = false)
        } else {
            _copilotState.value = current.copy(
                isListening = true,
                showVoiceOverlay = true,
                lastCopilotAnswer = "Écoute en cours... Parlez à Gemini Live (\"Stations essence\", \"Raccourci vers Plateau\", \"Météo\")"
            )
        }
    }

    fun dismissVoiceOverlay() {
        _copilotState.value = _copilotState.value.copy(showVoiceOverlay = false, isListening = false)
    }

    fun sendCopilotQuery(prompt: String) {
        viewModelScope.launch {
            _copilotState.value = _copilotState.value.copy(
                isListening = false,
                isProcessing = true,
                lastUserQuery = prompt,
                showVoiceOverlay = true
            )

            val currentStreet = navigationState.value.currentStreet
            val isVtc = currentTab.value == AppTab.VTC_CHAUFFEUR

            if (_isDegradedNetwork.value) {
                delay(800)
                _copilotState.value = _copilotState.value.copy(
                    isProcessing = false,
                    lastCopilotAnswer = "Mode réseau dégradé (2G/3G) : Connexion Gemini Live suspendue. Le guidage GPS standard reste 100% actif."
                )
            } else {
                val answer = repository.askGeminiCopilot(prompt, currentStreet, isVtc)
                _copilotState.value = _copilotState.value.copy(
                    isProcessing = false,
                    lastCopilotAnswer = answer
                )
            }
        }
    }

    fun addVtcTrip(passengerName: String, pickup: String, dropoffs: String, distanceKm: Double, fareCfa: Int) {
        viewModelScope.launch {
            repository.addNewTrip(passengerName, pickup, dropoffs, distanceKm, fareCfa)
        }
    }

    fun updateTripStatus(tripId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateTripState(tripId, newStatus)
        }
    }
}
