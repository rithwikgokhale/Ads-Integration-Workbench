package com.maxlab.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maxlab.domain.model.AdFormat
import com.maxlab.domain.model.Event
import com.maxlab.domain.model.EventCategory

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val timestampMs: Long,
    val sessionId: String,
    val category: EventCategory,
    val format: AdFormat?,
    val adUnitId: String?,
    val placement: String?,
    val latencyMs: Long?,
    val networkName: String?,
    val errorCode: Int?,
    val errorMessage: String?,
    val rawPayloadJson: String?
)

fun EventEntity.toDomain(): Event = Event(
    id = id,
    timestampMs = timestampMs,
    sessionId = sessionId,
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

fun Event.toEntity(): EventEntity = EventEntity(
    id = id,
    timestampMs = timestampMs,
    sessionId = sessionId,
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
