package com.maxlab.domain.logging

import com.maxlab.core.Clock
import com.maxlab.core.IdGenerator
import com.maxlab.domain.model.AdFormat
import com.maxlab.domain.model.Event
import com.maxlab.domain.model.EventCategory
import com.maxlab.domain.repository.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventLogger @Inject constructor(
    private val repository: EventRepository,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
    private val sessionProvider: SessionProvider
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun log(
        category: EventCategory,
        format: AdFormat? = null,
        adUnitId: String? = null,
        placement: String? = null,
        latencyMs: Long? = null,
        networkName: String? = null,
        errorCode: Int? = null,
        errorMessage: String? = null,
        rawPayloadJson: String? = null
    ) {
        val event = Event(
            id = idGenerator.newId(),
            timestampMs = clock.nowMs(),
            sessionId = sessionProvider.sessionId,
            category = category,
            format = format,
            adUnitId = adUnitId,
            placement = placement,
            latencyMs = latencyMs,
            networkName = networkName,
            errorCode = errorCode,
            errorMessage = errorMessage,
            rawPayloadJson = rawPayloadJson
        )
        scope.launch {
            repository.insertEvent(event)
        }
    }
}
