package com.masaibar.mediasessionsample.compose

import android.content.ComponentName
import android.os.Bundle
import androidx.annotation.OptIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.SuspendToFutureAdapter
import androidx.concurrent.futures.await
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import com.masaibar.mediasessionsample.MediaControllerCommand
import com.masaibar.mediasessionsample.MediaSessionCommand
import com.masaibar.mediasessionsample.PlayerService
import com.masaibar.mediasessionsample.notify
import com.masaibar.mediasessionsample.ui.theme.MediaSessionSampleTheme

@Composable
fun ComposePlayerScreen(
    viewModel: ComposePlayerViewModel,
    onVideoEnded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ComposePlayerScreen(uiState, onVideoEnded)
}

@OptIn(UnstableApi::class)
@Composable
private fun ComposePlayerScreen(
    uiState: UiState,
    onVideoEnded: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        val playerView = remember {
            PlayerView(context).apply {
                useController = true
            }
        }

        var mediaController by remember {
            mutableStateOf<MediaController?>(null)
        }

        LaunchedEffect(Unit) {
            if (mediaController == null) {
                val sessionToken =
                    SessionToken(context, ComponentName(context, PlayerService::class.java))
                mediaController = MediaController.Builder(
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
                playerView.player = mediaController
                val hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                mediaController?.notify(
                    MediaControllerCommand.PlayHls(hlsUrl)
                )
            }
        }

        DisposableEffect(
            AndroidView(
                factory = { playerView },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .align(Alignment.Center)
            )
        ) {
            onDispose {
                mediaController?.release()
            }
        }
    }
}

@Preview
@Composable
private fun ComposePlayerScreenPreview() {
    MediaSessionSampleTheme {
        ComposePlayerScreen(
            uiState = UiState(),
            onVideoEnded = {}
        )
    }
}
