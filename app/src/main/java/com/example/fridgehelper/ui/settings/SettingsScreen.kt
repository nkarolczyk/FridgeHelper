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
    // collectasstate zamienia flow na stan compose — ui reaguje na każdą zmianę
    val threshold by viewModel.daysThreshold.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ustawienia") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // karta z wyborem progu — przekazuje aktualną wartość i callback do zmiany
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
                "Powiadom mnie",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Otrzymasz powiadomienie gdy produktowi zostanie mniej niż $currentThreshold ${daysLabel(currentThreshold)}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                //chipy 1, 2, 3 dni; zaznaczony ten który pasuje do aktualnego progu
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

//dzien/dni zalezne od liczby
private fun daysLabel(days: Int) = when (days) {
    1    -> "dzień"
    else -> "dni"
}