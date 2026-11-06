package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.NexusViewModel
import com.example.ui.components.ChatMessageBubble
import com.example.ui.components.ClearHistoryDialog
import com.example.ui.components.CoreState
import com.example.ui.components.EnergyCore
import com.example.ui.components.FirebaseNoticeBanner
import com.example.ui.components.HudHeader
import com.example.ui.components.PreferencesDialog
import com.example.ui.components.QuickActionRow
import com.example.ui.theme.NexusBorderBright
import com.example.ui.theme.NexusBorderCyan
import com.example.ui.theme.NexusCyan
import com.example.ui.theme.NexusCyanGlow
import com.example.ui.theme.NexusDeepMidnight
import com.example.ui.theme.NexusEmeraldOnline
import com.example.ui.theme.NexusIndigoQuantum
import com.example.ui.theme.NexusSurfaceDark
import com.example.ui.theme.NexusSurfaceElevated
import com.example.ui.theme.NexusTextCyan
import com.example.ui.theme.NexusTextMuted
import com.example.ui.theme.NexusTextPrimary
import com.example.ui.theme.NexusTextSecondary
import com.example.ui.theme.NexusVoidBlack

@Composable
fun NexusMainScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val insets = WindowInsets.systemBars.asPaddingValues()

    val messages by viewModel.messages.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val coreState by viewModel.coreState.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val thinkingStatus by viewModel.thinkingStatus.collectAsState()
    val showClearDialog by viewModel.showClearDialog.collectAsState()
    val showPreferencesDialog by viewModel.showPreferencesDialog.collectAsState()

    val listState = rememberLazyListState()

    // Auto-scroll to latest message on update
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Audio Permission Launcher for Speech Recognition
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput { errorMsg ->
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                context,
                "Permissão de microfone necessária para comandos por voz.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(NexusVoidBlack),
        contentColor = NexusTextPrimary,
        containerColor = NexusVoidBlack
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            val isTablet = maxWidth >= 680.dp

            if (isTablet) {
                // TABLET DUAL-PANE COMMAND BRIDGE LAYOUT
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Left Pane: Holographic Energy Core & Telemetry Deck
                    TabletTelemetryPane(
                        state = coreState,
                        audioRms = audioRms,
                        userTitle = preferences.userTitle,
                        isSpeaking = isSpeaking,
                        isListening = isListening,
                        onStopSpeaking = { viewModel.stopSpeaking() },
                        modifier = Modifier
                            .width(340.dp)
                            .fillMaxHeight()
                    )

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(NexusBorderCyan)
                    )

                    // Right Pane: Main Terminal Chat Stream & Command Console
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        HudHeader(
                            state = coreState,
                            isSpeaking = isSpeaking,
                            autoSpeakEnabled = preferences.autoSpeak,
                            onToggleAutoSpeak = { viewModel.toggleAutoSpeak() },
                            onOpenPreferences = { viewModel.openPreferencesDialog() },
                            onOpenClearHistory = { viewModel.openClearDialog() }
                        )

                        FirebaseNoticeBanner()

                        // Chat Messages
                        ChatLogSection(
                            messages = messages,
                            userTitle = preferences.userTitle,
                            isSpeaking = isSpeaking,
                            isThinking = isThinking,
                            thinkingStatus = thinkingStatus,
                            listState = listState,
                            onSpeakMessage = { viewModel.speakMessage(it) },
                            onStopSpeak = { viewModel.stopSpeaking() },
                            modifier = Modifier.weight(1f)
                        )

                        QuickActionRow(
                            onActionSelected = { viewModel.sendMessage(it) }
                        )

                        CommandInputBar(
                            inputText = inputText,
                            onInputChanged = { viewModel.onInputTextChanged(it) },
                            onSendMessage = { viewModel.sendMessage() },
                            isListening = isListening,
                            onToggleMic = {
                                if (isListening) {
                                    viewModel.stopVoiceInput()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.startVoiceInput { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                // PHONE SINGLE-COLUMN RESPONSIVE LAYOUT
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HudHeader(
                        state = coreState,
                        isSpeaking = isSpeaking,
                        autoSpeakEnabled = preferences.autoSpeak,
                        onToggleAutoSpeak = { viewModel.toggleAutoSpeak() },
                        onOpenPreferences = { viewModel.openPreferencesDialog() },
                        onOpenClearHistory = { viewModel.openClearDialog() }
                    )

                    // Collapsible Phone Energy Core Hero Section
                    PhoneEnergyCoreSection(
                        state = coreState,
                        audioRms = audioRms,
                        isSpeaking = isSpeaking,
                        onStopSpeaking = { viewModel.stopSpeaking() }
                    )

                    FirebaseNoticeBanner()

                    // Chat Log Section
                    ChatLogSection(
                        messages = messages,
                        userTitle = preferences.userTitle,
                        isSpeaking = isSpeaking,
                        isThinking = isThinking,
                        thinkingStatus = thinkingStatus,
                        listState = listState,
                        onSpeakMessage = { viewModel.speakMessage(it) },
                        onStopSpeak = { viewModel.stopSpeaking() },
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionRow(
                        onActionSelected = { viewModel.sendMessage(it) }
                    )

                    CommandInputBar(
                        inputText = inputText,
                        onInputChanged = { viewModel.onInputTextChanged(it) },
                        onSendMessage = { viewModel.sendMessage() },
                        isListening = isListening,
                        onToggleMic = {
                            if (isListening) {
                                viewModel.stopVoiceInput()
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    viewModel.startVoiceInput { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                    )
                }
            }
        }

        // Clear History Confirmation Dialog
        if (showClearDialog) {
            ClearHistoryDialog(
                onConfirm = { viewModel.clearHistory() },
                onDismiss = { viewModel.closeClearDialog() }
            )
        }

        // Settings / Preferences Dialog
        if (showPreferencesDialog) {
            PreferencesDialog(
                currentPreferences = preferences,
                onSavePreferences = { viewModel.savePreferences(it) },
                onDismiss = { viewModel.closePreferencesDialog() }
            )
        }
    }
}

@Composable
private fun TabletTelemetryPane(
    state: CoreState,
    audioRms: Float,
    userTitle: String,
    isSpeaking: Boolean,
    isListening: Boolean,
    onStopSpeaking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(NexusDeepMidnight)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NÚCLEO PRINCIPAL",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NexusCyan,
                    letterSpacing = 2.sp
                )
            )

            Text(
                text = "MATRIZ HOLOGRÁFICA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = NexusTextMuted,
                    fontSize = 10.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Large Energy Core
            EnergyCore(
                state = state,
                size = 220.dp,
                audioRms = audioRms
            )

            Spacer(modifier = Modifier.height(20.dp))

            // State Display Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NexusSurfaceDark)
                    .border(1.dp, NexusBorderCyan, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = when (state) {
                        CoreState.IDLE -> "ESTADO: PRONTO // $userTitle.uppercase()"
                        CoreState.LISTENING -> "ESTADO: CAPTANDO SINAL DE ÁUDIO"
                        CoreState.THINKING -> "ESTADO: MATRIZ QUANTICA SINTETIZANDO"
                        CoreState.SPEAKING -> "ESTADO: SÍNTESE VOCAL ATIVA"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = when (state) {
                            CoreState.IDLE -> NexusCyan
                            CoreState.LISTENING -> Color(0xFF00FFC4)
                            CoreState.THINKING -> NexusIndigoQuantum
                            CoreState.SPEAKING -> NexusCyanGlow
                        }
                    )
                )
            }
        }

        // Diagnostics Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NexusSurfaceDark)
                .border(1.dp, NexusBorderCyan, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "TELEMETRIA DE BORDO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NexusCyanGlow
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            TelemetryRow(label = "Frequência Neural:", value = "432.0 THz")
            TelemetryRow(label = "Estabilidade do Fluxo:", value = "99.85%")
            TelemetryRow(label = "Protocolo de Voz:", value = "pt-BR (Ativo)")
            TelemetryRow(label = "Vetor Primário:", value = userTitle)

            if (isSpeaking) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NexusSurfaceElevated)
                        .clickable { onStopSpeaking() }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Parar voz",
                            tint = NexusCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Interromper Fala",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NexusCyan
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = NexusTextMuted,
                fontSize = 11.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = NexusTextCyan,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun PhoneEnergyCoreSection(
    state: CoreState,
    audioRms: Float,
    isSpeaking: Boolean,
    onStopSpeaking: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EnergyCore(
            state = state,
            size = if (expanded) 170.dp else 100.dp,
            audioRms = audioRms,
            onClick = { expanded = !expanded }
        )

        AnimatedVisibility(visible = isSpeaking) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NexusSurfaceDark)
                    .clickable { onStopSpeaking() }
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Interromper fala",
                        tint = NexusCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Voz Ativa • Toque para parar",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NexusCyan,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatLogSection(
    messages: List<com.example.data.local.ChatMessageEntity>,
    userTitle: String,
    isSpeaking: Boolean,
    isThinking: Boolean,
    thinkingStatus: String,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onSpeakMessage: (String) -> Unit,
    onStopSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    userTitle = userTitle,
                    isSpeakingThisMessage = isSpeaking,
                    onSpeakMessage = onSpeakMessage,
                    onStopSpeak = onStopSpeak
                )
            }

            // Visual thinking indicator at end of list
            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .testTag("thinking_spinner"),
                            color = NexusCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = thinkingStatus,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = NexusCyanGlow,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommandInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    isListening: Boolean,
    onToggleMic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(NexusDeepMidnight)
            .border(
                1.dp,
                NexusBorderCyan,
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Command Mic Button
            IconButton(
                onClick = onToggleMic,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isListening) Color(0xFF00FFC4).copy(alpha = 0.2f) else NexusSurfaceDark)
                    .border(
                        1.dp,
                        if (isListening) Color(0xFF00FFC4) else NexusBorderCyan,
                        CircleShape
                    )
                    .testTag("voice_input_button")
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isListening) "Parar de escutar áudio" else "Iniciar comando por voz",
                    tint = if (isListening) Color(0xFF00FFC4) else NexusCyanGlow,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input Field
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                placeholder = {
                    Text(
                        text = if (isListening) "Escutando comando..." else "Digite ou fale uma diretriz...",
                        color = NexusTextMuted,
                        fontSize = 14.sp
                    )
                },
                singleLine = false,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NexusCyan,
                    unfocusedBorderColor = NexusBorderCyan,
                    focusedTextColor = NexusTextPrimary,
                    unfocusedTextColor = NexusTextPrimary,
                    cursorColor = NexusCyan,
                    focusedContainerColor = NexusSurfaceDark,
                    unfocusedContainerColor = NexusSurfaceDark
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = onSendMessage,
                enabled = inputText.isNotBlank(),
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) NexusCyan else NexusSurfaceDark)
                    .testTag("send_message_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar mensagem de texto",
                    tint = if (inputText.isNotBlank()) NexusDeepMidnight else NexusTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
