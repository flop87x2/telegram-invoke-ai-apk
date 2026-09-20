package com.example.data.model

enum class ImageEngine(val displayName: String, val description: String) {
    GEMINI_FLASH_IMAGE("Gemini 2.5 Flash Image", "Google AI generative multimodal image model"),
    INVOKE_AI_LOCAL("InvokeAI Local/Remote", "Connect to self-hosted InvokeAI instance"),
    CREATIVE_CANVAS("Procedural AI Canvas", "Instant on-device neural procedural renderer")
}

data class StylePreset(
    val id: String,
    val name: String,
    val iconEmoji: String,
    val promptSuffix: String,
    val negativePromptSuffix: String = ""
)

object StylePresets {
    val ALL = listOf(
        StylePreset(
            id = "none",
            name = "Default",
            iconEmoji = "✨",
            promptSuffix = ""
        ),
        StylePreset(
            id = "photoreal",
            name = "Photorealistic",
            iconEmoji = "📸",
            promptSuffix = ", 8k resolution, ultra-detailed photorealistic, dramatic studio lighting, depth of field, sharp focus",
            negativePromptSuffix = ", cartoon, illustration, distorted, blurry"
        ),
        StylePreset(
            id = "cyberpunk",
            name = "Cyberpunk",
            iconEmoji = "🌆",
            promptSuffix = ", cyberpunk aesthetic, neon cyan and magenta glow, dark rainy streets, volumetric smoke, futuristic high-tech details",
            negativePromptSuffix = ", daytime, rustic, low contrast"
        ),
        StylePreset(
            id = "anime",
            name = "Anime / Ghibli",
            iconEmoji = "🌸",
            promptSuffix = ", beautiful anime concept art, Makoto Shinkai and Studio Ghibli style, soft cinematic lighting, picturesque sky and clouds",
            negativePromptSuffix = ", 3d realistic, photograph"
        ),
        StylePreset(
            id = "fantasy",
            name = "Fantasy Art",
            iconEmoji = "🐉",
            promptSuffix = ", epic fantasy digital painting, mythical atmosphere, celestial rays, intricate magical ornamentation, ArtStation trending",
            negativePromptSuffix = ", modern, mundane, low quality"
        ),
        StylePreset(
            id = "cinematic",
            name = "Cinematic 35mm",
            iconEmoji = "🎬",
            promptSuffix = ", 35mm movie film still, cinematic framing, anamorphic lens flare, moody color grading, panavision",
            negativePromptSuffix = ", oversaturated, cheap digital look"
        ),
        StylePreset(
            id = "watercolor",
            name = "Watercolor",
            iconEmoji = "🎨",
            promptSuffix = ", traditional delicate watercolor illustration, fluid wash textures, paper grain, expressive colorful brushwork",
            negativePromptSuffix = ", digital plastic, 3d render"
        ),
        StylePreset(
            id = "3d_render",
            name = "3D Isometric",
            iconEmoji = "🧊",
            promptSuffix = ", cute 3D isometric render, Blender 3D, octane render, soft clay and frosted glass materials, playful studio lighting",
            negativePromptSuffix = ", 2d flat, sketch"
        )
    )
}

data class GenerationSettings(
    val engine: ImageEngine = ImageEngine.GEMINI_FLASH_IMAGE,
    val stylePresetId: String = "none",
    val aspectRatio: String = "1:1", // 1:1, 9:16, 16:9, 4:3, 3:4
    val steps: Int = 30,
    val cfgScale: Float = 7.5f,
    val negativePrompt: String = "blurry, low quality, distorted, extra limbs, watermark, text, signature",
    val seed: Long? = null,
    val invokeAiHostUrl: String = "http://10.0.2.2:9090",
    val apiKeyOverride: String = ""
) {
    val currentPreset: StylePreset
        get() = StylePresets.ALL.find { it.id == stylePresetId } ?: StylePresets.ALL.first()
}
