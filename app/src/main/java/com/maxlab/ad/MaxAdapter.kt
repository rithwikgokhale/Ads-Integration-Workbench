package com.maxlab.ad

import com.maxlab.domain.model.AdFormat
import com.maxlab.domain.model.ConsentState

data class MaxError(
    val code: Int,
    val message: String
)

sealed class MaxEvent {
    data class InitSuccess(val networkName: String? = null) : MaxEvent()
    data class InitFailure(val error: MaxError) : MaxEvent()
    data class AdLoaded(val format: AdFormat, val adUnitId: String, val networkName: String?) : MaxEvent()
    data class AdLoadFailed(val format: AdFormat, val adUnitId: String, val error: MaxError) : MaxEvent()
    data class AdDisplayed(val format: AdFormat, val adUnitId: String) : MaxEvent()
    data class AdDisplayFailed(val format: AdFormat, val adUnitId: String, val error: MaxError) : MaxEvent()
    data class AdClicked(val format: AdFormat, val adUnitId: String) : MaxEvent()
    data class AdRevenue(val format: AdFormat, val adUnitId: String, val revenue: Double) : MaxEvent()
    data class RewardEarned(val adUnitId: String) : MaxEvent()
}

interface MaxAdapterListener {
    fun onEvent(event: MaxEvent)
}

interface MaxAdapter {
    fun setListener(listener: MaxAdapterListener?)
    suspend fun initialize(sdkKey: String)
    fun setPrivacy(consentState: ConsentState, isAgeRestrictedUser: Boolean)
    suspend fun loadBanner(adUnitId: String)
    fun showBanner()
    suspend fun loadInterstitial(adUnitId: String)
    fun showInterstitial()
    suspend fun loadRewarded(adUnitId: String)
    fun showRewarded()
}
