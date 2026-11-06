package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NexusUserPreferences(
    val userTitle: String = "chefe",
    val autoSpeak: Boolean = true,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val highThinking: Boolean = true,
    val hapticFeedback: Boolean = true,
    val personalityPreset: String = "Estratégico & Tático"
)

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nexus_core_preferences", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<NexusUserPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): NexusUserPreferences {
        return NexusUserPreferences(
            userTitle = prefs.getString(KEY_USER_TITLE, "chefe") ?: "chefe",
            autoSpeak = prefs.getBoolean(KEY_AUTO_SPEAK, true),
            speechRate = prefs.getFloat(KEY_SPEECH_RATE, 1.0f),
            speechPitch = prefs.getFloat(KEY_SPEECH_PITCH, 1.0f),
            highThinking = prefs.getBoolean(KEY_HIGH_THINKING, true),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC, true),
            personalityPreset = prefs.getString(KEY_PERSONALITY, "Estratégico & Tático") ?: "Estratégico & Tático"
        )
    }

    fun updateUserTitle(title: String) {
        val sanitized = if (title.isBlank()) "chefe" else title.trim()
        prefs.edit().putString(KEY_USER_TITLE, sanitized).apply()
        _preferences.value = _preferences.value.copy(userTitle = sanitized)
    }

    fun updateAutoSpeak(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SPEAK, enabled).apply()
        _preferences.value = _preferences.value.copy(autoSpeak = enabled)
    }

    fun updateSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 2.0f)
        prefs.edit().putFloat(KEY_SPEECH_RATE, clamped).apply()
        _preferences.value = _preferences.value.copy(speechRate = clamped)
    }

    fun updateSpeechPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.5f, 2.0f)
        prefs.edit().putFloat(KEY_SPEECH_PITCH, clamped).apply()
        _preferences.value = _preferences.value.copy(speechPitch = clamped)
    }

    fun updateHighThinking(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_THINKING, enabled).apply()
        _preferences.value = _preferences.value.copy(highThinking = enabled)
    }

    fun updatePersonalityPreset(preset: String) {
        prefs.edit().putString(KEY_PERSONALITY, preset).apply()
        _preferences.value = _preferences.value.copy(personalityPreset = preset)
    }

    companion object {
        private const val KEY_USER_TITLE = "user_title"
        private const val KEY_AUTO_SPEAK = "auto_speak"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_SPEECH_PITCH = "speech_pitch"
        private const val KEY_HIGH_THINKING = "high_thinking"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_PERSONALITY = "personality_preset"
    }
}
