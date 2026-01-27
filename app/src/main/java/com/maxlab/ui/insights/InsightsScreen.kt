package com.maxlab.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun InsightsScreen(
    paddingValues: PaddingValues,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Local Telemetry Analysis", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Success rate by format/ad unit", style = MaterialTheme.typography.titleSmall)
                state.successRates.take(10).forEach { row ->
                    Text("${row.format ?: "--"} ${row.adUnitId ?: "--"}: ${(row.rate * 100).toInt()}%")
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Latency p50/p95 by format", style = MaterialTheme.typography.titleSmall)
                state.latencyByFormat.forEach { (format, summary) ->
                    Text("$format: p50=${summary.p50 ?: "--"}ms p95=${summary.p95 ?: "--"}ms")
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Top errors", style = MaterialTheme.typography.titleSmall)
                state.topErrors.forEach { error ->
                    Text("${error.count}x code=${error.code ?: "--"} msg=${error.message ?: "--"}")
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Drop detector", style = MaterialTheme.typography.titleSmall)
                val report = state.dropReport
                if (report == null) {
                    Text("No significant drop detected.")
                } else {
                    Text("Previous: ${(report.previousRate * 100).toInt()}%")
                    Text("Current: ${(report.currentRate * 100).toInt()}%")
                    Text("Drop: ${(report.dropFraction * 100).toInt()}%")
                }
            }
        }
    }
}
