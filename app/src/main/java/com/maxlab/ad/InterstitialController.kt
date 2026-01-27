package com.maxlab.ad

import com.maxlab.data.repository.NetworkMonitor
import com.maxlab.data.repository.SettingsRepository
import com.maxlab.domain.logging.EventLogger
import com.maxlab.domain.model.AdFormat
import com.maxlab.domain.model.AdState
import com.maxlab.domain.model.EventCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialController @Inject constructor(
    private val sdkManager: MaxSdkManager,
    private val settingsRepository: SettingsRepository,
    private val networkMonitor: NetworkMonitor,
    private val eventLogger: EventLogger
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val stateFlow = MutableStateFlow<AdState>(AdState.Idle)
    private var lastLoadedAdUnitId: String? = null

    val state: StateFlow<AdState> = stateFlow

    init {
        scope.launch {
            sdkManager.adapterEvents.collectLatest { event ->
                when (event) {
                    is MaxEvent.AdLoaded -> if (event.format == AdFormat.INTERSTITIAL) {
                        stateFlow.value = AdState.Loaded
                        lastLoadedAdUnitId = event.adUnitId
                    }
                    is MaxEvent.AdLoadFailed -> if (event.format == AdFormat.INTERSTITIAL) {
                        stateFlow.value = AdState.Failed(event.error.message)
                    }
                    is MaxEvent.AdDisplayed -> if (event.format == AdFormat.INTERSTITIAL) {
                        stateFlow.value = AdState.Showing
                    }
                    is MaxEvent.AdDisplayFailed -> if (event.format == AdFormat.INTERSTITIAL) {
                        stateFlow.value = AdState.Failed(event.error.message)
                    }
                    else -> Unit
                }
            }
        }
    }

    suspend fun load(adUnitId: String) {
        val settings = settingsRepository.currentSettings()
        if (settings.offlineGuardEnabled && !networkMonitor.isOnline()) {
            eventLogger.log(
                category = EventCategory.SYSTEM,
                format = AdFormat.INTERSTITIAL,
                errorMessage = "OFFLINE_BLOCKED"
            )
            return
        }
        stateFlow.value = AdState.Loading
        val effectiveAdUnitId = if (settings.badInterstitialAdUnitId) "INVALID_AD_UNIT_ID" else adUnitId
        sdkManager.recordLoadStart(AdFormat.INTERSTITIAL, effectiveAdUnitId)
        sdkManager.currentAdapter().loadInterstitial(effectiveAdUnitId)
    }

    fun show() {
        sdkManager.currentAdapter().showInterstitial()
    }

    fun currentAdUnitId(): String? = lastLoadedAdUnitId
}
