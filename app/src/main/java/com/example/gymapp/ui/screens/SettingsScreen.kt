package com.example.gymapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymapp.R
import com.example.gymapp.data.model.ThemeMode
import com.example.gymapp.data.model.WeightUnit
import com.example.gymapp.ui.components.ThemeModeDialog
import com.example.gymapp.ui.components.WeightUnitDialog
import com.example.gymapp.ui.components.labelResource
import com.example.gymapp.ui.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showWeightUnitDialog by rememberSaveable { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeModeDialog(
            selectedMode = themeMode,
            onModeSelected = {
                viewModel.setThemeMode(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showWeightUnitDialog) {
        WeightUnitDialog(
            selectedUnit = weightUnit,
            onUnitSelected = {
                viewModel.setWeightUnit(it)
                showWeightUnitDialog = false
            },
            onDismiss = { showWeightUnitDialog = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        SettingsContent(
            themeMode = themeMode,
            weightUnit = weightUnit,
            onThemeClick = { showThemeDialog = true },
            onWeightUnitClick = { showWeightUnitDialog = true },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
internal fun SettingsContent(
    themeMode: ThemeMode,
    weightUnit: WeightUnit,
    onThemeClick: () -> Unit,
    onWeightUnitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsSectionHeading(stringResource(R.string.settings_appearance_heading))
        SettingsRow(
            title = stringResource(R.string.theme_title),
            summary = stringResource(themeMode.labelResource()),
            onClick = onThemeClick,
        )

        SettingsSectionHeading(
            text = stringResource(R.string.settings_measurements_heading),
            modifier = Modifier.padding(top = 12.dp),
        )
        SettingsRow(
            title = stringResource(R.string.weight_units_title),
            summary = "${weightUnit.displayName} (${weightUnit.symbol})",
            onClick = onWeightUnitClick,
        )
    }
}

@Composable
private fun SettingsSectionHeading(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun SettingsRow(
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$title, current setting $summary" },
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
