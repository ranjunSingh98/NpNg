package com.example.gymapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeContrastTest {
    @Test
    fun readableColorIsUnchanged() {
        val foreground = Color(0xFF00658A)
        val background = Color.White

        assertEquals(foreground, foreground.ensureMinimumContrast(background))
    }

    @Test
    fun brightAccentIsDarkenedToReadableContrastOnWhite() {
        val adjusted = ChestYellow.ensureMinimumContrast(Color.White)
        val lighter = maxOf(adjusted.luminance(), Color.White.luminance())
        val darker = minOf(adjusted.luminance(), Color.White.luminance())
        val ratio = (lighter + 0.05f) / (darker + 0.05f)

        assertTrue(ratio >= 4.5f)
        assertTrue(adjusted.luminance() < ChestYellow.luminance())
    }
}
