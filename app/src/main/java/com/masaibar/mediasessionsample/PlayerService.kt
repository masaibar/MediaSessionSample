package com.masaibar.mediasessionsample

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.masaibar.mediasessionsample.compose.BackgroundComposePlayerActivity

/**
 * Ref: https://developer.android.com/media/media3/session/background-playback?hl=ja
 */
@OptIn(UnstableApi::class)
class PlayerService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    private val mediaSessionCallback = object : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> =
            Futures.immediateFuture(mediaItems.map { mediaItem ->
                mediaItem.buildUpon()
                    .setUri(mediaItem.requestMetadata.mediaUri)
                    .build()
            }.toMutableList())
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).apply {
            // イヤホンが外れたら再生停止
            setHandleAudioBecomingNoisy(true)
        }.build()

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            BackgroundComposePlayerActivity.createIntent(this),
            FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(mediaSessionCallback)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        player.release()
        mediaSession.release()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Playerの細かな状態でハンドリングする
        stopSelf()
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaSession =
        mediaSession
}
