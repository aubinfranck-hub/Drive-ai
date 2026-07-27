package com.example.data.gps

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NavigationPoint(
    val streetName: String,
    val zone: String,
    val instruction: String,
    val secondsToManeuver: Int,
    val lat: Double,
    val lng: Double,
    val isTurnPoint: Boolean = false
)

data class NavigationState(
    val currentStreet: String = "Boulevard République, Plateau",
    val currentZone: String = "Abidjan Plateau",
    val nextInstruction: String = "Prenez le Pont HKB dans 400m vers Marcory",
    val secondsToNextManeuver: Int = 30,
    val speedKmH: Double = 45.0,
    val distanceRemainingKm: Double = 8.4,
    val etaMinutes: Int = 16,
    val lat: Double = 5.3214,
    val lng: Double = -4.0123,
    val isSimulationRunning: Boolean = true,
    val currentRouteName: String = "Plateau ➔ Cocody ➔ Marcory Zone 4",
    val secondsSinceTripEnded: Int? = null
)

class GpsSimulator {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val routePoints = listOf(
        NavigationPoint("Avenue Chardy, Plateau", "Abidjan Plateau", "Continuez tout droit sur 500m", 25, 5.3201, -4.0156),
        NavigationPoint("Boulevard Carde, Plateau", "Abidjan Plateau", "Tournez à droite vers le Pont HKB dans 300m", 18, 5.3225, -4.0130, true),
        NavigationPoint("Pont Henri Konan Bédié", "Abidjan Sud - Marcory", "Suivez la Voie Express HKB sur 2.5 km", 60, 5.3298, -4.0012),
        NavigationPoint("Boulevard VGE, Marcory", "Abidjan Sud - Marcory", "Prenez la sortie Zone 4 dans 450m", 35, 5.3050, -3.9890, true),
        NavigationPoint("Rue Pierre et Marie Curie, Zone 4", "Abidjan Sud - Marcory", "Série de ralentisseurs, serrez à gauche", 40, 5.2980, -3.9810),
        NavigationPoint("Boulevard Latrille, Cocody", "Abidjan Nord - Cocody", "Rejoignez le rond-point Saint-Jean dans 200m", 12, 5.3540, -3.9980, true),
        NavigationPoint("Boulevard de France, Cocody", "Abidjan Nord - Cocody", "Arrivée à destination dans 100m", 8, 5.3610, -3.9910)
    )

    private var currentIndex = 0
    private var simulationJob: Job? = null

    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    fun startSimulation() {
        if (simulationJob?.isActive == true) return
        simulationJob = scope.launch {
            while (true) {
                delay(3000)
                val state = _navigationState.value
                if (state.isSimulationRunning) {
                    currentIndex = (currentIndex + 1) % routePoints.size
                    val point = routePoints[currentIndex]
                    
                    val newSpeed = if (point.isTurnPoint) 0.0 else (30..65).random().toDouble()
                    val newDist = maxOf(0.5, state.distanceRemainingKm - 0.6)
                    val newEta = maxOf(1, (newDist * 2.1).toInt())

                    _navigationState.value = state.copy(
                        currentStreet = point.streetName,
                        currentZone = point.zone,
                        nextInstruction = point.instruction,
                        secondsToNextManeuver = point.secondsToManeuver,
                        speedKmH = newSpeed,
                        distanceRemainingKm = newDist,
                        etaMinutes = newEta,
                        lat = point.lat,
                        lng = point.lng
                    )
                }
            }
        }
    }

    fun setSpeed(speed: Double) {
        _navigationState.value = _navigationState.value.copy(speedKmH = speed)
    }

    fun togglePauseSimulation() {
        val current = _navigationState.value.isSimulationRunning
        _navigationState.value = _navigationState.value.copy(isSimulationRunning = !current)
    }

    // Scenario test presets
    fun applyScenarioRedLightStop() {
        _navigationState.value = _navigationState.value.copy(
            speedKmH = 0.0,
            secondsToNextManeuver = 45,
            currentStreet = "Feu Tricolore Bvd Mitterrand, Cocody",
            currentZone = "Abidjan Nord - Cocody",
            nextInstruction = "Continuez tout droit vers Saint-Jean dans 800m",
            secondsSinceTripEnded = null
        )
    }

    fun applyScenarioTrafficJamStop() {
        _navigationState.value = _navigationState.value.copy(
            speedKmH = 0.0,
            secondsToNextManeuver = 90,
            currentStreet = "Embouteillage Pont General de Gaulle, Treichville",
            currentZone = "Abidjan Sud - Treichville",
            nextInstruction = "Ralentissement important sur 1.2 km",
            secondsSinceTripEnded = null
        )
    }

    fun applyScenarioFastDriving() {
        _navigationState.value = _navigationState.value.copy(
            speedKmH = 65.0,
            secondsToNextManeuver = 40,
            currentStreet = "Voie Express Abidjan-Aéroport",
            currentZone = "Abidjan Sud - Port-Bouët",
            nextInstruction = "Restez sur la voie de gauche pendant 3 km",
            secondsSinceTripEnded = null
        )
    }

    fun applyScenarioImminentTurn() {
        _navigationState.value = _navigationState.value.copy(
            speedKmH = 0.0,
            secondsToNextManeuver = 10, // Failed Rule: <= 15s
            currentStreet = "Intersection Boulevard Latrille",
            currentZone = "Abidjan Nord - Cocody",
            nextInstruction = "Tournez immédiatement à gauche dans 10 secondes",
            secondsSinceTripEnded = null
        )
    }

    fun applyScenarioTripJustEnded() {
        _navigationState.value = _navigationState.value.copy(
            speedKmH = 0.0,
            secondsToNextManeuver = 120,
            currentStreet = "Arrivée Immeuble Pyramide, Plateau",
            currentZone = "Abidjan Plateau",
            nextInstruction = "Vous êtes arrivé à destination",
            secondsSinceTripEnded = 2 // Failed Rule: trip ended < 5s
        )
    }
}
