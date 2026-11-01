package com.nothing.musicwidget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

object VisualizerGenerator {

    fun createVisualizerBitmap(
        levels: FloatArray,
        isDarkCover: Boolean = true,
        widthPx: Int = 180,
        heightPx: Int = 140
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val dotRadius = 9f
        val dotDiameter = dotRadius * 2
        val dotGapY = 5f
        val colGapX = 14f

        val totalColWidth = 4 * dotDiameter + 3 * colGapX
        val startX = (widthPx - totalColWidth) / 2f + dotRadius
        val baseColor = if (isDarkCover) Color.WHITE else Color.BLACK

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        for (col in 0 until 4) {
            val colHeight = if (col < levels.size) levels[col] else 1f
            val cx = startX + col * (dotDiameter + colGapX)

            for (dot in 1..5) {
                val cy = heightPx - (dotRadius + (dot - 1) * (dotDiameter + dotGapY))

                val diff = colHeight - (dot - 1)
                val alpha = when {
                    diff >= 1f -> 255
                    diff > 0.06f && dot > 1 -> {
                        val factor = ((diff - 0.06f) / 0.94f).coerceIn(0f, 1f)
                        (factor * 230 + 25).toInt()
                    }
                    dot == 1 -> if (colHeight > 0.5f) 255 else 128
                    else -> 0
                }

                if (alpha > 0) {
                    paint.color = baseColor
                    paint.alpha = alpha
                    canvas.drawCircle(cx, cy, dotRadius, paint)
                }
            }
        }

        return bitmap
    }

    fun createIdleDotNoteBitmap(
        sizePx: Int = 200,
        isLight: Boolean = false
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isLight) Color.parseColor("#121212") else Color.WHITE
        }

        val r = sizePx / 36f
        val grid = arrayOf(
            Pair(6, 4), Pair(7, 4), Pair(8, 4), Pair(9, 4),
            Pair(9, 5), Pair(9, 6), Pair(9, 7), Pair(9, 8), Pair(9, 9),
            Pair(8, 9), Pair(7, 9), Pair(6, 10), Pair(5, 11), Pair(6, 11), Pair(7, 11),
            Pair(6, 12), Pair(7, 12)
        )

        val scale = sizePx / 16f
        for (pt in grid) {
            val cx = pt.first * scale
            val cy = pt.second * scale
            canvas.drawCircle(cx, cy, r, paint)
        }

        return bitmap
    }
}
