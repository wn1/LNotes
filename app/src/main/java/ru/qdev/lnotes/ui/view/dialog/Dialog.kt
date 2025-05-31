package ru.qdev.lnotes.ui.view.dialog

data class Dialog(val title: String,
                  val message: String,
                  val buttons: List<DialogButton>,
                  val id: String = "")
