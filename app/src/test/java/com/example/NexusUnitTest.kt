package com.example

import com.example.data.local.ChatMessageEntity
import com.example.data.preferences.NexusUserPreferences
import com.example.data.repository.NexusAiRepositoryImpl
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NexusUnitTest {

    @Test
    fun testChatMessageEntityCreation() {
        val message = ChatMessageEntity(
            sender = "chefe",
            text = "Relatório de status",
            status = "SENT"
        )
        assertEquals("chefe", message.sender)
        assertEquals("Relatório de status", message.text)
        assertEquals("SENT", message.status)
        assertTrue(message.timestamp > 0)
    }

    @Test
    fun testPreferencesDefaults() {
        val prefs = NexusUserPreferences()
        assertEquals("chefe", prefs.userTitle)
        assertTrue(prefs.autoSpeak)
        assertTrue(prefs.highThinking)
        assertEquals(1.0f, prefs.speechRate)
        assertEquals(1.0f, prefs.speechPitch)
    }

    @Test
    fun testNexusAiRepositoryResponseCrafting() = runBlocking {
        val repo = NexusAiRepositoryImpl()
        val prefs = NexusUserPreferences(userTitle = "chefe")
        val chunks = repo.generateNexusResponse("Status do sistema", emptyList(), prefs).toList()
        
        assertTrue(chunks.isNotEmpty())
        val finalResponse = chunks.last()
        assertTrue(finalResponse.contains("chefe"))
        assertTrue(finalResponse.contains("NEXUS"))
    }
}
