package com.example.fridgehelper.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fridgehelper.BuildConfig
import com.example.fridgehelper.data.api.RecipeDto
import com.example.fridgehelper.data.api.SpoonacularApi
import com.example.fridgehelper.data.db.CachedRecipe
import com.example.fridgehelper.data.db.CachedRecipeDao
import com.example.fridgehelper.data.repository.FridgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// stany ekranu przepisów
sealed class RecipesUiState {
    object Idle : RecipesUiState() // przed pierwszym załadowaniem
    object Loading : RecipesUiState()
    object Empty : RecipesUiState() //lodówka pusta (nie ma składników)
    data class Success(val recipes: List<RecipeDto>, val fromCache: Boolean = false) : RecipesUiState()
    data class Error(val message: String) : RecipesUiState()
}

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val spoonacularApi: SpoonacularApi,
    private val repository: FridgeRepository,
    private val cachedRecipeDao: CachedRecipeDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecipesUiState>(RecipesUiState.Idle)
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    companion object {
        // cache ważny przez 1h — po tym czasie pobiera świeże dane z api
        private const val CACHE_TTL_MS = 60 * 60 * 1000L
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = RecipesUiState.Loading
            val products = repository.allProducts.first()

            if (products.isEmpty()) {
                _uiState.value = RecipesUiState.Empty
                return@launch
            }

            // sprawdza czy cache jest świeższy niż 1h
            val lastCachedAt = cachedRecipeDao.getLastCachedAt()
            val cacheIsFresh = lastCachedAt != null &&
                    (System.currentTimeMillis() - lastCachedAt) < CACHE_TTL_MS

            if (cacheIsFresh) {
                // zwraca zapisane wyniki bez odpytywania api
                val cached = cachedRecipeDao.getAll()
                _uiState.value = RecipesUiState.Success(
                    recipes = cached.toRecipeDtoList(),
                    fromCache = true
                )
                return@launch
            }

            // składniki jako jeden string
            val ingredients = products.joinToString(",") { it.name }

            try {
                val recipes = spoonacularApi.findByIngredients(
                    ingredients = ingredients,
                    number = 10,
                    apiKey = BuildConfig.SPOONACULAR_API_KEY
                )

                if (recipes.isEmpty()) {
                    _uiState.value = RecipesUiState.Error("Nie znaleziono przepisów dla Twoich składników.")
                    return@launch
                }

                // czyści stary cache i zapisuje nowe wyniki
                cachedRecipeDao.clearAll()
                cachedRecipeDao.insertAll(recipes.toCachedRecipeList())

                _uiState.value = RecipesUiState.Success(recipes = recipes, fromCache = false)

            } catch (e: Exception) {
                // gdy api niedostępne pokazuje stary cache zamiast błędu
                val staleCache = cachedRecipeDao.getAll()
                if (staleCache.isNotEmpty()) {
                    _uiState.value = RecipesUiState.Success(
                        recipes = staleCache.toRecipeDtoList(),
                        fromCache = true
                    )
                } else {
                    _uiState.value = RecipesUiState.Error("blad połączenia: ${e.localizedMessage}")
                }
            }
        }
    }

    // konwersja encji room dto używanego przez ui
    private fun List<CachedRecipe>.toRecipeDtoList(): List<RecipeDto> =
        map { RecipeDto(
            id = it.id,
            title = it.title,
            imageUrl = it.imageUrl,
            usedIngredients = it.usedIngredients,
            missedIngredients = it.missedIngredients
        )}

    // konwersja dto encji room do zapisu w cache
    private fun List<RecipeDto>.toCachedRecipeList(): List<CachedRecipe> =
        map { CachedRecipe(
            id = it.id,
            title = it.title,
            imageUrl = it.imageUrl,
            usedIngredients = it.usedIngredients,
            missedIngredients = it.missedIngredients
        )}
}