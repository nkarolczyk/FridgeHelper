package com.example.fridgehelper.ui.fridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fridgehelper.data.db.Product
import com.example.fridgehelper.data.repository.FridgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


// pobiera dane z repo i daje UI
@HiltViewModel
class FridgeViewModel @Inject constructor(
    private val repository: FridgeRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addProduct(name: String, expiryDate: Long) {
        viewModelScope.launch {
            repository.addProduct(
                Product(
                    name = name,
                    expiryDate = expiryDate
                )
            )
        }
    }

    fun removeProduct(product: Product) {
        viewModelScope.launch {
            repository.removeProduct(product)
        }
    }
}