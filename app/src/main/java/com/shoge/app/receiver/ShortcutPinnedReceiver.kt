package com.shoge.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Receives the callback from the launcher after the user confirms
 * "Add to Home Screen" in the system pin dialog.
 */
class ShortcutPinnedReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_SHORTCUT_NAME = "shoge_shortcut_name"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(EXTRA_SHORTCUT_NAME) ?: "Shortcut"
        Toast.makeText(
            context,
            "\"$name\" added to your home screen!",
            Toast.LENGTH_SHORT
        ).show()
    }
}