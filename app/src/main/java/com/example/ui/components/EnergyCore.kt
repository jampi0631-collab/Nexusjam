package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NexusBlueNeon
import com.example.ui.theme.NexusCyan
import com.example.ui.theme.NexusCyanGlow
import com.example.ui.theme.NexusIndigoQuantum
import kotlin.math.cos
import kotlin.math.sin

enum class CoreState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun EnergyCore(
    state: CoreState,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    audioRms: Float = 0f,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_anim")

    // Dynamic rotation speeds based on state
    val rotationDuration = when (state) {
        CoreState.THINKING -> 2400
        CoreState.LISTENING -> 4000
        CoreState.SPEAKING -> 3200
        CoreState.IDLE -> 8000
    }

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(rotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween((rotationDuration * 1.5).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    // Pulse breathing animation
    val pulseDuration = when (state) {
        CoreState.THINKING -> 600
        CoreState.LISTENING -> 800
        CoreState.SPEAKING -> 700
        CoreState.IDLE -> 1800
    }

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state == CoreState.THINKING) 800 else 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_alpha"
    )

    val waveRadiusScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (state == CoreState.THINKING) 800 else 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_radius"
    )

    val primaryGlow = when (state) {
        CoreState.THINKING -> NexusIndigoQuantum
        CoreState.LISTENING -> Color(0xFF00FFC4)
        CoreState.SPEAKING -> NexusBlueNeon
        CoreState.IDLE -> NexusCyan
    }

    val secondaryGlow = when (state) {
        CoreState.THINKING -> NexusCyan
        CoreState.LISTENING -> NexusCyanGlow
        CoreState.SPEAKING -> NexusCyan
        CoreState.IDLE -> NexusCyanGlow
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f * 0.85f

            // 1. Expanding energetic pulse wave
            val dynamicWaveScale = if (state == CoreState.LISTENING) {
                waveRadiusScale * (1f + audioRms * 0.3f)
            } else {
                waveRadiusScale
            }
            drawCircle(
                color = primaryGlow.copy(alpha = waveAlpha * 0.5f),
                radius = baseRadius * dynamicWaveScale,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 2. Central radiant energy orb (Radial Gradient)
            val corePulseRadius = baseRadius * 0.36f * (if (state == CoreState.LISTENING) pulseScale * (1f + audioRms * 0.2f) else pulseScale)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryGlow.copy(alpha = 0.9f),
                        secondaryGlow.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = corePulseRadius * 1.8f
                ),
                radius = corePulseRadius * 1.8f,
                center = center
            )

            // 3. Middle Gyroscopic Ring with Orbital Nodes (Counter-rotating)
            rotate(degrees = counterRotationAngle, pivot = center) {
                val midRadius = baseRadius * 0.65f
                drawCircle(
                    color = secondaryGlow.copy(alpha = 0.6f),
                    radius = midRadius,
                    center = center,
                    style = Stroke(
                        width = 1.8.dp.toPx()
                    )
                )

                // 4 orbital quantum nodes
                for (i in 0 until 4) {
                    val angle = Math.toRadians((i * 90.0))
                    val nodeX = center.x + (midRadius * cos(angle)).toFloat()
                    val nodeY = center.y + (midRadius * sin(angle)).toFloat()
                    drawCircle(
                        color = Color.White,
                        radius = 3.5.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                    drawCircle(
                        color = primaryGlow,
                        radius = 6.dp.toPx(),
                        center = Offset(nodeX, nodeY),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            // 4. Outer Segmented Tech Ring with Arc Slots & Ticks (Rotating)
            rotate(degrees = rotationAngle, pivot = center) {
                val outerRadius = baseRadius * 0.92f

                // Segmented arcs
                val arcSegments = 6
                val sweep = 42f
                for (i in 0 until arcSegments) {
                    val startAngle = i * 60f + 9f
                    drawArc(
                        color = primaryGlow.copy(alpha = 0.85f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Precision telemetry tick marks
                val tickCount = 24
                for (i in 0 until tickCount) {
                    val angle = Math.toRadians((i * (360.0 / tickCount)))
                    val innerTickR = outerRadius - (if (i % 3 == 0) 8.dp.toPx() else 4.dp.toPx())
                    val outerTickR = outerRadius + 2.dp.toPx()
                    val startX = center.x + (innerTickR * cos(angle)).toFloat()
                    val startY = center.y + (innerTickR * sin(angle)).toFloat()
                    val endX = center.x + (outerTickR * cos(angle)).toFloat()
                    val endY = center.y + (outerTickR * sin(angle)).toFloat()

                    drawLine(
                        color = secondaryGlow.copy(alpha = if (i % 3 == 0) 0.8f else 0.35f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (i % 3 == 0) 2.dp.toPx() else 1.dp.toPx()
                    )
                }
            }

            // 5. Innermost reactor ring
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = baseRadius * 0.22f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
