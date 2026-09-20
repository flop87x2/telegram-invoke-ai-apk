package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ChatWallpaper(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val patternColor = if (isDark) {
        Color.White.copy(alpha = 0.025f)
    } else {
        Color.Black.copy(alpha = 0.035f)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 48.dp.toPx()
            val w = size.width
            val h = size.height

            // Render subtle diagonal grid with tiny decorative dots
            var y = 0f
            while (y < h) {
                var x = 0f
                while (x < w) {
                    drawCircle(
                        color = patternColor,
                        radius = 1.2.dp.toPx(),
                        center = Offset(x, y)
                    )
                    x += step
                }
                y += step
            }

            // Occasional decorative star / diamond motifs
            val largeStep = 144.dp.toPx()
            var ly = 36.dp.toPx()
            while (ly < h) {
                var lx = 36.dp.toPx()
                while (lx < w) {
                    // Small subtle plus/star
                    drawLine(
                        color = patternColor,
                        start = Offset(lx - 4.dp.toPx(), ly),
                        end = Offset(lx + 4.dp.toPx(), ly),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = patternColor,
                        start = Offset(lx, ly - 4.dp.toPx()),
                        end = Offset(lx, ly + 4.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                    lx += largeStep
                }
                ly += largeStep
            }
        }

        content()
    }
}
