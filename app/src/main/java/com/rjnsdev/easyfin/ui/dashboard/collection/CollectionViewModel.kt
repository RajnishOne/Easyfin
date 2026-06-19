package com.rjnsdev.easyfin.ui.dashboard.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rjnsdev.easyfin.data.remote.BaseItemDto
import com.rjnsdev.easyfin.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CollectionState {
    object Loading : CollectionState()
    data class Success(val serverUrl: String, val views: List<BaseItemDto>) : CollectionState()
    data class Error(val message: String) : CollectionState()
}

class CollectionViewModel(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollectionState>(CollectionState.Loading)
    val uiState: StateFlow<CollectionState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = CollectionState.Loading
            val viewsResult = mediaRepository.getUserViews()
            if (viewsResult.isSuccess) {
                val url = mediaRepository.getActiveServerUrl() ?: ""
                _uiState.value = CollectionState.Success(url, viewsResult.getOrNull() ?: emptyList())
            } else {
                _uiState.value = CollectionState.Error(viewsResult.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
