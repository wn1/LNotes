package ru.qdev.lnotes.ui.sheet.delete_unused.model

import ru.qdev.lnotes.db.model.StatusOfExecution

data class SelectedStatus(val status: StatusOfExecution, val isSelected: Boolean)
