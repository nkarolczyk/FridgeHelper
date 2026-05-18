package com.example.fridgehelper.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fridgehelper.data.db.MonthStat
import com.example.fridgehelper.data.repository.FridgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    repository: FridgeRepository
) : ViewModel() {

    // Flow z bazy ciagle
    val usedByMonth: StateFlow<List<MonthStat>> = repository.getMonthlyUsed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wastedByMonth: StateFlow<List<MonthStat>> = repository.getMonthlyWasted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

