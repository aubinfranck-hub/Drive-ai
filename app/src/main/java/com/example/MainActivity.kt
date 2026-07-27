package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GeminiVoiceDialog
import com.example.ui.screens.AdminPartnersScreen
import com.example.ui.screens.NavigationScreen
import com.example.ui.screens.VtcDriverScreen
import com.example.ui.theme.DriveAITheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DriveAITheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val navState by viewModel.navigationState.collectAsStateWithLifecycle()
    val adEvaluation by viewModel.adEvaluation.collectAsStateWithLifecycle()
    val activeAd by viewModel.activeAudioAd.collectAsStateWithLifecycle()
    val adCountdown by viewModel.adCountdownSeconds.collectAsStateWithLifecycle()
    val isDegradedNetwork by viewModel.isDegradedNetwork.collectAsStateWithLifecycle()
    val copilotState by viewModel.copilotState.collectAsStateWithLifecycle()

    val vtcTrips by viewModel.vtcTrips.collectAsStateWithLifecycle()
    val partnerCampaigns by viewModel.partnerCampaigns.collectAsStateWithLifecycle()
    val adLogs by viewModel.adLogs.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.GPS_DRIVE,
                    onClick = { viewModel.selectTab(AppTab.GPS_DRIVE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "GPS Navigation"
                        )
                    },
                    label = {
                        Text(
                            text = "GPS Drive",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.GPS_DRIVE) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF0B57D0),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.VTC_CHAUFFEUR,
                    onClick = { viewModel.selectTab(AppTab.VTC_CHAUFFEUR) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "VTC Driver Mode"
                        )
                    },
                    label = {
                        Text(
                            text = "Espace VTC",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.VTC_CHAUFFEUR) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFFF97316),
                        indicatorColor = Color(0xFFEA580C),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.ADMIN_PARTENAIRES,
                    onClick = { viewModel.selectTab(AppTab.ADMIN_PARTENAIRES) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Admin & Partenaires"
                        )
                    },
                    label = {
                        Text(
                            text = "Admin & Pubs",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.ADMIN_PARTENAIRES) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFF10B981),
                        indicatorColor = Color(0xFF047857),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.GPS_DRIVE -> {
                    NavigationScreen(
                        navigationState = navState,
                        adEvaluation = adEvaluation,
                        activeAudioAd = activeAd,
                        adCountdownSeconds = adCountdown,
                        isDegradedNetwork = isDegradedNetwork,
                        onToggleNetworkDegraded = { viewModel.toggleDegradedNetwork() },
                        onSetSpeed = { viewModel.gpsSimulator.setSpeed(it) },
                        onScenarioRedLight = { viewModel.gpsSimulator.applyScenarioRedLightStop() },
                        onScenarioTrafficJam = { viewModel.gpsSimulator.applyScenarioTrafficJamStop() },
                        onScenarioFastDriving = { viewModel.gpsSimulator.applyScenarioFastDriving() },
                        onScenarioImminentTurn = { viewModel.gpsSimulator.applyScenarioImminentTurn() },
                        onTriggerAdManual = { viewModel.triggerAudioAdManualOrAuto() },
                        onDismissAd = { viewModel.dismissAudioAd() },
                        onOpenGeminiMic = { viewModel.toggleVoiceCopilotListening() }
                    )
                }

                AppTab.VTC_CHAUFFEUR -> {
                    VtcDriverScreen(
                        trips = vtcTrips,
                        onAddTrip = { passenger, pickup, dropoff, dist, fare ->
                            viewModel.addVtcTrip(passenger, pickup, dropoff, dist, fare)
                        },
                        onUpdateStatus = { id, newStatus ->
                            viewModel.updateTripStatus(id, newStatus)
                        }
                    )
                }

                AppTab.ADMIN_PARTENAIRES -> {
                    AdminPartnersScreen(
                        campaigns = partnerCampaigns,
                        adLogs = adLogs
                    )
                }
            }

            // Gemini Voice Copilot Bottom Sheet Overlay
            GeminiVoiceDialog(
                copilotState = copilotState,
                isDegradedNetwork = isDegradedNetwork,
                onDismiss = { viewModel.dismissVoiceOverlay() },
                onSendQuery = { viewModel.sendCopilotQuery(it) }
            )
        }
    }
}
