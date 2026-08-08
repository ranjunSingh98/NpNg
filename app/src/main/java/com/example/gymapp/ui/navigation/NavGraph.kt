package com.example.gymapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gymapp.data.WorkoutDatabase
import com.example.gymapp.data.repository.WorkoutRepository
import com.example.gymapp.data.repository.UserPreferencesRepository
import com.example.gymapp.ui.screens.ActiveWorkoutScreen
import com.example.gymapp.ui.screens.DashboardScreen
import com.example.gymapp.ui.screens.HistoryScreen
import com.example.gymapp.ui.screens.InsightsScreen
import com.example.gymapp.ui.screens.SettingsScreen
import com.example.gymapp.ui.viewmodel.WorkoutViewModel
import androidx.compose.ui.platform.LocalContext

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object ActiveWorkout : Screen("active_workout/{workoutType}?sessionId={sessionId}") {
        fun createRoute(workoutType: String, sessionId: Long? = null) =
            "active_workout/$workoutType" + (sessionId?.let { "?sessionId=$it" } ?: "")
    }
    object History : Screen("history")
    object Insights : Screen("insights")
    object Settings : Screen("settings")
}

@Composable
fun NpNgNavGraph(
    navController: NavHostController,
    userPreferencesRepository: UserPreferencesRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Ensure repositories and factory are stable across recompositions
    val viewModel: WorkoutViewModel = viewModel(
        factory = remember {
            val database = WorkoutDatabase.getDatabase(context)
            val workoutRepository = WorkoutRepository(database.workoutDao())
            WorkoutViewModel.Factory(workoutRepository, userPreferencesRepository)
        }
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onWorkoutSelected = { workoutType, sessionId ->
                    navController.navigate(Screen.ActiveWorkout.createRoute(workoutType, sessionId))
                },
                onViewHistory = {
                    navController.navigate(Screen.History.route)
                },
                onViewInsights = {
                    navController.navigate(Screen.Insights.route)
                },
                onViewSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(
            route = Screen.ActiveWorkout.route,
            arguments = listOf(
                navArgument("workoutType") { type = NavType.StringType },
                navArgument("sessionId") { 
                    type = NavType.LongType
                    defaultValue = -1L 
                }
            )
        ) { backStackEntry ->
            val workoutType = backStackEntry.arguments?.getString("workoutType") ?: ""
            val sessionId = backStackEntry.arguments?.getLong("sessionId").takeIf { it != -1L }
            
            ActiveWorkoutScreen(
                workoutType = workoutType,
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                },
                initialSessionId = sessionId
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onWorkoutSelected = { workoutType, sessionId ->
                    navController.navigate(Screen.ActiveWorkout.createRoute(workoutType, sessionId))
                }
            )
        }
        composable(Screen.Insights.route) {
            InsightsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
