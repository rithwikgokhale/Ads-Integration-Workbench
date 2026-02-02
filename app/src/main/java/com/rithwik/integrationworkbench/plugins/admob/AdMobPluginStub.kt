package com.rithwik.integrationworkbench.plugins.admob

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
 * Stub implementation for AdMob integration.
 * Returns NOT_IMPLEMENTED for all actions until the real SDK is wired.
 *
 * TODO: Wire Google Mobile Ads SDK (https://developers.google.com/admob/android/quick-start)
 */
@Singleton
class AdMobPluginStub @Inject constructor() : AdsIntegrationPlugin {
    override val network: AdNetwork = AdNetwork.ADMOB

    override val supportedFormats: Set<AdFormat> = setOf(
        AdFormat.BANNER,
        AdFormat.INTERSTITIAL,
        AdFormat.REWARDED,
        AdFormat.REWARDED_INTERSTITIAL,
        AdFormat.NATIVE,
        AdFormat.APP_OPEN
    )

    private val _state = MutableStateFlow(PluginState(network = network))
    override val state: Flow<PluginState> = _state.asStateFlow()

    override fun getAvailableActions(): List<PluginActionDescriptor> = listOf(
        PluginActionDescriptor(
            action = PluginAction.Initialize,
            label = "Initialize AdMob",
            description = "Initialize the Google Mobile Ads SDK (stub)",
            requiresInit = false
        ),
        PluginActionDescriptor(
            action = PluginAction.Load(AdFormat.BANNER, ""),
            label = "Load Banner",
            description = "Load an AdMob banner ad (stub)"
        ),
        PluginActionDescriptor(
            action = PluginAction.Load(AdFormat.INTERSTITIAL, ""),
            label = "Load Interstitial",
            description = "Load an AdMob interstitial ad (stub)"
        ),
        PluginActionDescriptor(
            action = PluginAction.Load(AdFormat.REWARDED, ""),
            label = "Load Rewarded",
            description = "Load an AdMob rewarded ad (stub)"
        ),
        PluginActionDescriptor(
            action = PluginAction.HealthCheck,
            label = "Health Check",
            description = "Check AdMob plugin status",
            requiresInit = false
        )
    )

    override suspend fun configure(config: PluginConfig) {
        // TODO: Store AdMob app ID and ad unit IDs
    }

    override suspend fun initialize(): PluginInitResult {
        // TODO: Wire MobileAds.initialize()
        return PluginInitResult.NotImplemented
    }

    override suspend fun execute(action: PluginAction): PluginActionResult {
        // TODO: Wire actual AdMob SDK calls
        return PluginActionResult.NotImplemented
    }

    override fun setPrivacyConsent(hasConsent: Boolean, isAgeRestricted: Boolean) {
        // TODO: Wire RequestConfiguration for COPPA/consent
    }

    override suspend fun healthCheck(): HealthCheckResult {
        return HealthCheckResult(
            network = network,
            isInitialized = false,
            loadedFormats = emptySet(),
            lastError = "AdMob SDK not wired (stub)",
            sdkVersion = null
        )
    }

    override fun destroy() {
        // No-op for stub
    }
}
