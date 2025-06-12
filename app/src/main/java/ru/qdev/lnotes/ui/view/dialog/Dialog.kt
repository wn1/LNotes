package ru.qdev.lnotes.ui.view.dialog

import ru.qdev.lnotes.ui.view.menu.DialogMenuItem

data class Dialog(val title: String,
                  val message: String,
                  val buttons: List<DialogButton>,
                  val menuList: List<DialogMenuItem> = listOf(),
                  val id: String = "",
                  val inputMaxLen: Int? = null,
                  val dialogType: DialogType = DialogType.Dialog)
