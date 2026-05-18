package com.example.fridgehelper.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fridgehelper.data.db.MonthStat
import com.example.fridgehelper.ui.theme.Coral700
import com.example.fridgehelper.ui.theme.Green500
import com.example.fridgehelper.ui.theme.fridgeTopBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val usedByMonth by viewModel.usedByMonth.collectAsState()
    val wastedByMonth by viewModel.wastedByMonth.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                colors = fridgeTopBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Monthly waste overview", style = MaterialTheme.typography.titleMedium)

            // legenda
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(color = Green500, label = "Used")
                LegendDot(color = Coral700, label = "Wasted")
            }

            val allMonths = (usedByMonth.map { it.month } + wastedByMonth.map { it.month })
                .distinct().sorted()

            if (allMonths.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No data yet.\nMark products as Used or Wasted to see stats.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                WasteChart(
                    allMonths = allMonths,
                    usedByMonth = usedByMonth,
                    wastedByMonth = wastedByMonth
                )
            }

            // podsumowanie sumaryczne
            val totalUsed = usedByMonth.sumOf { it.count }
            val totalWasted = wastedByMonth.sumOf { it.count }
            if (totalUsed + totalWasted > 0) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryItem(label = "Total used", count = totalUsed, color = Green500)
                    SummaryItem(label = "Total wasted", count = totalWasted, color = Coral700)
                }
            }
        }
    }
}

@Composable
private fun WasteChart(
    allMonths: List<String>,
    usedByMonth: List<MonthStat>,
    wastedByMonth: List<MonthStat>
) {
    val usedMap = usedByMonth.associate { it.month to it.count }
    val wastedMap = wastedByMonth.associate { it.month to it.count }
    val maxCount = ((usedByMonth + wastedByMonth).maxOfOrNull { it.count } ?: 1)
        .coerceAtLeast(1)

    val usedColor = Green500
    val wastedColor = Coral700
    val n = allMonths.size

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val groupWidth = size.width / n
            val barWidth = groupWidth * 0.28f
            val gap = groupWidth * 0.08f
            val chartHeight = size.height

            allMonths.forEachIndexed { i, month ->
                val used = usedMap[month] ?: 0
                val wasted = wastedMap[month] ?: 0
                val groupLeft = i * groupWidth + groupWidth * 0.08f

                // słupek "Used" (lewy)
                val usedH = (used.toFloat() / maxCount) * chartHeight
                drawRect(
                    color = usedColor,
                    topLeft = Offset(groupLeft, chartHeight - usedH),
                    size = Size(barWidth, usedH)
                )

                // słupek "Wasted" (prawy)
                val wastedH = (wasted.toFloat() / maxCount) * chartHeight
                drawRect(
                    color = wastedColor,
                    topLeft = Offset(groupLeft + barWidth + gap, chartHeight - wastedH),
                    size = Size(barWidth, wastedH)
                )
            }
        }

        // etykiety miesięcy pod wykresem
        val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        Row(modifier = Modifier.fillMaxWidth()) {
            allMonths.forEach { month ->
                val parts = month.split("-")
                val label = month
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SummaryItem(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
