package com.nothing.musicwidget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF

object VisualizerGenerator {

    fun getRoundedSquircleBitmap(src: Bitmap?, cornerRadiusPercent: Float): Bitmap {
        if (src == null || src.isRecycled) {
            return createDefaultCoverBitmap(320, cornerRadiusPercent)
        }
        return try {
            val size = minOf(src.width, src.height)
            if (size <= 0) return createDefaultCoverBitmap(320, cornerRadiusPercent)
            val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
            val radiusPx = size * cornerRadiusPercent

            // Draw squircle mask
            canvas.drawRoundRect(rect, radiusPx, radiusPx, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

            // Center-crop source into square output
            val srcRect = if (src.width >= src.height) {
                val offset = (src.width - src.height) / 2
                Rect(offset, 0, offset + size, size)
            } else {
                val offset = (src.height - src.width) / 2
                Rect(0, offset, size, offset + size)
            }
            canvas.drawBitmap(src, srcRect, rect, paint)
            output
        } catch (e: Exception) {
            createDefaultCoverBitmap(320, cornerRadiusPercent)
        }
    }

    fun createDefaultCoverBitmap(sizePx: Int, cornerRadiusPercent: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1B1B1B")
            style = Paint.Style.FILL
        }
        val radius = sizePx * cornerRadiusPercent
        canvas.drawRoundRect(RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat()), radius, radius, paint)

        val noteBitmap = createIdleDotNoteBitmap(sizePx / 2, false)
        val offset = sizePx / 4f
        canvas.drawBitmap(noteBitmap, offset, offset, null)
        return bitmap
    }

    fun isBitmapDark(bitmap: Bitmap?): Boolean {
        if (bitmap == null) return true
        return try {
            val w = bitmap.width
            val h = bitmap.height
            var totalLuminance = 0.0
            var count = 0
            for (x in 1..4) {
                for (y in 1..4) {
                    val color = bitmap.getPixel((w * x) / 5, (h * y) / 5)
                    val r = Color.red(color)
                    val g = Color.green(color)
                    val b = Color.blue(color)
                    val lum = 0.299 * r + 0.587 * g + 0.114 * b
                    totalLuminance += lum
                    count++
                }
            }
            (totalLuminance / count) < 135
        } catch (e: Exception) {
            true
        }
    }

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
        sizePx: Int = 320,
        isLight: Boolean = false
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = if (isLight) Color.parseColor("#121212") else Color.WHITE
        }

        val cols = 8
        val rows = 8
        val padding = sizePx * 0.16f
        val usable = sizePx - (padding * 2f)
        val dotSpacing = usable / 8f
        val dotRadius = dotSpacing * 0.48f

        // Auténtico glifo Nothing OS de 34 puntos en matriz 8x8
        val activeDots = arrayOf(
            // Viga superior (3 filas de 6 puntos)
            Pair(2, 0), Pair(3, 0), Pair(4, 0), Pair(5, 0), Pair(6, 0), Pair(7, 0),
            Pair(2, 1), Pair(3, 1), Pair(4, 1), Pair(5, 1), Pair(6, 1), Pair(7, 1),
            Pair(2, 2), Pair(3, 2), Pair(4, 2), Pair(5, 2), Pair(6, 2), Pair(7, 2),

            // Tallo izquierdo (3 puntos)
            Pair(2, 3), Pair(2, 4), Pair(2, 5),

            // Cabeza izquierda (5 puntos)
            Pair(0, 6), Pair(1, 6), Pair(2, 6),
            Pair(0, 7), Pair(1, 7),

            // Tallo derecho (3 puntos)
            Pair(7, 3), Pair(7, 4), Pair(7, 5),

            // Cabeza derecha (5 puntos)
            Pair(5, 6), Pair(6, 6), Pair(7, 6),
            Pair(5, 7), Pair(6, 7)
        )

        for (pt in activeDots) {
            val cx = padding + pt.first * dotSpacing + dotSpacing / 2f
            val cy = padding + pt.second * dotSpacing + dotSpacing / 2f
            canvas.drawCircle(cx, cy, dotRadius, paint)
        }

        return bitmap
    }
}
