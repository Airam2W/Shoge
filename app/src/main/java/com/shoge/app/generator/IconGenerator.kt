package com.shoge.app.generator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface

object IconGenerator {

    private const val ICON_SIZE = 512   // px — high-res for all densities

    // ── Color + letter ────────────────────────────────────────────────────────

    /**
     * Draws a solid-color square with a white bold letter centred on it.
     * The system will mask this into the device's icon shape (circle, squircle…).
     *
     * @param color   Background fill color (e.g. 0xFFFF6B00)
     * @param initial The letter to show (typically the first char of the shortcut name)
     */
    fun fromColor(color: Int, initial: String): Bitmap {
        val bmp = Bitmap.createBitmap(ICON_SIZE, ICON_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        // 1. Solid background
        canvas.drawColor(color)

        // 2. White letter, centred in the safe zone
        //    The adaptive icon safe zone is 66 % of the total canvas, so we
        //    scale the text to fit within that region comfortably.
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color      = Color.WHITE
            textSize        = ICON_SIZE * 0.40f     // 40 % of canvas height
            textAlign       = Paint.Align.CENTER
            typeface        = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val bounds = Rect()
        textPaint.getTextBounds(initial, 0, initial.length, bounds)

        // drawText y-position: centre of canvas, adjusted for text bounds
        val yPos = ICON_SIZE / 2f - bounds.exactCenterY()
        canvas.drawText(initial, ICON_SIZE / 2f, yPos, textPaint)

        return bmp
    }

    // ── User-picked image ─────────────────────────────────────────────────────

    /**
     * Crops the source bitmap to a centred square and scales it to [ICON_SIZE].
     * No text overlay — the image is used as-is.
     */
    fun fromBitmap(source: Bitmap): Bitmap {
        // Centre-crop to square
        val dim = minOf(source.width, source.height)
        val x   = (source.width  - dim) / 2
        val y   = (source.height - dim) / 2
        val cropped = Bitmap.createBitmap(source, x, y, dim, dim)

        return if (cropped.width == ICON_SIZE && cropped.height == ICON_SIZE) {
            cropped
        } else {
            Bitmap.createScaledBitmap(cropped, ICON_SIZE, ICON_SIZE, true)
        }
    }
}