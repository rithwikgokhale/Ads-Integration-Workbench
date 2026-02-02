package com.rithwik.integrationworkbench.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rithwik.integrationworkbench.domain.model.EventRecord
import com.rithwik.integrationworkbench.domain.repository.EventRepository
import com.rithwik.integrationworkbench.domain.telemetry.DropReport
import com.rithwik.integrationworkbench.domain.telemetry.ErrorGroup
import com.rithwik.integrationworkbench.domain.telemetry.LatencySummary
import com.rithwik.integrationworkbench.domain.telemetry.SuccessRateRow
import com.rithwik.integrationworkbench.domain.telemetry.TelemetryAnalyzer
import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class InsightsUiState(
    val successRates: List<SuccessRateRow> = emptyList(),
    val latencyByFormat: Map<AdFormat, LatencySummary> = emptyMap(),
    val latencyByNetwork: Map<AdNetwork, LatencySummary> = emptyMap(),
    val topErrors: List<ErrorGroup> = emptyList(),
    val dropReport: DropReport? = null,
    val totalEvents: Int = 0
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    repository: EventRepository,
    private val analyzer: TelemetryAnalyzer
) : ViewModel() {
    val uiState: StateFlow<InsightsUiState> = repository.observeRecentEvents(2000)
        .map { events -> buildState(events) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    private fun buildState(events: List<EventRecord>): InsightsUiState {
        val successRates = analyzer.successRateByKey(events) { event ->
            Triple(event.network, event.format, event.adUnitId)
        }

        val latencyByFormat = analyzer.latencyByFormat(events)
        val latencyByNetwork = analyzer.latencyByNetwork(events)
        val topErrors = analyzer.groupErrors(events).take(10)
        val dropReport = analyzer.dropDetector(events, nowMs = System.currentTimeMillis())

        return InsightsUiState(
            successRates = successRates,
            latencyByFormat = latencyByFormat,
            latencyByNetwork = latencyByNetwork,
            topErrors = topErrors,
            dropReport = dropReport,
            totalEvents = events.size
        )
    }
}
