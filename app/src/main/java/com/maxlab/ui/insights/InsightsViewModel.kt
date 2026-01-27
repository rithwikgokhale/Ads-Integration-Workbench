package com.maxlab.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxlab.domain.model.AdFormat
import com.maxlab.domain.model.Event
import com.maxlab.domain.repository.EventRepository
import com.maxlab.domain.telemetry.DropReport
import com.maxlab.domain.telemetry.ErrorGroup
import com.maxlab.domain.telemetry.LatencySummary
import com.maxlab.domain.telemetry.TelemetryAnalyzer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class SuccessRateRow(
    val format: AdFormat?,
    val adUnitId: String?,
    val rate: Double
)

data class InsightsUiState(
    val successRates: List<SuccessRateRow> = emptyList(),
    val latencyByFormat: Map<AdFormat, LatencySummary> = emptyMap(),
    val topErrors: List<ErrorGroup> = emptyList(),
    val dropReport: DropReport? = null
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    repository: EventRepository,
    private val analyzer: TelemetryAnalyzer
) : ViewModel() {
    val uiState: StateFlow<InsightsUiState> = repository.observeRecentEvents(2000)
        .map { events -> buildState(events) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    private fun buildState(events: List<Event>): InsightsUiState {
        val successRates = analyzer.successRateByKey(events) { event ->
            event.format to event.adUnitId
        }.map { (key, value) ->
            SuccessRateRow(format = key.first, adUnitId = key.second, rate = value)
        }.sortedByDescending { it.rate }

        val latencyByFormat = analyzer.latencyByFormat(events)
        val topErrors = analyzer.groupErrors(events).take(10)
        val dropReport = analyzer.dropDetector(events, nowMs = System.currentTimeMillis())

        return InsightsUiState(
            successRates = successRates,
            latencyByFormat = latencyByFormat,
            topErrors = topErrors,
            dropReport = dropReport
        )
    }
}
