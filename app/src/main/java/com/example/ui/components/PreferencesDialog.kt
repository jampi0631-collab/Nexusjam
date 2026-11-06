package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.preferences.NexusUserPreferences
import com.example.ui.theme.NexusBorderBright
import com.example.ui.theme.NexusBorderCyan
import com.example.ui.theme.NexusCyan
import com.example.ui.theme.NexusCyanGlow
import com.example.ui.theme.NexusDeepMidnight
import com.example.ui.theme.NexusSurfaceDark
import com.example.ui.theme.NexusSurfaceElevated
import com.example.ui.theme.NexusTextCyan
import com.example.ui.theme.NexusTextMuted
import com.example.ui.theme.NexusTextPrimary
import com.example.ui.theme.NexusTextSecondary

@Composable
fun PreferencesDialog(
    currentPreferences: NexusUserPreferences,
    onSavePreferences: (NexusUserPreferences) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(currentPreferences.userTitle) }
    var autoSpeak by remember { mutableStateOf(currentPreferences.autoSpeak) }
    var speechRate by remember { mutableFloatStateOf(currentPreferences.speechRate) }
    var speechPitch by remember { mutableFloatStateOf(currentPreferences.speechPitch) }
    var highThinking by remember { mutableStateOf(currentPreferences.highThinking) }
    var selectedPreset by remember { mutableStateOf(currentPreferences.personalityPreset) }

    val presets = listOf(
        "Estratégico & Tático",
        "Analítico Científico",
        "Sintético & Direto",
        "Inovador & Visionário"
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(NexusDeepMidnight)
                .border(1.dp, NexusBorderCyan, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "CONFIGURAÇÕES DO NEXUS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NexusCyan,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Título do Usuário / Criador
            Text(
                text = "Como o NEXUS deve chamar você:",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = NexusTextSecondary
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                placeholder = { Text("Ex: chefe, comandante, capitão...", color = NexusTextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NexusCyan,
                    unfocusedBorderColor = NexusBorderCyan,
                    focusedTextColor = NexusTextPrimary,
                    unfocusedTextColor = NexusTextPrimary,
                    cursorColor = NexusCyan
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_title_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Modo Raciocínio Profundo (High Thinking)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Raciocínio Profundo",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = NexusTextPrimary
                        )
                    )
                    Text(
                        text = "Ativa inferência analítica estendida para tarefas complexas",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NexusTextMuted
                        )
                    )
                }

                Switch(
                    checked = highThinking,
                    onCheckedChange = { highThinking = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NexusCyan,
                        checkedTrackColor = NexusSurfaceElevated,
                        uncheckedThumbColor = NexusTextMuted,
                        uncheckedTrackColor = NexusSurfaceDark
                    ),
                    modifier = Modifier.testTag("high_thinking_switch")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Falar Respostas Automaticamente
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Voz Automática (TTS)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = NexusTextPrimary
                        )
                    )
                    Text(
                        text = "Sintetiza em voz alta em Português (pt-BR) ao receber resposta",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = NexusTextMuted
                        )
                    )
                }

                Switch(
                    checked = autoSpeak,
                    onCheckedChange = { autoSpeak = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NexusCyan,
                        checkedTrackColor = NexusSurfaceElevated,
                        uncheckedThumbColor = NexusTextMuted,
                        uncheckedTrackColor = NexusSurfaceDark
                    ),
                    modifier = Modifier.testTag("auto_speak_switch")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Velocidade de Fala (Speech Rate)
            Text(
                text = "Velocidade da Voz: ${(speechRate * 10).toInt() / 10f}x",
                style = MaterialTheme.typography.bodySmall.copy(color = NexusTextSecondary)
            )
            Slider(
                value = speechRate,
                onValueChange = { speechRate = it },
                valueRange = 0.7f..1.5f,
                steps = 7,
                colors = SliderDefaults.colors(
                    thumbColor = NexusCyan,
                    activeTrackColor = NexusBorderBright,
                    inactiveTrackColor = NexusSurfaceDark
                ),
                modifier = Modifier.testTag("speech_rate_slider")
            )

            // 5. Tom da Voz (Speech Pitch)
            Text(
                text = "Tom da Voz: ${(speechPitch * 10).toInt() / 10f}x",
                style = MaterialTheme.typography.bodySmall.copy(color = NexusTextSecondary)
            )
            Slider(
                value = speechPitch,
                onValueChange = { speechPitch = it },
                valueRange = 0.7f..1.3f,
                steps = 5,
                colors = SliderDefaults.colors(
                    thumbColor = NexusCyan,
                    activeTrackColor = NexusBorderBright,
                    inactiveTrackColor = NexusSurfaceDark
                ),
                modifier = Modifier.testTag("speech_pitch_slider")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Matriz de Personalidade
            Text(
                text = "Perfil Estratégico do NEXUS:",
                style = MaterialTheme.typography.labelMedium.copy(color = NexusTextSecondary)
            )
            Spacer(modifier = Modifier.height(6.dp))
            presets.forEach { preset ->
                val isSelected = selectedPreset == preset
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NexusSurfaceElevated else NexusSurfaceDark)
                        .border(
                            1.dp,
                            if (isSelected) NexusCyan else NexusBorderCyan,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedPreset = preset }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = preset,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) NexusCyanGlow else NexusTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        onSavePreferences(
                            currentPreferences.copy(
                                userTitle = if (title.isBlank()) "chefe" else title.trim(),
                                autoSpeak = autoSpeak,
                                speechRate = speechRate,
                                speechPitch = speechPitch,
                                highThinking = highThinking,
                                personalityPreset = selectedPreset
                            )
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_preferences_button")
                ) {
                    Text(
                        text = "Salvar Preferências",
                        fontWeight = FontWeight.Bold,
                        color = NexusDeepMidnight
                    )
                }
            }
        }
    }
}
