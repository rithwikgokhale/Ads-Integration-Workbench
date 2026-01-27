package com.maxlab.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SecretsConfig(
    val sdkKey: String,
    val bannerAdUnitId: String,
    val interstitialAdUnitId: String,
    val rewardedAdUnitId: String
)

data class SecretsStatus(
    val config: SecretsConfig?,
    val source: SecretsSource,
    val errorMessage: String? = null
)

enum class SecretsSource {
    REAL,
    TEMPLATE,
    MISSING
}

data class IssueReproSettings(
    val initTimeoutGuardEnabled: Boolean = false,
    val badBannerAdUnitId: Boolean = false,
    val badInterstitialAdUnitId: Boolean = false,
    val badRewardedAdUnitId: Boolean = false,
    val offlineGuardEnabled: Boolean = true,
    val consentState: ConsentState = ConsentState.UNKNOWN,
    val isAgeRestrictedUser: Boolean = false
)
