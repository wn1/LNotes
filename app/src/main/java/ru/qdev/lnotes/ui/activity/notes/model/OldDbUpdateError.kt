package ru.qdev.lnotes.ui.activity.notes.model

import ru.qdev.lnotes.ThisApp
import src.R

enum class OldDbUpdateError(var errorCode: String) {
    ERROR_1("1.1"),
    ERROR_2("1.2"),
    ERROR_3("1.3"),
    ERROR_4("1.4");

    override fun toString(): String {
        return String.format(ThisApp.getContext().getString(R.string.error_with_id), errorCode)
    }
}