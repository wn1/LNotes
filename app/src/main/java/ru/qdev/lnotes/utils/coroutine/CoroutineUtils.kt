package ru.qdev.lnotes.utils.coroutine

import kotlinx.coroutines.CancellationException

object CoroutineUtils {
    fun throwIfCancel(e: Throwable) {
        if (e is CancellationException){
            throw e
        }
    }
}