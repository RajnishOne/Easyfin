package com.rjnsdev.easyfin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rjnsdev.easyfin.data.local.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainViewModel(private val secureStorage: SecureStorage) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val activeId = secureStorage.activeServerId.firstOrNull()
            if (activeId != null) {
                _startDestination.value = "dashboard"
            } else {
                _startDestination.value = "auth"
            }
        }
    }
}
