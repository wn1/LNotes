package ru.qdev.lnotes.core.values

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import ru.qdev.lnotes.model.NotesViewType

/*
Синглтон для того, чтобы сохранять значения в ОЗУ, независимо от пересоздания активностей
 */

class AppValues(val context: Context) {
    val notesViewTypeS = mutableStateOf<NotesViewType>(NotesViewType.Notes)
    val notesSearchText = mutableStateOf("")
}