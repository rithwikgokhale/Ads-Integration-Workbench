package com.rithwik.integrationworkbench.domain.repository

import com.rithwik.integrationworkbench.domain.model.ConsentState
import com.rithwik.integrationworkbench.domain.model.HarnessSettings
import com.rithwik.integrationworkbench.plugins.AdNetwork
import com.rithwik.integrationworkbench.plugins.PluginConfig
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val harnessSettings: Flow<HarnessSettings>
    suspend fun updateHarnessSettings(settings: HarnessSettings)
    suspend fun getHarnessSettings(): HarnessSettings

    val consentState: Flow<ConsentState>
    suspend fun setConsentState(state: ConsentState)

    val isAgeRestrictedUser: Flow<Boolean>
    suspend fun setAgeRestrictedUser(value: Boolean)

    suspend fun getPluginConfig(network: AdNetwork): PluginConfig?
    suspend fun savePluginConfig(config: PluginConfig)
    fun observePluginConfig(network: AdNetwork): Flow<PluginConfig?>
}
