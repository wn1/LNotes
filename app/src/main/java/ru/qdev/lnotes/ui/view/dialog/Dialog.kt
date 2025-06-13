package ru.qdev.lnotes.ui.view.dialog

import android.content.Context
import ru.qdev.lnotes.ui.view.menu.DialogMenuItem
import src.R

data class Dialog(val title: String,
                  val message: String,
                  val buttons: List<DialogButton>,
                  val menuList: List<DialogMenuItem> = listOf(),
                  val id: String = "",
                  val inputMaxLen: Int? = null,
                  val dialogButtonLayoutType: DialogButtonLayoutType =
                      DialogButtonLayoutType.Horizontal,
                  val dialogType: DialogType = DialogType.Dialog) {
    companion object {
        fun makeTestInputText(context: Context) : Dialog {
            return Dialog(
                title = "Input text",
                message = "Message",
                buttons = listOf(
                    DialogButton(
                        title = context.getString(R.string.action_ok)
                    ),
                    DialogButton(
                        title = context.getString(R.string.cancel)
                    ),
                ),
                dialogType = DialogType.InputText
            )
        }

        fun makeDialog(context: Context) : Dialog {
            return Dialog(
                title = "Dialog title",
                message = "Message",
                buttons = listOf(
                    DialogButton(
                        title = context.getString(R.string.action_ok)
                    ),
                    DialogButton(
                        title = context.getString(R.string.cancel)
                    ),
                ),
            )
        }
    }
}
