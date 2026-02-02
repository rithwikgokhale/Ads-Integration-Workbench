package com.rithwik.integrationworkbench.domain.model

import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork
import kotlinx.serialization.Serializable

/**
 * Type of event in the telemetry pipeline.
 */
@Serializable
enum class EventType {
    INIT,
    LOAD,
    SHOW,
    CLICK,
    REVENUE,
    REWARD,
    ERROR,
    SYSTEM
}

/**
 * Status of an event outcome.
 */
@Serializable
enum class Status {
    SUCCESS,
    FAILURE,
    PENDING,
    CANCELLED,
    NOT_IMPLEMENTED
}

/**
 * Domain model for an event record.
 */
@Serializable
data class EventRecord(
    val id: String,
    val timestampMs: Long,
    val sessionId: String,
    val eventType: EventType,
    val status: Status,
    val network: AdNetwork? = null,
    val format: AdFormat? = null,
    val adUnitId: String? = null,
    val placement: String? = null,
    val latencyMs: Long? = null,
    val networkName: String? = null,
    val errorCode: Int? = null,
    val errorMessage: String? = null,
    val extras: Map<String, String> = emptyMap(),
    val rawPayloadJson: String? = null
)

/**
 * Privacy consent state.
 */
@Serializable
enum class ConsentState {
    UNKNOWN,
    GRANTED,
    DENIED
}

/**
 * Harness settings for issue reproduction.
 */
@Serializable
data class HarnessSettings(
    val initTimeoutEnabled: Boolean = false,
    val initTimeoutMs: Long = 5000L,
    val initRetryEnabled: Boolean = false,
    val maxInitRetries: Int = 3,
    val badConfigInjectionEnabled: Boolean = false,
    val offlineGuardEnabled: Boolean = true,
    val simulateLoadFailure: Boolean = false,
    val failureRatePercent: Int = 10,
    val consentState: ConsentState = ConsentState.UNKNOWN,
    val isAgeRestrictedUser: Boolean = false
)

/**
 * App configuration (redacted for export).
 */
@Serializable
data class AppConfigExport(
    val appVersion: String,
    val buildType: String,
    val enabledPlugins: List<String>,
    val harnessSettings: HarnessSettings,
    val consentState: String,
    val isAgeRestrictedUser: Boolean
)

/**
 * Device info for debug bundle.
 */
@Serializable
data class DeviceInfoExport(
    val manufacturer: String,
    val model: String,
    val sdkInt: Int,
    val release: String,
    val locale: String,
    val appVersion: String,
    val isOnline: Boolean
)
