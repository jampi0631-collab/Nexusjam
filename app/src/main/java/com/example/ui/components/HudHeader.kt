package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexusAmberAlert
import com.example.ui.theme.NexusBlueNeon
import com.example.ui.theme.NexusBorderBright
import com.example.ui.theme.NexusBorderCyan
import com.example.ui.theme.NexusCyan
import com.example.ui.theme.NexusCyanGlow
import com.example.ui.theme.NexusEmeraldOnline
import com.example.ui.theme.NexusIndigoQuantum
import com.example.ui.theme.NexusSurfaceDark
import com.example.ui.theme.NexusTextMuted
import com.example.ui.theme.NexusTextSecondary

@Composable
fun HudHeader(
    state: CoreState,
    isSpeaking: Boolean,
    autoSpeakEnabled: Boolean,
    onToggleAutoSpeak: () -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when (state) {
            CoreState.IDLE -> NexusEmeraldOnline
            CoreState.LISTENING -> NexusCyan
            CoreState.THINKING -> NexusIndigoQuantum
            CoreState.SPEAKING -> NexusBlueNeon
        },
        label = "status_color"
    )

    val statusText = when (state) {
        CoreState.IDLE -> "CORE // ONLINE"
        CoreState.LISTENING -> "AUDIO // ESCUTANDO"
        CoreState.THINKING -> "SÍNTESE // PENSANDO"
        CoreState.SPEAKING -> "VOZ // TRANSMITINDO"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(NexusSurfaceDark.copy(alpha = 0.85f))
            .border(
                width = 1.dp,
                color = NexusBorderCyan,
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Identity & Status Telemetry
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NEXUS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = NexusCyan
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI // v3.8",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = NexusTextMuted
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // HUD Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Auto-speak audio toggle
                IconButton(
                    onClick = onToggleAutoSpeak,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("toggle_speech_button")
                ) {
                    Icon(
                        imageVector = if (autoSpeakEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Alternar reprodução de áudio",
                        tint = if (autoSpeakEnabled) NexusCyanGlow else NexusTextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Clear history button
                IconButton(
                    onClick = onOpenClearHistory,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Limpar histórico do terminal",
                        tint = NexusTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Settings button
                IconButton(
                    onClick = onOpenPreferences,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configurações e preferências do NEXUS",
                        tint = NexusBorderBright,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
