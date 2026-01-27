package com.maxlab.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maxlab.domain.model.AdState
import com.maxlab.domain.model.InitState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "SDK Mode: ${state.sdkMode}",
            style = MaterialTheme.typography.titleMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::initializeSdk) {
                Text("Initialize MAX")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Init state: ${initLabel(state.initState)}", style = MaterialTheme.typography.bodyMedium)
                Text("Last init error: ${state.lastInitError ?: "None"}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "Last event: ${state.lastEvent?.category ?: "None"} at ${state.lastEvent?.timestampMs?.let { it.toHumanTime() } ?: "--"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Banner ID: ${state.bannerAdUnitId ?: "--"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Interstitial ID: ${state.interstitialAdUnitId ?: "--"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Rewarded ID: ${state.rewardedAdUnitId ?: "--"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text("Issue repro: ${state.issueReproSummary}", style = MaterialTheme.typography.bodySmall)
            }
        }

        Text("Banner", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::loadBanner) { Text("Load Banner") }
            Button(onClick = viewModel::showBanner) { Text("Show Banner") }
        }
        BannerContainer(state.bannerState)

        Text("Interstitial", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::loadInterstitial) { Text("Load Interstitial") }
            Button(onClick = viewModel::showInterstitial) { Text("Show Interstitial") }
        }
        Text("State: ${state.interstitialState}", style = MaterialTheme.typography.bodySmall)

        Text("Rewarded", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::loadRewarded) { Text("Load Rewarded") }
            Button(onClick = viewModel::showRewarded) { Text("Show Rewarded") }
        }
        Text("State: ${state.rewardedState}", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BannerContainer(state: AdState) {
    val label = when (state) {
        AdState.Idle -> "Banner idle"
        AdState.Loading -> "Banner loading..."
        AdState.Loaded -> "Banner loaded (mock view)"
        AdState.Showing -> "Banner showing (mock view)"
        is AdState.Failed -> "Banner failed: ${state.errorMessage}"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFFE2E8F0))
                .padding(16.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun initLabel(state: InitState): String = when (state) {
    InitState.NotStarted -> "Not Started"
    InitState.Initializing -> "Initializing"
    InitState.Ready -> "Ready"
    is InitState.Failed -> "Failed"
}

private fun Long.toHumanTime(): String {
    val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    return formatter.format(Date(this))
}
