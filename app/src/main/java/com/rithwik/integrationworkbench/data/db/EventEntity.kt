package com.rithwik.integrationworkbench.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rithwik.integrationworkbench.domain.model.EventRecord
import com.rithwik.integrationworkbench.domain.model.EventType
import com.rithwik.integrationworkbench.domain.model.Status
import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val timestampMs: Long,
    val sessionId: String,
    val eventType: EventType,
    val status: Status,
    val network: AdNetwork?,
    val format: AdFormat?,
    val adUnitId: String?,
    val placement: String?,
    val latencyMs: Long?,
    val networkName: String?,
    val errorCode: Int?,
    val errorMessage: String?,
    val extrasJson: String?,
    val rawPayloadJson: String?
)

fun EventEntity.toDomain(): EventRecord = EventRecord(
    id = id,
    timestampMs = timestampMs,
    sessionId = sessionId,
    eventType = eventType,
    status = status,
    network = network,
    format = format,
    adUnitId = adUnitId,
    placement = placement,
    latencyMs = latencyMs,
    networkName = networkName,
    errorCode = errorCode,
    errorMessage = errorMessage,
    extras = extrasJson?.let { parseExtras(it) } ?: emptyMap(),
    rawPayloadJson = rawPayloadJson
)

fun EventRecord.toEntity(): EventEntity = EventEntity(
    id = id,
    timestampMs = timestampMs,
    sessionId = sessionId,
    eventType = eventType,
    status = status,
    network = network,
    format = format,
    adUnitId = adUnitId,
    placement = placement,
    latencyMs = latencyMs,
    networkName = networkName,
    errorCode = errorCode,
    errorMessage = errorMessage,
    extrasJson = if (extras.isNotEmpty()) extras.entries.joinToString(",") { "${it.key}=${it.value}" } else null,
    rawPayloadJson = rawPayloadJson
)

private fun parseExtras(json: String): Map<String, String> =
    json.split(",")
        .mapNotNull { pair ->
            val parts = pair.split("=", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }
        .toMap()
