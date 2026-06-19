package com.rjnsdev.easyfin.ui.dashboard.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rjnsdev.easyfin.data.remote.BaseItemDto
import com.rjnsdev.easyfin.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ExploreState {
    object Loading : ExploreState()
    data class Success(
        val serverUrl: String,
        val latestMediaRows: List<MediaRow>
    ) : ExploreState()
    data class Error(val message: String) : ExploreState()
}

data class MediaRow(
    val title: String,
    val items: List<BaseItemDto>
)

class ExploreViewModel(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreState>(ExploreState.Loading)
    val uiState: StateFlow<ExploreState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = ExploreState.Loading
            val viewsResult = mediaRepository.getUserViews()
            if (viewsResult.isSuccess) {
                val views = viewsResult.getOrNull() ?: emptyList()
                val rows = mutableListOf<MediaRow>()
                
                // Fetch latest for each view
                for (view in views) {
                    val latestResult = mediaRepository.getLatestMedia(parentId = view.Id)
                    if (latestResult.isSuccess) {
                        val items = latestResult.getOrNull() ?: emptyList()
                        if (items.isNotEmpty()) {
                            rows.add(MediaRow(title = "Latest in ${view.Name ?: "Library"}", items = items))
                        }
                    }
                }
                val url = mediaRepository.getActiveServerUrl() ?: ""
                _uiState.value = ExploreState.Success(url, rows)
            } else {
                _uiState.value = ExploreState.Error(viewsResult.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
