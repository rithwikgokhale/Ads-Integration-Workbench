package com.maxlab.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxlab.ad.MaxSdkManager
import com.maxlab.domain.model.SecretsSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class SetupUiState(
    val needsSetup: Boolean = true,
    val setupDismissed: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sdkManager: MaxSdkManager
) : ViewModel() {
    private val dismissed = MutableStateFlow(false)

    val setupUiState: StateFlow<SetupUiState> = sdkManager.secretsStatus
        .combine(dismissed) { status, dismissed ->
            val needsSetup = status.source != SecretsSource.REAL ||
                status.config?.sdkKey == "PASTE_SDK_KEY_HERE"
            SetupUiState(needsSetup = needsSetup, setupDismissed = dismissed)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SetupUiState())

    fun dismissSetup() {
        dismissed.value = true
    }
}
