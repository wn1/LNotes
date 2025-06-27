package ru.qdev.lnotes.utils.compose

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity

object KeyboardUtils {
    @ExperimentalLayoutApi
    @Composable
    fun keyboardAsState(): State<Boolean> {
        //TODO не работает
//        val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        val isImeVisible = WindowInsets.isImeVisible
        return rememberUpdatedState(isImeVisible)
    }
}