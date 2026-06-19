package com.rjnsdev.easyfin.ui.dashboard.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rjnsdev.easyfin.data.local.SecureStorage
import com.rjnsdev.easyfin.data.local.ServerProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val secureStorage: SecureStorage
) : ViewModel() {

    val serverProfiles: StateFlow<List<ServerProfile>> = secureStorage.serverProfiles
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeServerId: StateFlow<String?> = secureStorage.activeServerId
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun switchServer(serverId: String) {
        viewModelScope.launch {
            secureStorage.setActiveServerId(serverId)
        }
    }

    fun deleteServer(serverId: String) {
        viewModelScope.launch {
            secureStorage.deleteProfile(serverId)
        }
    }

    fun logoutAll() {
        viewModelScope.launch {
            secureStorage.clearAll()
        }
    }
}
