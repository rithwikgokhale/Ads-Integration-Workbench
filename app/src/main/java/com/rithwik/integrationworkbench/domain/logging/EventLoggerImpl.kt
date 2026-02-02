package com.rithwik.integrationworkbench.domain.logging

import com.rithwik.integrationworkbench.core.Clock
import com.rithwik.integrationworkbench.core.IdGenerator
import com.rithwik.integrationworkbench.domain.model.EventRecord
import com.rithwik.integrationworkbench.domain.model.EventType
import com.rithwik.integrationworkbench.domain.model.Status
import com.rithwik.integrationworkbench.domain.repository.EventRepository
import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventLoggerImpl @Inject constructor(
    private val repository: EventRepository,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
    private val sessionProvider: SessionProvider,
    private val sanitizer: EventSanitizer
) : EventLogger {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun log(
        eventType: EventType,
        status: Status,
        network: AdNetwork?,
        format: AdFormat?,
        adUnitId: String?,
        placement: String?,
        latencyMs: Long?,
        networkName: String?,
        errorCode: Int?,
        errorMessage: String?,
        extras: Map<String, String>,
        rawPayloadJson: String?
    ) {
        val event = EventRecord(
            id = idGenerator.newId(),
            timestampMs = clock.nowMs(),
            sessionId = sessionProvider.sessionId,
            eventType = eventType,
            status = status,
            network = network,
            format = format,
            adUnitId = sanitizer.redactAdUnitId(adUnitId),
            placement = placement,
            latencyMs = latencyMs,
            networkName = networkName,
            errorCode = errorCode,
            errorMessage = errorMessage,
            extras = extras,
            rawPayloadJson = sanitizer.sanitizePayload(rawPayloadJson)
        )
        logEvent(event)
    }

    override fun logEvent(event: EventRecord) {
        scope.launch {
            repository.insertEvent(event)
        }
    }
}
