package com.example.fridgehelper.ui.recipes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fridgehelper.BuildConfig
import com.example.fridgehelper.data.api.RecipeDetailDto
import com.example.fridgehelper.data.api.SpoonacularApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

sealed class RecipeDetailUiState {
    object Loading : RecipeDetailUiState()
    data class Success(val recipe: RecipeDetailDto) : RecipeDetailUiState()
    data class Error(val message: String) : RecipeDetailUiState()
}

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val spoonacularApi: SpoonacularApi,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recipeId: Int = checkNotNull(savedStateHandle["recipeId"])

    private val _uiState = MutableStateFlow<RecipeDetailUiState>(RecipeDetailUiState.Loading)
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = RecipeDetailUiState.Loading
            try {
                val detail = spoonacularApi.getRecipeDetail(recipeId, BuildConfig.SPOONACULAR_API_KEY)
                _uiState.value = RecipeDetailUiState.Success(detail)
            } catch (e: Exception) {
                val msg = when (e) {
                    is UnknownHostException, is SocketTimeoutException, is IOException ->
                        "No internet connection :("
                    else -> "Failed to load recipe :("
                }
                _uiState.value = RecipeDetailUiState.Error(msg)
            }
        }
    }
}
