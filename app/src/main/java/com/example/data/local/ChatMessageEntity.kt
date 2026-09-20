package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val sender: String, // "USER" or "BOT"
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val prompt: String? = null,
    val negativePrompt: String? = null,
    val model: String? = null,
    val seed: Long? = null,
    val steps: Int? = null,
    val cfgScale: Float? = null,
    val aspectRatio: String? = null,
    val generationTimeMs: Long? = null,
    val status: String = "SUCCESS", // "SENT", "GENERATING", "SUCCESS", "ERROR"
    val errorMessage: String? = null
)
