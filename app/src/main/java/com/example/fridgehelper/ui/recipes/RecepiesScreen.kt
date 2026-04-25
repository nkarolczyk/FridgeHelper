package com.example.fridgehelper.ui.recipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fridgehelper.data.api.RecipeDto
import com.example.fridgehelper.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    navController: NavController,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Przepisy") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            // każdy stan ui ma swój composable
            when (val state = uiState) {
                is RecipesUiState.Idle    -> {}
                is RecipesUiState.Loading -> CircularProgressIndicator()
                is RecipesUiState.Empty   -> EmptyFridgeView()
                is RecipesUiState.Error   -> ErrorView(message = state.message) {
                    viewModel.loadRecipes()
                }
                is RecipesUiState.Success -> RecipeList(
                    recipes = state.recipes,
                    fromCache = state.fromCache,
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun RecipeList(
    recipes: List<RecipeDto>,
    fromCache: Boolean,
    onRecipeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (fromCache) {
            item {
                Text(
                    "Wyniki z pamięci podręcznej",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
        items(recipes, key = { it.id }) { recipe ->
            RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
        }
    }
}

@Composable
private fun RecipeCard(recipe: RecipeDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // asyncimage z coil — ładuje obrazek z url asynchronicznie
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis // "..." gdy tytuł za długi
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // zielony badge — ile składników z lodówki pasuje
                    IngredientBadge(
                        count = recipe.usedIngredients,
                        label = "masz",
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    // czerwony badge — ile składników brakuje
                    IngredientBadge(
                        count = recipe.missedIngredients,
                        label = "brakuje",
                        color = MaterialTheme.colorScheme.errorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientBadge(count: Int, label: String, color: Color) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color
    ) {
        Text(
            "$count $label",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun EmptyFridgeView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Lodówka jest pusta", style = MaterialTheme.typography.titleMedium)
        Text(
            "Dodaj produkty, żeby zobaczyć przepisy.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry) { Text("Spróbuj ponownie") }
    }
}