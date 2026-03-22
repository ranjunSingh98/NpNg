package com.example.gymapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val CATEGORY_ORDER = stringPreferencesKey("category_order")
    }

    val categoryOrder: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            val orderString = preferences[PreferencesKeys.CATEGORY_ORDER] ?: ""
            if (orderString.isEmpty()) emptyList() else orderString.split(",")
        }

    suspend fun saveCategoryOrder(order: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CATEGORY_ORDER] = order.joinToString(",")
        }
    }
}
