package com.maxlab.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class EventCategory {
    INIT,
    AD_LOAD,
    AD_DISPLAY,
    AD_CLICK,
    AD_REVENUE,
    ERROR,
    SYSTEM
}

@Serializable
enum class AdFormat {
    BANNER,
    INTERSTITIAL,
    REWARDED
}

@Serializable
data class Event(
    val id: String,
    val timestampMs: Long,
    val sessionId: String,
    val category: EventCategory,
    val format: AdFormat? = null,
    val adUnitId: String? = null,
    val placement: String? = null,
    val latencyMs: Long? = null,
    val networkName: String? = null,
    val errorCode: Int? = null,
    val errorMessage: String? = null,
    val rawPayloadJson: String? = null
)

enum class SdkMode {
    REAL,
    MOCK
}

enum class ConsentState {
    UNKNOWN,
    GRANTED,
    DENIED
}

sealed class InitState {
    data object NotStarted : InitState()
    data object Initializing : InitState()
    data object Ready : InitState()
    data class Failed(val errorMessage: String) : InitState()
}

sealed class AdState {
    data object Idle : AdState()
    data object Loading : AdState()
    data object Loaded : AdState()
    data object Showing : AdState()
    data class Failed(val errorMessage: String) : AdState()
}
