package com.example.gymapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.gymapp.R
import com.example.gymapp.data.model.ThemeMode

@Composable
fun ThemeModeDialog(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme_title)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.theme_explanation),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = mode == selectedMode,
                                onClick = { onModeSelected(mode) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = mode == selectedMode, onClick = null)
                        Text(
                            text = stringResource(mode.labelResource()),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_done))
            }
        },
    )
}

fun ThemeMode.labelResource(): Int = when (this) {
    ThemeMode.System -> R.string.theme_system
    ThemeMode.Dark -> R.string.theme_dark
    ThemeMode.Light -> R.string.theme_light
}
