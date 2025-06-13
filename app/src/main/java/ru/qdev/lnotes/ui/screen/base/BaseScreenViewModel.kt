package ru.qdev.lnotes.ui.screen.base

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import ru.qdev.lnotes.ui.view.dialog.Dialog
import ru.qdev.lnotes.ui.view.dialog.DialogButton
import ru.qdev.lnotes.ui.view.menu.DialogMenuItem
import src.R

interface BaseScreenViewModelListener {
    fun hideDialogOrMenu(index: Int)
    fun onDialogMenuItemClick(dialog: Dialog, dialogMenuItem: DialogMenuItem)
    fun onDialogButtonClick(dialog: Dialog, dialogButton: DialogButton, inputText: String)
}

abstract class BaseScreenViewModel : ViewModel(), BaseScreenViewModelListener {
    abstract fun provideContext(): Context

    val BaseTag = this::class.java.simpleName

    val dialogMenuS = mutableStateOf<List<Dialog>>(listOf())

    fun showDialogOrMenu(dialog: Dialog) {
        Log.i(BaseTag, "showDialogOrMenu, dialogId: ${dialog.id}")
        dialogMenuS.value = dialogMenuS.value.plus(dialog)
    }

    fun hideDialogOrMenu(dialog: Dialog) {
        Log.i(BaseTag, "hideDialogOrMenu, dialogId: ${dialog.id}")
        dialogMenuS.value = dialogMenuS.value.mapNotNull {
            if (it === dialog) return@mapNotNull null
            return@mapNotNull it
        }
    }

    fun showError(message: String) {
        Log.i(BaseTag, "showError")
        showDialogOrMenu(
            dialog = Dialog(
                title = provideContext().getString(R.string.error_title),
                message = message,
                buttons = listOf(
                    DialogButton(
                        title = provideContext().getString(R.string.action_ok)
                    )
                )
            )
        )
    }

    override fun hideDialogOrMenu(index: Int) {
        Log.i(BaseTag, "hideDialogOrMenu, index: ${index}")
        dialogMenuS.value = dialogMenuS.value.mapIndexedNotNull { inx, item ->
            if (index == inx) return@mapIndexedNotNull null
            return@mapIndexedNotNull item
        }
    }

    override fun onDialogMenuItemClick(
        dialog: Dialog,
        dialogMenuItem: DialogMenuItem
    ) {
        Log.i(BaseTag, "onDialogMenuItemClick, dialogId: ${dialog.id}" +
                ", itemId: ${dialogMenuItem.id}")
        hideDialogOrMenu(dialog)
    }

    override fun onDialogButtonClick(dialog: Dialog,
                                     dialogButton: DialogButton,
                                     inputText: String) {
        Log.i(BaseTag, "onDialogButtonClick, dialogId: ${dialog.id}" +
                ", buttonId: ${dialogButton.id}")

        hideDialogOrMenu(dialog)
    }
}