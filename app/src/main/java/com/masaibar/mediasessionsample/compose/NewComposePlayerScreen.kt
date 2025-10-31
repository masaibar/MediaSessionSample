package com.masaibar.mediasessionsample.compose

import android.content.ComponentName
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.concurrent.futures.SuspendToFutureAdapter
import androidx.concurrent.futures.await
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import com.google.common.util.concurrent.ListenableFuture
import com.masaibar.mediasessionsample.MediaControllerCommand
import com.masaibar.mediasessionsample.MediaSessionCommand
import com.masaibar.mediasessionsample.PlayerService
import com.masaibar.mediasessionsample.notify
import com.masaibar.mediasessionsample.ui.theme.MediaSessionSampleTheme

@Composable
fun NewComposePlayerScreen(
    viewModel: ComposePlayerViewModel,
    onVideoEnded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NewComposePlayerScreen(uiState, onVideoEnded)
}

@OptIn(UnstableApi::class)
@Composable
private fun NewComposePlayerScreen(
    uiState: UiState,
    onVideoEnded: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        var mediaController by remember {
            mutableStateOf<MediaController?>(null)
        }

        var aspectRatio by remember {
            mutableFloatStateOf(16f / 9f)
        }

        val playerListener = remember {
            object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    if (videoSize.width > 0 && videoSize.height > 0) {
                        aspectRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            if (mediaController == null) {
                val sessionToken =
                    SessionToken(context, ComponentName(context, PlayerService::class.java))
                val controller = MediaController.Builder(
                    context,
                    sessionToken
                ).setListener(
                    object : MediaController.Listener {
                        override fun onCustomCommand(
                            controller: MediaController,
                            command: SessionCommand,
                            args: Bundle
                        ): ListenableFuture<SessionResult> {
                            return SuspendToFutureAdapter.launchFuture {
                                when (command.customAction) {
                                    MediaSessionCommand.OnVideoEnded.action -> {
                                        onVideoEnded()
                                        SessionResult(SessionResult.RESULT_SUCCESS)
                                    }

                                    else -> SessionResult(SessionError.ERROR_NOT_SUPPORTED)
                                }
                            }
                        }
                    }
                ).buildAsync().await()

                controller.addListener(playerListener)
                mediaController = controller

                val hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                controller.notify(MediaControllerCommand.PlayHls(hlsUrl))
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                mediaController?.removeListener(playerListener)
                mediaController?.release()
                mediaController = null
            }
        }

        mediaController?.let { controller ->
            PlayerSurface(
                player = controller,
                surfaceType = SURFACE_TYPE_SURFACE_VIEW,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .align(Alignment.Center)
            )
        }
    }
}

@Preview
@Composable
private fun NewComposePlayerScreenPreview() {
    MediaSessionSampleTheme {
        NewComposePlayerScreen(
            uiState = UiState(),
            onVideoEnded = {}
        )
    }
}
