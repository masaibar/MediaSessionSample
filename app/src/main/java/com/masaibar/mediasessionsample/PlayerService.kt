package com.masaibar.mediasessionsample

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Ref: https://developer.android.com/media/media3/session/background-playback?hl=ja
 */
@OptIn(UnstableApi::class)
class PlayerService : MediaSessionService(), Player.Listener {
    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this).apply {
            // イヤホンが外れたら再生停止
            setHandleAudioBecomingNoisy(true)
        }.build()
    }
    private var mediaSession: MediaSession? = null

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onCreate() {
        super.onCreate()
        player.addListener(this)
        mediaSession = MediaSession.Builder(this, player).build()
        playStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.run {
            player.apply {
                removeListener(this@PlayerService)
                release()
            }
            release()
            mediaSession = null
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // TODO: Playerの細かな状態でハンドリングする
        stopSelf()
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
    }

    private fun playStart() {
        val hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        val mediaItem = MediaItem.Builder()
            .setUri(hlsUrl)
            .build()
        val factory = DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS)
            .setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS).also {
                HlsMediaSource.Factory(it)
            }
        val mediaSource = DefaultMediaSourceFactory(factory).createMediaSource(mediaItem)

        player.apply {
            setMediaSource(mediaSource)
            prepare()
            play()
        }
    }
}
