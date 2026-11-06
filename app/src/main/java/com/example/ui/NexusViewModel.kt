package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.local.NexusDatabase
import com.example.data.preferences.NexusUserPreferences
import com.example.data.preferences.PreferencesManager
import com.example.data.repository.NexusAiRepository
import com.example.data.repository.NexusAiRepositoryImpl
import com.example.speech.SpeechManager
import com.example.ui.components.CoreState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NexusViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NexusDatabase.getInstance(application)
    private val chatDao = database.chatDao()
    private val preferencesManager = PreferencesManager(application)
    private val speechManager = SpeechManager(application)
    private val aiRepository: NexusAiRepository = NexusAiRepositoryImpl()

    val preferences: StateFlow<NexusUserPreferences> = preferencesManager.preferences

    val messages: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSpeaking: StateFlow<Boolean> = speechManager.isSpeaking
    val isListening: StateFlow<Boolean> = speechManager.isListening
    val audioRms: StateFlow<Float> = speechManager.audioRms

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _thinkingStatus = MutableStateFlow("Sincronizando matriz quântica...")
    val thinkingStatus: StateFlow<String> = _thinkingStatus.asStateFlow()

    private val _coreState = MutableStateFlow(CoreState.IDLE)
    val coreState: StateFlow<CoreState> = _coreState.asStateFlow()

    private val _showClearDialog = MutableStateFlow(false)
    val showClearDialog: StateFlow<Boolean> = _showClearDialog.asStateFlow()

    private val _showPreferencesDialog = MutableStateFlow(false)
    val showPreferencesDialog: StateFlow<Boolean> = _showPreferencesDialog.asStateFlow()

    private var activeGenerationJob: Job? = null

    init {
        // Monitor speaking and listening states to sync CoreState
        viewModelScope.launch {
            isListening.collect { listening ->
                if (listening) {
                    _coreState.value = CoreState.LISTENING
                } else if (!_isThinking.value && !isSpeaking.value) {
                    _coreState.value = CoreState.IDLE
                }
            }
        }

        viewModelScope.launch {
            isSpeaking.collect { speaking ->
                if (speaking) {
                    _coreState.value = CoreState.SPEAKING
                } else if (!_isThinking.value && !isListening.value) {
                    _coreState.value = CoreState.IDLE
                }
            }
        }

        // Seed initial greeting if database is empty
        viewModelScope.launch {
            if (chatDao.getMessageCount() == 0) {
                val title = preferences.value.userTitle
                chatDao.insertMessage(
                    ChatMessageEntity(
                        sender = "nexus",
                        text = "E aí, $title! O NEXUS tá 100% online e pronto. Em que posso te ajudar hoje?",
                        status = "DELIVERED"
                    )
                )
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage(customPrompt: String? = null) {
        val promptToSend = (customPrompt ?: _inputText.value).trim()
        if (promptToSend.isBlank() || _isThinking.value) return

        _inputText.value = ""
        speechManager.stopSpeaking()

        viewModelScope.launch {
            val title = preferences.value.userTitle
            // 1. Insert user message into Room
            chatDao.insertMessage(
                ChatMessageEntity(
                    sender = title,
                    text = promptToSend,
                    status = "SENT"
                )
            )

            // 2. Set thinking state
            _isThinking.value = true
            _coreState.value = CoreState.THINKING
            _thinkingStatus.value = if (preferences.value.highThinking) {
                "Pensando detalhadamente..."
            } else {
                "Digitando resposta..."
            }

            // 3. Create placeholder for NEXUS response in Room
            val placeholderId = chatDao.insertMessage(
                ChatMessageEntity(
                    sender = "nexus",
                    text = "...",
                    status = "THINKING"
                )
            )

            // 4. Stream response from repository
            activeGenerationJob?.cancel()
            activeGenerationJob = viewModelScope.launch {
                try {
                    var accumulatedText = ""
                    aiRepository.generateNexusResponse(
                        prompt = promptToSend,
                        history = messages.value,
                        preferences = preferences.value
                    ).collect { chunk ->
                        accumulatedText = chunk
                        chatDao.updateMessage(
                            ChatMessageEntity(
                                id = placeholderId,
                                sender = "nexus",
                                text = accumulatedText,
                                status = "DELIVERED"
                            )
                        )
                    }

                    _isThinking.value = false
                    _coreState.value = CoreState.IDLE

                    // Auto-speak if enabled
                    if (preferences.value.autoSpeak && accumulatedText.isNotBlank()) {
                        speechManager.speak(
                            text = accumulatedText,
                            rate = preferences.value.speechRate,
                            pitch = preferences.value.speechPitch
                        )
                    }
                } catch (e: Exception) {
                    _isThinking.value = false
                    _coreState.value = CoreState.IDLE
                    chatDao.updateMessage(
                        ChatMessageEntity(
                            id = placeholderId,
                            sender = "nexus",
                            text = "Erro no fluxo neural: ${e.localizedMessage ?: "Falha ao sintetizar dados."}",
                            status = "ERROR"
                        )
                    )
                }
            }
        }
    }

    fun startVoiceInput(onVoiceError: (String) -> Unit) {
        speechManager.startListening(
            onResult = { recognizedText ->
                _inputText.value = recognizedText
                sendMessage(recognizedText)
            },
            onError = { errorMsg ->
                _coreState.value = CoreState.IDLE
                onVoiceError(errorMsg)
            }
        )
    }

    fun stopVoiceInput() {
        speechManager.stopListening()
        if (!_isThinking.value && !isSpeaking.value) {
            _coreState.value = CoreState.IDLE
        }
    }

    fun speakMessage(text: String) {
        speechManager.speak(
            text = text,
            rate = preferences.value.speechRate,
            pitch = preferences.value.speechPitch
        )
    }

    fun stopSpeaking() {
        speechManager.stopSpeaking()
    }

    fun toggleAutoSpeak() {
        preferencesManager.updateAutoSpeak(!preferences.value.autoSpeak)
    }

    fun openClearDialog() {
        _showClearDialog.value = true
    }

    fun closeClearDialog() {
        _showClearDialog.value = false
    }

    fun clearHistory() {
        viewModelScope.launch {
            speechManager.stopSpeaking()
            chatDao.clearHistory()
            val title = preferences.value.userTitle
            chatDao.insertMessage(
                ChatMessageEntity(
                    sender = "nexus",
                    text = "Histórico limpo com sucesso, $title! Pode mandar sua próxima dúvida ou assunto.",
                    status = "DELIVERED"
                )
            )
            _showClearDialog.value = false
        }
    }

    fun openPreferencesDialog() {
        _showPreferencesDialog.value = true
    }

    fun closePreferencesDialog() {
        _showPreferencesDialog.value = false
    }

    fun savePreferences(newPrefs: NexusUserPreferences) {
        preferencesManager.updateUserTitle(newPrefs.userTitle)
        preferencesManager.updateAutoSpeak(newPrefs.autoSpeak)
        preferencesManager.updateSpeechRate(newPrefs.speechRate)
        preferencesManager.updateSpeechPitch(newPrefs.speechPitch)
        preferencesManager.updateHighThinking(newPrefs.highThinking)
        preferencesManager.updatePersonalityPreset(newPrefs.personalityPreset)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.release()
    }
}
