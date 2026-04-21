package com.example.fridgehelper.ui.scanner

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors



// cały interfejs ekranu skanowania
// obsługę kamery, wykrywanie kodów kreskowych, ręczne wpisywanie kodu
//  wyświetlanie formularza dodania produktu po skanie


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    navController: NavController,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {   //przyznanie uprawnien dla kamery wazne!!
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(uiState) {
        if (uiState == ScannerUiState.Saved) navController.popBackStack()
    }

    // pole do recznego wpisywania kodu
    var manualBarcode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skanuj produkt") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // tu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = manualBarcode,
                    onValueChange = { manualBarcode = it.filter(Char::isDigit) },
                    label = { Text("Wpisz kod kreskowy") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.onManualBarcodeEntered(manualBarcode)
                            manualBarcode = ""
                        }
                    )
                )
                IconButton( //weryfukacja tego wpisanego kodu
                    onClick = {
                        viewModel.onManualBarcodeEntered(manualBarcode)
                        manualBarcode = ""
                    },
                    enabled = manualBarcode.isNotBlank() && uiState == ScannerUiState.Scanning
                ) {
                    Icon(Icons.Default.Search, "Szukaj")
                }
            }

            Divider()

            // PODGLAD KAMERY
            Box(modifier = Modifier.weight(1f)) {
                if (!hasCameraPermission) {
                    NoCameraPermissionView(
                        onRequestAgain = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                } else {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onBarcodeDetected = viewModel::onBarcodeDetected
                    )
                    ScannerOverlay()

                    if (uiState == ScannerUiState.Loading) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // dialog dodania produktu po kodzie
    val scannedState = uiState
    if (scannedState is ScannerUiState.Scanned) {
        ScannedProductDialog(
            state = scannedState,
            onConfirm = { name, expiryDate -> viewModel.saveProduct(name, expiryDate) },
            onDismiss = { viewModel.resetToScanning() }
        )
    }
}

// CameraX
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).also { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                processImageProxy(barcodeScanner, imageProxy, onBarcodeDetected)
                            }
                        }
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("ScannerScreen", "CameraX bind failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
        }
    )
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcodeDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    // przekaz klatki obrazu do ML Kit w celu odczytu kodu kreskowego
    barcodeScanner.process(image)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull { it.format == Barcode.FORMAT_EAN_13 }
                ?.rawValue?.let { onBarcodeDetected(it) }
        }
        .addOnFailureListener { Log.w("ScannerScreen", "ML Kit error", it) }
        .addOnCompleteListener { imageProxy.close() }
}

// Overlay celownika
@Composable
private fun ScannerOverlay() {
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.fillMaxWidth(0.75f).height(120.dp)) {
                CornerDecoration(Modifier.align(Alignment.TopStart), topLeft = true)
                CornerDecoration(Modifier.align(Alignment.TopEnd), topRight = true)
                CornerDecoration(Modifier.align(Alignment.BottomStart), bottomLeft = true)
                CornerDecoration(Modifier.align(Alignment.BottomEnd), bottomRight = true)
            }
            Spacer(Modifier.height(16.dp))
            Text("Ustaw kod EAN-13 w ramce", color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CornerDecoration(
    modifier: Modifier = Modifier,
    topLeft: Boolean = false,
    topRight: Boolean = false,
    bottomLeft: Boolean = false,
    bottomRight: Boolean = false,
) {
    Box(modifier.size(24.dp)) {
        if (topLeft || bottomLeft) Box(Modifier.width(3.dp).fillMaxHeight().background(Color.White))
        if (topRight || bottomRight) Box(Modifier.width(3.dp).fillMaxHeight().align(Alignment.TopEnd).background(Color.White))
        if (topLeft || topRight) Box(Modifier.fillMaxWidth().height(3.dp).background(Color.White))
        if (bottomLeft || bottomRight) Box(Modifier.fillMaxWidth().height(3.dp).align(Alignment.BottomStart).background(Color.White))
    }
}

// dialog po skanowaniu
@Composable
private fun ScannedProductDialog(
    state: ScannerUiState.Scanned,
    onConfirm: (name: String, expiryDate: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(state.productName ?: "") }
    var days by remember { mutableStateOf("7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj produkt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.productName == null) {
                    Text(
                        "Produkt nieznaleziony w bazie. Wpisz nazwę ręcznie.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("Kod: ${state.barcode}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

                // wartości odżywcze (jeśli dostępne)
                if (state.calories != null || state.protein != null || state.fat != null || state.carbs != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Wartości odżywcze / 100g", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                state.calories?.let { NutrientChip("⚡ ${it.toInt()} kcal") }
                                state.protein?.let { NutrientChip("💪 %.1fg B".format(it)) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                state.fat?.let { NutrientChip("🧈 %.1fg T".format(it)) }
                                state.carbs?.let { NutrientChip("🌾 %.1fg W".format(it)) }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa produktu") },
                    singleLine = true,
                    isError = name.isBlank()
                )
                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it.filter(Char::isDigit) },
                    label = { Text("Ważny przez ile dni?") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val daysInt = days.toIntOrNull() ?: 7
                        onConfirm(name, System.currentTimeMillis() + daysInt * 24L * 60 * 60 * 1000)
                    }
                },
                enabled = name.isNotBlank()
            ) { Text("Dodaj") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
private fun NutrientChip(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall)
}

// Brak uprawnień

@Composable
private fun NoCameraPermissionView(onRequestAgain: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Brak uprawnień do kamery", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Aby skanować kody kreskowe, aplikacja potrzebuje dostępu do kamery.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequestAgain) { Text("Nadaj uprawnienia") }
    }
}