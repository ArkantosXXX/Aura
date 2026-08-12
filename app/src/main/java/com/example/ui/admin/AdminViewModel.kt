package com.example.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.AnnouncementEntity
import com.example.data.local.entities.AuditLogEntity
import com.example.data.local.entities.ErrorReportEntity
import com.example.data.local.entities.FeatureFlagEntity
import com.example.data.repository.AdminRepository
import com.example.domain.model.AnnouncementType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class AdminUiState(
    val featureFlags: List<FeatureFlagEntity> = emptyList(),
    val announcements: List<AnnouncementEntity> = emptyList(),
    val auditLogs: List<AuditLogEntity> = emptyList(),
    val errorReports: List<ErrorReportEntity> = emptyList(),
    val isEmergencyKillActive: Boolean = false,
    val isAdminAuthorized: Boolean = true
)

class AdminViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    val uiState: StateFlow<AdminUiState> = combine(
        adminRepository.featureFlags,
        adminRepository.announcements,
        adminRepository.auditLogs,
        adminRepository.errorReports
    ) { flags, announcements, logs, reports ->
        val killFlag = flags.find { it.key == "emergency_kill_switch" }?.value ?: false
        AdminUiState(
            featureFlags = flags,
            announcements = announcements,
            auditLogs = logs,
            errorReports = reports,
            isEmergencyKillActive = killFlag
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminUiState()
    )

    fun toggleFeatureFlag(key: String, enabled: Boolean) {
        viewModelScope.launch {
            adminRepository.setFeatureFlag(key, enabled)
            adminRepository.logAudit(
                adminId = "admin_super",
                action = "TOGGLE_FEATURE_FLAG",
                target = key,
                details = "Set $key to $enabled"
            )
        }
    }

    fun toggleEmergencyKillSwitch(active: Boolean) {
        viewModelScope.launch {
            adminRepository.setFeatureFlag("emergency_kill_switch", active)
            adminRepository.logAudit(
                adminId = "admin_super",
                action = "EMERGENCY_KILL_SWITCH",
                target = "SYSTEM",
                details = "Emergency kill switch set to $active"
            )
        }
    }

    fun addAnnouncement(title: String, message: String) {
        viewModelScope.launch {
            val announcement = AnnouncementEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                message = message,
                type = AnnouncementType.INFO,
                startDate = System.currentTimeMillis(),
                endDate = System.currentTimeMillis() + 7 * 24 * 3600 * 1000L
            )
            adminRepository.addAnnouncement(announcement)
            adminRepository.logAudit(
                adminId = "admin_super",
                action = "ADD_ANNOUNCEMENT",
                target = title,
                details = "Created announcement $title"
            )
        }
    }

    class Factory(private val container: com.example.di.AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminViewModel(container.adminRepository) as T
        }
    }
}
