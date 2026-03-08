package ru.qdev.lnotes.ui.sheet.delete_unused.model

import ru.qdev.lnotes.db.model.StatusOfExecution

data class ConfirmedData(
    val statuses: List<StatusOfExecution>,
    val dayCount: Int
)
