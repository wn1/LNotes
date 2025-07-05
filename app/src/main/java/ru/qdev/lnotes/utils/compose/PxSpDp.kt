package ru.qdev.lnotes.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun Float.toDp() : Dp {
    return with(LocalDensity.current) {
        this@toDp.toDp()
    }
}