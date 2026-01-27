package com.maxlab.ad

import com.maxlab.domain.model.ConsentState

class RealMaxAdapter : MaxAdapter {
    private var listener: MaxAdapterListener? = null

    override fun setListener(listener: MaxAdapterListener?) {
        this.listener = listener
    }

    override suspend fun initialize(sdkKey: String) {
        // TODO: Wire AppLovin SDK init (AppLovin MAX Android docs).
        listener?.onEvent(MaxEvent.InitFailure(MaxError(9001, "Real adapter not yet wired")))
    }

    override fun setPrivacy(consentState: ConsentState, isAgeRestrictedUser: Boolean) {
        // TODO: Wire privacy and consent in AppLovin MAX Android docs.
    }

    override suspend fun loadBanner(adUnitId: String) {
        // TODO: Wire banner load in AppLovin MAX Android docs.
        listener?.onEvent(MaxEvent.AdLoadFailed(com.maxlab.domain.model.AdFormat.BANNER, adUnitId, MaxError(9002, "Banner load not wired")))
    }

    override fun showBanner() {
        // TODO: Wire banner display in AppLovin MAX Android docs.
    }

    override suspend fun loadInterstitial(adUnitId: String) {
        // TODO: Wire interstitial load in AppLovin MAX Android docs.
        listener?.onEvent(
            MaxEvent.AdLoadFailed(com.maxlab.domain.model.AdFormat.INTERSTITIAL, adUnitId, MaxError(9003, "Interstitial load not wired"))
        )
    }

    override fun showInterstitial() {
        // TODO: Wire interstitial show in AppLovin MAX Android docs.
    }

    override suspend fun loadRewarded(adUnitId: String) {
        // TODO: Wire rewarded load in AppLovin MAX Android docs.
        listener?.onEvent(
            MaxEvent.AdLoadFailed(com.maxlab.domain.model.AdFormat.REWARDED, adUnitId, MaxError(9004, "Rewarded load not wired"))
        )
    }

    override fun showRewarded() {
        // TODO: Wire rewarded show in AppLovin MAX Android docs.
    }
}
