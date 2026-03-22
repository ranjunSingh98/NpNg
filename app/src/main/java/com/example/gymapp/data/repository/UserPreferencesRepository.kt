package com.example.gymapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val CATEGORY_ORDER = stringPreferencesKey("category_order")
        val HAS_SEEN_UPDATE_02 = booleanPreferencesKey("has_seen_update_02")
    }

    val categoryOrder: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            val orderString = preferences[PreferencesKeys.CATEGORY_ORDER] ?: ""
            if (orderString.isEmpty()) emptyList() else orderString.split(",")
        }

    val hasSeenUpdate02: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_UPDATE_02] ?: false
        }

    suspend fun saveCategoryOrder(order: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CATEGORY_ORDER] = order.joinToString(",")
        }
    }

    suspend fun setHasSeenUpdate02(hasSeen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_UPDATE_02] = hasSeen
        }
    }
}
