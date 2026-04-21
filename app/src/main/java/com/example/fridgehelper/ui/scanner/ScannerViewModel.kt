package com.example.fridgehelper.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fridgehelper.data.api.OpenFoodApi
import com.example.fridgehelper.data.db.Product
import com.example.fridgehelper.data.repository.FridgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// logika ekranu skanera - pobieranie danych po kodzie kreskowym, stan UI oraz zapis produktu do bazy

sealed class ScannerUiState {
    object Scanning : ScannerUiState()
    object Loading : ScannerUiState()
    data class Scanned(
        val barcode: String,
        val productName: String?,
        val calories: Double?,
        val protein: Double?,
        val fat: Double?,
        val carbs: Double?
    ) : ScannerUiState()
    object Saved : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val openFoodApi: OpenFoodApi,
    private val repository: FridgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Scanning)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private var lastScannedBarcode: String? = null

    fun onBarcodeDetected(barcode: String) {
        lookupBarcode(barcode)
    }

    fun onManualBarcodeEntered(barcode: String) {
        if (barcode.isBlank()) return
        lookupBarcode(barcode.trim())
    }

    private fun lookupBarcode(barcode: String) {
        if (_uiState.value != ScannerUiState.Scanning) return
        if (barcode == lastScannedBarcode) return
        lastScannedBarcode = barcode

        //ZAPYTANIE DO API PO KODZIE KRESKOWYM
        viewModelScope.launch {
            _uiState.value = ScannerUiState.Loading
            try {
                val response = openFoodApi.getProduct(barcode)
                val product = if (response.status == 1) response.product else null
                val nutriments = product?.nutriments
                _uiState.value = ScannerUiState.Scanned(
                    barcode = barcode,
                    productName = product?.bestName(),
                    calories = nutriments?.caloriesPer100g,
                    protein = nutriments?.proteinPer100g,
                    fat = nutriments?.fatPer100g,
                    carbs = nutriments?.carbsPer100g
                )
            } catch (e: Exception) {
                _uiState.value = ScannerUiState.Scanned(
                    barcode = barcode,
                    productName = null,
                    calories = null,
                    protein = null,
                    fat = null,
                    carbs = null
                )
            }
        }
    }

    fun saveProduct(name: String, expiryDate: Long) {
        if (name.isBlank()) return
        val state = _uiState.value as? ScannerUiState.Scanned ?: return
        viewModelScope.launch {
            repository.addProduct(
                Product(
                    name = name,
                    barcode = lastScannedBarcode,
                    expiryDate = expiryDate,
                    calories = state.calories,
                    protein = state.protein,
                    fat = state.fat,
                    carbs = state.carbs
                )
            )
            _uiState.value = ScannerUiState.Saved
        }
    }

    fun resetToScanning() {
        lastScannedBarcode = null
        _uiState.value = ScannerUiState.Scanning
    }
}