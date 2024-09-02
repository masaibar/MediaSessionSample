package com.masaibar.mediasessionsample.compose

import android.content.ComponentName
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.masaibar.mediasessionsample.PlayerService
import com.masaibar.mediasessionsample.ui.theme.MediaSessionSampleTheme

@Composable
fun ComposePlayerScreen(viewModel: ComposePlayerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ComposePlayerScreen(uiState)
}

@Composable
private fun ComposePlayerScreen(uiState: UiState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        VideoPlayer(
            mediaItem = uiState.mediaItem
        )
    }
}

@Composable
fun VideoPlayer(
    mediaItem: MediaItem?
) {
    val context = LocalContext.current

    val playerView = remember {
        PlayerView(context).apply {
            useController = true
        }
    }

    var mediaController by remember {
        mutableStateOf<MediaController?>(null)
    }

    LaunchedEffect(mediaItem) {
        if (mediaItem != null) {
            if (mediaController == null) {
                val sessionToken =
                    SessionToken(context, ComponentName(context, PlayerService::class.java))
                mediaController = MediaController.Builder(context, sessionToken)
                    .buildAsync()
                    .await()

                mediaController?.addListener(object : Player.Listener {
                    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        // do nothing
                    }

                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        // do nothing
                    }
                })

                playerView.player = mediaController
                mediaController?.playWhenReady = true
                mediaController?.prepare()
            }
            if (mediaController?.currentMediaItem != mediaItem) {
                mediaController?.setMediaItem(mediaItem)
            }
        }
    }

    DisposableEffect(
        AndroidView(
            factory = { playerView },
            modifier = Modifier.background(Color.Black)
        )
    ) {
        onDispose {
            mediaController?.release()
        }
    }
}

@Preview
@Composable
private fun ComposePlayerScreenPreview() {
    MediaSessionSampleTheme {
        ComposePlayerScreen(uiState = UiState())
    }
}
