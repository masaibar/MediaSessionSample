package com.masaibar.mediasessionsample

import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Ref: https://developer.android.com/media/media3/session/background-playback?hl=ja
 */
class PlayerService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // TODO: Playerの細かな状態でハンドリングする
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
    }
}
