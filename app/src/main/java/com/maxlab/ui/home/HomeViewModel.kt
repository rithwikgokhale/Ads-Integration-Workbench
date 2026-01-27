package com.maxlab.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxlab.ad.BannerController
import com.maxlab.ad.InterstitialController
import com.maxlab.ad.MaxSdkManager
import com.maxlab.ad.RewardedController
import com.maxlab.core.redactId
import com.maxlab.data.repository.SettingsRepository
import com.maxlab.domain.model.AdState
import com.maxlab.domain.model.Event
import com.maxlab.domain.model.InitState
import com.maxlab.domain.model.SdkMode
import com.maxlab.domain.model.SecretsStatus
import com.maxlab.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class HomeUiState(
    val initState: InitState = InitState.NotStarted,
    val lastInitError: String? = null,
    val lastEvent: Event? = null,
    val sdkMode: SdkMode = SdkMode.MOCK,
    val secretsStatus: SecretsStatus = SecretsStatus(null, com.maxlab.domain.model.SecretsSource.MISSING),
    val bannerState: AdState = AdState.Idle,
    val interstitialState: AdState = AdState.Idle,
    val rewardedState: AdState = AdState.Idle,
    val bannerAdUnitId: String? = null,
    val interstitialAdUnitId: String? = null,
    val rewardedAdUnitId: String? = null,
    val issueReproSummary: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sdkManager: MaxSdkManager,
    private val bannerController: BannerController,
    private val interstitialController: InterstitialController,
    private val rewardedController: RewardedController,
    private val eventRepository: EventRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val lastEventFlow: Flow<Event?> = eventRepository.observeRecentEvents(1)
        .map { it.firstOrNull() }

    private val baseFlow = combine(
        sdkManager.initState,
        sdkManager.sdkMode,
        sdkManager.secretsStatus
    ) { initState, sdkMode, secretsStatus ->
        Triple(initState, sdkMode, secretsStatus)
    }

    private val adStateFlow = combine(
        bannerController.state,
        interstitialController.state,
        rewardedController.state
    ) { bannerState, interstitialState, rewardedState ->
        Triple(bannerState, interstitialState, rewardedState)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        baseFlow,
        adStateFlow,
        lastEventFlow,
        settingsRepository.settingsFlow
    ) { base, adStates, lastEvent, settings ->
        val (initState, sdkMode, secretsStatus) = base
        val (bannerState, interstitialState, rewardedState) = adStates
        HomeUiState(
            initState = initState,
            lastInitError = if (initState is InitState.Failed) initState.errorMessage else null,
            lastEvent = lastEvent,
            sdkMode = sdkMode,
            secretsStatus = secretsStatus,
            bannerState = bannerState,
            interstitialState = interstitialState,
            rewardedState = rewardedState,
            bannerAdUnitId = redactId(secretsStatus.config?.bannerAdUnitId),
            interstitialAdUnitId = redactId(secretsStatus.config?.interstitialAdUnitId),
            rewardedAdUnitId = redactId(secretsStatus.config?.rewardedAdUnitId),
            issueReproSummary = buildString {
                if (settings.initTimeoutGuardEnabled) append("Init timeout guard ON. ")
                if (settings.badBannerAdUnitId) append("Bad banner ID. ")
                if (settings.badInterstitialAdUnitId) append("Bad interstitial ID. ")
                if (settings.badRewardedAdUnitId) append("Bad rewarded ID. ")
                if (settings.offlineGuardEnabled) append("Offline guard ON. ")
                append("Consent: ${settings.consentState}. ")
                append("Age restricted: ${settings.isAgeRestrictedUser}.")
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun initializeSdk() {
        viewModelScope.launch {
            sdkManager.initialize()
        }
    }

    fun loadBanner() {
        viewModelScope.launch {
            val adUnitId = uiState.value.secretsStatus.config?.bannerAdUnitId ?: "INVALID_AD_UNIT_ID"
            bannerController.load(adUnitId)
        }
    }

    fun showBanner() {
        bannerController.show()
    }

    fun loadInterstitial() {
        viewModelScope.launch {
            val adUnitId = uiState.value.secretsStatus.config?.interstitialAdUnitId ?: "INVALID_AD_UNIT_ID"
            interstitialController.load(adUnitId)
        }
    }

    fun showInterstitial() {
        interstitialController.show()
    }

    fun loadRewarded() {
        viewModelScope.launch {
            val adUnitId = uiState.value.secretsStatus.config?.rewardedAdUnitId ?: "INVALID_AD_UNIT_ID"
            rewardedController.load(adUnitId)
        }
    }

    fun showRewarded() {
        rewardedController.show()
    }
}
