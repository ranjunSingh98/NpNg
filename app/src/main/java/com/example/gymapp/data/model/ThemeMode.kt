package com.example.gymapp.data.model

enum class ThemeMode(val preferenceValue: String) {
    System("system"),
    Dark("dark"),
    Light("light");

    companion object {
        fun fromPreferenceValue(value: String?): ThemeMode =
            entries.firstOrNull { it.preferenceValue == value } ?: Dark
    }
}

fun ThemeMode.usesDarkTheme(systemInDarkTheme: Boolean): Boolean = when (this) {
    ThemeMode.System -> systemInDarkTheme
    ThemeMode.Dark -> true
    ThemeMode.Light -> false
}
