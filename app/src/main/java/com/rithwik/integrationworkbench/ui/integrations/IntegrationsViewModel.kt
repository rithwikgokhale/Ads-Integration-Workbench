package com.rithwik.integrationworkbench.ui.integrations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rithwik.integrationworkbench.domain.model.HarnessSettings
import com.rithwik.integrationworkbench.domain.repository.SettingsRepository
import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork
import com.rithwik.integrationworkbench.plugins.AdsIntegrationPlugin
import com.rithwik.integrationworkbench.plugins.PluginConfig
import com.rithwik.integrationworkbench.plugins.PluginRegistry
import com.rithwik.integrationworkbench.plugins.PluginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IntegrationsUiState(
    val plugins: List<PluginUiState> = emptyList(),
    val selectedPlugin: AdNetwork? = null,
    val harnessSettings: HarnessSettings = HarnessSettings(),
    val editingConfig: PluginConfig? = null
)

data class PluginUiState(
    val network: AdNetwork,
    val displayName: String,
    val supportedFormats: Set<AdFormat>,
    val state: PluginState,
    val config: PluginConfig?
)

@HiltViewModel
class IntegrationsViewModel @Inject constructor(
    private val pluginRegistry: PluginRegistry,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val selectedPlugin = MutableStateFlow<AdNetwork?>(null)
    private val editingConfig = MutableStateFlow<PluginConfig?>(null)

    val uiState: StateFlow<IntegrationsUiState> = combine(
        settingsRepository.harnessSettings,
        selectedPlugin,
        editingConfig
    ) { harnessSettings, selected, editing ->
        val plugins = pluginRegistry.getAll().map { plugin ->
            val config = settingsRepository.getPluginConfig(plugin.network)
            PluginUiState(
                network = plugin.network,
                displayName = plugin.network.displayName,
                supportedFormats = plugin.supportedFormats,
                state = PluginState(network = plugin.network),
                config = config
            )
        }
        IntegrationsUiState(
            plugins = plugins,
            selectedPlugin = selected,
            harnessSettings = harnessSettings,
            editingConfig = editing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IntegrationsUiState())

    fun selectPlugin(network: AdNetwork) {
        selectedPlugin.value = network
        viewModelScope.launch {
            val config = settingsRepository.getPluginConfig(network)
                ?: PluginConfig(network = network)
            editingConfig.value = config
        }
    }

    fun updateHarnessSettings(settings: HarnessSettings) {
        viewModelScope.launch {
            settingsRepository.updateHarnessSettings(settings)
        }
    }

    fun updateEditingConfig(config: PluginConfig) {
        editingConfig.value = config
    }

    fun savePluginConfig() {
        val config = editingConfig.value ?: return
        viewModelScope.launch {
            settingsRepository.savePluginConfig(config)
            editingConfig.value = null
        }
    }

    fun cancelEdit() {
        editingConfig.value = null
        selectedPlugin.value = null
    }
}
