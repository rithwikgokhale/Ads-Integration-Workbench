package com.rithwik.integrationworkbench.domain.logging

import com.rithwik.integrationworkbench.domain.model.EventRecord
import com.rithwik.integrationworkbench.domain.model.EventType
import com.rithwik.integrationworkbench.domain.model.Status
import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork

interface EventLogger {
    fun log(
        eventType: EventType,
        status: Status,
        network: AdNetwork? = null,
        format: AdFormat? = null,
        adUnitId: String? = null,
        placement: String? = null,
        latencyMs: Long? = null,
        networkName: String? = null,
        errorCode: Int? = null,
        errorMessage: String? = null,
        extras: Map<String, String> = emptyMap(),
        rawPayloadJson: String? = null
    )

    fun logEvent(event: EventRecord)
}
