package com.shoge.app.model

import android.graphics.Bitmap

// Immutable snapshot of what the user has filled in.
// We use a data class so copy() gives us easy partial updates.
data class FormState(
    val url: String = "",
    val shortcutName: String = "",
    val iconColor: Int = 0xFFFF6B00.toInt(),   // default: brand orange
    val iconBitmap: Bitmap? = null,
    val iconType: IconType = IconType.COLOR,
    val selectedBrowser: BrowserInfo? = null
)

enum class IconType { COLOR, IMAGE }