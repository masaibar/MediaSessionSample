package com.masaibar.mediasessionsample.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.masaibar.mediasessionsample.ui.theme.MediaSessionSampleTheme

class BackgroundComposePlayerActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, BackgroundComposePlayerActivity::class.java)
    }

    private val viewModel: ComposePlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MediaSessionSampleTheme {
                ComposePlayerScreen(
                    viewModel = viewModel,
                    onVideoEnded = {
                        finish()
                    }
                )
            }
        }
    }
}

