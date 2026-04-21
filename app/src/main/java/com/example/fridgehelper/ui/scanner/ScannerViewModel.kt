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

// logika ekranu skanera — pobieranie danych po kodzie, stany ui, zapis do bazy

// możliwe stany ekranu skanera
sealed class ScannerUiState {
    object Scanning : ScannerUiState()  // kamera aktywna, czeka na kod
    object Loading : ScannerUiState()   // trwa zapytanie do open food facts
    data class Scanned(                 // kod zeskanowany — pokaż dialog z danymi produktu
        val barcode: String,
        val productName: String?,       // null gdy api nie zna produktu
        val calories: Double?,
        val protein: Double?,
        val fat: Double?,
        val carbs: Double?
    ) : ScannerUiState()
    object Saved : ScannerUiState()             // produkt zapisany — wróć do lodówki
    data class Error(val message: String) : ScannerUiState()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val openFoodApi: OpenFoodApi,
    private val repository: FridgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Scanning)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    // zapamiętuje ostatni kod żeby nie odpytywać api dwa razy dla tego samego kodu
    private var lastScannedBarcode: String? = null

    // wywoływane przez camerax gdy ml kit wykryje kod w kadrze
    fun onBarcodeDetected(barcode: String) {
        lookupBarcode(barcode)
    }

    // wywoływane gdy użytkownik ręcznie wpisze kod i kliknie szukaj
    fun onManualBarcodeEntered(barcode: String) {
        if (barcode.isBlank()) return
        lookupBarcode(barcode.trim())
    }

    private fun lookupBarcode(barcode: String) {
        // ignoruje nowe kody gdy już trwa ładowanie lub dialog jest otwarty
        if (_uiState.value != ScannerUiState.Scanning) return
        // ignoruje ten sam kod zeskanowany ponownie
        if (barcode == lastScannedBarcode) return
        lastScannedBarcode = barcode

        viewModelScope.launch {
            _uiState.value = ScannerUiState.Loading
            try {
                val response = openFoodApi.getProduct(barcode)
                // status 1 = produkt znaleziony w bazie open food facts
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
                // błąd sieci — otwiera dialog z pustą nazwą do ręcznego wpisania
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
        // pobiera wartości odżywcze ze stanu scanned żeby zapisać razem z produktem
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
            // saved powoduje automatyczny powrót do ekranu lodówki (launchedeffect w screen)
            _uiState.value = ScannerUiState.Saved
        }
    }

    // resetuje stan — użytkownik anulował dialog, może skanować ponownie
    fun resetToScanning() {
        lastScannedBarcode = null
        _uiState.value = ScannerUiState.Scanning
    }
}