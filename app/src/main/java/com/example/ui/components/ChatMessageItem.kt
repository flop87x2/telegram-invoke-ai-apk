package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.local.ChatMessageEntity
import com.example.ui.theme.InvokeAccentPurple
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramCyan
import com.example.ui.theme.TelegramDarkBubbleIn
import com.example.ui.theme.TelegramDarkBubbleOut
import com.example.ui.theme.TelegramLightBubbleIn
import com.example.ui.theme.TelegramLightBubbleOut
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    onImageClick: (ChatMessageEntity) -> Unit,
    onRemixPrompt: (ChatMessageEntity) -> Unit,
    onNotifyCopied: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "USER"
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val bubbleBg = if (isUser) {
        if (isDark) TelegramDarkBubbleOut else TelegramLightBubbleOut
    } else {
        if (isDark) TelegramDarkBubbleIn else TelegramLightBubbleIn
    }

    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        // Bubble container
        Surface(
            shape = bubbleShape,
            color = bubbleBg,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 340.dp)
                .testTag("chat_bubble_${message.id}")
        ) {
            Column(
                modifier = Modifier.padding(
                    if (message.imagePath != null) 4.dp else 10.dp
                )
            ) {
                // If message is generating
                if (message.status == "GENERATING") {
                    GeneratingBubbleContent(message = message)
                }
                // If message has an image
                else if (message.imagePath != null) {
                    val imgFile = remember(message.imagePath) { File(message.imagePath) }
                    val ratioFloat = when (message.aspectRatio) {
                        "9:16" -> 9f / 16f
                        "16:9" -> 16f / 9f
                        "4:3" -> 4f / 3f
                        "3:4" -> 3f / 4f
                        else -> 1f
                    }

                    // Image container with click for lightbox
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(ratioFloat)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onImageClick(message) }
                    ) {
                        AsyncImage(
                            model = imgFile,
                            contentDescription = message.prompt ?: "Generated Artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Top right quick zoom badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom Image",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Bottom gradient tag
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = message.model ?: "Invoke AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Prompt text & parameters badge
                    if (!message.prompt.isNullOrBlank()) {
                        Text(
                            text = message.prompt,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Metadata chips (Seed, Aspect, Time)
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (message.aspectRatio != null) {
                            ParamChip(label = "📐 ${message.aspectRatio}")
                        }
                        if (message.seed != null) {
                            ParamChip(label = "🌱 #${message.seed.toString().takeLast(6)}")
                        }
                        if (message.generationTimeMs != null) {
                            val sec = String.format(Locale.US, "%.1fs", message.generationTimeMs / 1000f)
                            ParamChip(label = "⚡ $sec")
                        }
                    }

                    // Action buttons row: Remix, Copy, Share
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onRemixPrompt(message) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Remix Prompt",
                                tint = TelegramBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                message.prompt?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                    onNotifyCopied("Prompt copied to clipboard!")
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Prompt",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                shareImage(context, message)
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Image",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Timestamp
                        Text(
                            text = formattedTime,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                // Text only message
                else {
                    Text(
                        text = message.text,
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                        if (isUser) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Sent",
                                tint = TelegramCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratingBubbleContent(message: ChatMessageEntity) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                strokeWidth = 2.5.dp,
                color = TelegramBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Invoke AI is generating...",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Shimmering prompt card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = "Prompt: \"${message.prompt ?: message.text}\"",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Model: ${message.model ?: "Invoke AI"} • ${message.steps ?: 30} steps",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = InvokeAccentPurple
                )
            }
        }
    }
}

@Composable
private fun ParamChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun shareImage(context: Context, message: ChatMessageEntity) {
    val path = message.imagePath ?: return
    val file = File(path)
    if (!file.exists()) return

    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Generated with Invoke AI Bot: \"${message.prompt ?: ""}\"")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Image via"))
    } catch (e: Exception) {
        // Fallback to text share
        val textIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this AI artwork prompt: \"${message.prompt ?: ""}\"")
        }
        context.startActivity(Intent.createChooser(textIntent, "Share Prompt"))
    }
}
