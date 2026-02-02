package com.rithwik.integrationworkbench.plugins.mock

import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork
import com.rithwik.integrationworkbench.plugins.AdState
import com.rithwik.integrationworkbench.plugins.AdsIntegrationPlugin
import com.rithwik.integrationworkbench.plugins.HealthCheckResult
import com.rithwik.integrationworkbench.plugins.PluginAction
import com.rithwik.integrationworkbench.plugins.PluginActionDescriptor
import com.rithwik.integrationworkbench.plugins.PluginActionResult
import com.rithwik.integrationworkbench.plugins.PluginConfig
import com.rithwik.integrationworkbench.plugins.PluginInitResult
import com.rithwik.integrationworkbench.plugins.PluginState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Mock implementation of AdsIntegrationPlugin for testing and demo purposes.
 */
@Singleton
class MockAdsPlugin @Inject constructor() : AdsIntegrationPlugin {
    override val network: AdNetwork = AdNetwork.MOCK

    override val supportedFormats: Set<AdFormat> = setOf(
        AdFormat.BANNER,
        AdFormat.INTERSTITIAL,
        AdFormat.REWARDED
    )

    private val _state = MutableStateFlow(PluginState(network = network))
    override val state: Flow<PluginState> = _state.asStateFlow()

    private var config: PluginConfig? = null
    private var simulateInitFailure = false
    private var simulateLoadFailure = false
    private var failureRate = 0.1
    private val random = Random.Default

    override fun getAvailableActions(): List<PluginActionDescriptor> = listOf(
        PluginActionDescriptor(
            action = PluginAction.Initialize,
            label = "Initialize",
            description = "Initialize the Mock SDK",
            requiresInit = false
        ),
        PluginActionDescriptor(
            action = PluginAction.Load(AdFormat.BANNER, ""),
            label = "Load Banner",
            description = "Load a mock banner ad"
        ),
        PluginActionDescriptor(
            action = PluginAction.Show(AdFormat.BANNER, ""),
            label = "Show Banner",
            description = "Show a mock banner ad",
            requiresLoad = true
        ),
        PluginActionDescriptor(
            action = PluginAction.Load(AdFormat.INTERSTITIAL, ""),
            label = "Load Interstitial",
            description = "Load a mock interstitial ad"
        ),
        PluginActionDescriptor(
            action = PluginAction.Show(AdFormat.INTERSTITIAL, ""),
            label = "Show Interstitial",
            description = "Show a mock interstitial ad",
            requiresLoad = true
        ),
        PluginActionDescriptor(
            action = PluginAction.Load(AdFormat.REWARDED, ""),
            label = "Load Rewarded",
            description = "Load a mock rewarded ad"
        ),
        PluginActionDescriptor(
            action = PluginAction.Show(AdFormat.REWARDED, ""),
            label = "Show Rewarded",
            description = "Show a mock rewarded ad",
            requiresLoad = true
        ),
        PluginActionDescriptor(
            action = PluginAction.HealthCheck,
            label = "Health Check",
            description = "Check plugin status",
            requiresInit = false
        )
    )

    override suspend fun configure(config: PluginConfig) {
        this.config = config
        simulateInitFailure = config.extras["simulateInitFailure"] == "true"
        simulateLoadFailure = config.extras["simulateLoadFailure"] == "true"
        failureRate = config.extras["failureRate"]?.toDoubleOrNull() ?: 0.1
    }

    override suspend fun initialize(): PluginInitResult {
        _state.update { it.copy(isInitializing = true, initError = null) }

        val startTime = System.currentTimeMillis()
        delay(random.nextLong(500, 1500))
        val latency = System.currentTimeMillis() - startTime

        return if (simulateInitFailure) {
            _state.update { it.copy(isInitializing = false, isInitialized = false, initError = "Simulated init failure") }
            PluginInitResult.Failure(errorCode = 1001, errorMessage = "Simulated init failure", latencyMs = latency)
        } else {
            _state.update { it.copy(isInitializing = false, isInitialized = true, initError = null) }
            PluginInitResult.Success(networkName = "mock-network", latencyMs = latency)
        }
    }

    override suspend fun execute(action: PluginAction): PluginActionResult {
        val startTime = System.currentTimeMillis()

        return when (action) {
            is PluginAction.Initialize -> {
                when (val result = initialize()) {
                    is PluginInitResult.Success -> PluginActionResult.Success(
                        action = action,
                        latencyMs = result.latencyMs,
                        networkName = result.networkName
                    )
                    is PluginInitResult.Failure -> PluginActionResult.Failure(
                        action = action,
                        errorCode = result.errorCode,
                        errorMessage = result.errorMessage,
                        latencyMs = result.latencyMs
                    )
                    is PluginInitResult.NotImplemented -> PluginActionResult.NotImplemented
                }
            }

            is PluginAction.Load -> {
                if (!_state.value.isInitialized) {
                    return PluginActionResult.Failure(
                        action = action,
                        errorCode = -1,
                        errorMessage = "SDK not initialized"
                    )
                }

                updateAdState(action.adFormat, AdState.Loading)
                delay(random.nextLong(300, 1200))
                val latency = System.currentTimeMillis() - startTime

                if (simulateLoadFailure || random.nextDouble() < failureRate) {
                    updateAdState(action.adFormat, AdState.Failed(1002, "Simulated load failure"))
                    PluginActionResult.Failure(
                        action = action,
                        errorCode = 1002,
                        errorMessage = "Simulated load failure",
                        latencyMs = latency
                    )
                } else {
                    updateAdState(action.adFormat, AdState.Loaded)
                    PluginActionResult.Success(
                        action = action,
                        latencyMs = latency,
                        networkName = "mock-network"
                    )
                }
            }

            is PluginAction.Show -> {
                val adState = _state.value.adStates[action.adFormat]
                if (adState != AdState.Loaded) {
                    return PluginActionResult.Failure(
                        action = action,
                        errorCode = -2,
                        errorMessage = "Ad not loaded"
                    )
                }

                updateAdState(action.adFormat, AdState.Showing)
                delay(random.nextLong(100, 500))
                val latency = System.currentTimeMillis() - startTime

                updateAdState(action.adFormat, AdState.Idle)
                PluginActionResult.Success(
                    action = action,
                    latencyMs = latency,
                    networkName = "mock-network",
                    extras = if (action.adFormat == AdFormat.REWARDED) {
                        mapOf("rewardType" to "coins", "rewardAmount" to 100)
                    } else {
                        emptyMap()
                    }
                )
            }

            is PluginAction.Destroy -> {
                updateAdState(action.adFormat, AdState.Idle)
                PluginActionResult.Success(
                    action = action,
                    latencyMs = System.currentTimeMillis() - startTime
                )
            }

            is PluginAction.HealthCheck -> {
                val result = healthCheck()
                PluginActionResult.Success(
                    action = action,
                    latencyMs = System.currentTimeMillis() - startTime,
                    extras = mapOf(
                        "isInitialized" to result.isInitialized,
                        "loadedFormats" to result.loadedFormats.map { it.name }
                    )
                )
            }
        }
    }

    override fun setPrivacyConsent(hasConsent: Boolean, isAgeRestricted: Boolean) {
        // Mock implementation - just log
    }

    override suspend fun healthCheck(): HealthCheckResult {
        val currentState = _state.value
        val loadedFormats = currentState.adStates
            .filter { it.value == AdState.Loaded }
            .keys
        return HealthCheckResult(
            network = network,
            isInitialized = currentState.isInitialized,
            loadedFormats = loadedFormats,
            lastError = currentState.initError,
            sdkVersion = "mock-1.0.0"
        )
    }

    override fun destroy() {
        _state.update {
            PluginState(
                network = network,
                isInitialized = false,
                isInitializing = false,
                initError = null,
                adStates = emptyMap()
            )
        }
    }

    private fun updateAdState(format: AdFormat, state: AdState) {
        _state.update { current ->
            current.copy(adStates = current.adStates + (format to state))
        }
    }
}
