package com.maxlab.data.debug

import android.content.Context
import android.os.Build
import com.maxlab.core.redactId
import com.maxlab.core.sanitizePayload
import com.maxlab.data.repository.NetworkMonitor
import com.maxlab.data.repository.SecretsRepository
import com.maxlab.data.repository.SettingsRepository
import com.maxlab.domain.model.Event
import com.maxlab.domain.model.SecretsSource
import com.maxlab.domain.repository.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Serializable
data class EventExport(
    val id: String,
    val timestampMs: Long,
    val sessionId: String,
    val category: String,
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
data class AppConfigExport(
    val sdkMode: String,
    val sdkKeyRedacted: String?,
    val bannerAdUnitIdRedacted: String?,
    val interstitialAdUnitIdRedacted: String?,
    val rewardedAdUnitIdRedacted: String?,
    val consentState: String,
    val isAgeRestrictedUser: Boolean,
    val offlineGuardEnabled: Boolean,
    val initTimeoutGuardEnabled: Boolean,
    val badBannerAdUnitId: Boolean,
    val badInterstitialAdUnitId: Boolean,
    val badRewardedAdUnitId: Boolean
)

@Serializable
data class DeviceInfoExport(
    val manufacturer: String,
    val model: String,
    val sdkInt: Int,
    val release: String,
    val locale: String,
    val appVersion: String,
    val isOnline: Boolean
)

@Singleton
class DebugBundleExporter @Inject constructor(
    private val context: Context,
    private val eventRepository: EventRepository,
    private val settingsRepository: SettingsRepository,
    private val secretsRepository: SecretsRepository,
    private val networkMonitor: NetworkMonitor,
    private val json: Json
) {
    suspend fun export(): File = withContext(Dispatchers.IO) {
        val secrets = secretsRepository.loadSecrets()
        val config = secrets.config
        val secretsToRedact = listOf(config?.sdkKey, config?.bannerAdUnitId, config?.interstitialAdUnitId, config?.rewardedAdUnitId)
        val events = eventRepository.observeRecentEvents(2000).first()
        val errors = eventRepository.getLastNErrors(50)
        val settings = settingsRepository.currentSettings()
        val appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"

        val hasRealSecrets = secrets.source == SecretsSource.REAL &&
            config?.sdkKey?.isNotBlank() == true &&
            config.sdkKey != "PASTE_SDK_KEY_HERE"
        val appConfig = AppConfigExport(
            sdkMode = if (hasRealSecrets) "REAL" else "MOCK",
            sdkKeyRedacted = redactId(config?.sdkKey),
            bannerAdUnitIdRedacted = redactId(config?.bannerAdUnitId),
            interstitialAdUnitIdRedacted = redactId(config?.interstitialAdUnitId),
            rewardedAdUnitIdRedacted = redactId(config?.rewardedAdUnitId),
            consentState = settings.consentState.name,
            isAgeRestrictedUser = settings.isAgeRestrictedUser,
            offlineGuardEnabled = settings.offlineGuardEnabled,
            initTimeoutGuardEnabled = settings.initTimeoutGuardEnabled,
            badBannerAdUnitId = settings.badBannerAdUnitId,
            badInterstitialAdUnitId = settings.badInterstitialAdUnitId,
            badRewardedAdUnitId = settings.badRewardedAdUnitId
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

        val exportEvents = events.map { it.toExport(secretsToRedact) }
        val exportErrors = errors.map { it.toExport(secretsToRedact) }

        val zipFile = File(context.cacheDir, "maxlab-debug-bundle-${System.currentTimeMillis()}.zip")
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
        }
        zipFile
    }

    private fun Event.toExport(secrets: List<String?>): EventExport {
        return EventExport(
            id = id,
            timestampMs = timestampMs,
            sessionId = sessionId,
            category = category.name,
            format = format?.name,
            adUnitId = redactId(adUnitId),
            placement = placement,
            latencyMs = latencyMs,
            networkName = networkName,
            errorCode = errorCode,
            errorMessage = errorMessage,
            rawPayloadJson = sanitizePayload(rawPayloadJson, secrets)
        )
    }
}
