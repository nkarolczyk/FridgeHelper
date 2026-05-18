package com.example.fridgehelper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.combine
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
        val KEY_NOTIFY_HOUR   = intPreferencesKey("notify_hour")
        val KEY_NOTIFY_MINUTE = intPreferencesKey("notify_minute")
        // domyślnie powiadamia 3 dni przed końcem
        const val DEFAULT_THRESHOLD    = 3
        const val DEFAULT_NOTIFY_HOUR   = 9
        const val DEFAULT_NOTIFY_MINUTE = 0
    }

    //ui reaguje automatycznie gdy wartość się zmieni
    val daysThreshold: Flow<Int> = context.dataStore.data
        .map { preferences ->
            // jeśli brak wpisu zwraca domyślną wartość
            preferences[KEY_DAYS_THRESHOLD] ?: DEFAULT_THRESHOLD
        }

    // godzina i minuta powiadomień jako jedna para (UI obserwuje)
    val notifyTime: Flow<Pair<Int, Int>> = combine(
        context.dataStore.data.map { it[KEY_NOTIFY_HOUR]   ?: DEFAULT_NOTIFY_HOUR },
        context.dataStore.data.map { it[KEY_NOTIFY_MINUTE] ?: DEFAULT_NOTIFY_MINUTE }
    ) { hour, minute -> hour to minute }

    suspend fun setDaysThreshold(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DAYS_THRESHOLD] = days
        }
    }

    suspend fun setNotifyTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFY_HOUR]   = hour
            preferences[KEY_NOTIFY_MINUTE] = minute
        }
    }
}