package com.rithwik.integrationworkbench.plugins.unity

import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork
import com.rithwik.integrationworkbench.plugins.AdsIntegrationPlugin
import com.rithwik.integrationworkbench.plugins.HealthCheckResult
import com.rithwik.integrationworkbench.plugins.PluginAction
import com.rithwik.integrationworkbench.plugins.PluginActionDescriptor
import com.rithwik.integrationworkbench.plugins.PluginActionResult
import com.rithwik.integrationworkbench.plugins.PluginConfig
import com.rithwik.integrationworkbench.plugins.PluginInitResult
import com.rithwik.integrationworkbench.plugins.PluginState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub implementation for Unity Ads integration.
 * Returns NOT_IMPLEMENTED for all actions until the real SDK is wired.
 *
 * TODO: Wire Unity Ads SDK (https://docs.unity.com/ads/en-us/manual/InstallingTheSdkAndroid)
 */
@Singleton
class UnityAdsPluginStub @Inject constructor() : AdsIntegrationPlugin {
    override val network: AdNetwork = AdNetwork.UNITY

    override val supportedFormats: Set<AdFormat> = setOf(
        AdFormat.INTERSTITIAL,
        AdFormat.REWARDED,
        AdFormat.BANNER
    )

    private val _state = MutableStateFlow(PluginState(network = network))
    override val state: Flow<PluginState> = _state.asStateFlow()

    override fun getAvailableActions(): List<PluginActionDescriptor> = listOf(
        PluginActionDescriptor(
            action = PluginAction.Initialize,
            label = "Initialize Unity Ads",
            description = "Initialize the Unity Ads SDK (stub)",
            requiresInit = false
        ),
        PluginActionDescriptor(
            action = PluginAction.Load(AdFormat.INTERSTITIAL, ""),
            label = "Load Interstitial",
            description = "Load a Unity Ads interstitial (stub)"
        ),
        PluginActionDescriptor(
            action = PluginAction.Load(AdFormat.REWARDED, ""),
            label = "Load Rewarded",
            description = "Load a Unity Ads rewarded ad (stub)"
        ),
        PluginActionDescriptor(
            action = PluginAction.Load(AdFormat.BANNER, ""),
            label = "Load Banner",
            description = "Load a Unity Ads banner (stub)"
        ),
        PluginActionDescriptor(
            action = PluginAction.HealthCheck,
            label = "Health Check",
            description = "Check Unity Ads plugin status",
            requiresInit = false
        )
    )

    override suspend fun configure(config: PluginConfig) {
        // TODO: Store Unity game ID and placement IDs
    }

    override suspend fun initialize(): PluginInitResult {
        // TODO: Wire UnityAds.initialize()
        return PluginInitResult.NotImplemented
    }

    override suspend fun execute(action: PluginAction): PluginActionResult {
        // TODO: Wire actual Unity Ads SDK calls
        return PluginActionResult.NotImplemented
    }

    override fun setPrivacyConsent(hasConsent: Boolean, isAgeRestricted: Boolean) {
        // TODO: Wire MetaData for GDPR/COPPA
    }

    override suspend fun healthCheck(): HealthCheckResult {
        return HealthCheckResult(
            network = network,
            isInitialized = false,
            loadedFormats = emptySet(),
            lastError = "Unity Ads SDK not wired (stub)",
            sdkVersion = null
        )
    }

    override fun destroy() {
        // No-op for stub
    }
}
