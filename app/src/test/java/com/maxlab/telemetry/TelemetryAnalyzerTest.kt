package com.maxlab.telemetry

import com.maxlab.domain.model.AdFormat
import com.maxlab.domain.model.Event
import com.maxlab.domain.model.EventCategory
import com.maxlab.domain.telemetry.TelemetryAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class TelemetryAnalyzerTest {
    private val analyzer = TelemetryAnalyzer()

    @Test
    fun `percentile correctness`() {
        val events = (1..100).map {
            baseEvent(latencyMs = it.toLong())
        }
        val p50 = analyzer.percentileLatency(events, 0.50)
        val p95 = analyzer.percentileLatency(events, 0.95)
        assertEquals(50L, p50)
        assertEquals(95L, p95)
    }

    @Test
    fun `success rate correctness`() {
        val events = listOf(
            baseEvent(category = EventCategory.AD_LOAD),
            baseEvent(category = EventCategory.AD_DISPLAY),
            baseEvent(category = EventCategory.ERROR)
        )
        val rate = analyzer.successRate(events)
        assertEquals(2.0 / 3.0, rate, 0.0001)
    }

    @Test
    fun `drop detector triggers when expected`() {
        val now = 120 * 60_000L
        val previousEvents = (0 until 30).map {
            baseEvent(timestampMs = now - 60_000L - (it * 1000L))
        } + (0 until 5).map {
            baseEvent(timestampMs = now - 60_000L - (it * 1000L), category = EventCategory.ERROR)
        }
        val currentEvents = (0 until 5).map {
            baseEvent(timestampMs = now - (it * 1000L), category = EventCategory.ERROR)
        }
        val report = analyzer.dropDetector(previousEvents + currentEvents, nowMs = now)
        assertNotNull(report)
    }

    @Test
    fun `drop detector does not trigger when stable`() {
        val now = 120 * 60_000L
        val previousEvents = (0 until 10).map {
            baseEvent(timestampMs = now - 60_000L - (it * 1000L))
        }
        val currentEvents = (0 until 10).map {
            baseEvent(timestampMs = now - (it * 1000L))
        }
        val report = analyzer.dropDetector(previousEvents + currentEvents, nowMs = now)
        assertNull(report)
    }

    private fun baseEvent(
        timestampMs: Long = System.currentTimeMillis(),
        category: EventCategory = EventCategory.AD_LOAD,
        latencyMs: Long? = 100L
    ): Event = Event(
        id = UUID.randomUUID().toString(),
        timestampMs = timestampMs,
        sessionId = "session",
        category = category,
        format = AdFormat.BANNER,
        adUnitId = "unit",
        placement = "home",
        latencyMs = latencyMs,
        networkName = "mock",
        errorCode = if (category == EventCategory.ERROR) 1001 else null,
        errorMessage = if (category == EventCategory.ERROR) "fail" else null,
        rawPayloadJson = "{}"
    )
}
