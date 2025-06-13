package ru.qdev.lnotes.ui.screen.base

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import ru.qdev.lnotes.ui.view.dialog.Dialog
import ru.qdev.lnotes.ui.view.dialog.DialogButton
import ru.qdev.lnotes.ui.view.menu.DialogMenuItem
import src.R

interface BaseScreenViewModelListener {
    fun hideDialogMenu(index: Int)
    fun onDialogMenuItemClick(dialog: Dialog, dialogMenuItem: DialogMenuItem)
    fun onDialogButtonClick(dialog: Dialog, dialogButton: DialogButton, inputText: String)
}

abstract class BaseScreenViewModel : ViewModel(), BaseScreenViewModelListener {
    abstract fun provideContext(): Context

    val dialogMenuS = mutableStateOf<List<Dialog>>(listOf())

    fun showDialogMenu(menu: Dialog) {
        dialogMenuS.value = dialogMenuS.value.plus(menu)
    }

    fun hideDialogMenu(menu: Dialog) {
        dialogMenuS.value = dialogMenuS.value.mapNotNull {
            if (it === menu) return@mapNotNull null
            return@mapNotNull it
        }
    }

    fun showError(message: String) {
        showDialogMenu(
            menu = Dialog(
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

    override fun hideDialogMenu(index: Int) {
        dialogMenuS.value = dialogMenuS.value.mapIndexedNotNull { inx, item ->
            if (index == inx) return@mapIndexedNotNull null
            return@mapIndexedNotNull item
        }
    }

    override fun onDialogMenuItemClick(
        dialog: Dialog,
        dialogMenuItem: DialogMenuItem
    ) {
        hideDialogMenu(dialog)
    }

    override fun onDialogButtonClick(dialog: Dialog,
                                     dialogButton: DialogButton,
                                     inputText: String) {
        hideDialogMenu(dialog)
    }
}