package com.maxlab.ui.issue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxlab.domain.model.ConsentState

@Composable
fun IssueReproScreen(
    paddingValues: PaddingValues,
    viewModel: IssueReproViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Issue Repro Harness", style = MaterialTheme.typography.titleMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ToggleRow(
                    label = "Init Timeout Guard (5s timeout + retries)",
                    checked = settings.initTimeoutGuardEnabled,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(initTimeoutGuardEnabled = it)) }
                )
                ToggleRow(
                    label = "Bad Banner Ad Unit ID",
                    checked = settings.badBannerAdUnitId,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(badBannerAdUnitId = it)) }
                )
                ToggleRow(
                    label = "Bad Interstitial Ad Unit ID",
                    checked = settings.badInterstitialAdUnitId,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(badInterstitialAdUnitId = it)) }
                )
                ToggleRow(
                    label = "Bad Rewarded Ad Unit ID",
                    checked = settings.badRewardedAdUnitId,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(badRewardedAdUnitId = it)) }
                )
                ToggleRow(
                    label = "Offline Guard (block loads when offline)",
                    checked = settings.offlineGuardEnabled,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(offlineGuardEnabled = it)) }
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Privacy / Consent State (Mock)", style = MaterialTheme.typography.titleSmall)
                ConsentRow(
                    label = "UNKNOWN",
                    selected = settings.consentState == ConsentState.UNKNOWN,
                    onSelected = { viewModel.setConsentState(ConsentState.UNKNOWN) }
                )
                ConsentRow(
                    label = "GRANTED",
                    selected = settings.consentState == ConsentState.GRANTED,
                    onSelected = { viewModel.setConsentState(ConsentState.GRANTED) }
                )
                ConsentRow(
                    label = "DENIED",
                    selected = settings.consentState == ConsentState.DENIED,
                    onSelected = { viewModel.setConsentState(ConsentState.DENIED) }
                )
                ToggleRow(
                    label = "Age Restricted User",
                    checked = settings.isAgeRestrictedUser,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(isAgeRestrictedUser = it)) }
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ConsentRow(label: String, selected: Boolean, onSelected: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        RadioButton(selected = selected, onClick = onSelected)
        Text(label)
    }
}
