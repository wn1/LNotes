package ru.qdev.lnotes.db.enum

import android.content.Context
import src.R

enum class StatusOfExecution (val dbValue: Int) {
    CREATED(0),
    COMPLETED(1),
    NOT_NEED(2);

    fun getTitle(context: Context): String {
        return context.getString(
            when(this) {
                CREATED -> R.string.set_in_work
                COMPLETED -> R.string.set_done
                NOT_NEED -> R.string.set_no_needed
            }
        )
    }

    companion object {
        fun fromDbValue(v: Int) : StatusOfExecution {
            return entries.firstOrNull { it.dbValue == v } ?: CREATED
        }
    }
}