package ru.qdev.lnotes.utils.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.clickableStyled(
    enabled: Boolean = true,
    onClick: () -> Unit
) : Modifier {
    return this.clickable(
        interactionSource = remember {  MutableInteractionSource() },
        indication = ripple(color = Color.DarkGray),
    ) {
        onClick()
    }
}