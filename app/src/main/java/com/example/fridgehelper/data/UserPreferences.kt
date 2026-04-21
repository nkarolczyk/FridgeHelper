package com.example.fridgehelper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// rozszerzenie tworzące jeden datastore dla całej aplikacji
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        val KEY_DAYS_THRESHOLD = intPreferencesKey("days_threshold")
        // domyślnie powiadamia 3 dni przed końcem
        const val DEFAULT_THRESHOLD = 3
    }

    //ui reaguje automatycznie gdy wartość się zmieni
    val daysThreshold: Flow<Int> = context.dataStore.data
        .map { preferences ->
            // jeśli brak wpisu zwraca domyślną wartość
            preferences[KEY_DAYS_THRESHOLD] ?: DEFAULT_THRESHOLD
        }

    suspend fun setDaysThreshold(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DAYS_THRESHOLD] = days
        }
    }
}