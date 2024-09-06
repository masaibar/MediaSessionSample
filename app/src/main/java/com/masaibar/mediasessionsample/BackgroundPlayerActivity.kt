package com.masaibar.mediasessionsample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.concurrent.futures.SuspendToFutureAdapter
import androidx.concurrent.futures.await
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.masaibar.mediasessionsample.databinding.ActivityPlayerBinding
import kotlinx.coroutines.launch

class BackgroundPlayerActivity : AppCompatActivity(), MediaController.Listener {

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, BackgroundPlayerActivity::class.java)
    }

    private val binding: ActivityPlayerBinding by lazy {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

    private lateinit var mediaController: MediaController

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
        lifecycleScope.launch {
            mediaController = MediaController.Builder(
                this@BackgroundPlayerActivity,
                sessionToken
            ).setListener(
                this@BackgroundPlayerActivity
            ).buildAsync().await()
            mediaController.notify(
                MediaControllerCommand.EnteredInForeground
            )
            binding.playerView.player = mediaController

            val hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
            mediaController.notify(
                MediaControllerCommand.PlayHls(hlsUrl)
            )
        }
    }

    override fun onStop() {
        lifecycleScope.launch {
            mediaController.notify(
                MediaControllerCommand.EnteredInBackground
            )
        }
        binding.playerView.player = null
        super.onStop()
    }
}

