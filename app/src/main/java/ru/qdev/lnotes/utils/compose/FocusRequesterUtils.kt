package ru.qdev.lnotes.utils.compose

import android.util.Log
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusDirection.Companion.Enter
import androidx.compose.ui.focus.FocusRequester

private const val TAG = "FocusRequesterUtils"

fun FocusRequester.tryRequestFocus(focusDirection: FocusDirection = Enter) {
    try {
        this.requestFocus(focusDirection)
    }
    catch (ex: Throwable) {
        Log.w(TAG, "tryRequestFocus ex: $ex")
    }
}
