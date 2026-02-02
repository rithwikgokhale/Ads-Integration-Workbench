package com.rithwik.integrationworkbench.ui.debug

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rithwik.integrationworkbench.domain.model.EventRecord
import com.rithwik.integrationworkbench.ui.common.ShareUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugConsoleScreen(
    paddingValues: PaddingValues,
    viewModel: DebugConsoleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedEvent by remember { mutableStateOf<EventRecord?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Debug Console", style = MaterialTheme.typography.titleMedium)

        // Event type filters
        Text("Event Type", style = MaterialTheme.typography.bodySmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(EventTypeFilter.values().toList()) { filter ->
                FilterChip(
                    selected = state.selectedEventTypeFilters.contains(filter),
                    onClick = { viewModel.toggleEventTypeFilter(filter) },
                    label = { Text(filter.label, style = MaterialTheme.typography.bodySmall) }
                )
            }
        }

        // Status filters
        Text("Status", style = MaterialTheme.typography.bodySmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(StatusFilter.values().toList()) { filter ->
                FilterChip(
                    selected = state.selectedStatusFilters.contains(filter),
                    onClick = { viewModel.toggleStatusFilter(filter) },
                    label = { Text(filter.label, style = MaterialTheme.typography.bodySmall) }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.exportBundle() }) {
                Text(if (state.isExporting) "Exporting..." else "Export Bundle")
            }
            OutlinedButton(onClick = { viewModel.clearFilters() }) {
                Text("Clear Filters")
            }
        }

        Text("Events (${state.events.size})", style = MaterialTheme.typography.bodyMedium)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.events) { event ->
                EventRow(event = event, onClick = { selectedEvent = event })
            }
        }
    }

    if (selectedEvent != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { selectedEvent = null }
        ) {
            EventDetails(selectedEvent!!)
        }
    }

    LaunchedEffect(state.exportFile) {
        state.exportFile?.let {
            ShareUtil.shareFile(context, it, "Share Debug Bundle")
            viewModel.clearExportFile()
        }
    }
}

@Composable
private fun EventRow(event: EventRecord, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${event.eventType} • ${event.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (event.status) {
                        com.rithwik.integrationworkbench.domain.model.Status.SUCCESS -> MaterialTheme.colorScheme.primary
                        com.rithwik.integrationworkbench.domain.model.Status.FAILURE -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = event.network?.displayName ?: "--",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "${event.timestampMs.toHumanTime()} (${event.timestampMs} ms)",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Format: ${event.format?.displayName ?: "--"} • AdUnit: ${event.adUnitId ?: "--"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Latency: ${event.latencyMs ?: 0}ms • Session: ${event.sessionId.take(6)}",
                style = MaterialTheme.typography.bodySmall
            )
            if (event.errorCode != null || event.errorMessage != null) {
                Text(
                    text = "Error: ${event.errorCode ?: "--"} ${event.errorMessage ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EventDetails(event: EventRecord) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Event Details", style = MaterialTheme.typography.titleMedium)
        Text("ID: ${event.id}")
        Text("Type: ${event.eventType}")
        Text("Status: ${event.status}")
        Text("Network: ${event.network?.displayName ?: "--"}")
        Text("Format: ${event.format?.displayName ?: "--"}")
        Text("Ad Unit: ${event.adUnitId ?: "--"}")
        Text("Placement: ${event.placement ?: "--"}")
        Text("Latency: ${event.latencyMs ?: "--"}ms")
        Text("Network Name: ${event.networkName ?: "--"}")
        Text("Session: ${event.sessionId}")
        Text("Timestamp: ${event.timestampMs.toHumanTime()}")
        if (event.errorCode != null) Text("Error Code: ${event.errorCode}")
        if (event.errorMessage != null) Text("Error Message: ${event.errorMessage}")
        if (event.extras.isNotEmpty()) {
            Text("Extras:")
            event.extras.forEach { (k, v) -> Text("  $k: $v") }
        }
        Text("Raw JSON:")
        Text(event.rawPayloadJson ?: "{}", style = MaterialTheme.typography.bodySmall)
    }
}

private fun Long.toHumanTime(): String {
    val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    return formatter.format(Date(this))
}
