package com.nothing.musicwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlin.random.Random

class NothingMusicWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_PLAY_PAUSE = "com.nothing.musicwidget.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.nothing.musicwidget.ACTION_NEXT"
        const val ACTION_PREV = "com.nothing.musicwidget.ACTION_PREV"
        const val ACTION_TOGGLE_LAYER = "com.nothing.musicwidget.ACTION_TOGGLE_LAYER"
        const val ACTION_GO_LAYER_2 = "com.nothing.musicwidget.ACTION_GO_LAYER_2"

        var currentLayer = 1

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, NothingMusicWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            try {
                val isPlaying = MusicNotificationListener.isPlaying

                val layoutId = when (currentLayer) {
                    1 -> R.layout.widget_nothing_music_layer1
                    3 -> R.layout.widget_nothing_music_layer3
                    else -> R.layout.widget_nothing_music_layer2
                }

                val views = RemoteViews(context.packageName, layoutId)

                when (currentLayer) {
                    1 -> {
                        val idleBitmap = VisualizerGenerator.createIdleDotNoteBitmap(320, false)
                        views.setImageViewBitmap(R.id.iv_idle_note, idleBitmap)
                        views.setOnClickPendingIntent(R.id.widget_container_layer1, getPendingSelfIntent(context, ACTION_TOGGLE_LAYER))
                    }
                    2 -> {
                        val rawArt = MusicNotificationListener.currentAlbumArt
                        val coverBitmap = VisualizerGenerator.getRoundedSquircleBitmap(rawArt, 0.15f)
                        views.setImageViewBitmap(R.id.iv_cover_art, coverBitmap)

                        val isDark = VisualizerGenerator.isBitmapDark(rawArt)
                        val frameIds = intArrayOf(
                            R.id.iv_viz_frame1,
                            R.id.iv_viz_frame2,
                            R.id.iv_viz_frame3,
                            R.id.iv_viz_frame4,
                            R.id.iv_viz_frame5,
                            R.id.iv_viz_frame6
                        )

                        if (isPlaying) {
                            views.setInt(R.id.vf_dot_visualizer, "setFlipInterval", 120)
                            val frameLevels = arrayOf(
                                floatArrayOf(1.2f, 2.8f, 3.8f, 2.0f),
                                floatArrayOf(2.5f, 3.9f, 2.4f, 1.4f),
                                floatArrayOf(3.6f, 2.1f, 1.8f, 3.2f),
                                floatArrayOf(2.1f, 3.4f, 3.9f, 2.8f),
                                floatArrayOf(3.8f, 1.9f, 2.6f, 3.7f),
                                floatArrayOf(1.8f, 3.6f, 2.2f, 1.6f)
                            )
                            for (i in 0 until 6) {
                                val vizBmp = VisualizerGenerator.createVisualizerBitmap(frameLevels[i], isDark, 180, 140)
                                views.setImageViewBitmap(frameIds[i], vizBmp)
                            }
                        } else {
                            val idleLevels = floatArrayOf(1f, 1f, 1f, 1f)
                            val idleBmp = VisualizerGenerator.createVisualizerBitmap(idleLevels, isDark, 180, 140)
                            for (id in frameIds) {
                                views.setImageViewBitmap(id, idleBmp)
                            }
                        }

                        views.setOnClickPendingIntent(R.id.widget_container_layer2, getPendingSelfIntent(context, ACTION_TOGGLE_LAYER))
                    }
                    3 -> {
                        views.setTextViewText(R.id.tv_track_title, MusicNotificationListener.currentTitle)
                        views.setTextViewText(R.id.tv_track_artist, MusicNotificationListener.currentArtist)

                        val rawArt = MusicNotificationListener.currentAlbumArt
                        val detailCoverBitmap = VisualizerGenerator.getRoundedSquircleBitmap(rawArt, 0.11f)
                        views.setImageViewBitmap(R.id.iv_details_art, detailCoverBitmap)

                        val serviceIcon = when (MusicNotificationListener.currentService) {
                            "spotify" -> R.drawable.ic_service_spotify
                            "ytmusic" -> R.drawable.ic_service_ytmusic
                            else -> R.drawable.ic_service_music
                        }
                        views.setImageViewResource(R.id.iv_player_service, serviceIcon)

                        val duration = MusicNotificationListener.currentDurationMs
                        val pos = MusicNotificationListener.currentPositionMs
                        val progress = if (duration > 0) ((pos.toFloat() / duration) * 100).toInt().coerceIn(0, 100) else 0
                        views.setProgressBar(R.id.pb_timeline, 100, progress, false)

                        views.setOnClickPendingIntent(R.id.iv_details_art, getPendingSelfIntent(context, ACTION_GO_LAYER_2))
                        views.setOnClickPendingIntent(R.id.iv_player_service, getPendingSelfIntent(context, ACTION_PLAY_PAUSE))
                        views.setOnClickPendingIntent(R.id.widget_container_layer3, getPendingSelfIntent(context, ACTION_TOGGLE_LAYER))
                    }
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    val fallbackViews = RemoteViews(context.packageName, R.layout.widget_nothing_music_layer1)
                    val idleBitmap = VisualizerGenerator.createIdleDotNoteBitmap(320, false)
                    fallbackViews.setImageViewBitmap(R.id.iv_idle_note, idleBitmap)
                    fallbackViews.setOnClickPendingIntent(R.id.widget_container_layer1, getPendingSelfIntent(context, ACTION_TOGGLE_LAYER))
                    appWidgetManager.updateAppWidget(appWidgetId, fallbackViews)
                } catch (ignored: Exception) {}
            }
        }

        private fun getPendingSelfIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, NothingMusicWidgetProvider::class.java).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context,
                action.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_PLAY_PAUSE -> MusicNotificationListener.playPause()
            ACTION_NEXT -> MusicNotificationListener.next()
            ACTION_PREV -> MusicNotificationListener.previous()
            ACTION_GO_LAYER_2 -> {
                currentLayer = 2
                updateAllWidgets(context)
            }
            ACTION_TOGGLE_LAYER -> {
                currentLayer = when (currentLayer) {
                    1 -> {
                        // Tocar en Idle despierta el widget, inicia reproducción y muestra Capa 2
                        MusicNotificationListener.playPause()
                        2
                    }
                    2 -> 3
                    3 -> 1
                    else -> 1
                }
                updateAllWidgets(context)
            }
        }
    }
}
