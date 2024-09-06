package com.masaibar.mediasessionsample

import android.content.Context
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.concurrent.futures.await
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult

sealed interface MediaSessionCommand {
    val action: String
    val bundle: Bundle

    data object OnVideoEnded : MediaSessionCommand {
        override val action: String = "on_video_ended"
        override val bundle: Bundle = Bundle.EMPTY
    }
}

@OptIn(UnstableApi::class)
suspend fun MediaSession.notify(
    context: Context,
    command: MediaSessionCommand
): SessionResult {
    val myPackageName = context.packageName
    val controller = connectedControllers.firstOrNull {
        it.packageName == myPackageName
    } ?: return SessionResult(SessionError.ERROR_SESSION_DISCONNECTED)

    return this.sendCustomCommand(
        controller,
        SessionCommand(
            command.action,
            command.bundle
        ),
        Bundle.EMPTY
    ).await()
}
