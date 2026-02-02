package com.rithwik.integrationworkbench.plugins

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry of all available ad integration plugins.
 * Uses Hilt multibinding to collect all plugins.
 */
@Singleton
class PluginRegistry @Inject constructor(
    private val plugins: Set<@JvmSuppressWildcards AdsIntegrationPlugin>
) {
    fun getAll(): List<AdsIntegrationPlugin> = plugins.toList()

    fun getByNetwork(network: AdNetwork): AdsIntegrationPlugin? =
        plugins.find { it.network == network }

    fun getAvailableNetworks(): List<AdNetwork> =
        plugins.map { it.network }
}
