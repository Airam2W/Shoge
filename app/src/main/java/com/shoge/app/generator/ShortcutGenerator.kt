package com.shoge.app.generator

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.shoge.app.ShortcutActivity
import com.shoge.app.model.FormState
import com.shoge.app.model.IconType
import com.shoge.app.receiver.ShortcutPinnedReceiver

object ShortcutGenerator {

    /**
     * Builds a [ShortcutInfoCompat] from [state] and asks the launcher
     * to pin it on the home screen.
     *
     * Returns a [GeneratorResult] describing what happened so the caller
     * can show appropriate UI feedback.
     */
    fun generate(context: Context, state: FormState): GeneratorResult {

        // 1. Guard — not all launchers support pinned shortcuts
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return GeneratorResult.PinningNotSupported
        }

        return try {

            // 2. Build the icon bitmap
            val initial = state.shortcutName
                .firstOrNull()?.uppercaseChar()?.toString() ?: "S"

            val iconBitmap = when (state.iconType) {
                IconType.COLOR -> IconGenerator.fromColor(state.iconColor, initial)
                IconType.IMAGE -> state.iconBitmap
                    ?.let { IconGenerator.fromBitmap(it) }
                    ?: IconGenerator.fromColor(state.iconColor, initial) // fallback
            }

            // 3. Wrap as an adaptive IconCompat
            //    createWithAdaptiveBitmap → launcher applies its own shape mask
            val icon = IconCompat.createWithAdaptiveBitmap(iconBitmap)

            // 4. Intent that ShortcutActivity receives when the user taps the shortcut
            val launchIntent = Intent(context, ShortcutActivity::class.java).apply {
                action = Intent.ACTION_VIEW   // required — shortcuts must have an action
                putExtra(ShortcutActivity.EXTRA_URL,             state.url)
                putExtra(ShortcutActivity.EXTRA_BROWSER_PACKAGE, state.selectedBrowser?.packageName)
                // Ensure it starts cleanly outside the app's back stack
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            // 5. Unique, URL-scoped ID so regenerating the same shortcut replaces it
            val shortcutId = buildId(state)

            val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
                .setShortLabel(state.shortcutName)          // shown under the icon
                .setLongLabel("Open ${state.shortcutName}") // shown in search / suggestions
                .setIcon(icon)
                .setIntent(launchIntent)
                .build()

            // 6. PendingIntent that fires when the user accepts the pin dialog
            val callbackIntent = Intent(context, ShortcutPinnedReceiver::class.java).apply {
                putExtra(ShortcutPinnedReceiver.EXTRA_SHORTCUT_NAME, state.shortcutName)
            }
            val successCallback = PendingIntent.getBroadcast(
                context,
                shortcutId.hashCode(),          // unique request code per shortcut
                callbackIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 7. Show the system "Add to Home Screen" dialog
            ShortcutManagerCompat.requestPinShortcut(
                context,
                shortcutInfo,
                successCallback.intentSender
            )

            GeneratorResult.DialogShown

        } catch (e: Exception) {
            GeneratorResult.Error(e.message ?: "Unknown error")
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds a stable shortcut ID from the name + URL.
     * Same name+URL always produces the same ID, so regeneration updates
     * the existing shortcut rather than creating a duplicate.
     */
    private fun buildId(state: FormState): String {
        val sanitized = state.shortcutName
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "_")
            .take(40)   // keep the ID short
        val urlHash = state.url.hashCode().toString().replace("-", "n")
        return "shoge_${sanitized}_$urlHash"
    }
}