package com.rithwik.integrationworkbench.domain.logging

import com.rithwik.integrationworkbench.domain.repository.SettingsRepository
import com.rithwik.integrationworkbench.plugins.AdNetwork
import com.rithwik.integrationworkbench.plugins.PluginRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sanitizes event data by redacting sensitive information.
 */
@Singleton
class EventSanitizer @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val pluginRegistry: PluginRegistry
) {
    private val sensitivePatterns = listOf(
        Regex("\"(sdk[Kk]ey|api[Kk]ey|app[Ii]d|secret)\"\\s*:\\s*\"[^\"]+\""),
        Regex("(ca-app-pub-\\d+-\\d+)"),
        Regex("([a-f0-9]{32,})"),
    )

    fun redactAdUnitId(adUnitId: String?): String? {
        if (adUnitId.isNullOrBlank()) return adUnitId
        if (adUnitId.length <= 8) return "****"
        return adUnitId.take(4) + "..." + adUnitId.takeLast(4)
    }

    fun sanitizePayload(payload: String?): String? {
        if (payload.isNullOrBlank()) return payload

        var sanitized = payload

        // Redact known sensitive patterns
        sensitivePatterns.forEach { pattern ->
            sanitized = sanitized?.replace(pattern) { match ->
                val value = match.value
                if (value.length > 8) {
                    value.take(4) + "..." + value.takeLast(4)
                } else {
                    "****"
                }
            }
        }

        // Redact stored credentials
        try {
            val secrets = runBlocking { collectSecrets() }
            secrets.forEach { secret ->
                if (secret.length > 4) {
                    sanitized = sanitized?.replace(secret, redactAdUnitId(secret) ?: "****")
                }
            }
        } catch (e: Exception) {
            // Ignore - best effort
        }

        return sanitized
    }

    private suspend fun collectSecrets(): List<String> {
        val secrets = mutableListOf<String>()
        pluginRegistry.getAvailableNetworks().forEach { network ->
            val config = settingsRepository.getPluginConfig(network)
            config?.credentials?.values?.let { secrets.addAll(it) }
            config?.adUnitIds?.values?.let { secrets.addAll(it) }
        }
        return secrets.filter { it.isNotBlank() }
    }
}
