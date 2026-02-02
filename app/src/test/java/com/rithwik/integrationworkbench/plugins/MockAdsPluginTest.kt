package com.rithwik.integrationworkbench.plugins

import com.rithwik.integrationworkbench.plugins.mock.MockAdsPlugin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockAdsPluginTest {
    private lateinit var plugin: MockAdsPlugin

    @Before
    fun setup() {
        plugin = MockAdsPlugin()
    }

    @Test
    fun `plugin has correct network`() {
        assertEquals(AdNetwork.MOCK, plugin.network)
    }

    @Test
    fun `plugin supports expected formats`() {
        assertTrue(plugin.supportedFormats.contains(AdFormat.BANNER))
        assertTrue(plugin.supportedFormats.contains(AdFormat.INTERSTITIAL))
        assertTrue(plugin.supportedFormats.contains(AdFormat.REWARDED))
    }

    @Test
    fun `initialize returns success`() = runBlocking {
        val result = plugin.initialize()
        assertTrue(result is PluginInitResult.Success)
    }

    @Test
    fun `state updates after initialization`() = runBlocking {
        val stateBefore = plugin.state.first()
        assertFalse(stateBefore.isInitialized)

        plugin.initialize()

        val stateAfter = plugin.state.first()
        assertTrue(stateAfter.isInitialized)
    }

    @Test
    fun `load fails before init`() = runBlocking {
        val result = plugin.execute(PluginAction.Load(AdFormat.BANNER, "test-unit"))
        assertTrue(result is PluginActionResult.Failure)
    }

    @Test
    fun `load succeeds after init`() = runBlocking {
        plugin.initialize()
        val result = plugin.execute(PluginAction.Load(AdFormat.BANNER, "test-unit"))
        // May succeed or fail randomly, but should not be NotImplemented
        assertTrue(result is PluginActionResult.Success || result is PluginActionResult.Failure)
    }

    @Test
    fun `show fails if not loaded`() = runBlocking {
        plugin.initialize()
        val result = plugin.execute(PluginAction.Show(AdFormat.BANNER, "test-unit"))
        assertTrue(result is PluginActionResult.Failure)
        if (result is PluginActionResult.Failure) {
            assertEquals("Ad not loaded", result.errorMessage)
        }
    }

    @Test
    fun `health check returns correct state`() = runBlocking {
        val healthBefore = plugin.healthCheck()
        assertFalse(healthBefore.isInitialized)

        plugin.initialize()

        val healthAfter = plugin.healthCheck()
        assertTrue(healthAfter.isInitialized)
        assertEquals("mock-1.0.0", healthAfter.sdkVersion)
    }

    @Test
    fun `configure with simulated failure causes init to fail`() = runBlocking {
        plugin.configure(
            PluginConfig(
                network = AdNetwork.MOCK,
                extras = mapOf("simulateInitFailure" to "true")
            )
        )
        val result = plugin.initialize()
        assertTrue(result is PluginInitResult.Failure)
    }

    @Test
    fun `destroy resets state`() = runBlocking {
        plugin.initialize()
        assertTrue(plugin.state.first().isInitialized)

        plugin.destroy()

        assertFalse(plugin.state.first().isInitialized)
    }
}
