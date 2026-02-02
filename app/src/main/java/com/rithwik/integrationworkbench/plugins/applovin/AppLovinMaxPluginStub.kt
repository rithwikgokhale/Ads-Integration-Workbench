package com.rithwik.integrationworkbench.plugins.applovin

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
 * Stub implementation for AppLovin MAX integration.
 * Returns NOT_IMPLEMENTED for all actions until the real SDK is wired.
 *
 * TODO: Wire AppLovin MAX SDK (https://dash.applovin.com/documentation/mediation/android/getting-started/integration)
 */
@Singleton
class AppLovinMaxPluginStub @Inject constructor() : AdsIntegrationPlugin {
    override val network: AdNetwork = AdNetwork.APPLOVIN

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
            label = "Initialize MAX",
            description = "Initialize the AppLovin MAX SDK (stub)",
            requiresInit = false
        ),
        PluginActionDescriptor(
            action = PluginAction.Load(AdFormat.BANNER, ""),
            label = "Load Banner",
            description = "Load a MAX banner ad (stub)"
        ),
        PluginActionDescriptor(
            action = PluginAction.Show(AdFormat.BANNER, ""),
            label = "Show Banner",
            description = "Show a MAX banner ad (stub)",
            requiresLoad = true
        ),
        PluginActionDescriptor(
            action = PluginAction.Load(AdFormat.INTERSTITIAL, ""),
            label = "Load Interstitial",
            description = "Load a MAX interstitial ad (stub)"
        ),
        PluginActionDescriptor(
            action = PluginAction.Show(AdFormat.INTERSTITIAL, ""),
            label = "Show Interstitial",
            description = "Show a MAX interstitial ad (stub)",
            requiresLoad = true
        ),
        PluginActionDescriptor(
            action = PluginAction.Load(AdFormat.REWARDED, ""),
            label = "Load Rewarded",
            description = "Load a MAX rewarded ad (stub)"
        ),
        PluginActionDescriptor(
            action = PluginAction.Show(AdFormat.REWARDED, ""),
            label = "Show Rewarded",
            description = "Show a MAX rewarded ad (stub)",
            requiresLoad = true
        ),
        PluginActionDescriptor(
            action = PluginAction.HealthCheck,
            label = "Health Check",
            description = "Check AppLovin MAX plugin status",
            requiresInit = false
        )
    )

    override suspend fun configure(config: PluginConfig) {
        // TODO: Store SDK key and ad unit IDs
        // config.credentials["sdkKey"]
        // config.adUnitIds[AdFormat.BANNER], etc.
    }

    override suspend fun initialize(): PluginInitResult {
        // TODO: Wire AppLovinSdk.getInstance(context).initializeSdk { }
        return PluginInitResult.NotImplemented
    }

    override suspend fun execute(action: PluginAction): PluginActionResult {
        // TODO: Wire actual AppLovin MAX SDK calls
        // For Load: MaxInterstitialAd, MaxRewardedAd, MaxAdView
        // For Show: ad.showAd()
        return PluginActionResult.NotImplemented
    }

    override fun setPrivacyConsent(hasConsent: Boolean, isAgeRestricted: Boolean) {
        // TODO: Wire AppLovinPrivacySettings
        // AppLovinPrivacySettings.setHasUserConsent(hasConsent, context)
        // AppLovinPrivacySettings.setIsAgeRestrictedUser(isAgeRestricted, context)
    }

    override suspend fun healthCheck(): HealthCheckResult {
        return HealthCheckResult(
            network = network,
            isInitialized = false,
            loadedFormats = emptySet(),
            lastError = "AppLovin MAX SDK not wired (stub)",
            sdkVersion = null
        )
    }

    override fun destroy() {
        // No-op for stub
    }
}
