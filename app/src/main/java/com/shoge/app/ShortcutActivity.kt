package com.shoge.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ShortcutActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL             = "shoge_extra_url"
        const val EXTRA_BROWSER_PACKAGE = "shoge_extra_browser_pkg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No setContentView — window is transparent; there's nothing to show.

        val url            = intent.getStringExtra(EXTRA_URL)
        val browserPackage = intent.getStringExtra(EXTRA_BROWSER_PACKAGE)

        if (url.isNullOrBlank()) {
            // Shortcut was created with a missing URL — nothing we can do
            Toast.makeText(this, "Invalid shortcut: no URL stored.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        openUrl(url, browserPackage)
        finish()    // close immediately so this never appears in recents
    }

    // ── URL launching ─────────────────────────────────────────────────────────

    private fun openUrl(url: String, browserPackage: String?) {
        val uri = Uri.parse(url)

        // First attempt: open in the specific browser the user chose
        if (!browserPackage.isNullOrBlank()) {
            val specificIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                `package` = browserPackage
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (tryStart(specificIntent)) return
        }

        // Second attempt: let the system pick any browser
        val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (tryStart(fallbackIntent)) return

        // Last resort: nothing could open the URL
        Toast.makeText(this, "No app found to open this link.", Toast.LENGTH_LONG).show()
    }

    /**
     * Attempts to start [intent].
     * Returns true on success, false if no matching activity was found.
     */
    private fun tryStart(intent: Intent): Boolean {
        return try {
            startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}