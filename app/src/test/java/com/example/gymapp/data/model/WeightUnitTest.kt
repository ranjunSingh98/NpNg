package com.example.gymapp.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WeightUnitTest {
    @Test
    fun poundsRemainCanonicalAtInputAndDisplayBoundary() {
        assertEquals(185.0, 185.0.storedPoundsToDisplay(WeightUnit.Pounds), 0.0)
        assertEquals(185.0, 185.0.displayWeightToStoredPounds(WeightUnit.Pounds), 0.0)
    }

    @Test
    fun kilogramsConvertToAndFromCanonicalPounds() {
        val pounds = 100.0.displayWeightToStoredPounds(WeightUnit.Kilograms)

        assertEquals(220.462262, pounds, 0.000001)
        assertEquals(100.0, pounds.storedPoundsToDisplay(WeightUnit.Kilograms), 0.000001)
    }

    @Test
    fun storedWeightFormattingUsesSelectedUnitAndTrimsZeros() {
        assertEquals("185", 185.0.formatStoredWeight(WeightUnit.Pounds))
        assertEquals("83.9", 185.0.formatStoredWeight(WeightUnit.Kilograms))
        assertEquals("83.91", 185.0.formatStoredWeightInput(WeightUnit.Kilograms))
    }

    @Test
    fun unknownPreferenceFallsBackToPounds() {
        assertEquals(WeightUnit.Pounds, WeightUnit.fromPreferenceValue("stones"))
        assertEquals(WeightUnit.Pounds, WeightUnit.fromPreferenceValue(null))
    }
}
