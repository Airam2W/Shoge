package com.shoge.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.shoge.app.model.BrowserInfo

object BrowserDetector {

    /**
     * Returns all apps that registered an intent handler for https:// URLs.
     * Deduplicated by package name and sorted alphabetically.
     */
    fun getInstalledBrowsers(context: Context): List<BrowserInfo> {
        val pm = context.packageManager

        // A generic https URL triggers every installed browser
        val probe = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"))

        @Suppress("QueryPermissionsNeeded")   // covered by <queries> in manifest
        val handlers = pm.queryIntentActivities(probe, PackageManager.MATCH_ALL)

        return handlers
            .map { info ->
                BrowserInfo(
                    packageName = info.activityInfo.packageName,
                    label       = info.loadLabel(pm).toString(),
                    icon        = info.loadIcon(pm)
                )
            }
            .distinctBy { it.packageName }           // one entry per app
            .sortedBy   { it.label.lowercase() }
    }
}