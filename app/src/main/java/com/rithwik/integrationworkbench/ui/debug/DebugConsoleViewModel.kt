package com.rithwik.integrationworkbench.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rithwik.integrationworkbench.data.exporter.DebugBundleExporter
import com.rithwik.integrationworkbench.domain.model.EventRecord
import com.rithwik.integrationworkbench.domain.model.EventType
import com.rithwik.integrationworkbench.domain.model.Status
import com.rithwik.integrationworkbench.domain.repository.EventRepository
import com.rithwik.integrationworkbench.plugins.AdNetwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class EventTypeFilter(val label: String, val eventType: EventType) {
    INIT("Init", EventType.INIT),
    LOAD("Load", EventType.LOAD),
    SHOW("Show", EventType.SHOW),
    CLICK("Click", EventType.CLICK),
    REVENUE("Revenue", EventType.REVENUE),
    REWARD("Reward", EventType.REWARD),
    ERROR("Error", EventType.ERROR),
    SYSTEM("System", EventType.SYSTEM)
}

enum class StatusFilter(val label: String, val status: Status) {
    SUCCESS("Success", Status.SUCCESS),
    FAILURE("Failure", Status.FAILURE),
    PENDING("Pending", Status.PENDING),
    CANCELLED("Cancelled", Status.CANCELLED),
    NOT_IMPLEMENTED("N/A", Status.NOT_IMPLEMENTED)
}

data class DebugConsoleUiState(
    val events: List<EventRecord> = emptyList(),
    val selectedEventTypeFilters: Set<EventTypeFilter> = EventTypeFilter.values().toSet(),
    val selectedStatusFilters: Set<StatusFilter> = StatusFilter.values().toSet(),
    val selectedNetworkFilters: Set<AdNetwork> = emptySet(),
    val isExporting: Boolean = false,
    val exportFile: File? = null
)

@HiltViewModel
class DebugConsoleViewModel @Inject constructor(
    private val repository: EventRepository,
    private val exporter: DebugBundleExporter
) : ViewModel() {
    private val eventTypeFilters = MutableStateFlow(EventTypeFilter.values().toSet())
    private val statusFilters = MutableStateFlow(StatusFilter.values().toSet())
    private val networkFilters = MutableStateFlow<Set<AdNetwork>>(emptySet())
    private val exporting = MutableStateFlow(false)
    private val exportFile = MutableStateFlow<File?>(null)

    val uiState: StateFlow<DebugConsoleUiState> = combine(
        repository.observeRecentEvents(2000),
        eventTypeFilters,
        statusFilters,
        networkFilters,
        exporting,
        exportFile
    ) { events, typeFilters, statusFilters, networkFilters, exporting, exportFile ->
        val filtered = events.filter { event ->
            val typeMatch = typeFilters.any { it.eventType == event.eventType }
            val statusMatch = statusFilters.any { it.status == event.status }
            val networkMatch = networkFilters.isEmpty() || event.network in networkFilters
            typeMatch && statusMatch && networkMatch
        }
        DebugConsoleUiState(
            events = filtered,
            selectedEventTypeFilters = typeFilters,
            selectedStatusFilters = statusFilters,
            selectedNetworkFilters = networkFilters,
            isExporting = exporting,
            exportFile = exportFile
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugConsoleUiState())

    fun toggleEventTypeFilter(filter: EventTypeFilter) {
        val current = eventTypeFilters.value.toMutableSet()
        if (current.contains(filter)) {
            current.remove(filter)
        } else {
            current.add(filter)
        }
        eventTypeFilters.value = if (current.isEmpty()) EventTypeFilter.values().toSet() else current
    }

    fun toggleStatusFilter(filter: StatusFilter) {
        val current = statusFilters.value.toMutableSet()
        if (current.contains(filter)) {
            current.remove(filter)
        } else {
            current.add(filter)
        }
        statusFilters.value = if (current.isEmpty()) StatusFilter.values().toSet() else current
    }

    fun toggleNetworkFilter(network: AdNetwork) {
        val current = networkFilters.value.toMutableSet()
        if (current.contains(network)) {
            current.remove(network)
        } else {
            current.add(network)
        }
        networkFilters.value = current
    }

    fun clearFilters() {
        eventTypeFilters.value = EventTypeFilter.values().toSet()
        statusFilters.value = StatusFilter.values().toSet()
        networkFilters.value = emptySet()
    }

    fun exportBundle() {
        viewModelScope.launch {
            exporting.value = true
            exportFile.value = exporter.export()
            exporting.value = false
        }
    }

    fun clearExportFile() {
        exportFile.value = null
    }
}
