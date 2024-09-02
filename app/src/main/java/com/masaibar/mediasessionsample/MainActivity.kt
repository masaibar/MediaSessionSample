package com.masaibar.mediasessionsample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import com.masaibar.mediasessionsample.compose.BackgroundComposePlayerActivity
import com.masaibar.mediasessionsample.ui.theme.MediaSessionSampleTheme

class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediaSessionSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                BackgroundPlayerActivity.createIntent(this@MainActivity).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }.let {
                                    startActivity(it)
                                }
                            }
                        ) {
                            Text("Open Background Player")
                        }
                        Button(
                            onClick = {
                                BackgroundComposePlayerActivity.createIntent(this@MainActivity)
                                    .apply {
                                        flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    }.let {
                                    startActivity(it)
                                }
                            }
                        ) {
                            Text("Open Background Compose Player")
                        }
                        Button(
                            onClick = {
                                PlayerActivity.createIntent(this@MainActivity).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }.let {
                                    startActivity(it)
                                }
                            }
                        ) {
                            Text("Open Player")
                        }
                    }
                }
            }
        }
    }
}
