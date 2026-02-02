package com.rithwik.integrationworkbench.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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

    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Telemetry Insights", style = MaterialTheme.typography.titleMedium)
            Text("Total events: ${state.totalEvents}", style = MaterialTheme.typography.bodySmall)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Success Rate by Network/Format", style = MaterialTheme.typography.titleSmall)
                    if (state.successRates.isEmpty()) {
                        Text("No data yet", style = MaterialTheme.typography.bodySmall)
                    } else {
                        state.successRates.take(10).forEach { row ->
                            Text(
                                "${row.network?.displayName ?: "--"} / ${row.format?.displayName ?: "--"}: " +
                                    "${(row.rate * 100).toInt()}% (${row.total} events)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Latency p50/p95 by Format", style = MaterialTheme.typography.titleSmall)
                    if (state.latencyByFormat.isEmpty()) {
                        Text("No data yet", style = MaterialTheme.typography.bodySmall)
                    } else {
                        state.latencyByFormat.forEach { (format, summary) ->
                            Text(
                                "${format.displayName}: p50=${summary.p50 ?: "--"}ms p95=${summary.p95 ?: "--"}ms",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Latency p50/p95 by Network", style = MaterialTheme.typography.titleSmall)
                    if (state.latencyByNetwork.isEmpty()) {
                        Text("No data yet", style = MaterialTheme.typography.bodySmall)
                    } else {
                        state.latencyByNetwork.forEach { (network, summary) ->
                            Text(
                                "${network.displayName}: p50=${summary.p50 ?: "--"}ms p95=${summary.p95 ?: "--"}ms",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Top Errors", style = MaterialTheme.typography.titleSmall)
                    if (state.topErrors.isEmpty()) {
                        Text("No errors recorded", style = MaterialTheme.typography.bodySmall)
                    } else {
                        state.topErrors.forEach { error ->
                            Text(
                                "${error.count}x code=${error.code ?: "--"} msg=${error.message ?: "--"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Drop Detector (30 min window)", style = MaterialTheme.typography.titleSmall)
                    val report = state.dropReport
                    if (report == null) {
                        Text("No significant drop detected", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text(
                            "ALERT: Success rate dropped!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text("Previous 30 min: ${(report.previousRate * 100).toInt()}%")
                        Text("Current 30 min: ${(report.currentRate * 100).toInt()}%")
                        Text("Drop: ${(report.dropFraction * 100).toInt()}%")
                    }
                }
            }
        }
    }
}
