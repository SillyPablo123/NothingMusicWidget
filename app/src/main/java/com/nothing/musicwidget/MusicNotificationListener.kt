package com.nothing.musicwidget

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MusicNotificationListener : NotificationListenerService() {

    companion object {
        var activeController: MediaController? = null
        var currentTitle: String = "Nada reproduciéndose"
        var currentArtist: String = "Inicia Spotify o tu reproductor"
        var currentAlbumArt: Bitmap? = null
        var isPlaying: Boolean = false
        var currentPositionMs: Long = 0
        var currentDurationMs: Long = 0

        fun playPause() {
            activeController?.let { controller ->
                val state = controller.playbackState?.state
                if (state == PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause()
                } else {
                    controller.transportControls.play()
                }
            }
        }

        fun next() {
            activeController?.transportControls?.skipToNext()
        }

        fun previous() {
            activeController?.transportControls?.skipToPrevious()
        }
    }

    private var sessionManager: MediaSessionManager? = null

    override fun onCreate() {
        super.onCreate()
        sessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        findActiveMediaSession()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        findActiveMediaSession()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        findActiveMediaSession()
    }

    private fun findActiveMediaSession() {
        try {
            val listenerComponent = ComponentName(this, MusicNotificationListener::class.java)
            val controllers = sessionManager?.getActiveSessions(listenerComponent)

            if (!controllers.isNullOrEmpty()) {
                val controller = controllers[0]
                activeController = controller
                registerControllerCallback(controller)
                updateMediaInfo(controller)
            }
        } catch (e: SecurityException) {
            // Permiso aún no concedido
        }
    }

    private fun registerControllerCallback(controller: MediaController) {
        controller.registerCallback(object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                updateMediaInfo(controller)
            }

            override fun onPlaybackStateChanged(state: PlaybackState?) {
                updateMediaInfo(controller)
            }
        })
    }

    private fun updateMediaInfo(controller: MediaController) {
        val metadata = controller.metadata
        val state = controller.playbackState

        currentTitle = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Canción desconocida"
        currentArtist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Artista desconocido"
        currentAlbumArt = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)

        isPlaying = state?.state == PlaybackState.STATE_PLAYING
        currentPositionMs = state?.position ?: 0
        currentDurationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0

        NothingMusicWidgetProvider.updateAllWidgets(this)
    }
}
