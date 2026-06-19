package com.rjnsdev.easyfin.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rjnsdev.easyfin.data.remote.AuthRequest
import com.rjnsdev.easyfin.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(serverUrl: String, username: String, pw: String, customHeader: String) {
        if (serverUrl.isBlank() || username.isBlank() || pw.isBlank()) {
            _authState.value = AuthState.Error("Please fill all required fields")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val request = AuthRequest(username, pw)
            val result = authRepository.authenticate(serverUrl, request, customHeader.takeIf { it.isNotBlank() })
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }
}
