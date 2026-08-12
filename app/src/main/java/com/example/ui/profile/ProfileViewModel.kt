package com.example.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.UserSettingsEntity
import com.example.data.repository.SettingsRepository
import com.example.domain.model.ThemeOption
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val settings: UserSettingsEntity = UserSettingsEntity(id = "settings", userId = "default_user"),
    val exportSuccessMessage: String? = null
)

class ProfileViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = settingsRepository.getUserSettings().map { settings ->
        ProfileUiState(settings = settings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun setTheme(theme: ThemeOption) {
        viewModelScope.launch {
            val current = uiState.value.settings
            settingsRepository.saveUserSettings(current.copy(theme = theme))
        }
    }

    fun setWorkingHours(start: String, end: String) {
        viewModelScope.launch {
            val current = uiState.value.settings
            settingsRepository.saveUserSettings(current.copy(workStart = start, workEnd = end))
        }
    }

    class Factory(private val container: com.example.di.AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(container.settingsRepository) as T
        }
    }
}
