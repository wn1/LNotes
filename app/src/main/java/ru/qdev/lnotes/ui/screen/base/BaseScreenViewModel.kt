package ru.qdev.lnotes.ui.screen.base

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import ru.qdev.lnotes.ui.view.dialog.Dialog
import ru.qdev.lnotes.ui.view.dialog.DialogButton
import ru.qdev.lnotes.ui.view.menu.DialogMenuItem
import src.R
import java.lang.ref.WeakReference
import kotlin.jvm.Throws

interface BaseScreenViewModelListener {
    fun hideDialogOrMenu(index: Int)
    fun onDialogMenuItemClick(dialog: Dialog, dialogMenuItem: DialogMenuItem)
    fun onDialogButtonClick(dialog: Dialog, dialogButton: DialogButton, inputText: String)
}

abstract class BaseScreenViewModel : ViewModel(),
    BaseScreenViewModelListener,
    DefaultLifecycleObserver {
    abstract fun provideContext(): Context
    abstract fun provideSavedStateHandle(): SavedStateHandle

    val BaseTag = this::class.java.simpleName

    val dialogMenuS = mutableStateOf<List<Dialog>>(listOf())

    var activityRef: WeakReference<Activity>? = null
        private set

    fun bind(activity: Activity?) {
        activityRef = WeakReference(activity)
    }

    fun getActivity(): Activity? {
        return activityRef?.get()
    }

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

    fun showError(message: String, buttonId: String = "") {
        Log.i(BaseTag, "showError ${message.take(5000)}")
        showDialogOrMenu(
            dialog = Dialog(
                title = provideContext().getString(R.string.error_title),
                message = message,
                buttons = listOf(
                    DialogButton(
                        title = provideContext().getString(R.string.action_ok),
                        id = buttonId
                    )
                )
            )
        )
    }

    fun showError(error: Throwable, buttonId: String = "") {
        Log.i(BaseTag, "showError $error")
        val message = provideContext().getString(R.string.error_s, error.message)
        showError(message, buttonId)
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