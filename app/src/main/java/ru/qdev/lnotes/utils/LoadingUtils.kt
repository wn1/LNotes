package ru.qdev.lnotes.utils

import androidx.compose.runtime.MutableState

object LoadingUtils {
    suspend fun loading(loaderState: MutableState<Boolean>, function: suspend () -> Unit) {
        try {
            loaderState.value = true
            function()
        }
        finally {
            loaderState.value = false
        }
    }
}