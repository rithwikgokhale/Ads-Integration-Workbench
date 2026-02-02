package com.rithwik.integrationworkbench.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rithwik.integrationworkbench.domain.model.ConsentState
import com.rithwik.integrationworkbench.domain.model.HarnessSettings
import com.rithwik.integrationworkbench.domain.repository.SettingsRepository
import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.AdNetwork
import com.rithwik.integrationworkbench.plugins.PluginConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "workbench_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json
) : SettingsRepository {

    private object Keys {
        val initTimeoutEnabled = booleanPreferencesKey("init_timeout_enabled")
        val initTimeoutMs = longPreferencesKey("init_timeout_ms")
        val initRetryEnabled = booleanPreferencesKey("init_retry_enabled")
        val maxInitRetries = intPreferencesKey("max_init_retries")
        val badConfigInjectionEnabled = booleanPreferencesKey("bad_config_injection_enabled")
        val offlineGuardEnabled = booleanPreferencesKey("offline_guard_enabled")
        val simulateLoadFailure = booleanPreferencesKey("simulate_load_failure")
        val failureRatePercent = intPreferencesKey("failure_rate_percent")
        val consentState = intPreferencesKey("consent_state")
        val isAgeRestrictedUser = booleanPreferencesKey("is_age_restricted_user")
        fun pluginConfig(network: AdNetwork) = stringPreferencesKey("plugin_config_${network.name}")
    }

    override val harnessSettings: Flow<HarnessSettings> = context.dataStore.data.map { prefs ->
        HarnessSettings(
            initTimeoutEnabled = prefs[Keys.initTimeoutEnabled] ?: false,
            initTimeoutMs = prefs[Keys.initTimeoutMs] ?: 5000L,
            initRetryEnabled = prefs[Keys.initRetryEnabled] ?: false,
            maxInitRetries = prefs[Keys.maxInitRetries] ?: 3,
            badConfigInjectionEnabled = prefs[Keys.badConfigInjectionEnabled] ?: false,
            offlineGuardEnabled = prefs[Keys.offlineGuardEnabled] ?: true,
            simulateLoadFailure = prefs[Keys.simulateLoadFailure] ?: false,
            failureRatePercent = prefs[Keys.failureRatePercent] ?: 10,
            consentState = consentStateFromInt(prefs[Keys.consentState] ?: 0),
            isAgeRestrictedUser = prefs[Keys.isAgeRestrictedUser] ?: false
        )
    }

    override suspend fun updateHarnessSettings(settings: HarnessSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.initTimeoutEnabled] = settings.initTimeoutEnabled
            prefs[Keys.initTimeoutMs] = settings.initTimeoutMs
            prefs[Keys.initRetryEnabled] = settings.initRetryEnabled
            prefs[Keys.maxInitRetries] = settings.maxInitRetries
            prefs[Keys.badConfigInjectionEnabled] = settings.badConfigInjectionEnabled
            prefs[Keys.offlineGuardEnabled] = settings.offlineGuardEnabled
            prefs[Keys.simulateLoadFailure] = settings.simulateLoadFailure
            prefs[Keys.failureRatePercent] = settings.failureRatePercent
            prefs[Keys.consentState] = settings.consentState.toInt()
            prefs[Keys.isAgeRestrictedUser] = settings.isAgeRestrictedUser
        }
    }

    override suspend fun getHarnessSettings(): HarnessSettings = harnessSettings.first()

    override val consentState: Flow<ConsentState> = context.dataStore.data.map { prefs ->
        consentStateFromInt(prefs[Keys.consentState] ?: 0)
    }

    override suspend fun setConsentState(state: ConsentState) {
        context.dataStore.edit { prefs ->
            prefs[Keys.consentState] = state.toInt()
        }
    }

    override val isAgeRestrictedUser: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.isAgeRestrictedUser] ?: false
    }

    override suspend fun setAgeRestrictedUser(value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.isAgeRestrictedUser] = value
        }
    }

    override suspend fun getPluginConfig(network: AdNetwork): PluginConfig? {
        val stored = context.dataStore.data.first()[Keys.pluginConfig(network)]
        return stored?.let { decodePluginConfig(it) }
    }

    override suspend fun savePluginConfig(config: PluginConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.pluginConfig(config.network)] = encodePluginConfig(config)
        }
    }

    override fun observePluginConfig(network: AdNetwork): Flow<PluginConfig?> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.pluginConfig(network)]?.let { decodePluginConfig(it) }
        }

    private fun consentStateFromInt(value: Int): ConsentState = when (value) {
        1 -> ConsentState.GRANTED
        2 -> ConsentState.DENIED
        else -> ConsentState.UNKNOWN
    }

    private fun ConsentState.toInt(): Int = when (this) {
        ConsentState.UNKNOWN -> 0
        ConsentState.GRANTED -> 1
        ConsentState.DENIED -> 2
    }

    private fun encodePluginConfig(config: PluginConfig): String {
        val parts = mutableListOf<String>()
        parts.add("network=${config.network.name}")
        parts.add("enabled=${config.enabled}")
        config.credentials.forEach { (k, v) -> parts.add("cred:$k=$v") }
        config.adUnitIds.forEach { (f, id) -> parts.add("adUnit:${f.name}=$id") }
        config.extras.forEach { (k, v) -> parts.add("extra:$k=$v") }
        return parts.joinToString("|")
    }

    private fun decodePluginConfig(encoded: String): PluginConfig {
        val parts = encoded.split("|")
        var network = AdNetwork.MOCK
        var enabled = true
        val credentials = mutableMapOf<String, String>()
        val adUnitIds = mutableMapOf<AdFormat, String>()
        val extras = mutableMapOf<String, String>()

        parts.forEach { part ->
            val (key, value) = part.split("=", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
            when {
                key == "network" -> network = AdNetwork.valueOf(value)
                key == "enabled" -> enabled = value.toBoolean()
                key.startsWith("cred:") -> credentials[key.removePrefix("cred:")] = value
                key.startsWith("adUnit:") -> adUnitIds[AdFormat.valueOf(key.removePrefix("adUnit:"))] = value
                key.startsWith("extra:") -> extras[key.removePrefix("extra:")] = value
            }
        }

        return PluginConfig(
            network = network,
            enabled = enabled,
            credentials = credentials,
            adUnitIds = adUnitIds,
            extras = extras
        )
    }
}
