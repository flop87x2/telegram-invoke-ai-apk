package com.example

import com.example.data.model.GenerationSettings
import com.example.data.model.ImageEngine
import com.example.data.model.StylePresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class InvokeAiChatTest {

    @Test
    fun verifyStylePresetsExist() {
        assertTrue(StylePresets.ALL.isNotEmpty())
        val cyberpunk = StylePresets.ALL.find { it.id == "cyberpunk" }
        assertNotNull(cyberpunk)
        assertTrue(cyberpunk!!.promptSuffix.contains("cyberpunk"))
    }

    @Test
    fun verifyDefaultGenerationSettings() {
        val settings = GenerationSettings()
        assertEquals(ImageEngine.GEMINI_FLASH_IMAGE, settings.engine)
        assertEquals("1:1", settings.aspectRatio)
        assertEquals(30, settings.steps)
        assertEquals(7.5f, settings.cfgScale)
        assertTrue(settings.negativePrompt.isNotBlank())
    }
}
