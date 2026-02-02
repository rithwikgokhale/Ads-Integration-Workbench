package com.rithwik.integrationworkbench.data.exporter

import android.content.Context
import android.os.Build
import com.rithwik.integrationworkbench.core.NetworkMonitor
import com.rithwik.integrationworkbench.domain.logging.EventSanitizer
import com.rithwik.integrationworkbench.domain.model.AppConfigExport
import com.rithwik.integrationworkbench.domain.model.DeviceInfoExport
import com.rithwik.integrationworkbench.domain.model.EventRecord
import com.rithwik.integrationworkbench.domain.repository.EventRepository
import com.rithwik.integrationworkbench.domain.repository.SettingsRepository
import com.rithwik.integrationworkbench.plugins.PluginRegistry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import com.rithwik.integrationworkbench.BuildConfig
import javax.inject.Singleton

@Serializable
data class EventExport(
    val id: String,
    val timestampMs: Long,
    val sessionId: String,
    val eventType: String,
    val status: String,
    val network: String? = null,
    val format: String? = null,
    val adUnitId: String? = null,
    val placement: String? = null,
    val latencyMs: Long? = null,
    val networkName: String? = null,
    val errorCode: Int? = null,
    val errorMessage: String? = null,
    val rawPayloadJson: String? = null
)

@Serializable
data class PluginConfigRedacted(
    val network: String,
    val enabled: Boolean,
    val credentialsRedacted: Map<String, String>,
    val adUnitIdsRedacted: Map<String, String>
)

@Singleton
class DebugBundleExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventRepository: EventRepository,
    private val settingsRepository: SettingsRepository,
    private val pluginRegistry: PluginRegistry,
    private val networkMonitor: NetworkMonitor,
    private val sanitizer: EventSanitizer,
    private val json: Json
) {
    suspend fun export(): File = withContext(Dispatchers.IO) {
        val events = eventRepository.observeRecentEvents(2000).first()
        val errors = eventRepository.getLastNErrors(50)
        val harnessSettings = settingsRepository.getHarnessSettings()
        val appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"

        val appConfig = AppConfigExport(
            appVersion = appVersion,
            buildType = if (BuildConfig.DEBUG) "debug" else "release",
            enabledPlugins = pluginRegistry.getAvailableNetworks().map { it.name },
            harnessSettings = harnessSettings,
            consentState = harnessSettings.consentState.name,
            isAgeRestrictedUser = harnessSettings.isAgeRestrictedUser
        )

        val deviceInfo = DeviceInfoExport(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            sdkInt = Build.VERSION.SDK_INT,
            release = Build.VERSION.RELEASE ?: "unknown",
            locale = Locale.getDefault().toString(),
            appVersion = appVersion,
            isOnline = networkMonitor.isOnline()
        )

        val pluginConfigs = pluginRegistry.getAvailableNetworks().mapNotNull { network ->
            settingsRepository.getPluginConfig(network)?.let { config ->
                PluginConfigRedacted(
                    network = config.network.name,
                    enabled = config.enabled,
                    credentialsRedacted = config.credentials.mapValues { sanitizer.redactAdUnitId(it.value) ?: "****" },
                    adUnitIdsRedacted = config.adUnitIds.mapKeys { it.key.name }
                        .mapValues { sanitizer.redactAdUnitId(it.value) ?: "****" }
                )
            }
        }

        val exportEvents = events.map { it.toExport() }
        val exportErrors = errors.map { it.toExport() }

        val zipFile = File(context.cacheDir, "workbench-debug-bundle-${System.currentTimeMillis()}.zip")
        ZipOutputStream(zipFile.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("events.json"))
            zip.write(json.encodeToString(exportEvents).toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("app_config.json"))
            zip.write(json.encodeToString(appConfig).toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("device_info.json"))
            zip.write(json.encodeToString(deviceInfo).toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("last_50_errors.json"))
            zip.write(json.encodeToString(exportErrors).toByteArray())
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("plugin_config_redacted.json"))
            zip.write(json.encodeToString(pluginConfigs).toByteArray())
            zip.closeEntry()
        }
        zipFile
    }

    private fun EventRecord.toExport(): EventExport {
        return EventExport(
            id = id,
            timestampMs = timestampMs,
            sessionId = sessionId,
            eventType = eventType.name,
            status = status.name,
            network = network?.name,
            format = format?.name,
            adUnitId = sanitizer.redactAdUnitId(adUnitId),
            placement = placement,
            latencyMs = latencyMs,
            networkName = networkName,
            errorCode = errorCode,
            errorMessage = errorMessage,
            rawPayloadJson = sanitizer.sanitizePayload(rawPayloadJson)
        )
    }
}

