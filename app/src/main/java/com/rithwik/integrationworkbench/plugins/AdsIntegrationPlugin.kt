package com.rithwik.integrationworkbench.plugins

import kotlinx.coroutines.flow.Flow

/**
 * Represents a supported ad network.
 */
enum class AdNetwork(val displayName: String) {
    MOCK("Mock Ads"),
    ADMOB("AdMob"),
    UNITY("Unity Ads"),
    APPLOVIN("AppLovin MAX"),
    IRONSOURCE("ironSource"),
    VUNGLE("Vungle"),
    CHARTBOOST("Chartboost")
}

/**
 * Represents ad formats supported by the workbench.
 */
enum class AdFormat(val displayName: String) {
    BANNER("Banner"),
    INTERSTITIAL("Interstitial"),
    REWARDED("Rewarded"),
    REWARDED_INTERSTITIAL("Rewarded Interstitial"),
    NATIVE("Native"),
    APP_OPEN("App Open")
}

/**
 * Configuration for a plugin instance.
 */
data class PluginConfig(
    val network: AdNetwork,
    val enabled: Boolean = true,
    val credentials: Map<String, String> = emptyMap(),
    val adUnitIds: Map<AdFormat, String> = emptyMap(),
    val extras: Map<String, String> = emptyMap()
)

/**
 * Sealed class representing actions that can be performed on a plugin.
 */
sealed class PluginAction(val format: AdFormat?) {
    data object Initialize : PluginAction(null)
    data class Load(val adFormat: AdFormat, val adUnitId: String) : PluginAction(adFormat)
    data class Show(val adFormat: AdFormat, val adUnitId: String) : PluginAction(adFormat)
    data class Destroy(val adFormat: AdFormat) : PluginAction(adFormat)
    data object HealthCheck : PluginAction(null)
}

/**
 * Describes an action available from a plugin.
 */
data class PluginActionDescriptor(
    val action: PluginAction,
    val label: String,
    val description: String,
    val requiresInit: Boolean = true,
    val requiresLoad: Boolean = false
)

/**
 * Result of a plugin action execution.
 */
sealed class PluginActionResult {
    data class Success(
        val action: PluginAction,
        val latencyMs: Long,
        val networkName: String? = null,
        val extras: Map<String, Any> = emptyMap()
    ) : PluginActionResult()

    data class Failure(
        val action: PluginAction,
        val errorCode: Int,
        val errorMessage: String,
        val latencyMs: Long? = null
    ) : PluginActionResult()

    data object NotImplemented : PluginActionResult()
    data object Cancelled : PluginActionResult()
}

/**
 * Result of plugin initialization.
 */
sealed class PluginInitResult {
    data class Success(val networkName: String, val latencyMs: Long) : PluginInitResult()
    data class Failure(val errorCode: Int, val errorMessage: String, val latencyMs: Long? = null) : PluginInitResult()
    data object NotImplemented : PluginInitResult()
}

/**
 * Result of a health check.
 */
data class HealthCheckResult(
    val network: AdNetwork,
    val isInitialized: Boolean,
    val loadedFormats: Set<AdFormat>,
    val lastError: String? = null,
    val sdkVersion: String? = null
)

/**
 * State of a plugin.
 */
data class PluginState(
    val network: AdNetwork,
    val isInitialized: Boolean = false,
    val isInitializing: Boolean = false,
    val initError: String? = null,
    val adStates: Map<AdFormat, AdState> = emptyMap()
)

/**
 * State of an individual ad unit.
 */
sealed class AdState {
    data object Idle : AdState()
    data object Loading : AdState()
    data object Loaded : AdState()
    data object Showing : AdState()
    data class Failed(val errorCode: Int, val errorMessage: String) : AdState()
}

/**
 * Core interface for all ad integration plugins.
 */
interface AdsIntegrationPlugin {
    /** The ad network this plugin represents. */
    val network: AdNetwork

    /** List of ad formats supported by this plugin. */
    val supportedFormats: Set<AdFormat>

    /** Observable state of the plugin. */
    val state: Flow<PluginState>

    /** List of actions available from this plugin. */
    fun getAvailableActions(): List<PluginActionDescriptor>

    /** Configure the plugin with credentials and ad unit IDs. */
    suspend fun configure(config: PluginConfig)

    /** Initialize the SDK. */
    suspend fun initialize(): PluginInitResult

    /** Execute an action. */
    suspend fun execute(action: PluginAction): PluginActionResult

    /** Set privacy/consent state. */
    fun setPrivacyConsent(hasConsent: Boolean, isAgeRestricted: Boolean)

    /** Perform a health check. */
    suspend fun healthCheck(): HealthCheckResult

    /** Clean up resources. */
    fun destroy()
}
