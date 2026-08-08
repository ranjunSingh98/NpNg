package com.example.gymapp.data.model

import java.math.RoundingMode

enum class WeightUnit(
    val preferenceValue: String,
    val symbol: String,
    val displayName: String,
) {
    Pounds("pounds", "lb", "Pounds"),
    Kilograms("kilograms", "kg", "Kilograms");

    companion object {
        fun fromPreferenceValue(value: String?): WeightUnit =
            entries.firstOrNull { it.preferenceValue == value } ?: Pounds
    }
}

/** Weights remain stored as pounds so existing workouts and backups stay compatible. */
fun Double.storedPoundsToDisplay(unit: WeightUnit): Double = when (unit) {
    WeightUnit.Pounds -> this
    WeightUnit.Kilograms -> this * POUNDS_TO_KILOGRAMS
}

fun Double.displayWeightToStoredPounds(unit: WeightUnit): Double = when (unit) {
    WeightUnit.Pounds -> this
    WeightUnit.Kilograms -> this / POUNDS_TO_KILOGRAMS
}

fun Double.formatStoredWeight(unit: WeightUnit): String =
    storedPoundsToDisplay(unit).formatWeightDecimal(scale = 1)

fun Double.formatStoredWeightInput(unit: WeightUnit): String =
    storedPoundsToDisplay(unit).formatWeightDecimal(scale = 2)

private fun Double.formatWeightDecimal(scale: Int): String =
    toBigDecimal()
        .setScale(scale, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()

private const val POUNDS_TO_KILOGRAMS = 0.45359237
