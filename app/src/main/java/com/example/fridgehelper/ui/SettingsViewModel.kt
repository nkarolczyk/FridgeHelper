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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // UI obserwuje ten StateFlow — reaguje natychmiast na zmianę
    val daysThreshold: StateFlow<Int> = userPreferences.daysThreshold
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences.DEFAULT_THRESHOLD
        )

    fun setThreshold(days: Int) {
        viewModelScope.launch {
            userPreferences.setDaysThreshold(days)
            // przeplanuj Workera z nowym progiem
            FridgeApp.scheduleExpiryCheck(context, days)
        }
    }
}