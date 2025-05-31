package ru.qdev.lnotes.ui.view.dialog

enum class DialogButtonStyle {
    Primary,
    Secondary
}

data class DialogButton(val title: String,
                        val style: DialogButtonStyle = DialogButtonStyle.Secondary,
                        val id: String = "")
