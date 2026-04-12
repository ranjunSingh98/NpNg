package com.example.gymapp.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.gymapp.R
import com.example.gymapp.ui.theme.AbsOrange
import com.example.gymapp.ui.theme.ArmsPurple
import com.example.gymapp.ui.theme.BackBlue
import com.example.gymapp.ui.theme.CardioRed
import com.example.gymapp.ui.theme.ChestYellow
import com.example.gymapp.ui.theme.LegsGreen
import com.example.gymapp.ui.theme.PullLime
import com.example.gymapp.ui.theme.PushCyan
import com.example.gymapp.ui.theme.ShouldersTeal

data class WorkoutCategory(
    val name: String,
    val accentColor: Color,
    val iconRes: Int? = null,
    val imageVector: ImageVector? = null,
    val tintIcon: Boolean = true
) {
    init {
        require(iconRes != null || imageVector != null) {
            "Either iconRes or imageVector must be non-null"
        }
    }

    companion object {
        val categories = listOf(
            WorkoutCategory("Legs", LegsGreen, iconRes = R.drawable.ic_legs),
            WorkoutCategory("Back", BackBlue, iconRes = R.drawable.ic_back),
            WorkoutCategory("Chest", ChestYellow, iconRes = R.drawable.ic_chest),
            WorkoutCategory("Arms", ArmsPurple, iconRes = R.drawable.ic_arms),
            WorkoutCategory("Shoulders", ShouldersTeal, iconRes = R.drawable.ic_shoulders),
            WorkoutCategory("Push", PushCyan, iconRes = R.drawable.ic_push),
            WorkoutCategory("Pull", PullLime, iconRes = R.drawable.ic_pull),
            WorkoutCategory("Abs", AbsOrange, iconRes = R.drawable.ic_abs),
            WorkoutCategory("Cardio", CardioRed, iconRes = R.drawable.ic_cardio)
        )

        fun getByName(name: String): WorkoutCategory? {
            return categories.find { it.name.equals(name, ignoreCase = true) }
        }
    }
}
