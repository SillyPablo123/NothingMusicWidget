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

        var currentLayer = 2

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
                        val idleBitmap = VisualizerGenerator.createIdleDotNoteBitmap(240, false)
                        views.setImageViewBitmap(R.id.iv_idle_note, idleBitmap)
                        views.setOnClickPendingIntent(R.id.widget_container_layer1, getPendingSelfIntent(context, ACTION_TOGGLE_LAYER))
                    }
                    2 -> {
                        if (MusicNotificationListener.currentAlbumArt != null) {
                            views.setImageViewBitmap(R.id.iv_cover_art, MusicNotificationListener.currentAlbumArt)
                        } else {
                            views.setImageViewResource(R.id.iv_cover_art, R.drawable.ic_default_cover)
                        }

                        val levels = if (isPlaying) {
                            floatArrayOf(
                                Random.nextFloat() * 3.2f + 1.6f,
                                Random.nextFloat() * 3.8f + 1.2f,
                                Random.nextFloat() * 3.4f + 1.4f,
                                Random.nextFloat() * 3.0f + 1.2f
                            )
                        } else {
                            floatArrayOf(1f, 1f, 1f, 1f)
                        }
                        val vizBitmap = VisualizerGenerator.createVisualizerBitmap(levels, true, 200, 160)
                        views.setImageViewBitmap(R.id.iv_dot_visualizer, vizBitmap)

                        views.setOnClickPendingIntent(R.id.widget_container_layer2, getPendingSelfIntent(context, ACTION_TOGGLE_LAYER))
                    }
                    3 -> {
                        views.setTextViewText(R.id.tv_track_title, MusicNotificationListener.currentTitle)
                        views.setTextViewText(R.id.tv_track_artist, MusicNotificationListener.currentArtist)

                        if (MusicNotificationListener.currentAlbumArt != null) {
                            views.setImageViewBitmap(R.id.iv_details_art, MusicNotificationListener.currentAlbumArt)
                        } else {
                            views.setImageViewResource(R.id.iv_details_art, R.drawable.ic_default_cover)
                        }

                        views.setImageViewResource(
                            R.id.btn_widget_play_pause,
                            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        )

                        val duration = MusicNotificationListener.currentDurationMs
                        val pos = MusicNotificationListener.currentPositionMs
                        val progress = if (duration > 0) ((pos.toFloat() / duration) * 100).toInt() else 0
                        views.setProgressBar(R.id.pb_timeline, 100, progress, false)

                        views.setOnClickPendingIntent(R.id.btn_widget_play_pause, getPendingSelfIntent(context, ACTION_PLAY_PAUSE))
                        views.setOnClickPendingIntent(R.id.btn_widget_next, getPendingSelfIntent(context, ACTION_NEXT))
                        views.setOnClickPendingIntent(R.id.btn_widget_prev, getPendingSelfIntent(context, ACTION_PREV))
                        views.setOnClickPendingIntent(R.id.iv_details_art, getPendingSelfIntent(context, ACTION_TOGGLE_LAYER))
                    }
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    val fallbackViews = RemoteViews(context.packageName, R.layout.widget_nothing_music_layer1)
                    val idleBitmap = VisualizerGenerator.createIdleDotNoteBitmap(240, false)
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
            ACTION_TOGGLE_LAYER -> {
                currentLayer = if (currentLayer == 2) 3 else 2
                updateAllWidgets(context)
            }
        }
    }
}
