package com.rjnsdev.easyfin.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rjnsdev.easyfin.data.local.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class PlayerConfig(
    val url: String,
    val accessToken: String,
    val customHeader: String
)

class MediaPlayerViewModel(
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _playerConfig = MutableStateFlow<PlayerConfig?>(null)
    val playerConfig: StateFlow<PlayerConfig?> = _playerConfig.asStateFlow()

    fun loadMedia(itemId: String) {
        viewModelScope.launch {
            val profile = secureStorage.activeProfile.firstOrNull()
            if (profile != null) {
                // Jellyfin generic stream endpoint
                val streamUrl = "${profile.url}/Videos/$itemId/stream?api_key=${profile.accessToken}&static=true"
                _playerConfig.value = PlayerConfig(
                    url = streamUrl,
                    accessToken = profile.accessToken,
                    customHeader = profile.customHeader
                )
            }
        }
    }
}
