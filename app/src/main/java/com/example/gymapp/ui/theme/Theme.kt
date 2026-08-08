package com.example.gymapp.ui.theme

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    background = Background,
    surface = Surface,
    primary = Primary,
    onPrimary = OnPrimary,
    onBackground = OnBackground,
    onSurface = OnBackground,
    surfaceVariant = Color(0xFF2B3135),
    onSurfaceVariant = Color(0xFFC0C8CD),
    primaryContainer = Color(0xFF004C6A),
    onPrimaryContainer = Color(0xFFC3E8FF),
    secondary = SecondaryText,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3C484E),
    onSecondaryContainer = Color(0xFFD8E4EA),
    outline = Color(0xFF8A9297),
    outlineVariant = Color(0xFF41484D),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00658A),
    onPrimary = Color.White,
    background = Color(0xFFF7F9FC),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF191C1E),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFE1E7EC),
    onSurfaceVariant = Color(0xFF41484D),
    primaryContainer = Color(0xFFC3E8FF),
    onPrimaryContainer = Color(0xFF001E2C),
    secondary = Color(0xFF4E616D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E5F0),
    onSecondaryContainer = Color(0xFF0A1E27),
    outline = Color(0xFF71787D),
    outlineVariant = Color(0xFFC1C7CC),
)

val LocalIsDarkTheme = staticCompositionLocalOf { true }

@Composable
fun NpNgTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // Set light/dark icons according to the theme
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
            
            // Allow drawing behind display cutout (notches, hole-punches) 
            // to maximize screen usage on tall screens like Samsung Galaxy.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes.layoutInDisplayCutoutMode = 
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}

fun Color.ensureMinimumContrast(
    background: Color,
    minimumRatio: Float = 4.5f,
): Color {
    fun ratio(first: Color, second: Color): Float {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    if (ratio(this, background) >= minimumRatio) return this

    val target = if (background.luminance() > 0.5f) Color.Black else Color.White
    for (step in 1..20) {
        val candidate = lerp(this, target, step / 20f)
        if (ratio(candidate, background) >= minimumRatio) return candidate
    }
    return target
}
