package com.rithwik.integrationworkbench.ui.actions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rithwik.integrationworkbench.plugins.AdFormat
import com.rithwik.integrationworkbench.plugins.PluginAction
import com.rithwik.integrationworkbench.plugins.PluginActionResult

@Composable
fun ActionsScreen(
    paddingValues: PaddingValues,
    viewModel: ActionsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Select Plugin", style = MaterialTheme.typography.titleMedium)
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.availableNetworks) { network ->
                    FilterChip(
                        selected = state.selectedNetwork == network,
                        onClick = { viewModel.selectNetwork(network) },
                        label = { Text(network.displayName) }
                    )
                }
            }
        }

        if (state.selectedNetwork != null) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Actions", style = MaterialTheme.typography.titleMedium)
            }

            item {
                ActionsGrid(
                    actions = state.availableActions,
                    onExecute = viewModel::executeAction,
                    isExecuting = state.isExecuting,
                    pluginState = state.pluginState
                )
            }

            // Banner placeholder
            if (state.bannerVisible) {
                item {
                    BannerPlaceholder(onClose = viewModel::hideBanner)
                }
            }

            // Last result
            state.lastResult?.let { result ->
                item {
                    ResultCard(result)
                }
            }

            // Plugin state
            state.pluginState?.let { pluginState ->
                item {
                    PluginStateCard(pluginState)
                }
            }
        }
    }
}

@Composable
private fun ActionsGrid(
    actions: List<com.rithwik.integrationworkbench.plugins.PluginActionDescriptor>,
    onExecute: (PluginAction) -> Unit,
    isExecuting: Boolean,
    pluginState: com.rithwik.integrationworkbench.plugins.PluginState?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            actions.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { descriptor ->
                        val enabled = !isExecuting && canExecute(descriptor, pluginState)
                        Button(
                            onClick = { onExecute(descriptor.action) },
                            enabled = enabled,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isExecuting) {
                                CircularProgressIndicator(modifier = Modifier.height(16.dp))
                            } else {
                                Text(descriptor.label)
                            }
                        }
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun canExecute(
    descriptor: com.rithwik.integrationworkbench.plugins.PluginActionDescriptor,
    state: com.rithwik.integrationworkbench.plugins.PluginState?
): Boolean {
    if (state == null) return !descriptor.requiresInit
    if (descriptor.requiresInit && !state.isInitialized) return false
    if (descriptor.requiresLoad) {
        val format = descriptor.action.format ?: return true
        val adState = state.adStates[format]
        return adState == com.rithwik.integrationworkbench.plugins.AdState.Loaded
    }
    return true
}

@Composable
private fun BannerPlaceholder(onClose: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Mock Banner Ad", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = onClose) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: PluginActionResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Last Result", style = MaterialTheme.typography.titleSmall)
            when (result) {
                is PluginActionResult.Success -> {
                    Text("Success", color = MaterialTheme.colorScheme.primary)
                    Text("Latency: ${result.latencyMs}ms")
                    result.networkName?.let { Text("Network: $it") }
                }
                is PluginActionResult.Failure -> {
                    Text("Failure", color = MaterialTheme.colorScheme.error)
                    Text("Error: ${result.errorCode} - ${result.errorMessage}")
                }
                is PluginActionResult.NotImplemented -> {
                    Text("Not Implemented", color = Color.Gray)
                }
                is PluginActionResult.Cancelled -> {
                    Text("Cancelled", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun PluginStateCard(state: com.rithwik.integrationworkbench.plugins.PluginState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Plugin State", style = MaterialTheme.typography.titleSmall)
            Text("Initialized: ${state.isInitialized}")
            Text("Initializing: ${state.isInitializing}")
            state.initError?.let { Text("Init Error: $it", color = MaterialTheme.colorScheme.error) }
            if (state.adStates.isNotEmpty()) {
                Text("Ad States:")
                state.adStates.forEach { (format, adState) ->
                    Text("  $format: $adState")
                }
            }
        }
    }
}
