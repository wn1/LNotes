package ru.qdev.lnotes.db.enum

enum class StatusOfExecution (val dbValue: Int) {
    CREATED(0),
    COMPLETED(1),
    NOT_NEED(2);

    companion object {
        fun fromDbValue(v: Int) : StatusOfExecution {
            return entries.firstOrNull { it.dbValue == v } ?: CREATED
        }
    }
}