package com.example.fridgehelper.ui.recipes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fridgehelper.BuildConfig
import com.example.fridgehelper.data.api.ExtendedIngredientDto
import com.example.fridgehelper.data.api.InstructionDto
import com.example.fridgehelper.data.api.RecipeDetailDto
import com.example.fridgehelper.data.api.SpoonacularApi
import com.example.fridgehelper.data.db.CachedRecipeDetail
import com.example.fridgehelper.data.db.CachedRecipeDetailDao
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
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
    private val cachedRecipeDetailDao: CachedRecipeDetailDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recipeId: Int = checkNotNull(savedStateHandle["recipeId"])
    private val gson = Gson()

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
                cachedRecipeDetailDao.insert(detail.toEntity())
                cachedRecipeDetailDao.deleteOlderThan(System.currentTimeMillis() - 30 * 24L * 60 * 60 * 1000)
                _uiState.value = RecipeDetailUiState.Success(detail)
            } catch (e: Exception) {
                // przy błędzie sieciowym próbuj cache (ten sam przepis mógł być otwarty wcześniej)
                val cached = cachedRecipeDetailDao.getById(recipeId)
                if (cached != null) {
                    _uiState.value = RecipeDetailUiState.Success(cached.toDto())
                } else {
                    val msg = when {
                        e is UnknownHostException || e is SocketTimeoutException || e is IOException ->
                            "No internet connection :("
                        e is HttpException && e.code() == 402 ->
                            "API limit reached. Try again tomorrow."
                        else -> "Failed to load recipe :("
                    }
                    _uiState.value = RecipeDetailUiState.Error(msg)
                }
            }
        }
    }

    private fun RecipeDetailDto.toEntity() = CachedRecipeDetail(
        id = id,
        title = title,
        imageUrl = imageUrl,
        readyInMinutes = readyInMinutes,
        servings = servings,
        summary = summary,
        instructionsJson = gson.toJson(instructions),
        ingredientsJson = gson.toJson(ingredients)
    )

    private fun CachedRecipeDetail.toDto() = RecipeDetailDto(
        id = id,
        title = title,
        imageUrl = imageUrl,
        readyInMinutes = readyInMinutes,
        servings = servings,
        summary = summary,
        instructions = gson.fromJson(instructionsJson, Array<InstructionDto>::class.java)?.toList(),
        ingredients = gson.fromJson(ingredientsJson, Array<ExtendedIngredientDto>::class.java)?.toList()
    )
}
