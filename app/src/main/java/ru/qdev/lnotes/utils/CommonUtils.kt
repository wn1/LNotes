package ru.qdev.lnotes.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri

object CommonUtils {

    private const val TAG = "CommonUtils"

    fun openLinkInBrowser(activity: Activity, url: String) {
        try {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    url.toUri()
                )
            )
        }
        catch (ex: Throwable) {
            Log.e(TAG, "openLinkInBrowser ex: $ex", ex)
        }
    }
}