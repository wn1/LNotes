package ru.qdev.lnotes.ui.screen.note

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.qdev.lnotes.core.events.DbManager
import ru.qdev.lnotes.db.dao.NotesDao
import ru.qdev.lnotes.db.entity.NotesEntry
import ru.qdev.lnotes.ui.screen.base.BaseScreenViewModel
import javax.inject.Inject

@HiltViewModel
class NoteListScreenViewModel @Inject constructor(
    private val dbManager: DbManager
): BaseScreenViewModel() {
    lateinit var notesDao: NotesDao

    init {
        dbManager.openNotesDb()
        fillTest()
        notesDao = dbManager.notesDatabase!!.notesDao()
    }

    val testS = mutableStateOf<List<NotesEntry>>(listOf())

    private fun fillTest() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val entries = notesDao.getAll()
                testS.value = entries
            }
        }
    }
}