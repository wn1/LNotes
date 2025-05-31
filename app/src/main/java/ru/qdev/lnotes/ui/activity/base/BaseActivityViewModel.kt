package ru.qdev.lnotes.ui.activity.base

import android.content.Context
import androidx.core.app.NotificationCompat.MessagingStyle.Message
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import ru.qdev.lnotes.ui.view.dialog.Dialog
import ru.qdev.lnotes.ui.view.dialog.DialogButton
import javax.inject.Inject

open class BaseActivityViewModel (
    context: Context,
    private val savedStateHandle: SavedStateHandle
) :  ViewModel() {

    fun showDialog(dialog: Dialog) {
        //TODO
    }

    fun showError(message: String, okButtonId: String = "") {
        //TODO
    }

    open fun onDialogButtonClick(button: DialogButton) {
        //TODO
    }
}