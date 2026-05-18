package com.example.fridgehelper.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fridgehelper.FridgeApp
import com.example.fridgehelper.data.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // UI obserwuje StateFlow — reaguje natychmiast na zmianę
    val daysThreshold: StateFlow<Int> = userPreferences.daysThreshold
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences.DEFAULT_THRESHOLD
        )

    // para (hour, minute) jako jeden flow
    val notifyTime: StateFlow<Pair<Int, Int>> = userPreferences.notifyTime
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences.DEFAULT_NOTIFY_HOUR to UserPreferences.DEFAULT_NOTIFY_MINUTE
        )

    fun setThreshold(days: Int) {
        viewModelScope.launch {
            userPreferences.setDaysThreshold(days)
            val (hour, minute) = userPreferences.notifyTime.first()
            FridgeApp.scheduleExpiryCheck(context, days, hour, minute)
        }
    }

    fun setNotifyTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferences.setNotifyTime(hour, minute)
            // przy zmianie godziny przelicza opóźnienie zachowując obecny próg dni
            FridgeApp.scheduleExpiryCheck(context, daysThreshold.value, hour, minute)
        }
    }
}