# Plugin Authoring Guide

This guide explains how to add a new ad network plugin to the Ads Integration Workbench.

## Overview

Plugins implement the `AdsIntegrationPlugin` interface and are registered via Hilt multibinding. The workbench automatically discovers and presents all registered plugins in the UI.

## Step 1: Create Plugin Class

Create a new package under `plugins/` for your network:

```kotlin
package com.rithwik.integrationworkbench.plugins.yournetwork

import com.rithwik.integrationworkbench.plugins.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YourNetworkPlugin @Inject constructor() : AdsIntegrationPlugin {
    override val network: AdNetwork = AdNetwork.YOUR_NETWORK // Add to enum first
    
    override val supportedFormats: Set<AdFormat> = setOf(
        AdFormat.BANNER,
        AdFormat.INTERSTITIAL,
        AdFormat.REWARDED
    )
    
    private val _state = MutableStateFlow(PluginState(network = network))
    override val state: Flow<PluginState> = _state
    
    private var config: PluginConfig? = null
    
    override fun getAvailableActions(): List<PluginActionDescriptor> = listOf(
        PluginActionDescriptor(
            action = PluginAction.Initialize,
            label = "Initialize",
            description = "Initialize YourNetwork SDK",
            requiresInit = false
        ),
        // Add more actions...
    )
    
    override suspend fun configure(config: PluginConfig) {
        this.config = config
        // Extract credentials: config.credentials["appId"]
        // Extract ad units: config.adUnitIds[AdFormat.BANNER]
    }
    
    override suspend fun initialize(): PluginInitResult {
        _state.update { it.copy(isInitializing = true) }
        
        // Call your SDK init here
        // YourSdk.initialize(context, config.credentials["appId"])
        
        _state.update { it.copy(isInitializing = false, isInitialized = true) }
        return PluginInitResult.Success(networkName = "your-network", latencyMs = 0)
    }
    
    override suspend fun execute(action: PluginAction): PluginActionResult {
        return when (action) {
            is PluginAction.Initialize -> {
                val result = initialize()
                // Convert to PluginActionResult
            }
            is PluginAction.Load -> {
                // Call SDK load
            }
            is PluginAction.Show -> {
                // Call SDK show
            }
            else -> PluginActionResult.NotImplemented
        }
    }
    
    override fun setPrivacyConsent(hasConsent: Boolean, isAgeRestricted: Boolean) {
        // Set GDPR/COPPA flags on your SDK
    }
    
    override suspend fun healthCheck(): HealthCheckResult {
        return HealthCheckResult(
            network = network,
            isInitialized = _state.value.isInitialized,
            loadedFormats = emptySet(),
            lastError = _state.value.initError,
            sdkVersion = "1.0.0"
        )
    }
    
    override fun destroy() {
        _state.value = PluginState(network = network)
    }
}
```

## Step 2: Add Network Enum

Add your network to `AdNetwork` in `AdsIntegrationPlugin.kt`:

```kotlin
enum class AdNetwork(val displayName: String) {
    MOCK("Mock Ads"),
    ADMOB("AdMob"),
    UNITY("Unity Ads"),
    YOUR_NETWORK("Your Network"),  // Add here
    // ...
}
```

## Step 3: Register with Hilt

Add binding in `di/AppModule.kt`:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class PluginModule {
    @Binds @IntoSet
    abstract fun bindMockPlugin(plugin: MockAdsPlugin): AdsIntegrationPlugin
    
    @Binds @IntoSet
    abstract fun bindYourNetworkPlugin(plugin: YourNetworkPlugin): AdsIntegrationPlugin
}
```

## Step 4: Add SDK Dependency

Add the SDK to `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.yournetwork:sdk:1.0.0")
}
```

## State Management

Use `MutableStateFlow` to track plugin state:

```kotlin
private fun updateAdState(format: AdFormat, state: AdState) {
    _state.update { current ->
        current.copy(adStates = current.adStates + (format to state))
    }
}
```

State transitions:
- `Idle` → `Loading` → `Loaded` → `Showing` → `Idle`
- `Loading` → `Failed` (on error)

## Handling Callbacks

Most SDKs use callbacks. Convert to coroutines with `suspendCancellableCoroutine`:

```kotlin
override suspend fun initialize(): PluginInitResult = suspendCancellableCoroutine { cont ->
    YourSdk.initialize(context, object : InitListener {
        override fun onSuccess() {
            cont.resume(PluginInitResult.Success(...))
        }
        override fun onError(code: Int, message: String) {
            cont.resume(PluginInitResult.Failure(code, message))
        }
    })
}
```

## Testing

Create tests in `test/java/com/rithwik/integrationworkbench/plugins/`:

```kotlin
class YourNetworkPluginTest {
    @Test
    fun `plugin has correct network`() {
        val plugin = YourNetworkPlugin()
        assertEquals(AdNetwork.YOUR_NETWORK, plugin.network)
    }
    
    @Test
    fun `state updates after init`() = runBlocking {
        val plugin = YourNetworkPlugin()
        plugin.initialize()
        assertTrue(plugin.state.first().isInitialized)
    }
}
```

## Best Practices

1. **Always log events**: Use EventLogger for all significant transitions
2. **Handle errors gracefully**: Return `PluginActionResult.Failure` with meaningful codes
3. **Support privacy APIs**: Implement `setPrivacyConsent` properly
4. **Clean up resources**: Implement `destroy()` to release SDK resources
5. **Measure latency**: Track time from action start to callback
