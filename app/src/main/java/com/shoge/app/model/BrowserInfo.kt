package com.shoge.app.model

import android.graphics.drawable.Drawable

data class BrowserInfo(
    val packageName: String,   // e.g. "com.android.chrome"
    val label: String,         // e.g. "Chrome"
    val icon: Drawable         // pulled from PackageManager at runtime
) {
    // AutoCompleteTextView calls toString() to display the selected item
    override fun toString(): String = label
}