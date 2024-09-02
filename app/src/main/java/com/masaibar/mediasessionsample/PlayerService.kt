package com.masaibar.mediasessionsample

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import com.masaibar.mediasessionsample.compose.BackgroundComposePlayerActivity

/**
 * Ref: https://developer.android.com/media/media3/session/background-playback?hl=ja
 */
@OptIn(UnstableApi::class)
class PlayerService : MediaSessionService() {

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this).apply {
            // イヤホンが外れたら再生停止
            setHandleAudioBecomingNoisy(true)
        }.build()
    }

    private val pendingIntent: PendingIntent by lazy {
        PendingIntent.getActivity(
            this,
            0,
            BackgroundComposePlayerActivity.createIntent(this),
            FLAG_IMMUTABLE
        )
    }

    private val mediaSession by lazy {
        MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        playStart()
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

    private fun playStart() {
        val hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        val mediaItem = MediaItem.Builder()
            .setUri(hlsUrl)
            .build()
        val factory = DefaultHttpDataSource.Factory().also {
            HlsMediaSource.Factory(it)
        }
        val mediaSource = DefaultMediaSourceFactory(factory).createMediaSource(mediaItem)

        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()
    }
}
