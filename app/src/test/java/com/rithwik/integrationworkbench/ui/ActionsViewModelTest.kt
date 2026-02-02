package com.rithwik.integrationworkbench.ui

import com.rithwik.integrationworkbench.core.Clock
import com.rithwik.integrationworkbench.core.HarnessEnv
import com.rithwik.integrationworkbench.core.IdGenerator
import com.rithwik.integrationworkbench.core.NetworkMonitor
import com.rithwik.integrationworkbench.domain.logging.EventLogger
import com.rithwik.integrationworkbench.domain.logging.EventLoggerImpl
import com.rithwik.integrationworkbench.domain.logging.EventSanitizer
import com.rithwik.integrationworkbench.domain.logging.SessionProvider
import com.rithwik.integrationworkbench.domain.model.ConsentState
import com.rithwik.integrationworkbench.domain.model.EventRecord
import com.rithwik.integrationworkbench.domain.model.EventType
import com.rithwik.integrationworkbench.domain.model.HarnessSettings
import com.rithwik.integrationworkbench.domain.model.Status
import com.rithwik.integrationworkbench.domain.repository.EventRepository
import com.rithwik.integrationworkbench.domain.repository.SettingsRepository
import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork
import com.rithwik.integrationworkbench.plugins.AdsIntegrationPlugin
import com.rithwik.integrationworkbench.plugins.PluginConfig
import com.rithwik.integrationworkbench.plugins.PluginRegistry
import com.rithwik.integrationworkbench.plugins.mock.MockAdsPlugin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ActionsViewModelTest {

    private lateinit var mockPlugin: MockAdsPlugin
    private lateinit var pluginRegistry: PluginRegistry

    @Before
    fun setup() {
        mockPlugin = MockAdsPlugin()
        pluginRegistry = PluginRegistry(setOf(mockPlugin))
    }

    @Test
    fun `plugin registry returns available networks`() {
        val networks = pluginRegistry.getAvailableNetworks()
        assertEquals(1, networks.size)
        assertEquals(AdNetwork.MOCK, networks[0])
    }

    @Test
    fun `plugin registry finds plugin by network`() {
        val plugin = pluginRegistry.getByNetwork(AdNetwork.MOCK)
        assertNotNull(plugin)
        assertEquals(AdNetwork.MOCK, plugin?.network)
    }

    @Test
    fun `plugin registry returns null for unknown network`() {
        val plugin = pluginRegistry.getByNetwork(AdNetwork.ADMOB)
        assertEquals(null, plugin)
    }
}
