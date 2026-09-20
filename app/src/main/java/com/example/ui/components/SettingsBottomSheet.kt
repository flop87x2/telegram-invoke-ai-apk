package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GenerationSettings
import com.example.data.model.ImageEngine
import com.example.ui.theme.InvokeAccentPurple
import com.example.ui.theme.TelegramBlue
import com.example.ui.theme.TelegramCyan

val ASPECT_RATIOS = listOf("1:1", "9:16", "16:9", "4:3", "3:4")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsBottomSheet(
    settings: GenerationSettings,
    onSaveSettings: (GenerationSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedEngine by remember { mutableStateOf(settings.engine) }
    var selectedAspect by remember { mutableStateOf(settings.aspectRatio) }
    var steps by remember { mutableIntStateOf(settings.steps) }
    var cfgScale by remember { mutableFloatStateOf(settings.cfgScale) }
    var negativePrompt by remember { mutableStateOf(settings.negativePrompt) }
    var seedText by remember { mutableStateOf(settings.seed?.toString() ?: "") }
    var apiKeyOverride by remember { mutableStateOf(settings.apiKeyOverride) }
    var invokeHost by remember { mutableStateOf(settings.invokeAiHostUrl) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = TelegramBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Invoke AI Bot Parameters",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Generation Engine
            Text(
                text = "Generation Engine",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            ImageEngine.values().forEach { engine ->
                val isChosen = selectedEngine == engine
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedEngine = engine },
                    color = if (isChosen) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = engine.displayName,
                                fontSize = 14.sp,
                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = engine.description,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isChosen) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Selected",
                                tint = TelegramBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Aspect Ratio
            Text(
                text = "Aspect Ratio",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ASPECT_RATIOS.forEach { ratio ->
                    val isSelected = selectedAspect == ratio
                    val label = when (ratio) {
                        "1:1" -> "1:1 Square"
                        "9:16" -> "9:16 Portrait"
                        "16:9" -> "16:9 Widescreen"
                        "4:3" -> "4:3 Landscape"
                        "3:4" -> "3:4 Classic"
                        else -> ratio
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) TelegramBlue else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedAspect = ratio }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Steps Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inference Steps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$steps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TelegramBlue
                )
            }
            Slider(
                value = steps.toFloat(),
                onValueChange = { steps = it.toInt() },
                valueRange = 15f..50f,
                steps = 34,
                colors = SliderDefaults.colors(
                    thumbColor = TelegramBlue,
                    activeTrackColor = TelegramBlue
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. CFG / Guidance Scale Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CFG Scale (Prompt Guidance)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format("%.1f", cfgScale),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TelegramBlue
                )
            }
            Slider(
                value = cfgScale,
                onValueChange = { cfgScale = it },
                valueRange = 1f..15f,
                steps = 27,
                colors = SliderDefaults.colors(
                    thumbColor = TelegramBlue,
                    activeTrackColor = TelegramBlue
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Negative Prompt Field
            Text(
                text = "Negative Prompt",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = negativePrompt,
                onValueChange = { negativePrompt = it },
                placeholder = { Text("blurry, low quality, distorted...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Seed Field
            Text(
                text = "Seed (Leave blank for random)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = seedText,
                onValueChange = { seedText = it.filter { ch -> ch.isDigit() } },
                placeholder = { Text("e.g. 482910") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (selectedEngine == ImageEngine.INVOKE_AI_LOCAL) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "InvokeAI Server Host",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = invokeHost,
                    onValueChange = { invokeHost = it },
                    placeholder = { Text("http://10.0.2.2:9090") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (selectedEngine == ImageEngine.GEMINI_FLASH_IMAGE) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Gemini API Key (Optional Override)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = apiKeyOverride,
                    onValueChange = { apiKeyOverride = it },
                    placeholder = { Text("Default uses AI Studio Secrets") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save button
            Button(
                onClick = {
                    val parsedSeed = seedText.toLongOrNull()
                    val updated = settings.copy(
                        engine = selectedEngine,
                        aspectRatio = selectedAspect,
                        steps = steps,
                        cfgScale = cfgScale,
                        negativePrompt = negativePrompt,
                        seed = parsedSeed,
                        invokeAiHostUrl = invokeHost,
                        apiKeyOverride = apiKeyOverride
                    )
                    onSaveSettings(updated)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Apply Parameters",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
