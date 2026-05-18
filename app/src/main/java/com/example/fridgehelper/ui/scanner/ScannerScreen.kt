package com.example.fridgehelper.ui.scanner

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import com.example.fridgehelper.data.db.Product
import com.example.fridgehelper.ui.theme.Amber100
import com.example.fridgehelper.ui.theme.Amber900
import com.example.fridgehelper.ui.theme.CardBorderExpired
import com.example.fridgehelper.ui.theme.CardBorderWarn
import com.example.fridgehelper.ui.theme.Coral100
import com.example.fridgehelper.ui.theme.Coral900
import com.example.fridgehelper.ui.theme.Green100
import com.example.fridgehelper.ui.theme.Green500
import com.example.fridgehelper.ui.theme.GreenBorder
import com.example.fridgehelper.ui.theme.NavInactive
import com.example.fridgehelper.ui.theme.StatusOkText
import com.example.fridgehelper.ui.theme.TextPrimary
import com.example.fridgehelper.ui.theme.TextSecondary
import com.example.fridgehelper.ui.theme.TextTertiary
import com.example.fridgehelper.ui.theme.ViewfinderBg
import com.example.fridgehelper.ui.theme.fridgeTopBarColors
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    navController: NavController,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val recentScans by viewModel.recentlyScanned.collectAsState()

    var hasCameraPermission by remember {
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

    var manualBarcode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan product") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = fridgeTopBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                    label = { Text("Enter barcode") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green500,
                        focusedLabelColor  = Green500,
                        cursorColor        = Green500
                    ),
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
                IconButton(
                    onClick = {
                        viewModel.onManualBarcodeEntered(manualBarcode)
                        manualBarcode = ""
                    },
                    enabled = manualBarcode.isNotBlank() && uiState == ScannerUiState.Scanning
                ) {
                    Icon(Icons.Default.Search, "Search")
                }
            }

            Divider(color = GreenBorder)

            // tło podglądu kamery ma ciemnozielony kolor (#0D2418) przed załadowaniem
            // weight(1f) sprawia, że kamera zajmuje całą pozostałą przestrzeń
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(ViewfinderBg)
            ) {
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
                            CircularProgressIndicator(color = Green500)
                        }
                    }
                }
            }
            // pasek ostatnio zeskanowanych
            if (recentScans.isNotEmpty()) {
                Divider(color = GreenBorder)
                Text(
                    "Recently scanned",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentScans, key = { it.id }) { product ->
                        RecentScanChip(product = product)
                    }
                }
            }
        }
    }

    val scannedState = uiState
    if (scannedState is ScannerUiState.Scanned) {
        ScannedProductDialog(
            state = scannedState,
            onConfirm = { name, expiryDate -> viewModel.saveProduct(name, expiryDate) },
            onDismiss = { viewModel.resetToScanning() }
        )
    }
}

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
    barcodeScanner.process(image)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull { it.format == Barcode.FORMAT_EAN_13 }
                ?.rawValue?.let { onBarcodeDetected(it) }
        }
        .addOnFailureListener { Log.w("ScannerScreen", "ML Kit error", it) }
        .addOnCompleteListener { imageProxy.close() }
}

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
            Text("Place EAN-13 barcode in the frame", color = NavInactive, style = MaterialTheme.typography.bodyMedium)
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
        if (topLeft || bottomLeft) Box(Modifier.width(3.dp).fillMaxHeight().background(Green500))
        if (topRight || bottomRight) Box(Modifier.width(3.dp).fillMaxHeight().align(Alignment.TopEnd).background(Green500))
        if (topLeft || topRight) Box(Modifier.fillMaxWidth().height(3.dp).background(Green500))
        if (bottomLeft || bottomRight) Box(Modifier.fillMaxWidth().height(3.dp).align(Alignment.BottomStart).background(Green500))
    }
}

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
        title = { Text("Add product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.productName == null) {
                    Text(
                        "Product not found in database. Enter name manually.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("Code: ${state.barcode}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

                if (state.calories != null || state.protein != null || state.fat != null || state.carbs != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Nutrition facts / 100g", style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                state.calories?.let { NutrientChip("⚡ ${it.toInt()} kcal") }
                                state.protein?.let { NutrientChip("💪 %.1fg P".format(it)) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                state.fat?.let { NutrientChip("🧈 %.1fg F".format(it)) }
                                state.carbs?.let { NutrientChip("🌾 %.1fg C".format(it)) }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product name") },
                    singleLine = true,
                    isError = name.isBlank()
                )

                if (state.suggestions.isNotEmpty()) {
                    Text(
                        "Spoonacular suggestions:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.suggestions.forEach { suggestion ->
                            SuggestionChip(
                                onClick = { name = suggestion },
                                label = { Text(suggestion) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it.filter(Char::isDigit) },
                    label = { Text("Valid for how many days?") },
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
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RecentScanChip(product: Product) {
    val daysLeft = ((product.expiryDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    val chipBg = when {
        daysLeft < 0  -> Coral100
        daysLeft <= 2 -> Amber100
        else          -> Green100
    }
    val chipBorder = when {
        daysLeft < 0  -> CardBorderExpired
        daysLeft <= 2 -> CardBorderWarn
        else          -> TextTertiary
    }
    val statusColor = when {
        daysLeft < 0  -> Coral900
        daysLeft <= 2 -> Amber900
        else          -> StatusOkText
    }
    val statusLabel = when {
        daysLeft < 0  -> "expired"
        daysLeft == 0 -> "today"
        daysLeft == 1 -> "1 day"
        else          -> "$daysLeft days"
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = chipBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, chipBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                product.name,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 96.dp)
            )
            Text(
                statusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor
            )
        }
    }
}

@Composable
private fun NutrientChip(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun NoCameraPermissionView(onRequestAgain: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Camera permission required", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "To scan barcodes, the app needs camera access.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequestAgain) { Text("Grant permission") }
    }
}
