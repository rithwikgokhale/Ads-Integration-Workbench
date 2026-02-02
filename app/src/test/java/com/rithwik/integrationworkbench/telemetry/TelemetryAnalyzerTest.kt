package com.rithwik.integrationworkbench.telemetry

import com.rithwik.integrationworkbench.domain.model.EventRecord
import com.rithwik.integrationworkbench.domain.model.EventType
import com.rithwik.integrationworkbench.domain.model.Status
import com.rithwik.integrationworkbench.domain.telemetry.TelemetryAnalyzer
import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork
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
            baseEvent(status = Status.SUCCESS),
            baseEvent(status = Status.SUCCESS),
            baseEvent(status = Status.FAILURE)
        )
        val rate = analyzer.successRate(events)
        assertEquals(2.0 / 3.0, rate, 0.0001)
    }

    @Test
    fun `success rate by key groups correctly`() {
        val events = listOf(
            baseEvent(network = AdNetwork.MOCK, format = AdFormat.BANNER, status = Status.SUCCESS),
            baseEvent(network = AdNetwork.MOCK, format = AdFormat.BANNER, status = Status.SUCCESS),
            baseEvent(network = AdNetwork.MOCK, format = AdFormat.BANNER, status = Status.FAILURE),
            baseEvent(network = AdNetwork.ADMOB, format = AdFormat.INTERSTITIAL, status = Status.SUCCESS)
        )
        val rates = analyzer.successRateByKey(events) { Triple(it.network, it.format, it.adUnitId) }
        assertEquals(2, rates.size)
        val mockBannerRate = rates.find { it.network == AdNetwork.MOCK && it.format == AdFormat.BANNER }
        assertNotNull(mockBannerRate)
        assertEquals(2.0 / 3.0, mockBannerRate!!.rate, 0.0001)
    }

    @Test
    fun `drop detector triggers when expected`() {
        val now = 120 * 60_000L
        val previousEvents = (0 until 30).map {
            baseEvent(timestampMs = now - 60_000L - (it * 1000L), status = Status.SUCCESS)
        } + (0 until 5).map {
            baseEvent(timestampMs = now - 60_000L - (it * 1000L), status = Status.FAILURE)
        }
        val currentEvents = (0 until 5).map {
            baseEvent(timestampMs = now - (it * 1000L), status = Status.FAILURE)
        }
        val report = analyzer.dropDetector(previousEvents + currentEvents, nowMs = now)
        assertNotNull(report)
    }

    @Test
    fun `drop detector does not trigger when stable`() {
        val now = 120 * 60_000L
        val previousEvents = (0 until 10).map {
            baseEvent(timestampMs = now - 60_000L - (it * 1000L), status = Status.SUCCESS)
        }
        val currentEvents = (0 until 10).map {
            baseEvent(timestampMs = now - (it * 1000L), status = Status.SUCCESS)
        }
        val report = analyzer.dropDetector(previousEvents + currentEvents, nowMs = now)
        assertNull(report)
    }

    @Test
    fun `group errors counts correctly`() {
        val events = listOf(
            baseEvent(status = Status.FAILURE, errorCode = 1001, errorMessage = "Error A"),
            baseEvent(status = Status.FAILURE, errorCode = 1001, errorMessage = "Error A"),
            baseEvent(status = Status.FAILURE, errorCode = 1002, errorMessage = "Error B"),
            baseEvent(status = Status.SUCCESS)
        )
        val groups = analyzer.groupErrors(events)
        assertEquals(2, groups.size)
        assertEquals(2, groups[0].count)
        assertEquals(1001, groups[0].code)
    }

    private fun baseEvent(
        timestampMs: Long = System.currentTimeMillis(),
        status: Status = Status.SUCCESS,
        network: AdNetwork = AdNetwork.MOCK,
        format: AdFormat = AdFormat.BANNER,
        latencyMs: Long? = 100L,
        errorCode: Int? = null,
        errorMessage: String? = null
    ): EventRecord = EventRecord(
        id = UUID.randomUUID().toString(),
        timestampMs = timestampMs,
        sessionId = "session",
        eventType = EventType.LOAD,
        status = status,
        network = network,
        format = format,
        adUnitId = "unit",
        placement = "home",
        latencyMs = latencyMs,
        networkName = "mock",
        errorCode = errorCode,
        errorMessage = errorMessage,
        extras = emptyMap(),
        rawPayloadJson = "{}"
    )
}
