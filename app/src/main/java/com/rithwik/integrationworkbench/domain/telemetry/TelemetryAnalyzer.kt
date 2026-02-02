package com.rithwik.integrationworkbench.domain.telemetry

import com.rithwik.integrationworkbench.domain.model.EventRecord
import com.rithwik.integrationworkbench.domain.model.Status
import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork

data class ErrorGroup(
    val count: Int,
    val code: Int?,
    val message: String?
)

data class LatencySummary(
    val p50: Long?,
    val p95: Long?
)

data class DropReport(
    val previousRate: Double,
    val currentRate: Double,
    val dropFraction: Double
)

data class SuccessRateRow(
    val network: AdNetwork?,
    val format: AdFormat?,
    val adUnitId: String?,
    val rate: Double,
    val total: Int
)

class TelemetryAnalyzer {
    fun successRate(events: List<EventRecord>): Double {
        val relevant = events.filter { it.format != null }
        val total = relevant.size
        if (total == 0) return 0.0
        val successes = relevant.count { it.status == Status.SUCCESS }
        return successes.toDouble() / total.toDouble()
    }

    fun successRateByKey(
        events: List<EventRecord>,
        key: (EventRecord) -> Triple<AdNetwork?, AdFormat?, String?>
    ): List<SuccessRateRow> {
        return events
            .filter { it.format != null }
            .groupBy { key(it) }
            .map { (k, grouped) ->
                val successes = grouped.count { it.status == Status.SUCCESS }
                SuccessRateRow(
                    network = k.first,
                    format = k.second,
                    adUnitId = k.third,
                    rate = if (grouped.isNotEmpty()) successes.toDouble() / grouped.size else 0.0,
                    total = grouped.size
                )
            }
            .sortedByDescending { it.rate }
    }

    fun percentileLatency(events: List<EventRecord>, p: Double): Long? {
        val latencies = events.mapNotNull { it.latencyMs }.sorted()
        if (latencies.isEmpty()) return null
        val rank = (p * latencies.size).toInt().coerceIn(1, latencies.size)
        return latencies[rank - 1]
    }

    fun latencyByFormat(events: List<EventRecord>): Map<AdFormat, LatencySummary> {
        return events
            .filter { it.format != null }
            .groupBy { it.format!! }
            .mapValues { (_, grouped) ->
                LatencySummary(
                    p50 = percentileLatency(grouped, 0.50),
                    p95 = percentileLatency(grouped, 0.95)
                )
            }
    }

    fun latencyByNetwork(events: List<EventRecord>): Map<AdNetwork, LatencySummary> {
        return events
            .filter { it.network != null }
            .groupBy { it.network!! }
            .mapValues { (_, grouped) ->
                LatencySummary(
                    p50 = percentileLatency(grouped, 0.50),
                    p95 = percentileLatency(grouped, 0.95)
                )
            }
    }

    fun groupErrors(events: List<EventRecord>): List<ErrorGroup> {
        return events
            .filter { it.status == Status.FAILURE }
            .groupBy { it.errorCode to it.errorMessage }
            .map { (key, grouped) -> ErrorGroup(grouped.size, key.first, key.second) }
            .sortedByDescending { it.count }
    }

    fun dropDetector(
        events: List<EventRecord>,
        nowMs: Long,
        windowMinutes: Int = 30,
        dropThreshold: Double = 0.2
    ): DropReport? {
        val windowMs = windowMinutes * 60_000L
        val currentStart = nowMs - windowMs
        val previousStart = nowMs - (windowMs * 2)
        val current = events.filter { it.timestampMs in currentStart..nowMs }
        val previous = events.filter { it.timestampMs in previousStart until currentStart }
        if (current.isEmpty() || previous.isEmpty()) return null
        val currentRate = successRate(current)
        val previousRate = successRate(previous)
        if (previousRate <= 0.0) return null
        val dropFraction = (previousRate - currentRate) / previousRate
        return if (dropFraction >= dropThreshold) {
            DropReport(previousRate, currentRate, dropFraction)
        } else {
            null
        }
    }
}
