package com.maxlab.ad

import com.maxlab.domain.model.AdFormat
import com.maxlab.domain.model.ConsentState
import kotlinx.coroutines.delay
import kotlin.random.Random

class MockMaxAdapter(
    private val random: Random = Random.Default,
    private val initDelayMs: Long = 1200L,
    private val loadDelayRangeMs: LongRange = 500L..1500L,
    private val failureRate: Double = 0.15
) : MaxAdapter {
    private var listener: MaxAdapterListener? = null
    private var isInitialized: Boolean = false
    private var lastBannerId: String? = null
    private var lastInterstitialId: String? = null
    private var lastRewardedId: String? = null

    override fun setListener(listener: MaxAdapterListener?) {
        this.listener = listener
    }

    override suspend fun initialize(sdkKey: String) {
        delay(initDelayMs)
        isInitialized = true
        listener?.onEvent(MaxEvent.InitSuccess(networkName = "mock"))
    }

    override fun setPrivacy(consentState: ConsentState, isAgeRestrictedUser: Boolean) {
        // Mock adapter tracks privacy only for payloads; no-op for now.
    }

    override suspend fun loadBanner(adUnitId: String) {
        lastBannerId = adUnitId
        simulateLoad(AdFormat.BANNER, adUnitId)
    }

    override fun showBanner() {
        if (!isInitialized) return
        val adUnitId = lastBannerId ?: "mock-banner"
        listener?.onEvent(MaxEvent.AdDisplayed(AdFormat.BANNER, adUnitId = adUnitId))
        listener?.onEvent(MaxEvent.AdRevenue(AdFormat.BANNER, adUnitId = adUnitId, revenue = 0.02))
    }

    override suspend fun loadInterstitial(adUnitId: String) {
        lastInterstitialId = adUnitId
        simulateLoad(AdFormat.INTERSTITIAL, adUnitId)
    }

    override fun showInterstitial() {
        if (!isInitialized) return
        val adUnitId = lastInterstitialId ?: "mock-interstitial"
        listener?.onEvent(MaxEvent.AdDisplayed(AdFormat.INTERSTITIAL, adUnitId = adUnitId))
        if (random.nextDouble() < 0.2) {
            listener?.onEvent(MaxEvent.AdClicked(AdFormat.INTERSTITIAL, adUnitId = adUnitId))
        }
        listener?.onEvent(MaxEvent.AdRevenue(AdFormat.INTERSTITIAL, adUnitId = adUnitId, revenue = 0.12))
    }

    override suspend fun loadRewarded(adUnitId: String) {
        lastRewardedId = adUnitId
        simulateLoad(AdFormat.REWARDED, adUnitId)
    }

    override fun showRewarded() {
        if (!isInitialized) return
        val adUnitId = lastRewardedId ?: "mock-rewarded"
        listener?.onEvent(MaxEvent.AdDisplayed(AdFormat.REWARDED, adUnitId = adUnitId))
        listener?.onEvent(MaxEvent.RewardEarned(adUnitId = adUnitId))
        listener?.onEvent(MaxEvent.AdRevenue(AdFormat.REWARDED, adUnitId = adUnitId, revenue = 0.25))
    }

    private suspend fun simulateLoad(format: AdFormat, adUnitId: String) {
        if (!isInitialized) {
            listener?.onEvent(
                MaxEvent.AdLoadFailed(format, adUnitId, MaxError(-1, "SDK not initialized (mock)"))
            )
            return
        }
        delay(random.nextLong(loadDelayRangeMs.first, loadDelayRangeMs.last))
        if (random.nextDouble() < failureRate) {
            listener?.onEvent(
                MaxEvent.AdLoadFailed(format, adUnitId, MaxError(1001, "Mock load failure"))
            )
        } else {
            listener?.onEvent(MaxEvent.AdLoaded(format, adUnitId, networkName = "mock-network"))
        }
    }
}
