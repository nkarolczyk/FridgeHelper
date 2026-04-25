package com.example.fridgehelper.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val threshold by viewModel.daysThreshold.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
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
        }
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
            Text(
                "You'll receive a notification when a product has less than $currentThreshold ${daysLabel(currentThreshold)} left.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 2, 3).forEach { days ->
                    FilterChip(
                        selected = currentThreshold == days,
                        onClick = { onThresholdSelected(days) },
                        label = { Text("$days ${daysLabel(days)}") },
                        modifier = Modifier.weight(1f)
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
