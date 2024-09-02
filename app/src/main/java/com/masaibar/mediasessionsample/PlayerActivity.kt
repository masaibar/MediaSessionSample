package com.masaibar.mediasessionsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.masaibar.mediasessionsample.databinding.ActivityPlayerBinding

@UnstableApi
class PlayerActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, PlayerActivity::class.java)
    }

    private val binding: ActivityPlayerBinding by lazy {
        ActivityPlayerBinding.inflate(layoutInflater)
    }
    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this).apply {
            // イヤホンが外れたら再生停止
            setHandleAudioBecomingNoisy(true)
        }.build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.playerView.player = player

        playStart()
    }

    override fun onDestroy() {
        player.release()
        binding.playerView.player = null
        super.onDestroy()
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
