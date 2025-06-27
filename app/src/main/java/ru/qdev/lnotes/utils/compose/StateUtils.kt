package ru.qdev.lnotes.utils.compose

import androidx.compose.runtime.MutableState

object StateUtils {
    fun MutableState<Int>.updateCounter() {
        this.value += 1
    }
}