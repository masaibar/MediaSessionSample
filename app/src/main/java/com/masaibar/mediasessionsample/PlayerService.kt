package com.masaibar.mediasessionsample

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.concurrent.futures.await
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.MediaSession.ConnectionResult.AcceptedResultBuilder
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.masaibar.mediasessionsample.compose.BackgroundComposePlayerActivity
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Ref
 * - https://developer.android.com/media/media3/session/background-playback?hl=ja
 * - https://zenn.dev/tomoya0x00/articles/ec7a3b07ea7d0a
 */
@OptIn(UnstableApi::class)
class PlayerService : MediaSessionService(), LifecycleOwner, Player.Listener {

    companion object {
        const val CUSTOM_ACTION_PREPARE_VIDEO = "action_prepare_video"
        const val CUSTOM_EVENT_VIDEO_ENDED = "video_ended"
        const val KEY_HLS_URL = "hls_url"
    }

    private val dispatcher = ServiceLifecycleDispatcher(this)
    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this).apply {
            // イヤホンが外れたら再生停止
            setHandleAudioBecomingNoisy(true)
            // 末尾まで再生したらpauseする
            setPauseAtEndOfMediaItems(true)
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

    private val isConnected: AtomicBoolean = AtomicBoolean(false)
    private val mediaSessionCallback = object : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: ControllerInfo
        ): ConnectionResult {
            isConnected.set(true)
            val sessionCommands = ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(
                    SessionCommand(
                        CUSTOM_ACTION_PREPARE_VIDEO,
                        Bundle.EMPTY
                    )
                )
                .build()

            return AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onDisconnected(session: MediaSession, controller: ControllerInfo) {
            isConnected.set(false)
            super.onDisconnected(session, controller)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> =
            when (customCommand.customAction) {
                CUSTOM_ACTION_PREPARE_VIDEO -> {
                    customCommand.customExtras.getString(KEY_HLS_URL)?.let {
                        playStart(it)
                    }
                    Futures.immediateFuture(
                        SessionResult(SessionResult.RESULT_SUCCESS)
                    )
                }

                else -> super.onCustomCommand(session, controller, customCommand, args)
            }

    }

    private val mediaSession: MediaSession by lazy {
        MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setCallback(mediaSessionCallback)
            .build()
    }

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        player.addListener(this)
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dispatcher.onServicePreSuperOnStart()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        player.run {
            release()
            removeListener(this@PlayerService)
        }
        mediaSession.release()
        super.onDestroy()
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)

        when (Pair(playWhenReady, reason)) {
            Pair(false, Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) -> {
                lifecycleScope.launch {
                    // setPauseAtEndOfMediaItemsとの合わせ技でビデオ再生終了を検知して通知する
                    mediaSession.notifyVideoEnded(this@PlayerService)
                }
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Playerの細かな状態でハンドリングする
        stopSelf()
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaSession =
        mediaSession

    private fun playStart(hlsUrl: String) {
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

suspend fun MediaController.prepareVideo(hlsUrl: String) {
    sendCustomCommand(
        SessionCommand(
            PlayerService.CUSTOM_ACTION_PREPARE_VIDEO,
            Bundle().apply {
                putString(PlayerService.KEY_HLS_URL, hlsUrl)
            }
        ),
        Bundle.EMPTY
    ).await()
}

suspend fun MediaSession.notifyVideoEnded(
    context: Context
) {
    sendCustomCommand(
        context,
        SessionCommand(
            PlayerService.CUSTOM_EVENT_VIDEO_ENDED,
            Bundle.EMPTY
        )
    )
}

@OptIn(UnstableApi::class)
suspend fun MediaSession.sendCustomCommand(
    context: Context,
    command: SessionCommand
): SessionResult {
    val myPackageName = context.packageName
    val controller = connectedControllers.firstOrNull {
        it.packageName == myPackageName
    } ?: return SessionResult(SessionError.ERROR_SESSION_DISCONNECTED)

    return this.sendCustomCommand(
        controller,
        command,
        Bundle.EMPTY
    ).await()
}
