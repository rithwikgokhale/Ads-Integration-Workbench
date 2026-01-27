package com.maxlab.ui.debug

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxlab.core.redactId
import com.maxlab.domain.model.Event
import com.maxlab.ui.util.shareFile
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
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Observability Timeline", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EventFilter.values().forEach { filter ->
                val selected = state.selectedFilters.contains(filter)
                FilterChip(
                    onClick = { viewModel.toggleFilter(filter) },
                    label = { Text(filter.label) },
                    selected = selected
                )
            }
        }
        Button(
            onClick = { viewModel.exportBundle() }
        ) {
            Text(if (state.isExporting) "Exporting..." else "Export Debug Bundle")
        }

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
        state.exportFile?.let { shareFile(context, it, "Share Debug Bundle") }
    }
}

@Composable
private fun EventRow(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "${event.category} • ${event.format ?: "--"} • ${redactId(event.adUnitId) ?: "--"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${event.timestampMs.toHumanTime()} (${event.timestampMs} ms)",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Session: ${event.sessionId.take(6)} • Placement: ${event.placement ?: "--"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Latency: ${event.latencyMs ?: 0} ms • Network: ${event.networkName ?: "--"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Error: ${event.errorCode ?: 0} ${event.errorMessage ?: ""}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun EventDetails(event: Event) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Event Details", style = MaterialTheme.typography.titleMedium)
        Text("ID: ${event.id}")
        Text("Category: ${event.category}")
        Text("Format: ${event.format ?: "--"}")
        Text("Ad Unit: ${event.adUnitId ?: "--"}")
        Text("Placement: ${event.placement ?: "--"}")
        Text("Latency: ${event.latencyMs ?: "--"}")
        Text("Network: ${event.networkName ?: "--"}")
        Text("Error: ${event.errorCode ?: "--"} ${event.errorMessage ?: ""}")
        Text("Raw JSON:")
        Text(event.rawPayloadJson ?: "{}")
    }
}

private fun Long.toHumanTime(): String {
    val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    return formatter.format(Date(this))
}
