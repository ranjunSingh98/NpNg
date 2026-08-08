package com.example.gymapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gymapp.data.model.ThemeMode
import com.example.gymapp.data.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val CATEGORY_ORDER = stringPreferencesKey("category_order")
        val HAS_SEEN_UPDATE_03 = booleanPreferencesKey("has_seen_update_03")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
    }

    val categoryOrder: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            val orderString = preferences[PreferencesKeys.CATEGORY_ORDER] ?: ""
            if (orderString.isEmpty()) emptyList() else orderString.split(",")
        }

    val hasSeenUpdate03: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_UPDATE_03] ?: false
        }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            ThemeMode.fromPreferenceValue(preferences[PreferencesKeys.THEME_MODE])
        }

    val weightUnit: Flow<WeightUnit> = context.dataStore.data
        .map { preferences ->
            WeightUnit.fromPreferenceValue(preferences[PreferencesKeys.WEIGHT_UNIT])
        }

    suspend fun saveCategoryOrder(order: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CATEGORY_ORDER] = order.joinToString(",")
        }
    }

    suspend fun setHasSeenUpdate03(hasSeen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_UPDATE_03] = hasSeen
        }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.preferenceValue
        }
    }

    suspend fun setWeightUnit(weightUnit: WeightUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = weightUnit.preferenceValue
        }
    }
}
