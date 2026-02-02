package com.rithwik.integrationworkbench.ui.integrations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rithwik.integrationworkbench.domain.model.ConsentState
import com.rithwik.integrationworkbench.plugins.AdFormat

@Composable
fun IntegrationsScreen(
    paddingValues: PaddingValues,
    viewModel: IntegrationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Ad Network Plugins", style = MaterialTheme.typography.titleMedium)
        }

        items(state.plugins) { plugin ->
            PluginCard(
                plugin = plugin,
                isSelected = state.selectedPlugin == plugin.network,
                onClick = { viewModel.selectPlugin(plugin.network) }
            )
        }

        state.editingConfig?.let { config ->
            item {
                ConfigEditorCard(
                    config = config,
                    onConfigChange = viewModel::updateEditingConfig,
                    onSave = viewModel::savePluginConfig,
                    onCancel = viewModel::cancelEdit
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Harness Settings", style = MaterialTheme.typography.titleMedium)
        }

        item {
            HarnessSettingsCard(
                settings = state.harnessSettings,
                onSettingsChange = viewModel::updateHarnessSettings
            )
        }
    }
}

@Composable
private fun PluginCard(
    plugin: PluginUiState,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(plugin.displayName, style = MaterialTheme.typography.titleSmall)
            Text(
                "Formats: ${plugin.supportedFormats.joinToString { it.displayName }}",
                style = MaterialTheme.typography.bodySmall
            )
            if (plugin.config != null) {
                Text("Configured", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ConfigEditorCard(
    config: com.rithwik.integrationworkbench.plugins.PluginConfig,
    onConfigChange: (com.rithwik.integrationworkbench.plugins.PluginConfig) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Configure ${config.network.displayName}", style = MaterialTheme.typography.titleSmall)

            OutlinedTextField(
                value = config.credentials["appId"] ?: "",
                onValueChange = { onConfigChange(config.copy(credentials = config.credentials + ("appId" to it))) },
                label = { Text("App ID / SDK Key") },
                modifier = Modifier.fillMaxWidth()
            )

            AdFormat.values().filter { it in setOf(AdFormat.BANNER, AdFormat.INTERSTITIAL, AdFormat.REWARDED) }.forEach { format ->
                OutlinedTextField(
                    value = config.adUnitIds[format] ?: "",
                    onValueChange = { onConfigChange(config.copy(adUnitIds = config.adUnitIds + (format to it))) },
                    label = { Text("${format.displayName} Ad Unit ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSave) { Text("Save") }
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun HarnessSettingsCard(
    settings: com.rithwik.integrationworkbench.domain.model.HarnessSettings,
    onSettingsChange: (com.rithwik.integrationworkbench.domain.model.HarnessSettings) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ToggleRow(
                label = "Init Timeout Guard (5s)",
                checked = settings.initTimeoutEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(initTimeoutEnabled = it)) }
            )
            ToggleRow(
                label = "Init Retry (exponential backoff)",
                checked = settings.initRetryEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(initRetryEnabled = it)) }
            )
            ToggleRow(
                label = "Bad Config Injection",
                checked = settings.badConfigInjectionEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(badConfigInjectionEnabled = it)) }
            )
            ToggleRow(
                label = "Offline Guard",
                checked = settings.offlineGuardEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(offlineGuardEnabled = it)) }
            )
            ToggleRow(
                label = "Simulate Load Failure",
                checked = settings.simulateLoadFailure,
                onCheckedChange = { onSettingsChange(settings.copy(simulateLoadFailure = it)) }
            )
            ToggleRow(
                label = "Age Restricted User",
                checked = settings.isAgeRestrictedUser,
                onCheckedChange = { onSettingsChange(settings.copy(isAgeRestrictedUser = it)) }
            )

            Text("Consent State", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ConsentState.values().forEach { consent ->
                    OutlinedButton(
                        onClick = { onSettingsChange(settings.copy(consentState = consent)) }
                    ) {
                        Text(
                            consent.name,
                            color = if (settings.consentState == consent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
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
