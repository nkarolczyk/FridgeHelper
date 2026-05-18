package com.example.fridgehelper.ui.fridge

import androidx.compose.foundation.border
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
import com.example.fridgehelper.ExpiryStatus
import com.example.fridgehelper.daysLeft
import com.example.fridgehelper.expiryStatus
import com.example.fridgehelper.data.db.Product
import com.example.fridgehelper.ui.Screen
import com.example.fridgehelper.ui.theme.Amber100
import com.example.fridgehelper.ui.theme.Amber900
import com.example.fridgehelper.ui.theme.CardBorderExpired
import com.example.fridgehelper.ui.theme.CardBorderWarn
import com.example.fridgehelper.ui.theme.Coral100
import com.example.fridgehelper.ui.theme.Coral700
import com.example.fridgehelper.ui.theme.Coral900
import com.example.fridgehelper.ui.theme.Green100
import com.example.fridgehelper.ui.theme.Green700
import com.example.fridgehelper.ui.theme.StatusOkText
import com.example.fridgehelper.ui.theme.TextPrimary
import com.example.fridgehelper.ui.theme.TextSecondary
import com.example.fridgehelper.ui.theme.TextTertiary
import com.example.fridgehelper.ui.theme.fridgeTopBarColors
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeScreen(
    navController: NavController,
    viewModel: FridgeViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToRemove by remember { mutableStateOf<Product?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FRIDGE") },
                colors = fridgeTopBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // mały FAB do skanowania kodu kreskowego
                SmallFloatingActionButton(
                    onClick = { navController.navigate(Screen.Scanner.route) },
                    containerColor = Green700,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.CameraAlt, "Scan")
                }
                Spacer(Modifier.height(8.dp))
                // główny FAB do ręcznego dodania produktu
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Green700,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Add product")
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
                    "Fridge is empty.\nAdd products with the + button",
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
                        onDelete = { productToRemove = product },
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

    productToRemove?.let { product ->
        RemoveProductDialog(
            productName = product.name,
            onUsed = {
                viewModel.markAsUsed(product)
                productToRemove = null
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Marked as Used",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.restoreProduct(product)
                }
            },
            onWasted = {
                viewModel.markAsWasted(product)
                productToRemove = null
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Marked as Wasted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.restoreProduct(product)
                }
            },
            onDismiss = { productToRemove = null }
        )
    }
}

@Composable
fun ProductCard(product: Product, onDelete: () -> Unit, onEdit: () -> Unit) {
    val now = System.currentTimeMillis()
    val daysLeft = daysLeft(product.expiryDate, now)
    val status = expiryStatus(product.expiryDate, now)

    // trzy warianty wizualne karty zależne od statusu produktu
    val cardBg = when (status) {
        ExpiryStatus.EXPIRED -> Coral100          // przeterminowany — pastelowy koral
        ExpiryStatus.WARNING -> Amber100          // wygasa — pastelowy bursztyn
        ExpiryStatus.OK      -> Green100          // ok — pastelowa zieleń
    }
    val cardBorder = when (status) {
        ExpiryStatus.EXPIRED -> CardBorderExpired // koral
        ExpiryStatus.WARNING -> CardBorderWarn    // bursztyn
        ExpiryStatus.OK      -> TextTertiary      // zieleń (#B2D9A6)
    }
    val statusColor = when (status) {
        ExpiryStatus.EXPIRED -> Coral900          // tekst "Expired!"
        ExpiryStatus.WARNING -> Amber900          // tekst "X days left"
        ExpiryStatus.OK      -> StatusOkText      // tekst "X days left" (zielony)
    }
    val statusText = when {
        daysLeft < 0  -> "Expired!"
        daysLeft == 0 -> "Expires today"
        daysLeft == 1 -> "1 day left"
        else          -> "$daysLeft days left"
    }

    val hasNutrition = product.calories != null || product.protein != null ||
            product.fat != null || product.carbs != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
                Text(
                    "Expires: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(product.expiryDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                if (hasNutrition) {
                    Spacer(Modifier.height(6.dp))
                    // badges wartości odżywczych — kcal czerwony, białko niebieski, tłuszcz bursztyn, węgle zielony
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        product.calories?.let {
                            NutritionBadge(label = "kcal", value = it.toInt().toString(), color = Color(0xFFE57373))
                        }
                        product.protein?.let {
                            NutritionBadge(label = "P", value = "%.1fg".format(it), color = Color(0xFF64B5F6))
                        }
                        product.fat?.let {
                            NutritionBadge(label = "F", value = "%.1fg".format(it), color = Color(0xFFFFB74D))
                        }
                        product.carbs?.let {
                            NutritionBadge(label = "C", value = "%.1fg".format(it), color = Color(0xFF81C784))
                        }
                    }
                    Text(
                        "per 100g",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
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
    val selectedDateLabel = datePickerState.selectedDateMillis?.let { fmt.format(Date(it)) } ?: "Pick date"

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Expiry date: $selectedDateLabel")
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
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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
    val selectedDateLabel = datePickerState.selectedDateMillis?.let { fmt.format(Date(it)) } ?: "Pick date"

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Expiry date: $selectedDateLabel")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    val date = datePickerState.selectedDateMillis ?: product.expiryDate
                    onConfirm(product.copy(name = name, expiryDate = date))
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun RemoveProductDialog(
    productName: String,
    onUsed: () -> Unit,
    onWasted: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove \"$productName\"") },
        text = { Text("How did it leave the fridge?") },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onWasted,
                    colors = ButtonDefaults.buttonColors(containerColor = Coral700)
                ) { Text("Wasted") }
                Button(
                    onClick = onUsed,
                    colors = ButtonDefaults.buttonColors(containerColor = Green700)
                ) { Text("Used") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
