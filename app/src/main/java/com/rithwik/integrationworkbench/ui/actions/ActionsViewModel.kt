package com.rithwik.integrationworkbench.ui.actions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rithwik.integrationworkbench.core.HarnessEnv
import com.rithwik.integrationworkbench.domain.logging.EventLogger
import com.rithwik.integrationworkbench.domain.model.EventType
import com.rithwik.integrationworkbench.domain.model.Status
import com.rithwik.integrationworkbench.domain.repository.SettingsRepository
import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork
import com.rithwik.integrationworkbench.plugins.AdState
import com.rithwik.integrationworkbench.plugins.AdsIntegrationPlugin
import com.rithwik.integrationworkbench.plugins.PluginAction
import com.rithwik.integrationworkbench.plugins.PluginActionDescriptor
import com.rithwik.integrationworkbench.plugins.PluginActionResult
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

data class ActionsUiState(
    val availableNetworks: List<AdNetwork> = emptyList(),
    val selectedNetwork: AdNetwork? = null,
    val availableActions: List<PluginActionDescriptor> = emptyList(),
    val pluginState: PluginState? = null,
    val lastResult: PluginActionResult? = null,
    val isExecuting: Boolean = false,
    val bannerVisible: Boolean = false
)

@HiltViewModel
class ActionsViewModel @Inject constructor(
    private val pluginRegistry: PluginRegistry,
    private val settingsRepository: SettingsRepository,
    private val harnessEnv: HarnessEnv,
    private val eventLogger: EventLogger
) : ViewModel() {
    private val selectedNetwork = MutableStateFlow<AdNetwork?>(null)
    private val lastResult = MutableStateFlow<PluginActionResult?>(null)
    private val isExecuting = MutableStateFlow(false)
    private val bannerVisible = MutableStateFlow(false)
    private val pluginState = MutableStateFlow<PluginState?>(null)

    val uiState: StateFlow<ActionsUiState> = combine(
        selectedNetwork,
        lastResult,
        isExecuting,
        bannerVisible,
        pluginState
    ) { network, result, executing, banner, state ->
        val plugin = network?.let { pluginRegistry.getByNetwork(it) }
        ActionsUiState(
            availableNetworks = pluginRegistry.getAvailableNetworks(),
            selectedNetwork = network,
            availableActions = plugin?.getAvailableActions() ?: emptyList(),
            pluginState = state,
            lastResult = result,
            isExecuting = executing,
            bannerVisible = banner
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActionsUiState())

    init {
        viewModelScope.launch {
            selectedNetwork.collect { network ->
                network?.let { n ->
                    pluginRegistry.getByNetwork(n)?.state?.collect { state ->
                        pluginState.value = state
                    }
                }
            }
        }
    }

    fun selectNetwork(network: AdNetwork) {
        selectedNetwork.value = network
        lastResult.value = null
        bannerVisible.value = false
        viewModelScope.launch {
            val plugin = pluginRegistry.getByNetwork(network)
            val config = settingsRepository.getPluginConfig(network)
            config?.let { plugin?.configure(it) }
        }
    }

    fun executeAction(action: PluginAction) {
        val network = selectedNetwork.value ?: return
        val plugin = pluginRegistry.getByNetwork(network) ?: return

        viewModelScope.launch {
            isExecuting.value = true
            lastResult.value = null

            val effectiveAction = when (action) {
                is PluginAction.Load -> {
                    val config = settingsRepository.getPluginConfig(network)
                    val adUnitId = config?.adUnitIds?.get(action.adFormat) ?: action.adUnitId
                    val effectiveAdUnitId = harnessEnv.applyBadConfigInjection(adUnitId)
                    PluginAction.Load(action.adFormat, effectiveAdUnitId)
                }
                is PluginAction.Show -> {
                    val config = settingsRepository.getPluginConfig(network)
                    val adUnitId = config?.adUnitIds?.get(action.adFormat) ?: action.adUnitId
                    PluginAction.Show(action.adFormat, adUnitId)
                }
                else -> action
            }

            val result = harnessEnv.executeWithGuards(network, effectiveAction) {
                plugin.execute(effectiveAction)
            }

            lastResult.value = result
            isExecuting.value = false

            // Log the result
            when (result) {
                is PluginActionResult.Success -> {
                    val eventType = when (effectiveAction) {
                        is PluginAction.Initialize -> EventType.INIT
                        is PluginAction.Load -> EventType.LOAD
                        is PluginAction.Show -> EventType.SHOW
                        else -> EventType.SYSTEM
                    }
                    eventLogger.log(
                        eventType = eventType,
                        status = Status.SUCCESS,
                        network = network,
                        format = effectiveAction.format,
                        latencyMs = result.latencyMs,
                        networkName = result.networkName
                    )

                    // Show banner if it's a banner show action
                    if (effectiveAction is PluginAction.Show && effectiveAction.adFormat == AdFormat.BANNER) {
                        bannerVisible.value = true
                    }
                }
                is PluginActionResult.Failure -> {
                    eventLogger.log(
                        eventType = EventType.ERROR,
                        status = Status.FAILURE,
                        network = network,
                        format = effectiveAction.format,
                        errorCode = result.errorCode,
                        errorMessage = result.errorMessage,
                        latencyMs = result.latencyMs
                    )
                }
                is PluginActionResult.NotImplemented -> {
                    eventLogger.log(
                        eventType = EventType.SYSTEM,
                        status = Status.NOT_IMPLEMENTED,
                        network = network,
                        format = effectiveAction.format,
                        errorMessage = "Action not implemented"
                    )
                }
                is PluginActionResult.Cancelled -> {
                    eventLogger.log(
                        eventType = EventType.SYSTEM,
                        status = Status.CANCELLED,
                        network = network,
                        format = effectiveAction.format
                    )
                }
            }
        }
    }

    fun hideBanner() {
        bannerVisible.value = false
    }
}
