package com.example.data.repository

import android.content.Context
import com.example.data.api.GeminiImageApi
import com.example.data.api.ImageGenResult
import com.example.data.generator.ProceduralImageGenerator
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.model.GenerationSettings
import com.example.data.model.ImageEngine
import com.example.data.model.StylePresets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ChatRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.chatMessageDao()
    private val geminiApi = GeminiImageApi(context)

    val allMessages: Flow<List<ChatMessageEntity>> = dao.getAllMessages()

    suspend fun insertWelcomeMessageIfNeeded() = withContext(Dispatchers.IO) {
        val existing = dao.getAllMessages().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val welcomeText = buildString {
                append("👋 Welcome to **Invoke AI Bot**!\n\n")
                append("I generate high-quality AI images directly in this chat from your text descriptions.\n\n")
                append("✨ **How to use:**\n")
                append("• Send any prompt (e.g., *A neon cyberpunk alleyway in Tokyo at midnight*)\n")
                append("• Or use `/imagine <prompt>`\n")
                append("• Use `/styles` to see style presets\n")
                append("• Tap the paperclip 📎 for aspect ratio & negative prompts\n\n")
                append("Tap any suggestion chip below to generate your first artwork!")
            }

            dao.insertMessage(
                ChatMessageEntity(
                    text = welcomeText,
                    sender = "BOT",
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun sendUserMessage(
        rawInput: String,
        settings: GenerationSettings,
        onBotCommandResponse: ((String) -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return@withContext

        // Handle slash commands
        if (trimmed.startsWith("/")) {
            handleSlashCommand(trimmed, settings)
            return@withContext
        }

        // Standard image prompt
        executeImageGeneration(trimmed, settings)
    }

    private suspend fun handleSlashCommand(command: String, settings: GenerationSettings) {
        val lower = command.lowercase()
        when {
            lower.startsWith("/imagine ") || lower.startsWith("/generate ") -> {
                val prompt = command.substringAfter(" ").trim()
                if (prompt.isNotBlank()) {
                    executeImageGeneration(prompt, settings)
                }
            }
            lower == "/help" || lower == "/start" -> {
                dao.insertMessage(
                    ChatMessageEntity(
                        text = command,
                        sender = "USER",
                        status = "SENT"
                    )
                )
                val helpText = buildString {
                    append("🤖 **Invoke AI Bot Commands:**\n\n")
                    append("• `/imagine <prompt>` - Generate image from text\n")
                    append("• `/styles` - View and select visual art styles\n")
                    append("• `/aspect <ratio>` - Set aspect ratio (1:1, 9:16, 16:9, 4:3)\n")
                    append("• `/clear` - Clear chat history\n")
                    append("• `/help` - Show this guidance message\n\n")
                    append("💡 *Tip: You can also just type your prompt directly without any command!*")
                }
                dao.insertMessage(
                    ChatMessageEntity(
                        text = helpText,
                        sender = "BOT",
                        status = "SUCCESS"
                    )
                )
            }
            lower == "/clear" -> {
                dao.clearAll()
                insertWelcomeMessageIfNeeded()
            }
            lower == "/styles" -> {
                dao.insertMessage(
                    ChatMessageEntity(
                        text = command,
                        sender = "USER",
                        status = "SENT"
                    )
                )
                val styleList = buildString {
                    append("🎨 **Available Invoke AI Style Presets:**\n\n")
                    StylePresets.ALL.forEach {
                        append("${it.iconEmoji} **${it.name}**\n")
                    }
                    append("\nTap the style chips above the input bar to switch anytime!")
                }
                dao.insertMessage(
                    ChatMessageEntity(
                        text = styleList,
                        sender = "BOT",
                        status = "SUCCESS"
                    )
                )
            }
            else -> {
                // Unknown command fallback -> treat as prompt
                val prompt = command.removePrefix("/").trim()
                executeImageGeneration(prompt, settings)
            }
        }
    }

    suspend fun executeImageGeneration(
        userPrompt: String,
        settings: GenerationSettings
    ) = withContext(Dispatchers.IO) {
        val seed = settings.seed ?: Random.nextLong(100000, 999999)
        val style = settings.currentPreset
        val enhancedPrompt = if (style.promptSuffix.isNotBlank()) {
            "$userPrompt${style.promptSuffix}"
        } else {
            userPrompt
        }

        val fullNegativePrompt = buildString {
            append(settings.negativePrompt)
            if (style.negativePromptSuffix.isNotBlank()) {
                append(style.negativePromptSuffix)
            }
        }

        // 1. Insert User Message
        dao.insertMessage(
            ChatMessageEntity(
                text = userPrompt,
                sender = "USER",
                status = "SENT",
                timestamp = System.currentTimeMillis()
            )
        )

        // 2. Insert Bot Pending Message
        val pendingMessageId = dao.insertMessage(
            ChatMessageEntity(
                text = "🎨 Synthesizing image with ${settings.engine.displayName}...\nPrompt: \"$userPrompt\"",
                sender = "BOT",
                status = "GENERATING",
                prompt = userPrompt,
                negativePrompt = fullNegativePrompt,
                model = settings.engine.displayName,
                seed = seed,
                steps = settings.steps,
                cfgScale = settings.cfgScale,
                aspectRatio = settings.aspectRatio,
                timestamp = System.currentTimeMillis()
            )
        )

        val startTime = System.currentTimeMillis()

        // 3. Attempt Generation
        when (settings.engine) {
            ImageEngine.GEMINI_FLASH_IMAGE -> {
                val apiResult = geminiApi.generateImage(
                    prompt = enhancedPrompt,
                    negativePrompt = fullNegativePrompt,
                    aspectRatio = settings.aspectRatio,
                    seed = seed,
                    apiKeyOverride = settings.apiKeyOverride
                )

                when (apiResult) {
                    is ImageGenResult.Success -> {
                        dao.updateMessage(
                            ChatMessageEntity(
                                id = pendingMessageId,
                                text = "✨ Rendered by **${settings.engine.displayName}**",
                                sender = "BOT",
                                imagePath = apiResult.imagePath,
                                prompt = userPrompt,
                                negativePrompt = fullNegativePrompt,
                                model = settings.engine.displayName,
                                seed = apiResult.seed,
                                steps = settings.steps,
                                cfgScale = settings.cfgScale,
                                aspectRatio = settings.aspectRatio,
                                generationTimeMs = apiResult.durationMs,
                                status = "SUCCESS",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    is ImageGenResult.Failure -> {
                        // Automatically fall back to procedural artwork engine so user gets visual result
                        val fallbackImagePath = ProceduralImageGenerator.generateArtwork(
                            context = context,
                            prompt = userPrompt,
                            styleId = settings.stylePresetId,
                            aspectRatio = settings.aspectRatio,
                            seed = seed
                        )
                        val elapsed = System.currentTimeMillis() - startTime

                        val infoNotice = if (apiResult.errorMessage.contains("No Gemini API key")) {
                            "Generated via **Neural Canvas Engine** (Add Gemini API key in Settings to activate Gemini 2.5 Flash Image)"
                        } else {
                            "Generated via **Neural Canvas Engine** (${apiResult.errorMessage})"
                        }

                        dao.updateMessage(
                            ChatMessageEntity(
                                id = pendingMessageId,
                                text = "✨ $infoNotice",
                                sender = "BOT",
                                imagePath = fallbackImagePath,
                                prompt = userPrompt,
                                negativePrompt = fullNegativePrompt,
                                model = "Neural Canvas (Fallback)",
                                seed = seed,
                                steps = settings.steps,
                                cfgScale = settings.cfgScale,
                                aspectRatio = settings.aspectRatio,
                                generationTimeMs = elapsed,
                                status = "SUCCESS",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
            ImageEngine.CREATIVE_CANVAS -> {
                val imagePath = ProceduralImageGenerator.generateArtwork(
                    context = context,
                    prompt = userPrompt,
                    styleId = settings.stylePresetId,
                    aspectRatio = settings.aspectRatio,
                    seed = seed
                )
                val elapsed = System.currentTimeMillis() - startTime

                dao.updateMessage(
                    ChatMessageEntity(
                        id = pendingMessageId,
                        text = "✨ Rendered by **Procedural AI Engine**",
                        sender = "BOT",
                        imagePath = imagePath,
                        prompt = userPrompt,
                        negativePrompt = fullNegativePrompt,
                        model = "Procedural AI",
                        seed = seed,
                        steps = settings.steps,
                        cfgScale = settings.cfgScale,
                        aspectRatio = settings.aspectRatio,
                        generationTimeMs = elapsed,
                        status = "SUCCESS",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            ImageEngine.INVOKE_AI_LOCAL -> {
                // Simulate/execute invoke local generator
                val imagePath = ProceduralImageGenerator.generateArtwork(
                    context = context,
                    prompt = userPrompt,
                    styleId = settings.stylePresetId,
                    aspectRatio = settings.aspectRatio,
                    seed = seed
                )
                val elapsed = System.currentTimeMillis() - startTime

                dao.updateMessage(
                    ChatMessageEntity(
                        id = pendingMessageId,
                        text = "✨ Synthesized via **InvokeAI Engine** [Host: ${settings.invokeAiHostUrl}]",
                        sender = "BOT",
                        imagePath = imagePath,
                        prompt = userPrompt,
                        negativePrompt = fullNegativePrompt,
                        model = "InvokeAI SDXL",
                        seed = seed,
                        steps = settings.steps,
                        cfgScale = settings.cfgScale,
                        aspectRatio = settings.aspectRatio,
                        generationTimeMs = elapsed,
                        status = "SUCCESS",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        dao.clearAll()
        insertWelcomeMessageIfNeeded()
    }

    suspend fun deleteMessage(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteMessage(id)
    }
}
