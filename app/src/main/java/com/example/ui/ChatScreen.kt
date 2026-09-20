package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.ChatWallpaper
import com.example.ui.components.ImageLightboxDialog
import com.example.ui.components.PromptSuggestionsBar
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.components.TelegramTopBar
import com.example.ui.theme.TelegramBlue
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val selectedImage by viewModel.selectedImageForLightbox.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val toastEvent by viewModel.copiedToastEvent.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Scroll to bottom when message count changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Handle toast events
    LaunchedEffect(toastEvent) {
        toastEvent?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToastEvent()
        }
    }

    // Show jump-to-bottom FAB when user scrolled up
    val showScrollToBottom by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            messages.isNotEmpty() && lastVisibleIndex < messages.size - 2
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TelegramTopBar(
                isGenerating = isGenerating,
                onOpenSettings = { viewModel.openSettings() },
                onClearChat = { viewModel.clearChat() },
                onShowStyles = { viewModel.openSettings() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        ChatWallpaper(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat messages list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_messages_list"),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        items(
                            items = messages,
                            key = { it.id }
                        ) { message ->
                            ChatMessageItem(
                                message = message,
                                onImageClick = { viewModel.openLightbox(it) },
                                onRemixPrompt = { viewModel.remixPrompt(it) },
                                onNotifyCopied = { viewModel.notifyCopied(it) }
                            )
                        }
                    }

                    // Prompt Inspiration & Styles Bar
                    PromptSuggestionsBar(
                        selectedStyleId = settings.stylePresetId,
                        onSelectStyle = { viewModel.selectStyle(it) },
                        onSelectPrompt = { prompt ->
                            viewModel.sendPrompt(prompt)
                        }
                    )

                    // Bottom Chat Input Bar
                    ChatInputBar(
                        text = inputText,
                        onTextChanged = { viewModel.onInputChanged(it) },
                        onSendMessage = { viewModel.sendMessage() },
                        onOpenSettings = { viewModel.openSettings() }
                    )
                }

                // Scroll to bottom floating button
                AnimatedVisibility(
                    visible = showScrollToBottom,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 120.dp, end = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        },
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = TelegramBlue,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Scroll to bottom",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    // Fullscreen Lightbox
    selectedImage?.let { msg ->
        ImageLightboxDialog(
            message = msg,
            onDismiss = { viewModel.closeLightbox() },
            onRemix = { viewModel.remixPrompt(it) },
            onNotifyCopied = { viewModel.notifyCopied(it) }
        )
    }

    // Settings & Parameters Bottom Sheet
    if (isSettingsOpen) {
        SettingsBottomSheet(
            settings = settings,
            onSaveSettings = { viewModel.updateSettings(it) },
            onDismiss = { viewModel.closeSettings() }
        )
    }
}
