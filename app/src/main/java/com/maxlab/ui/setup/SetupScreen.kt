package com.maxlab.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SetupScreen(
    onContinue: () -> Unit,
    padding: PaddingValues = PaddingValues(16.dp)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Setup required",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Copy app/src/main/assets/secrets.template.json to secrets.json and paste your AppLovin MAX SDK keys. " +
                "The app will run in MOCK mode until secrets.json exists.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = onContinue) {
            Text("Continue in Mock Mode")
        }
    }
}
