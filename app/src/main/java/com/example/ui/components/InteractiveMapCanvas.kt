package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gps.NavigationState

@Composable
fun InteractiveMapCanvas(
    navigationState: NavigationState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A), // Dark slate navy map canvas
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Background lagoon water area (Ébrié Lagoon Abidjan representation)
                val lagoonPath = Path().apply {
                    moveTo(0f, height * 0.45f)
                    cubicTo(
                        width * 0.25f, height * 0.40f,
                        width * 0.50f, height * 0.55f,
                        width, height * 0.48f
                    )
                    lineTo(width, height * 0.62f)
                    cubicTo(
                        width * 0.60f, height * 0.68f,
                        width * 0.30f, height * 0.52f,
                        0f, height * 0.60f
                    )
                    close()
                }
                drawPath(lagoonPath, color = Color(0xFF0284C7).copy(alpha = 0.25f))

                // Abidjan Grid Streets
                val roadGridColor = Color(0xFF334155)
                val highwayColor = Color(0xFF475569)

                // Secondary streets
                for (i in 1..4) {
                    val y = height * (i / 5f)
                    drawLine(
                        color = roadGridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 6f
                    )
                }
                for (i in 1..5) {
                    val x = width * (i / 6f)
                    drawLine(
                        color = roadGridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 6f
                    )
                }

                // Main Expressway (Boulevard VGE / Pont HKB)
                val hkbBridgePath = Path().apply {
                    moveTo(width * 0.15f, height * 0.15f)
                    lineTo(width * 0.45f, height * 0.45f)
                    lineTo(width * 0.75f, height * 0.85f)
                }
                drawPath(
                    hkbBridgePath,
                    color = highwayColor,
                    style = Stroke(width = 24f, cap = StrokeCap.Round)
                )

                // Active GPS Route Line (Cyan Neon Path)
                val routePath = Path().apply {
                    moveTo(width * 0.15f, height * 0.15f)
                    lineTo(width * 0.45f, height * 0.45f)
                    lineTo(width * 0.75f, height * 0.85f)
                }
                drawPath(
                    routePath,
                    color = Color(0xFF38BDF8),
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )
                drawPath(
                    routePath,
                    color = Color(0xFF0284C7),
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                    )
                )

                // POI Markers (Petroci Station, Ecobank, Hotel Ivoire)
                // Petroci Station Marcory
                drawCircle(
                    color = Color(0xFFF97316),
                    radius = 12f,
                    center = Offset(width * 0.35f, height * 0.32f)
                )
                // Ecobank Plateau
                drawCircle(
                    color = Color(0xFF10B981),
                    radius = 12f,
                    center = Offset(width * 0.20f, height * 0.22f)
                )
                // Orange Money Cocody
                drawCircle(
                    color = Color(0xFFF59E0B),
                    radius = 12f,
                    center = Offset(width * 0.65f, height * 0.70f)
                )

                // Vehicle Position (Center of route)
                val vehiclePos = Offset(width * 0.45f, height * 0.45f)

                // Halo Pulse
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.35f),
                    radius = pulseAnim,
                    center = vehiclePos
                )

                // Vehicle Marker Pin (VTC Vehicle Arrow)
                drawCircle(
                    color = Color(0xFF0284C7),
                    radius = 16f,
                    center = vehiclePos
                )

                val vehicleArrow = Path().apply {
                    moveTo(vehiclePos.x, vehiclePos.y - 12f)
                    lineTo(vehiclePos.x + 8f, vehiclePos.y + 8f)
                    lineTo(vehiclePos.x, vehiclePos.y + 4f)
                    lineTo(vehiclePos.x - 8f, vehiclePos.y + 8f)
                    close()
                }
                drawPath(vehicleArrow, color = Color.White)
            }

            // Map Overlays (Street Badge & Zone Label)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.90f)
            ) {
                Text(
                    text = "📍 ${navigationState.currentZone} • ${navigationState.currentStreet}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Speedometer Badge in Map Corner
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (navigationState.speedKmH == 0.0) Color(0xFF10B981) else Color(0xFF0284C7)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${navigationState.speedKmH.toInt()} km/h",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
