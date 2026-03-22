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
import com.example.gymapp.ui.viewmodel.WorkoutViewModel
import androidx.compose.ui.platform.LocalContext

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object ActiveWorkout : Screen("active_workout/{workoutType}") {
        fun createRoute(workoutType: String) = "active_workout/$workoutType"
    }
    object History : Screen("history")
}

@Composable
fun NpNgNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Ensure repositories and factory are stable across recompositions
    val viewModel: WorkoutViewModel = viewModel(
        factory = remember {
            val database = WorkoutDatabase.getDatabase(context)
            val workoutRepository = WorkoutRepository(database.workoutDao())
            val userPreferencesRepository = UserPreferencesRepository(context)
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
                onWorkoutTypeSelected = { workoutType ->
                    navController.navigate(Screen.ActiveWorkout.createRoute(workoutType))
                },
                onViewHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }
        composable(
            route = Screen.ActiveWorkout.route,
            arguments = listOf(navArgument("workoutType") { type = NavType.StringType })
        ) { backStackEntry ->
            val workoutType = backStackEntry.arguments?.getString("workoutType") ?: ""
            ActiveWorkoutScreen(
                workoutType = workoutType,
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
