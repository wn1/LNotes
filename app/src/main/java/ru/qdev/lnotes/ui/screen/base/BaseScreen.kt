package ru.qdev.lnotes.ui.screen.base

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import ru.qdev.lnotes.ui.view.dialog.Dialog
import ru.qdev.lnotes.ui.view.dialog.DialogView

@Composable
fun BaseScreen(baseViewModel: BaseScreenViewModel,
               content: @Composable () -> Unit) {
    val owner = LocalLifecycleOwner.current
    val context = LocalContext.current

    DisposableEffect (owner) {
        val lifecycle = owner.lifecycle
        lifecycle.addObserver(baseViewModel)

        onDispose {
            lifecycle.removeObserver(baseViewModel)
        }
    }

    LaunchedEffect("activity") {
        baseViewModel.bind(context.getActivity())
    }

    content()

    BaseScreenContent(
        listener = baseViewModel,
        dialogMenu = baseViewModel.dialogMenuS.value
    )
}

private fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
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

