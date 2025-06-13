package ru.qdev.lnotes.ui.view.dialog

import android.content.Context
import ru.qdev.lnotes.ui.view.menu.DialogMenuItem
import src.R

data class Dialog(val title: String,
                  val message: String,
                  val messageMaxLines: Int = 5,
                  val buttons: List<DialogButton>,
                  val menuList: List<DialogMenuItem> = listOf(),
                  val id: String = "",
                  val inputMaxLen: Int? = null,
                  val inputText: String? = null,
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

        fun makeDialogTest(context: Context) : Dialog {
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

        fun makeDialogLongTextTest(context: Context) : Dialog {
            return Dialog(
                title = "Dialog title",
                message = "Message\n\n\ntext\n\n\ntest",
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

        fun makeMenuTest(context: Context) : Dialog {
            return Dialog(
                title = "Menu title",
                message = "Message",
                buttons = listOf(),
                dialogType = DialogType.Menu,
                menuList = listOf(
                    DialogMenuItem(
                        title = "Menu item 1",
                        id = "1"
                    ),
                    DialogMenuItem(
                        title = "Menu item 2",
                        id = "2"
                    ),
                    DialogMenuItem(
                        title = "Menu item 3",
                        id = "3"
                    ),
                )
            )
        }

        fun makeOkCancelButtons(
            context: Context,
            onButtonId: String,
            cancelButtonId: String = ""
        ) : List<DialogButton>{
            return listOf(
                DialogButton(
                    title = context.getString(R.string.action_ok),
                    id = onButtonId
                ),
                DialogButton(
                    title = context.getString(R.string.cancel),
                    id = cancelButtonId
                )
            )
        }
    }
}
