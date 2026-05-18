package com.example.fridgehelper.ui.recipes

import android.os.Build
import android.text.Html
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fridgehelper.data.api.ExtendedIngredientDto
import com.example.fridgehelper.data.api.RecipeDetailDto
import com.example.fridgehelper.ui.theme.fridgeTopBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? RecipeDetailUiState.Success)?.recipe?.title ?: "Recipe"
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = fridgeTopBarColors()
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is RecipeDetailUiState.Loading -> CircularProgressIndicator()
                is RecipeDetailUiState.Error -> DetailErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadDetail() }
                )
                is RecipeDetailUiState.Success -> RecipeDetailContent(
                    recipe = state.recipe,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopStart)
                )
            }
        }
    }
}

@Composable
private fun RecipeDetailContent(recipe: RecipeDetailDto, modifier: Modifier = Modifier) {
    val steps = recipe.instructions?.flatMap { it.steps ?: emptyList() } ?: emptyList()
    val ingredients = recipe.ingredients ?: emptyList()

    LazyColumn(modifier = modifier) {
        item {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = recipe.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )
        }

        // tytuł, meta (czas/porcje), opis
        item {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(recipe.title, style = MaterialTheme.typography.headlineSmall)

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    recipe.readyInMinutes?.let { minutes ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("$minutes min", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    recipe.servings?.let { servings ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("$servings servings", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // summary z API
                recipe.summary?.takeIf { it.isNotBlank() }?.let { rawHtml ->
                    val plainSummary = remember(rawHtml) {
                        @Suppress("DEPRECATION")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            Html.fromHtml(rawHtml, Html.FROM_HTML_MODE_COMPACT).toString().trim()
                        else
                            Html.fromHtml(rawHtml).toString().trim()
                    }
                    if (plainSummary.isNotBlank()) {
                        Text(
                            plainSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // sekcja składników
        if (ingredients.isNotEmpty()) {
            item {
                Text(
                    "Ingredients",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            items(ingredients, key = { "${it.name}_${it.amount}" }) { ingredient ->
                IngredientRow(ingredient)
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }

        item {
            if (steps.isNotEmpty()) {
                Text(
                    "Preparation",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            } else {
                Text(
                    "No preparation steps for this recipe.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        itemsIndexed(steps, key = { _, step -> step.number }) { _, step ->
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            step.number.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    step.step,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun IngredientRow(ingredient: ExtendedIngredientDto) {
    val amountStr = if (ingredient.amount % 1 == 0.0)
        ingredient.amount.toInt().toString()
    else
        "%.1f".format(ingredient.amount)

    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            "·",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            buildString {
                if (ingredient.amount > 0) {
                    append(amountStr)
                    if (ingredient.unit.isNotBlank()) append(" ${ingredient.unit}")
                    append(" ")
                }
                append(ingredient.name)
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DetailErrorView(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(24.dp)
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry) { Text("Try again") }
    }
}
