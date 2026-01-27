package com.maxlab.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.maxlab.domain.model.ConsentState
import com.maxlab.domain.model.IssueReproSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "issue_repro_settings")

@Singleton
class SettingsRepository @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val initTimeoutGuardEnabled = booleanPreferencesKey("init_timeout_guard_enabled")
        val badBannerAdUnitId = booleanPreferencesKey("bad_banner_ad_unit_id")
        val badInterstitialAdUnitId = booleanPreferencesKey("bad_interstitial_ad_unit_id")
        val badRewardedAdUnitId = booleanPreferencesKey("bad_rewarded_ad_unit_id")
        val offlineGuardEnabled = booleanPreferencesKey("offline_guard_enabled")
        val consentState = intPreferencesKey("consent_state")
        val isAgeRestrictedUser = booleanPreferencesKey("is_age_restricted_user")
    }

    val settingsFlow: Flow<IssueReproSettings> = context.dataStore.data.map { prefs ->
        IssueReproSettings(
            initTimeoutGuardEnabled = prefs[Keys.initTimeoutGuardEnabled] ?: false,
            badBannerAdUnitId = prefs[Keys.badBannerAdUnitId] ?: false,
            badInterstitialAdUnitId = prefs[Keys.badInterstitialAdUnitId] ?: false,
            badRewardedAdUnitId = prefs[Keys.badRewardedAdUnitId] ?: false,
            offlineGuardEnabled = prefs[Keys.offlineGuardEnabled] ?: true,
            consentState = consentStateFromInt(prefs[Keys.consentState] ?: 0),
            isAgeRestrictedUser = prefs[Keys.isAgeRestrictedUser] ?: false
        )
    }

    suspend fun updateSettings(update: IssueReproSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.initTimeoutGuardEnabled] = update.initTimeoutGuardEnabled
            prefs[Keys.badBannerAdUnitId] = update.badBannerAdUnitId
            prefs[Keys.badInterstitialAdUnitId] = update.badInterstitialAdUnitId
            prefs[Keys.badRewardedAdUnitId] = update.badRewardedAdUnitId
            prefs[Keys.offlineGuardEnabled] = update.offlineGuardEnabled
            prefs[Keys.consentState] = update.consentState.toInt()
            prefs[Keys.isAgeRestrictedUser] = update.isAgeRestrictedUser
        }
    }

    suspend fun currentSettings(): IssueReproSettings = settingsFlow.first()

    private fun consentStateFromInt(value: Int): ConsentState =
        when (value) {
            1 -> ConsentState.GRANTED
            2 -> ConsentState.DENIED
            else -> ConsentState.UNKNOWN
        }

    private fun ConsentState.toInt(): Int =
        when (this) {
            ConsentState.UNKNOWN -> 0
            ConsentState.GRANTED -> 1
            ConsentState.DENIED -> 2
        }
}
