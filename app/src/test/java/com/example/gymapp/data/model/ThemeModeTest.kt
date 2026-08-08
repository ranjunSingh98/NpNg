package com.example.gymapp.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test
    fun missingOrUnknownPreferencePreservesTheExistingDarkTheme() {
        assertEquals(ThemeMode.Dark, ThemeMode.fromPreferenceValue(null))
        assertEquals(ThemeMode.Dark, ThemeMode.fromPreferenceValue("unknown"))
    }

    @Test
    fun storedValuesRoundTrip() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromPreferenceValue(mode.preferenceValue))
        }
    }

    @Test
    fun systemModeTracksTheDeviceWhileExplicitModesDoNot() {
        assertTrue(ThemeMode.System.usesDarkTheme(systemInDarkTheme = true))
        assertFalse(ThemeMode.System.usesDarkTheme(systemInDarkTheme = false))
        assertTrue(ThemeMode.Dark.usesDarkTheme(systemInDarkTheme = false))
        assertFalse(ThemeMode.Light.usesDarkTheme(systemInDarkTheme = true))
    }
}
