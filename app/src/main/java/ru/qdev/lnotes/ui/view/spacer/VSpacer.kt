package ru.qdev.lnotes.ui.view.spacer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun VSpacer (height: Dp) {
    Spacer(Modifier.height(height))
}

@Composable
fun HSpacer (width: Dp) {
    Spacer(Modifier.width(width))
}