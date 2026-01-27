package com.maxlab.ui.issue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxlab.data.repository.SettingsRepository
import com.maxlab.domain.model.ConsentState
import com.maxlab.domain.model.IssueReproSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class IssueReproViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {
    val settings: StateFlow<IssueReproSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IssueReproSettings())

    fun updateSettings(update: IssueReproSettings) {
        viewModelScope.launch {
            repository.updateSettings(update)
        }
    }

    fun setConsentState(state: ConsentState) {
        updateSettings(settings.value.copy(consentState = state))
    }
}
