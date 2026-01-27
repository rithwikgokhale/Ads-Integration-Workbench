package com.maxlab.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxlab.data.debug.DebugBundleExporter
import com.maxlab.domain.model.Event
import com.maxlab.domain.model.EventCategory
import com.maxlab.domain.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

enum class EventFilter(val label: String, val category: EventCategory) {
    INIT("Init", EventCategory.INIT),
    LOAD("Load", EventCategory.AD_LOAD),
    DISPLAY("Display", EventCategory.AD_DISPLAY),
    CLICK("Click", EventCategory.AD_CLICK),
    REVENUE("Revenue", EventCategory.AD_REVENUE),
    ERROR("Error", EventCategory.ERROR),
    SYSTEM("System", EventCategory.SYSTEM)
}

data class DebugConsoleUiState(
    val events: List<Event> = emptyList(),
    val selectedFilters: Set<EventFilter> = EventFilter.values().toSet(),
    val isExporting: Boolean = false,
    val exportFile: File? = null
)

@HiltViewModel
class DebugConsoleViewModel @Inject constructor(
    private val repository: EventRepository,
    private val exporter: DebugBundleExporter
) : ViewModel() {
    private val filters = MutableStateFlow(EventFilter.values().toSet())
    private val exporting = MutableStateFlow(false)
    private val exportFile = MutableStateFlow<File?>(null)

    val uiState: StateFlow<DebugConsoleUiState> = combine(
        repository.observeRecentEvents(2000),
        filters,
        exporting,
        exportFile
    ) { events, filters, exporting, exportFile ->
        val filtered = events.filter { event -> filters.any { it.category == event.category } }
        DebugConsoleUiState(events = filtered, selectedFilters = filters, isExporting = exporting, exportFile = exportFile)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DebugConsoleUiState())

    fun toggleFilter(filter: EventFilter) {
        val current = filters.value.toMutableSet()
        if (current.contains(filter)) {
            current.remove(filter)
        } else {
            current.add(filter)
        }
        filters.value = if (current.isEmpty()) EventFilter.values().toSet() else current
    }

    fun exportBundle() {
        viewModelScope.launch {
            exporting.value = true
            exportFile.value = exporter.export()
            exporting.value = false
        }
    }
}
