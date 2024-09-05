package com.masaibar.mediasessionsample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.concurrent.futures.await
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.masaibar.mediasessionsample.databinding.ActivityPlayerBinding
import kotlinx.coroutines.launch

class BackgroundPlayerActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, BackgroundPlayerActivity::class.java)
    }

    private val binding: ActivityPlayerBinding by lazy {
        ActivityPlayerBinding.inflate(layoutInflater)
    }

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
            val mediaController = MediaController.Builder(
                this@BackgroundPlayerActivity,
                sessionToken
            ).buildAsync().await()
            binding.playerView.player = mediaController

            val hlsUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
            mediaController.prepareVideo(hlsUrl)
        }
    }

    override fun onStop() {
        binding.playerView.player = null
        super.onStop()
    }
}

