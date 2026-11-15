package ru.qdev.lnotes.utils.compose

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

object ComposeUtils {
    @Composable
    fun SetStatusBarsColors(darkTheme: Boolean) {
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as? Activity)?.window
                window?.let {
                    val insetsController = WindowCompat.getInsetsController(window, view)

                    // Ключевой момент: isAppearanceLightStatusBars = true означает "тёмные иконки" (подходит для светлого фона)
                    // Поэтому в светлой теме (darkTheme = false) мы хотим тёмные иконки, устанавливаем true
                    // В тёмной теме (darkTheme = true) мы хотим светлые иконки, устанавливаем false
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                }
            }
        }
    }
}