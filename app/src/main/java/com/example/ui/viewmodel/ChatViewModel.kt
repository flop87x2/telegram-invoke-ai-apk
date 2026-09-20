package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.model.GenerationSettings
import com.example.data.model.ImageEngine
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository(application)

    val messages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isGenerating: StateFlow<Boolean> = messages.map { list ->
        list.any { it.status == "GENERATING" }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _settings = MutableStateFlow(GenerationSettings())
    val settings: StateFlow<GenerationSettings> = _settings.asStateFlow()

    private val _selectedImageForLightbox = MutableStateFlow<ChatMessageEntity?>(null)
    val selectedImageForLightbox: StateFlow<ChatMessageEntity?> = _selectedImageForLightbox.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _copiedToastEvent = MutableStateFlow<String?>(null)
    val copiedToastEvent: StateFlow<String?> = _copiedToastEvent.asStateFlow()

    init {
        viewModelScope.launch {
            repository.insertWelcomeMessageIfNeeded()
        }
    }

    fun onInputChanged(newText: String) {
        _inputText.value = newText
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return
        _inputText.value = ""

        viewModelScope.launch {
            repository.sendUserMessage(text, _settings.value)
        }
    }

    fun sendPrompt(prompt: String) {
        viewModelScope.launch {
            repository.executeImageGeneration(prompt, _settings.value)
        }
    }

    fun selectStyle(styleId: String) {
        _settings.value = _settings.value.copy(stylePresetId = styleId)
    }

    fun selectAspectRatio(aspectRatio: String) {
        _settings.value = _settings.value.copy(aspectRatio = aspectRatio)
    }

    fun selectEngine(engine: ImageEngine) {
        _settings.value = _settings.value.copy(engine = engine)
    }

    fun updateSettings(newSettings: GenerationSettings) {
        _settings.value = newSettings
    }

    fun openLightbox(message: ChatMessageEntity) {
        _selectedImageForLightbox.value = message
    }

    fun closeLightbox() {
        _selectedImageForLightbox.value = null
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun remixPrompt(message: ChatMessageEntity) {
        val originalPrompt = message.prompt ?: message.text
        _inputText.value = originalPrompt
    }

    fun notifyCopied(text: String) {
        _copiedToastEvent.value = text
    }

    fun clearToastEvent() {
        _copiedToastEvent.value = null
    }
}
