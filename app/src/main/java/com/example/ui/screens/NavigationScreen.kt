package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gps.NavigationState
import com.example.data.local.PartnerCampaignEntity
import com.example.data.rules.AdConditionRule
import com.example.data.rules.AdEvaluationResult
import com.example.ui.components.AdAudioPlayerOverlay
import com.example.ui.components.InteractiveMapCanvas

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NavigationScreen(
    navigationState: NavigationState,
    adEvaluation: AdEvaluationResult,
    activeAudioAd: PartnerCampaignEntity?,
    adCountdownSeconds: Int,
    isDegradedNetwork: Boolean,
    onToggleNetworkDegraded: () -> Unit,
    onSetSpeed: (Double) -> Unit,
    onScenarioRedLight: () -> Unit,
    onScenarioTrafficJam: () -> Unit,
    onScenarioFastDriving: () -> Unit,
    onScenarioImminentTurn: () -> Unit,
    onTriggerAdManual: () -> Unit,
    onDismissAd: () -> Unit,
    onOpenGeminiMic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Turn-By-Turn Banner (High Density Header Style)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0B57D0),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TurnRight,
                                contentDescription = "Next Turn",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = navigationState.nextInstruction,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )
                        Text(
                            text = "Axe principal • ${navigationState.currentStreet}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${navigationState.distanceRemainingKm} km",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF97316)
                        ) {
                            Text(
                                text = "${navigationState.etaMinutes} min",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Canvas Map View
            InteractiveMapCanvas(
                navigationState = navigationState,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Network Mode & Speed Simulator Control Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SignalCellularAlt,
                                contentDescription = "Network Mode",
                                tint = if (isDegradedNetwork) Color(0xFFEF4444) else Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDegradedNetwork) "Mode 2G/3G Dégradé (Hors-ligne Gemini)" else "Réseau 4G/5G Actif (Gemini Live)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Switch(
                            checked = isDegradedNetwork,
                            onCheckedChange = { onToggleNetworkDegraded() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEF4444),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF10B981)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Speed Control Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Speed",
                                tint = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Simulateur Vitesse : ",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${navigationState.speedKmH.toInt()} km/h",
                                color = if (navigationState.speedKmH == 0.0) Color(0xFF10B981) else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row {
                            OutlinedButton(
                                onClick = { onSetSpeed(0.0) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("0 km/h (Arrêt)", fontSize = 10.sp, color = Color(0xFF10B981))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = { onSetSpeed(50.0) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("50 km/h", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }

                    Slider(
                        value = navigationState.speedKmH.toFloat(),
                        onValueChange = { onSetSpeed(it.toDouble()) },
                        valueRange = 0f..90f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF0284C7),
                            inactiveTrackColor = Color(0xFF334155)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ad Engine V1 Strict Rules Inspector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MOTEUR PUB AUDIO (RÈGLES V1)",
                            color = Color(0xFFF97316),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (adEvaluation.canTrigger) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (adEvaluation.canTrigger) "PRET À DIFFUSER" else "PUB BLOQUÉE",
                                color = if (adEvaluation.canTrigger) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rules Checklist
                    val rules = listOf(
                        AdConditionRule.SPEED_ZERO to (navigationState.speedKmH == 0.0),
                        AdConditionRule.NO_CRITICAL_MANEUVER to (navigationState.secondsToNextManeuver > 15),
                        AdConditionRule.TIME_INTERVAL_PASSED to !adEvaluation.failedRules.contains(AdConditionRule.TIME_INTERVAL_PASSED),
                        AdConditionRule.TRIP_NOT_JUST_ENDED to !adEvaluation.failedRules.contains(AdConditionRule.TRIP_NOT_JUST_ENDED)
                    )

                    rules.forEach { (rule, isPassed) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isPassed) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = rule.description,
                                color = if (isPassed) Color.White else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = adEvaluation.explanationFr,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    if (adEvaluation.canTrigger) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onTriggerAdManual,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Ad",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Déclencher la pub audio (Arrêt 0 km/h)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Test Scenario Trigger Buttons
            Text(
                text = "Scénarios de test rapide moteur pub :",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onScenarioRedLight,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🟢 Arrêt Feu Rouge (0 km/h)", fontSize = 11.sp)
                }

                Button(
                    onClick = onScenarioTrafficJam,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("⏳ Embouteillage Prolongé", fontSize = 11.sp)
                }

                Button(
                    onClick = onScenarioFastDriving,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🚘 Conduite 60 km/h (Bloqué)", fontSize = 11.sp)
                }

                Button(
                    onClick = onScenarioImminentTurn,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("⚠️ Virage 10s (Bloqué)", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
        }

        // Active Audio Ad Banner Overlay
        AdAudioPlayerOverlay(
            activeAd = activeAudioAd,
            countdownSeconds = adCountdownSeconds,
            onDismiss = onDismissAd,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

        // Gemini Live Voice Mic Floating Action Button (High Density Design)
        FloatingActionButton(
            onClick = onOpenGeminiMic,
            containerColor = Color(0xFF0B57D0),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Gemini Live Mic",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}
