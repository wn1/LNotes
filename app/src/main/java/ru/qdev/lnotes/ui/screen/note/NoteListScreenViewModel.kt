package ru.qdev.lnotes.ui.screen.note

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.qdev.lnotes.core.events.DbManager
import ru.qdev.lnotes.core.pref.NotesPreferenceHelper
import ru.qdev.lnotes.db.dao.FolderDao
import ru.qdev.lnotes.db.dao.NotesDao
import ru.qdev.lnotes.db.entity.NotesEntry
import ru.qdev.lnotes.model.Folder
import ru.qdev.lnotes.model.FolderType
import ru.qdev.lnotes.ui.screen.base.BaseScreenViewModel
import ru.qdev.lnotes.utils.live_data.LiveEvent
import src.R
import javax.inject.Inject

interface NoteListScreenListener {
    fun onFolderMenuClick()
}

@HiltViewModel
class NoteListScreenViewModel @Inject constructor(
    private val dbManager: DbManager,
    @ApplicationContext private val context: Context,
    private val notesPreferenceHelper: NotesPreferenceHelper
): BaseScreenViewModel(), NoteListScreenListener {
    private lateinit var notesDao: NotesDao
    private lateinit var folderDao: FolderDao

    val selectedFolderS = mutableStateOf<Folder?>(null)
    val folderListS = mutableStateOf<List<Folder>>(listOf())
    val reloadNotesEvent = mutableStateOf<LiveEvent<Boolean>?>(null)

    private var fillFolderJob: Job? = null

    override fun provideContext(): Context {
        return context
    }

    init {
        dbManager.openNotesDb()
        notesDao = dbManager.notesDatabase!!.notesDao()
        folderDao = dbManager.notesDatabase!!.folderDao()

        fillTest()
        fillFolder()
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

    private fun fillFolder() {
        fillFolderJob?.cancel()
        fillFolderJob = viewModelScope.launch {
            var folderList = listOf(
                Folder(
                    id = null,
                    title = context.getString(R.string.add_category),
                    type = FolderType.AddFolderItem
                )
            )

            withContext(Dispatchers.IO) {
                folderList = folderList.plus(
                    folderDao.getAllOrderByLabel().map {
                        Folder.from(it)
                    }
                )
            }

            folderListS.value = folderList

            val folderId = notesPreferenceHelper.selectedFolderId
            if (folderId == null) {
                selectedFolderS.value = null
            }
            else {
                selectedFolderS.value = folderList.firstOrNull {
                    folderId == it.id
                }
            }

            reloadNotesEvent.value = LiveEvent(true)
        }
    }

    override fun onFolderMenuClick(){
        Log.i(TAG, "onFolderMenuClick")

    }

    companion object {
        private const val TAG = "NoteListScreenViewModel"
    }
}