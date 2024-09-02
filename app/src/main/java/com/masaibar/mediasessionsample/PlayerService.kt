package com.masaibar.mediasessionsample

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * Ref: https://developer.android.com/media/media3/session/background-playback?hl=ja
 */
@OptIn(UnstableApi::class)
class PlayerService : MediaSessionService(), Player.Listener {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private val mediaSessionCallback = object : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
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

        player.addListener(this)
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(mediaSessionCallback).build()
    }

    override fun onDestroy() {
        player.apply {
            removeListener(this@PlayerService)
            release()
        }
        mediaSession.release()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Playerの細かな状態でハンドリングする
        stopSelf()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        mediaSession
}
