package com.maxlab.domain.telemetry

import com.maxlab.domain.model.AdFormat
import com.maxlab.domain.model.Event
import com.maxlab.domain.model.EventCategory

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

class TelemetryAnalyzer {
    fun successRate(events: List<Event>): Double {
        val relevant = events.filter { it.format != null }
        val total = relevant.size
        if (total == 0) return 0.0
        val failures = relevant.count { it.category == EventCategory.ERROR }
        val successes = total - failures
        return successes.toDouble() / total.toDouble()
    }

    fun successRateByKey(
        events: List<Event>,
        key: (Event) -> Pair<AdFormat?, String?>
    ): Map<Pair<AdFormat?, String?>, Double> {
        return events
            .filter { it.format != null }
            .groupBy { key(it) }
            .mapValues { (_, grouped) -> successRate(grouped) }
    }

    fun percentileLatency(events: List<Event>, p: Double): Long? {
        val latencies = events.mapNotNull { it.latencyMs }.sorted()
        if (latencies.isEmpty()) return null
        val rank = (p * latencies.size).toInt().coerceIn(1, latencies.size)
        return latencies[rank - 1]
    }

    fun latencyByFormat(events: List<Event>): Map<AdFormat, LatencySummary> {
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

    fun groupErrors(events: List<Event>): List<ErrorGroup> {
        return events
            .filter { it.category == EventCategory.ERROR }
            .groupBy { it.errorCode to it.errorMessage }
            .map { (key, grouped) -> ErrorGroup(grouped.size, key.first, key.second) }
            .sortedByDescending { it.count }
    }

    fun dropDetector(
        events: List<Event>,
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
