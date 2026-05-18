package com.example.fridgehelper.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fridgehelper.ui.theme.Green700
import com.example.fridgehelper.ui.theme.TextPrimary
import com.example.fridgehelper.ui.theme.TextTertiary
import com.example.fridgehelper.ui.theme.fridgeTopBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val threshold   by viewModel.daysThreshold.collectAsState()
    val notifyTime  by viewModel.notifyTime.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = fridgeTopBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            NotificationThresholdCard(
                currentThreshold = threshold,
                onThresholdSelected = viewModel::setThreshold
            )
            NotifyTimeCard(
                hour   = notifyTime.first,
                minute = notifyTime.second,
                onEditClick = { showTimePicker = true }
            )
        }
    }

    if (showTimePicker) {
        val (h, m) = notifyTime
        NotifyTimeDialog(
            initialHour   = h,
            initialMinute = m,
            onConfirm = { hour, minute ->
                viewModel.setNotifyTime(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun NotificationThresholdCard(
    currentThreshold: Int,
    onThresholdSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Notify me",
                style = MaterialTheme.typography.titleMedium
            )
            // opis dynamicznie zmienia się z wyborem — liczba i label pogrubione i w ciemnej zieleni
            Text(
                buildAnnotatedString {
                    append("You'll receive a notification when a product has less than ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                        append("$currentThreshold ${daysLabel(currentThreshold)}")
                    }
                    append(" left.")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 2, 3).forEach { days ->
                    // aktywny chip: ciemnozielone tło + biały tekst; nieaktywny: ramka TextTertiary
                    FilterChip(
                        selected = currentThreshold == days,
                        onClick = { onThresholdSelected(days) },
                        label = { Text("$days ${daysLabel(days)}") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Green700,
                            selectedLabelColor = Color.White
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (currentThreshold == days) Color.Transparent else TextTertiary
                        )
                    )
                }
            }
        }
    }
}

private fun daysLabel(days: Int) = when (days) {
    1    -> "day"
    else -> "days"
}

@Composable
private fun NotifyTimeCard(hour: Int, minute: Int, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Notification time", style = MaterialTheme.typography.titleMedium)
            Text(
                buildAnnotatedString {
                    append("Notifications will be sent daily at ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary)) {
                        append("%02d:%02d".format(hour, minute))
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // przycisk otwierający time picker dialog
            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, TextTertiary)
            ) {
                Text(
                    "Change time: %02d:%02d".format(hour, minute),
                    color = TextPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotifyTimeDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour   = initialHour,
        initialMinute = initialMinute,
        is24Hour      = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification time") },
        text  = { TimeInput(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
