package com.example.fridgehelper.ui.fridge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fridgehelper.data.db.Product
import com.example.fridgehelper.ui.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeScreen(
    navController: NavController,
    viewModel: FridgeViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("LODÓWKA") }) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { navController.navigate(Screen.Scanner.route) }
                ) {
                    Icon(Icons.Default.CameraAlt, "Skanuj")
                }
                Spacer(Modifier.height(8.dp))
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "Dodaj produkt")
                }
            }
        }
    ) { padding ->
        if (products.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Lodówka jest pusta.\nDodaj produkty przyciskiem +",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onDelete = { viewModel.removeProduct(product) },
                        onEdit = { productToEdit = product }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            onConfirm = { name, date ->
                viewModel.addProduct(name, date)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            onConfirm = { updatedProduct ->
                viewModel.updateProduct(updatedProduct)
                productToEdit = null
            },
            onDismiss = { productToEdit = null }
        )
    }
}

@Composable
fun ProductCard(product: Product, onDelete: () -> Unit, onEdit: () -> Unit) {
    val daysLeft = ((product.expiryDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    val cardColor = when {
        daysLeft < 0  -> MaterialTheme.colorScheme.errorContainer
        daysLeft <= 2 -> Color(0xFFFFF3CD)
        else          -> MaterialTheme.colorScheme.surfaceVariant
    }
    val statusText = when {
        daysLeft < 0  -> "Przeterminowany!"
        daysLeft == 0 -> "Kończy się dziś"
        daysLeft == 1 -> "Zostaje 1 dzień"
        else          -> "Zostaje $daysLeft dni"
    }

    val hasNutrition = product.calories != null || product.protein != null ||
            product.fat != null || product.carbs != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Ważny do: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(product.expiryDate))}",
                    style = MaterialTheme.typography.bodySmall
                )

                if (hasNutrition) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        product.calories?.let {
                            NutritionBadge(label = "kcal", value = it.toInt().toString(), color = Color(0xFFE57373))
                        }
                        product.protein?.let {
                            NutritionBadge(label = "B", value = "%.1fg".format(it), color = Color(0xFF64B5F6))
                        }
                        product.fat?.let {
                            NutritionBadge(label = "T", value = "%.1fg".format(it), color = Color(0xFFFFB74D))
                        }
                        product.carbs?.let {
                            NutritionBadge(label = "W", value = "%.1fg".format(it), color = Color(0xFF81C784))
                        }
                    }
                    Text(
                        "na 100g",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edytuj")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń")
            }
        }
    }
}

@Composable
private fun NutritionBadge(label: String, value: String, color: Color) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, style = MaterialTheme.typography.labelSmall)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(onConfirm: (String, Long) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() + 7 * 24L * 60 * 60 * 1000
    )
    val fmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val selectedDateLabel = datePickerState.selectedDateMillis?.let { fmt.format(Date(it)) } ?: "Wybierz datę"

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj produkt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa produktu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Data ważności: $selectedDateLabel")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    val date = datePickerState.selectedDateMillis
                        ?: (System.currentTimeMillis() + 7 * 24L * 60 * 60 * 1000)
                    onConfirm(name, date)
                }
            }) { Text("Dodaj") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductDialog(
    product: Product,
    onConfirm: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = product.expiryDate
    )
    val fmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val selectedDateLabel = datePickerState.selectedDateMillis?.let { fmt.format(Date(it)) } ?: "Wybierz datę"

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj produkt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa produktu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Data ważności: $selectedDateLabel")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    val date = datePickerState.selectedDateMillis ?: product.expiryDate
                    onConfirm(product.copy(name = name, expiryDate = date))
                }
            }) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}
