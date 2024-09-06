package com.masaibar.mediasessionsample

import android.os.Bundle
import androidx.concurrent.futures.await
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult

sealed interface MediaControllerCommand {
    val action: String
    val bundle: Bundle

    data class PlayHls(val url: String) : MediaControllerCommand {
        companion object {
            const val ACTION = "play_hls"
            const val KEY_URL = "url"
        }

        override val action: String = "play_hls"
        override val bundle: Bundle = Bundle().apply {
            putString(KEY_URL, url)
        }
    }

    data object EnteredInBackground : MediaControllerCommand {
        override val action: String = "entered_in_background"
        override val bundle: Bundle = Bundle.EMPTY
    }

    data object EnteredInForeground : MediaControllerCommand {
        override val action: String = "entered_in_foreground"
        override val bundle: Bundle = Bundle.EMPTY
    }
}

suspend fun MediaController.notify(command: MediaControllerCommand): SessionResult =
    sendCustomCommand(
        SessionCommand(
            command.action,
            command.bundle
        ),
        Bundle.EMPTY
    ).await()
