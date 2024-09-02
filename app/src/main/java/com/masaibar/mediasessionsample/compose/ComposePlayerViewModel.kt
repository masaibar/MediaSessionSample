package com.masaibar.mediasessionsample.compose

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UiState(
    val mediaItem: MediaItem? = null
)

class ComposePlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        val requestMetadata = MediaItem.RequestMetadata.Builder()
            .setMediaUri(hlsUrl.toUri())
            .build()
        val mediaItem = MediaItem.Builder().setRequestMetadata(requestMetadata).build()
        _uiState.update {
            it.copy(mediaItem = mediaItem)
        }
    }
}
