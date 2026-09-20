package com.example.data.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.data.model.StylePresets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object ProceduralImageGenerator {

    suspend fun generateArtwork(
        context: Context,
        prompt: String,
        styleId: String,
        aspectRatio: String = "1:1",
        seed: Long = System.currentTimeMillis()
    ): String = withContext(Dispatchers.Default) {
        val (width, height) = when (aspectRatio) {
            "9:16" -> Pair(720, 1280)
            "16:9" -> Pair(1280, 720)
            "4:3" -> Pair(1024, 768)
            "3:4" -> Pair(768, 1024)
            else -> Pair(1024, 1024)
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val rng = Random(seed)

        val lowerPrompt = prompt.lowercase()

        // Determine visual theme based on keywords and style
        val isCyberpunk = styleId == "cyberpunk" || lowerPrompt.contains("cyber") || lowerPrompt.contains("neon") || lowerPrompt.contains("futuristic")
        val isSpace = lowerPrompt.contains("space") || lowerPrompt.contains("galaxy") || lowerPrompt.contains("star") || lowerPrompt.contains("nebula") || lowerPrompt.contains("astronaut") || lowerPrompt.contains("planet")
        val isNature = lowerPrompt.contains("mountain") || lowerPrompt.contains("lake") || lowerPrompt.contains("river") || lowerPrompt.contains("tree") || lowerPrompt.contains("forest") || lowerPrompt.contains("landscape")
        val isAnime = styleId == "anime" || lowerPrompt.contains("anime") || lowerPrompt.contains("ghibli") || lowerPrompt.contains("manga")
        val isFantasy = styleId == "fantasy" || lowerPrompt.contains("fantasy") || lowerPrompt.contains("dragon") || lowerPrompt.contains("castle") || lowerPrompt.contains("magic")
        val isWatercolor = styleId == "watercolor" || lowerPrompt.contains("watercolor")

        // 1. Render Atmospheric Gradient Background
        val (skyTopColor, skyBottomColor) = when {
            isCyberpunk -> Pair(Color.rgb(10, 12, 28), Color.rgb(38, 10, 60))
            isSpace -> Pair(Color.rgb(4, 6, 18), Color.rgb(20, 12, 45))
            isAnime -> Pair(Color.rgb(40, 130, 220), Color.rgb(255, 180, 150))
            isFantasy -> Pair(Color.rgb(20, 10, 40), Color.rgb(80, 30, 90))
            isWatercolor -> Pair(Color.rgb(240, 244, 248), Color.rgb(225, 235, 245))
            isNature -> Pair(Color.rgb(25, 45, 85), Color.rgb(240, 140, 80))
            else -> Pair(Color.rgb(20, 24, 40), Color.rgb(50, 70, 100))
        }

        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                skyTopColor, skyBottomColor,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Render Celestial Body / Glowing Sun / Moon / Portal
        val celestialPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val celestialX = width * (0.3f + rng.nextFloat() * 0.4f)
        val celestialY = height * (0.25f + rng.nextFloat() * 0.25f)
        val celestialRadius = (width.coerceAtMost(height) * 0.22f)

        if (isCyberpunk) {
            // Giant holographic grid sun
            celestialPaint.shader = RadialGradient(
                celestialX, celestialY, celestialRadius * 1.5f,
                intArrayOf(Color.argb(230, 255, 0, 128), Color.argb(160, 0, 229, 255), Color.TRANSPARENT),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(celestialX, celestialY, celestialRadius * 1.4f, celestialPaint)
            // Retro stripes across sun
            val stripePaint = Paint().apply {
                color = skyTopColor
                strokeWidth = 6f
            }
            for (s in 0..12) {
                val y = celestialY - celestialRadius + (s * (celestialRadius * 2f / 12f))
                if (y > celestialY - celestialRadius * 0.8f && y < celestialY + celestialRadius * 0.8f) {
                    stripePaint.strokeWidth = 3f + (s * 1.2f)
                    canvas.drawLine(celestialX - celestialRadius, y, celestialX + celestialRadius, y, stripePaint)
                }
            }
        } else if (isSpace) {
            // Ringed Planet + Glowing Nebula
            val nebulaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    celestialX, celestialY, celestialRadius * 2f,
                    intArrayOf(Color.argb(190, 138, 43, 226), Color.argb(130, 0, 191, 255), Color.TRANSPARENT),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(celestialX, celestialY, celestialRadius * 2f, nebulaPaint)

            // Planet sphere
            celestialPaint.shader = RadialGradient(
                celestialX - celestialRadius * 0.3f, celestialY - celestialRadius * 0.3f, celestialRadius,
                intArrayOf(Color.rgb(240, 200, 140), Color.rgb(180, 90, 50), Color.rgb(40, 20, 30)),
                floatArrayOf(0f, 0.6f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(celestialX, celestialY, celestialRadius * 0.8f, celestialPaint)

            // Planetary ring
            val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(160, 255, 230, 180)
                style = Paint.Style.STROKE
                strokeWidth = 14f
            }
            val ringOval = RectF(
                celestialX - celestialRadius * 1.6f,
                celestialY - celestialRadius * 0.4f,
                celestialX + celestialRadius * 1.6f,
                celestialY + celestialRadius * 0.4f
            )
            canvas.save()
            canvas.rotate(-22f, celestialX, celestialY)
            canvas.drawOval(ringOval, ringPaint)
            canvas.restore()
        } else {
            // Warm radiant sun or moon
            celestialPaint.shader = RadialGradient(
                celestialX, celestialY, celestialRadius * 1.8f,
                intArrayOf(Color.argb(220, 255, 245, 200), Color.argb(100, 255, 170, 70), Color.TRANSPARENT),
                floatArrayOf(0f, 0.4f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(celestialX, celestialY, celestialRadius * 1.6f, celestialPaint)
        }

        // 3. Starlight / Particle dust
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        for (i in 0..120) {
            val sx = rng.nextFloat() * width
            val sy = rng.nextFloat() * (height * 0.7f)
            val sr = 1f + rng.nextFloat() * 3f
            starPaint.alpha = 100 + rng.nextInt(155)
            canvas.drawCircle(sx, sy, sr, starPaint)
        }

        // 4. Foreground Silhouette / Skyline / Mountain Ranges
        if (isCyberpunk) {
            // Neon Skyscraper Skyline
            val buildingPaint = Paint().apply { color = Color.rgb(12, 16, 26) }
            val windowPaint = Paint().apply { color = Color.rgb(0, 229, 255) }
            var currentX = 0f
            while (currentX < width) {
                val bWidth = 50f + rng.nextFloat() * 90f
                val bHeight = height * (0.35f + rng.nextFloat() * 0.45f)
                val bTop = height - bHeight

                canvas.drawRect(currentX, bTop, currentX + bWidth, height.toFloat(), buildingPaint)

                // Neon roof antenna or sign
                if (rng.nextBoolean()) {
                    val antennaPaint = Paint().apply {
                        color = if (rng.nextBoolean()) Color.rgb(255, 0, 128) else Color.rgb(0, 229, 255)
                        strokeWidth = 3f
                    }
                    canvas.drawLine(currentX + bWidth / 2f, bTop, currentX + bWidth / 2f, bTop - 40f, antennaPaint)
                    canvas.drawCircle(currentX + bWidth / 2f, bTop - 42f, 4f, antennaPaint)
                }

                // Window grid lights
                for (wy in (bTop + 20f).toInt()..(height - 40) step 24) {
                    for (wx in (currentX + 8f).toInt()..(currentX + bWidth - 12f).toInt() step 16) {
                        if (rng.nextFloat() > 0.4f) {
                            windowPaint.color = when (rng.nextInt(3)) {
                                0 -> Color.rgb(0, 229, 255)
                                1 -> Color.rgb(255, 215, 0)
                                else -> Color.rgb(255, 64, 129)
                            }
                            windowPaint.alpha = 180 + rng.nextInt(75)
                            canvas.drawRect(wx.toFloat(), wy.toFloat(), wx + 8f, wy + 12f, windowPaint)
                        }
                    }
                }
                currentX += bWidth + 6f
            }

            // Ground neon reflection water / road
            val wetFloorPaint = Paint().apply {
                shader = LinearGradient(
                    0f, height * 0.82f, 0f, height.toFloat(),
                    Color.argb(120, 255, 0, 128), Color.rgb(8, 10, 18),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, height * 0.82f, width.toFloat(), height.toFloat(), wetFloorPaint)

        } else {
            // Layered Mountain Ridges & Atmospheric Landscape
            val numLayers = 4
            for (l in 0 until numLayers) {
                val layerProgress = l / (numLayers.toFloat() - 1)
                val layerColor = when {
                    isAnime -> {
                        val r = (30 + layerProgress * 20).toInt()
                        val g = (60 + layerProgress * 30).toInt()
                        val b = (100 - layerProgress * 50).toInt()
                        Color.rgb(r, g, b)
                    }
                    isFantasy -> {
                        val r = (25 + layerProgress * 30).toInt()
                        val g = (15 + layerProgress * 15).toInt()
                        val b = (45 + layerProgress * 30).toInt()
                        Color.rgb(r, g, b)
                    }
                    else -> {
                        val v = (20 + (1f - layerProgress) * 50).toInt()
                        Color.rgb(v, (v * 1.1f).toInt().coerceAtMost(255), (v * 1.3f).toInt().coerceAtMost(255))
                    }
                }

                val mountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = layerColor }
                val path = Path()
                val baseHeight = height * (0.55f + layerProgress * 0.25f)
                path.moveTo(0f, height.toFloat())
                path.lineTo(0f, baseHeight)

                var x = 0f
                val step = width / 8f
                while (x <= width + step) {
                    val peakY = baseHeight - (rng.nextFloat() * (height * 0.22f * (1f - layerProgress * 0.4f)))
                    path.lineTo(x + step / 2f, peakY)
                    path.lineTo(x + step, baseHeight + rng.nextFloat() * 20f)
                    x += step
                }
                path.lineTo(width.toFloat(), height.toFloat())
                path.close()
                canvas.drawPath(path, mountainPaint)
            }

            // Foreground pine trees silhouette
            val treePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(8, 14, 18) }
            for (t in 0..18) {
                val tx = rng.nextFloat() * width
                val ty = height * (0.85f + rng.nextFloat() * 0.12f)
                val th = 50f + rng.nextFloat() * 70f
                val tw = th * 0.35f

                val treePath = Path().apply {
                    moveTo(tx, ty - th)
                    lineTo(tx + tw, ty)
                    lineTo(tx - tw, ty)
                    close()
                }
                canvas.drawPath(treePath, treePaint)
            }
        }

        // 5. Watermark Badge / Tech Info Overlay in lower right
        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(170, 14, 22, 33)
            style = Paint.Style.FILL
        }
        val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 0, 229, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(180, 210, 230)
            textSize = 16f
        }

        val badgeW = 280f
        val badgeH = 68f
        val badgeMargin = 24f
        val badgeRect = RectF(
            width - badgeW - badgeMargin,
            height - badgeH - badgeMargin,
            width - badgeMargin,
            height - badgeMargin
        )
        canvas.drawRoundRect(badgeRect, 12f, 12f, badgeBgPaint)
        canvas.drawRoundRect(badgeRect, 12f, 12f, badgeBorderPaint)

        canvas.drawText("✦ INVOKE AI BOT", badgeRect.left + 16f, badgeRect.top + 28f, textPaint)
        val styleLabel = StylePresets.ALL.find { it.id == styleId }?.name ?: "Invoke Engine"
        canvas.drawText("$styleLabel • Seed #${seed.toString().takeLast(5)}", badgeRect.left + 16f, badgeRect.top + 52f, subTextPaint)

        // Save generated bitmap to app storage
        val outputDir = File(context.filesDir, "generated_art").apply { mkdirs() }
        val outputFile = File(outputDir, "proc_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png")
        FileOutputStream(outputFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
            fos.flush()
        }

        outputFile.absolutePath
    }
}
