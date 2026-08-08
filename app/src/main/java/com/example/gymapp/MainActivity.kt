package com.example.gymapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.gymapp.ui.navigation.NpNgNavGraph
import com.example.gymapp.ui.theme.NpNgTheme
import com.example.gymapp.data.model.ThemeMode
import com.example.gymapp.data.model.usesDarkTheme
import com.example.gymapp.data.repository.UserPreferencesRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        
        // Standard edge-to-edge configuration for best compatibility
        enableEdgeToEdge()

        val userPreferencesRepository = UserPreferencesRepository(applicationContext)
        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsState(
                initial = ThemeMode.Dark,
            )
            NpNgTheme(
                darkTheme = themeMode.usesDarkTheme(isSystemInDarkTheme()),
            ) {
                val navController = rememberNavController()
                NpNgNavGraph(
                    navController = navController,
                    userPreferencesRepository = userPreferencesRepository,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
