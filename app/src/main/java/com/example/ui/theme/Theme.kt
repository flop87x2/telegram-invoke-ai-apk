package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TelegramBlue,
    onPrimary = Color.White,
    primaryContainer = TelegramDarkSurfaceElevated,
    onPrimaryContainer = TelegramCyan,
    secondary = TelegramCyan,
    onSecondary = Color.Black,
    background = TelegramDarkBg,
    onBackground = TelegramDarkTextPrimary,
    surface = TelegramDarkSurface,
    onSurface = TelegramDarkTextPrimary,
    surfaceVariant = TelegramDarkSurfaceElevated,
    onSurfaceVariant = TelegramDarkTextSecondary,
    outline = TelegramDarkDivider
)

private val LightColorScheme = lightColorScheme(
    primary = TelegramBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1F5FE),
    onPrimaryContainer = TelegramBlueDark,
    secondary = TelegramBlueDark,
    onSecondary = Color.White,
    background = Color(0xFFEFE9E0),
    onBackground = TelegramLightTextPrimary,
    surface = TelegramLightSurface,
    onSurface = TelegramLightTextPrimary,
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = TelegramLightTextSecondary,
    outline = Color(0xFFE0E0E0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Prefer Telegram distinctive branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

