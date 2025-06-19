package ru.qdev.lnotes.ui.screen.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import ru.qdev.lnotes.ui.view.dialog.Dialog
import ru.qdev.lnotes.ui.view.dialog.DialogView

@Composable
fun BaseScreen(baseViewModel: BaseScreenViewModel,
               content: @Composable () -> Unit) {
    val owner = LocalLifecycleOwner.current

    DisposableEffect (owner) {
        val lifecycle = owner.lifecycle
        lifecycle.addObserver(baseViewModel)

        onDispose {
            lifecycle.removeObserver(baseViewModel)
        }
    }

    content()

    BaseScreenContent(
        listener = baseViewModel,
        dialogMenu = baseViewModel.dialogMenuS.value
    )
}

@Composable
private fun BaseScreenContent(
    listener: BaseScreenViewModelListener?,
    dialogMenu: List<Dialog>
) {
    dialogMenu.forEachIndexed { index, dialog ->
        DialogView(
            dialog = dialog,
            onDismiss = {
                listener?.hideDialogOrMenu(index)
            },
            onMenuItemClick = {
                listener?.onDialogMenuItemClick(
                    dialogMenuItem = it,
                    dialog = dialog
                )
            },
            onButtonClick = { button, inputText ->
                listener?.onDialogButtonClick(
                    dialog = dialog,
                    dialogButton = button,
                    inputText = inputText
                )
            }
        )
    }
}

