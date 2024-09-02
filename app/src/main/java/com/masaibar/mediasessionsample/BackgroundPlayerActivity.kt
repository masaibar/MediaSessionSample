package com.masaibar.mediasessionsample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.masaibar.mediasessionsample.databinding.ActivityPlayerBinding

class BackgroundPlayerActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, BackgroundPlayerActivity::class.java)
    }

    private val binding: ActivityPlayerBinding by lazy {
        ActivityPlayerBinding.inflate(layoutInflater)
    }
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(
            this,
            ComponentName(this, PlayerService::class.java)
        )
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                if (controllerFuture.isDone) {
                    val mediaController = controllerFuture.get()
                    binding.playerView.player = mediaController
                    playStart(mediaController)
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun onStop() {
        MediaController.releaseFuture(controllerFuture)
        binding.playerView.player = null
        super.onStop()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun playStart(player: MediaController) {
        val hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        val requestMetadata = MediaItem.RequestMetadata.Builder()
            .setMediaUri(hlsUrl.toUri())
            .build()
        val mediaItem = MediaItem.Builder().setRequestMetadata(requestMetadata).build()

        player.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }
}

