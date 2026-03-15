package com.example.gymapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    background = Background,
    surface = Surface,
    primary = Primary,
    onPrimary = OnPrimary,
    onBackground = OnBackground,
    secondary = SecondaryText // Using secondary for secondary text as it's a common pattern
)

private val LightColorScheme = lightColorScheme(
    // Default Material 3 light colors, can be customized later if needed
    primary = Primary,
    onPrimary = OnPrimary,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    secondary = Color(0xFF6200EE) // Placeholder, can be customized
)

@Composable
fun GymAppTheme(
    darkTheme: Boolean = true, // Set dark theme as default
    // Dynamic color is not used by default as per request
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Temporarily comment out the problematic line to resolve compilation error
            // WindowCompat.setStatusBarColor(window, colorScheme.background.toArgb())
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
