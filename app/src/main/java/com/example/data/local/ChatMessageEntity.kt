package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "chefe" or "nexus"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStrategicAnalysis: Boolean = false,
    val status: String = "SENT" // "SENT", "DELIVERED", "THINKING", "ERROR"
)
