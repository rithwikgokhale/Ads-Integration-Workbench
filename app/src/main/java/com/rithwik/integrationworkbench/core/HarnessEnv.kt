package com.rithwik.integrationworkbench.core

import com.rithwik.integrationworkbench.domain.model.EventType
import com.rithwik.integrationworkbench.domain.model.HarnessSettings
import com.rithwik.integrationworkbench.domain.model.Status
import com.rithwik.integrationworkbench.domain.logging.EventLogger
import com.rithwik.integrationworkbench.domain.repository.SettingsRepository
import com.rithwik.integrationworkbench.plugins.AdNetwork
import com.rithwik.integrationworkbench.plugins.PluginAction
import com.rithwik.integrationworkbench.plugins.PluginActionResult
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides harness environment for testing and debugging.
 * Applies settings like timeout guards, offline guards, and retry policies.
 */
@Singleton
class HarnessEnv @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val networkMonitor: NetworkMonitor,
    private val eventLogger: EventLogger
) {
    suspend fun getSettings(): HarnessSettings = settingsRepository.getHarnessSettings()

    /**
     * Checks if the action should be blocked due to offline guard.
     */
    suspend fun shouldBlockOffline(action: PluginAction): Boolean {
        val settings = getSettings()
        if (!settings.offlineGuardEnabled) return false

        return when (action) {
            is PluginAction.Initialize,
            is PluginAction.Load -> !networkMonitor.isOnline()
            else -> false
        }
    }

    /**
     * Logs an offline blocked event.
     */
    fun logOfflineBlocked(network: AdNetwork, action: PluginAction) {
        eventLogger.log(
            eventType = EventType.SYSTEM,
            status = Status.FAILURE,
            network = network,
            format = action.format,
            errorCode = -100,
            errorMessage = "OFFLINE_BLOCKED"
        )
    }

    /**
     * Execute with timeout guard and retry policy.
     */
    suspend fun <T> executeWithGuards(
        network: AdNetwork,
        action: PluginAction,
        block: suspend () -> T
    ): T {
        val settings = getSettings()

        // Offline guard
        if (shouldBlockOffline(action)) {
            logOfflineBlocked(network, action)
            @Suppress("UNCHECKED_CAST")
            return PluginActionResult.Failure(
                action = action,
                errorCode = -100,
                errorMessage = "OFFLINE_BLOCKED"
            ) as T
        }

        // Init timeout + retry for Initialize action
        if (action is PluginAction.Initialize && settings.initTimeoutEnabled) {
            return executeWithTimeoutAndRetry(network, settings, block)
        }

        return block()
    }

    private suspend fun <T> executeWithTimeoutAndRetry(
        network: AdNetwork,
        settings: HarnessSettings,
        block: suspend () -> T
    ): T {
        val maxAttempts = if (settings.initRetryEnabled) settings.maxInitRetries else 1
        val backoffDelays = listOf(1000L, 2000L, 4000L)

        repeat(maxAttempts) { attempt ->
            try {
                return withTimeout(settings.initTimeoutMs) {
                    block()
                }
            } catch (e: TimeoutCancellationException) {
                eventLogger.log(
                    eventType = EventType.SYSTEM,
                    status = Status.FAILURE,
                    network = network,
                    errorCode = -101,
                    errorMessage = "INIT_TIMEOUT",
                    extras = mapOf(
                        "attempt" to (attempt + 1).toString(),
                        "timeoutMs" to settings.initTimeoutMs.toString()
                    )
                )

                if (attempt < maxAttempts - 1) {
                    val delayMs = backoffDelays.getOrElse(attempt) { 4000L }
                    delay(delayMs)
                } else {
                    throw e
                }
            }
        }

        throw TimeoutCancellationException("Max retries exceeded")
    }

    /**
     * Apply bad config injection if enabled.
     */
    suspend fun applyBadConfigInjection(adUnitId: String): String {
        val settings = getSettings()
        return if (settings.badConfigInjectionEnabled) {
            "INVALID_AD_UNIT_ID"
        } else {
            adUnitId
        }
    }
}
