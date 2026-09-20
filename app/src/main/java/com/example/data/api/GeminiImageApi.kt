package com.example.data.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

sealed class ImageGenResult {
    data class Success(
        val imagePath: String,
        val textComment: String?,
        val seed: Long,
        val durationMs: Long
    ) : ImageGenResult()

    data class Failure(
        val errorMessage: String,
        val canFallback: Boolean = true
    ) : ImageGenResult()
}

class GeminiImageApi(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateImage(
        prompt: String,
        negativePrompt: String? = null,
        aspectRatio: String = "1:1",
        seed: Long = System.currentTimeMillis(),
        apiKeyOverride: String? = null
    ): ImageGenResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        val apiKey = when {
            !apiKeyOverride.isNullOrBlank() -> apiKeyOverride.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (apiKey.isBlank()) {
            return@withContext ImageGenResult.Failure(
                errorMessage = "No Gemini API key found. Configure it in AI Studio Secrets or app settings.",
                canFallback = true
            )
        }

        try {
            // Construct Gemini 2.5 Flash Image generateContent request
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"

            val fullPromptText = buildString {
                append(prompt)
                if (!negativePrompt.isNullOrBlank()) {
                    append(" (Negative constraint: avoid ")
                    append(negativePrompt)
                    append(")")
                }
            }

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", fullPromptText)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    val imageConfig = JSONObject().apply {
                        put("aspectRatio", aspectRatio)
                        put("imageSize", "1K")
                    }
                    put("imageConfig", imageConfig)
                    val modalities = JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    }
                    put("responseModalities", modalities)
                }
                put("generationConfig", generationConfig)
            }

            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiImageApi", "API Error ${response.code}: $responseBody")
                val errMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext ImageGenResult.Failure(
                    errorMessage = "Gemini API error: $errMsg",
                    canFallback = true
                )
            }

            val responseObj = JSONObject(responseBody)
            val candidates = responseObj.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext ImageGenResult.Failure(
                    errorMessage = "No generation candidates returned from Gemini API",
                    canFallback = true
                )
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var foundBase64: String? = null
            var responseText: String? = null

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("inlineData")) {
                        val inlineData = part.getJSONObject("inlineData")
                        foundBase64 = inlineData.optString("data")
                    }
                    if (part.has("text")) {
                        responseText = part.optString("text")
                    }
                }
            }

            if (foundBase64.isNullOrEmpty()) {
                return@withContext ImageGenResult.Failure(
                    errorMessage = responseText ?: "Image generation completed without image data",
                    canFallback = true
                )
            }

            // Decode image bytes and save to app storage
            val imageBytes = Base64.decode(foundBase64, Base64.DEFAULT)
            val imagesDir = File(context.filesDir, "generated_art").apply { mkdirs() }
            val savedFile = File(imagesDir, "invoke_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.png")

            FileOutputStream(savedFile).use { out ->
                out.write(imageBytes)
                out.flush()
            }

            val durationMs = System.currentTimeMillis() - startTime
            ImageGenResult.Success(
                imagePath = savedFile.absolutePath,
                textComment = responseText,
                seed = seed,
                durationMs = durationMs
            )
        } catch (e: Exception) {
            Log.e("GeminiImageApi", "Exception in generateImage", e)
            ImageGenResult.Failure(
                errorMessage = e.localizedMessage ?: "Unknown network error",
                canFallback = true
            )
        }
    }
}
