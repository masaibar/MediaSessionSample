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
//    private lateinit var controllerFuture: ListenableFuture<MediaController>

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

        }
//        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
//        controllerFuture.addListener(
//            {
//                if (controllerFuture.isDone) {
//                    val mediaController = controllerFuture.get()
//                    binding.playerView.player = mediaController
//                }
//            },
//            MoreExecutors.directExecutor()
//        )
    }

    override fun onStop() {
//        MediaController.releaseFuture(controllerFuture)
        binding.playerView.player = null
        super.onStop()
    }
}

