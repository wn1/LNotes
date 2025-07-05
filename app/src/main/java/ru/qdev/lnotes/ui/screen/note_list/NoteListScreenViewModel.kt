package ru.qdev.lnotes.ui.screen.note_list

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ru.qdev.lnotes.core.QDVAppConst.NoteAddingId
import ru.qdev.lnotes.core.events.QDVDbManager
import ru.qdev.lnotes.core.pref.NotesPreferenceHelper
import ru.qdev.lnotes.db.dao.FolderDao
import ru.qdev.lnotes.db.dao.NotesDao
import ru.qdev.lnotes.db.entity.FolderEntry
import ru.qdev.lnotes.db.entity.NotesEntry
import ru.qdev.lnotes.db.enum.StatusOfExecution
import ru.qdev.lnotes.model.Folder
import ru.qdev.lnotes.model.FolderType
import ru.qdev.lnotes.ui.activity.backup.QDVBackupActivity
import ru.qdev.lnotes.ui.activity.notes.QDVNotesHomeActivity
import ru.qdev.lnotes.ui.navigation.QDVNavigator
import ru.qdev.lnotes.ui.navigation.route.note.NoteEditScreenRoute
import ru.qdev.lnotes.ui.screen.base.BaseScreenViewModel
import ru.qdev.lnotes.ui.screen.note_edit.NoteEditScreenViewModel
import ru.qdev.lnotes.ui.view.dialog.Dialog
import ru.qdev.lnotes.ui.view.dialog.DialogButton
import ru.qdev.lnotes.ui.view.dialog.DialogType
import ru.qdev.lnotes.ui.view.menu.DialogMenuItem
import ru.qdev.lnotes.ui.view.menu.MenuItemType
import ru.qdev.lnotes.utils.LoadingUtils.loading
import ru.qdev.lnotes.utils.live_data.LiveEvent
import src.BuildConfig
import src.R
import java.util.Date
import javax.inject.Inject

interface NoteListScreenListener {
    fun onFolderMenuClick()
    fun onSelectFolder(folder: Folder)
    fun onFolderLongClick(folder: Folder)
    fun onNoteClick (note: NotesEntry)
    fun onNoteMenuClick (note: NotesEntry)
    fun onNoteAddingClick()
    fun onSearchClick()
    fun onSearchCancelClick()
    fun onBackupClick()
    fun onAboutAppClick()
    fun onMailToDeveloperClick()
}

@HiltViewModel
class NoteListScreenViewModel @Inject constructor(
    private val dbManager: QDVDbManager,
    @ApplicationContext private val context: Context,
    private val notesPreferenceHelper: NotesPreferenceHelper,
    private val navigator: QDVNavigator,
    private val savedStateHandle: SavedStateHandle
): BaseScreenViewModel(), NoteListScreenListener {
    private lateinit var notesDao: NotesDao
    private lateinit var folderDao: FolderDao

    val selectedFolderS = mutableStateOf<Folder?>(null)
    val folderListS = mutableStateOf<List<Folder>>(listOf())
    val reloadNotesAndGoToFirstEvent = mutableStateOf<LiveEvent<Boolean>?>(null)
    val drawerHideEvent = mutableStateOf<LiveEvent<Boolean>?>(null)
    val folderLoadingS = mutableStateOf(false)
    val notesCountS = mutableStateOf(0L)
    val searchText = mutableStateOf("") //TODO сохранять searchText

    private var folderForMenu: Folder? = null
    private var noteForMenu: NotesEntry? = null

    private var fillFolderJob: Job? = null
    private var moveFolderJob: Job? = null
    private var addFolderJob: Job? = null
    private var removeFolderJob: Job? = null
    private var renameFolderJob: Job? = null
    private var noteEditJob: Job? = null
    private var noteMoveJob: Job? = null
    private var notesCountUpdateJob: Job? = null
    private var selectedFolderForPager: Folder? = null

    private fun makeNotesPagingFlow(): Flow<PagingData<NotesEntry>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            ),
            pagingSourceFactory = {
                when (selectedFolderForPager?.type){
                    FolderType.AllFolder -> {
                        return@Pager notesDao.getNotesAllPagingSource(
                            searchText = textToSearch(searchText.value)
                        )
                    }
                    FolderType.UnknownFolder -> {
                        return@Pager notesDao.getNotesWithUnknownFolderPagingSource(
                            searchText = textToSearch(searchText.value)
                        )
                    }
                    else -> {
                        notesDao.getNotesByFolderIdPagingSource(
                            folderId = selectedFolderForPager?.id?.toLongOrNull(),
                            searchText = textToSearch(searchText.value)
                        )
                    }
                }
            }
        ).flow
    }

    val notesPagingFlow = makeNotesPagingFlow().cachedIn(viewModelScope)

    private fun notesCountUpdate() {
        notesCountUpdateJob?.cancel()
        notesCountUpdateJob = viewModelScope.launch {
            notesCountS.value = withContext(Dispatchers.IO) {
                when (selectedFolderForPager?.type) {
                FolderType.AllFolder -> {
                    notesDao.getNotesAllCount(
                        searchText = textToSearch(searchText.value)
                    )
                }

                FolderType.UnknownFolder -> {
                    notesDao.getNotesWithUnknownFolderCount(
                        searchText = textToSearch(searchText.value)
                    )
                }

                else -> {
                    notesDao.getNotesByFolderIdCount(
                        folderId = selectedFolderForPager?.id?.toLongOrNull(),
                        searchText = textToSearch(searchText.value)
                    )
                }
            }
            }
        }
    }

    private fun textToSearch(text: String) : String {
        return "%$text%"
    }

    override fun provideContext(): Context {
        return context
    }

    override fun provideSavedStateHandle(): SavedStateHandle {
        return savedStateHandle
    }

    init {
        dbManager.openNotesDb()
        notesDao = dbManager.notesDatabase!!.notesDao()
        folderDao = dbManager.notesDatabase!!.folderDao()

        fillFolder(withReloadAndGoToFirst = true)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        notesCountUpdate()
    }

    private fun fillFolder(withReloadAndGoToFirst: Boolean = false) {
        fillFolderJob?.cancel()
        fillFolderJob = viewModelScope.launch {
            loading(folderLoadingS) {
                var folderList = listOf(
                    Folder(
                        id = null,
                        title = context.getString(R.string.add_category),
                        type = FolderType.AddFolderItem
                    ),
                    Folder(
                        id = null,
                        title = context.getString(R.string.category_all),
                        type = FolderType.AllFolder
                    ),
                    Folder(
                        id = null,
                        title = context.getString(R.string.category_unknown),
                        type = FolderType.UnknownFolder
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

                val folderId = notesPreferenceHelper.getSelectedFolderFromPref()
                if (folderId == null) {
                    selectedFolderS.value = null
                } else {
                    val fId = folderId.first
                    val type = folderId.second
                    val folder = folderList.firstOrNull {
                        it.type == type && it.id == fId
                    }
                    selectedFolderS.value = folder
                }

                if (withReloadAndGoToFirst) {
                    reloadNotesAndGoToFirst()
                }
                else {
                    notesCountUpdate()
                }
            }
        }
    }

    fun reloadNotesAndGoToFirst() {
        selectedFolderForPager = selectedFolderS.value
        reloadNotesAndGoToFirstEvent.value = LiveEvent(true)
        notesCountUpdate()
    }

    override fun onFolderMenuClick(){
        Log.i(TAG, "onFolderMenuClick")

    }

    override fun onSelectFolder(folder: Folder) {
        Log.i(TAG, "onSelectFolder: ${folder.id}")
        when (folder.type) {
            FolderType.AddFolderItem -> {
                addFolderClick()
                return
            }
            else -> {}
        }
        notesPreferenceHelper.saveSelectedFolderToPref(folder)
        selectedFolderS.value = folder
        reloadNotesAndGoToFirst()
        drawerHideEvent.value = LiveEvent(true)
    }

    override fun onFolderLongClick(folder: Folder) {
        Log.i(TAG, "onFolderLongClick: ${folder.id}")

        if (folder.type != FolderType.Folder) {
            return
        }

        folderForMenu = folder
        showDialogOrMenu(
            Dialog(
                title = folder.title,
                message = "",
                dialogType = DialogType.Menu,
                buttons = listOf(),
                menuList = listOf(
                    DialogMenuItem(
                        title = context.getString(R.string.menu_delete),
                        id = MENU_FOLDER_DELETE
                    ),
                    DialogMenuItem(
                        title = context.getString(R.string.menu_rename),
                        id = MENU_FOLDER_RENAME
                    ),
                )
            )
        )
    }

    override fun onDialogButtonClick(dialog: Dialog,
                                     dialogButton: DialogButton,
                                     inputText: String) {
        super.onDialogButtonClick(dialog, dialogButton, inputText)

        when (dialogButton.id) {
            ADD_FOLDER_OK_B -> {
                addFolder(inputText)
                return
            }

            RENAME_FOLDER_OK_B -> {
                folderForMenu?.let {
                    renameFolder(it, inputText)
                }
                return
            }

            FOLDER_DELETE_CONFIRM_B -> {
                folderForMenu?.let {
                    deleteFolder(it)
                }
            }

            NOTE_DELETE_CONFIRM_B -> {
                noteForMenu?.let {
                    deleteNote(it)
                }
            }

            NOTES_SEARCH_OK_B -> {
                notesSearch(inputText)
            }
        }
    }

    override fun onDialogMenuItemClick(dialog: Dialog, dialogMenuItem: DialogMenuItem) {
        super.onDialogMenuItemClick(dialog, dialogMenuItem)

        val logStr = "onDialogMenuItemClick"

        val folder = folderForMenu
        val folderIsEmptyLog = "$logStr: folder is empty"

        if (dialog.id == NOTE_MOVE_SELECT_DIALOG){
            moveNoteToFolderWithId(noteForMenu, dialogMenuItem.id)
            return
        }

        when(dialogMenuItem.id) {
            MENU_FOLDER_DELETE -> {
                if (folder == null) {
                    Log.e(TAG, folderIsEmptyLog)
                    return
                }

                showDialogOrMenu(
                    Dialog(
                        title = context.getString(R.string.delete_folder_confirm_message),
                        message = context.getString(R.string.delete_folder_confirm, folder.title),
                        dialogType = DialogType.Dialog,
                        buttons = listOf(
                            DialogButton(
                                title = context.getString(R.string.delete_button),
                                id = FOLDER_DELETE_CONFIRM_B
                            ),
                            DialogButton(
                                title = context.getString(R.string.cancel),
                            )
                        )
                    )
                )

            }

            MENU_FOLDER_RENAME -> {
                if (folder == null) {
                    Log.e(TAG, folderIsEmptyLog)
                    return
                }

                showDialogOrMenu(
                    dialog = Dialog(
                        dialogType = DialogType.InputText,
                        title = context.getString(R.string.rename_folder_title),
                        inputText = folder.title,
                        message = "",
                        buttons = listOf(
                            DialogButton(
                                title = context.getString(R.string.action_ok),
                                id = RENAME_FOLDER_OK_B
                            ),
                            DialogButton(
                                title = context.getString(R.string.cancel)
                            )
                        )
                    )
                )
            }

            MENU_NOTE_SET_DONE -> {
                setStatusToNote(noteForMenu, StatusOfExecution.COMPLETED)
            }

            MENU_NOTE_SET_IN_WORK -> {
                setStatusToNote(noteForMenu, StatusOfExecution.CREATED)
            }

            MENU_NOTE_SET_NO_NEEDED -> {
                setStatusToNote(noteForMenu, StatusOfExecution.NOT_NEED)
            }

            MENU_NOTE_MOVE -> {
                moveNoteMenuPrepare()
            }

            MENU_NOTE_DELETE -> {
                showDialogOrMenu(
                    Dialog(
                        title = noteForMenu?.content ?: "",
                        titleMaxLines = MAX_DIALOG_NOTE_TITLE_LINES,
                        message = context.getString(R.string.delete_confirm),
                        dialogType = DialogType.Dialog,
                        buttons = listOf(
                            DialogButton(
                                title = context.getString(R.string.delete_button),
                                id = NOTE_DELETE_CONFIRM_B
                            ),
                            DialogButton(
                                title = context.getString(R.string.cancel),
                            )
                        )
                    )
                )
            }

            MENU_TEST1 -> {
                test()
            }
        }
    }

    private fun test() {
        noteEditJob?.cancel()
        noteEditJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                for (n in 0..100) {
                    val note = NotesEntry()
                    note.content = "test-$n"
                    note.createTimeU = Date().time
                    notesDao.insertAll(note)
                }
            }
            notesCountUpdate()
        }
    }

    private fun deleteNote(note: NotesEntry?) {
        val logStr = "deleteNote"
        if (note == null) {
            Log.e(TAG, "$logStr note is null")
            return
        }

        Log.i(TAG, "$logStr note id: ${note.uid}")

        noteEditJob?.cancel()
        noteEditJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                notesDao.delete(note)
            }
            notesCountUpdate()
        }
    }

    private fun moveNoteToFolderWithId(note: NotesEntry?, folderIdStr: String) {
        val logStr = "moveNoteToFolderWithId"
        if (note == null) {
            Log.e(TAG, "$logStr note is null")
            return
        }

        val folderId: Long? = if (folderIdStr.isEmpty()) {
            null
        } else {
            folderIdStr.toLongOrNull() ?: run {
                Log.e(TAG, "$logStr folderIdStr error")
                return
            }
        }

        Log.i(TAG, "$logStr note id: ${note.uid}, folderId: $folderIdStr")
        noteEditJob?.cancel()
        noteEditJob = viewModelScope.launch {
            note.folderId = folderId
            withContext(Dispatchers.IO) {
                notesDao.insertAll(note)
            }
            notesCountUpdate()
        }
    }

    private fun setStatusToNote(note: NotesEntry?, statusOfExecution: StatusOfExecution) {
        val logStr = "setStatusToNote"
        if (note == null) {
            Log.e(TAG, "$logStr note is null")
            return
        }

        Log.i(TAG, "$logStr note id: ${note.uid}")

        noteEditJob?.cancel()
        noteEditJob = viewModelScope.launch {
            setStatusToNoteTask(note, statusOfExecution)
        }
    }

    private suspend fun setStatusToNoteTask(note: NotesEntry, statusOfExecution: StatusOfExecution) {
        note.isReady = statusOfExecution.dbValue
        when (statusOfExecution) {
            StatusOfExecution.CREATED -> {
                note.updateTimeU = Date().time
                note.completeTimeU = null
            }

            StatusOfExecution.NOT_NEED, StatusOfExecution.COMPLETED -> {
                note.completeTimeU = Date().time
            }
        }
        withContext(Dispatchers.IO) {
            notesDao.insertAll(note)
        }
        notesCountUpdate()
    }

    private fun moveNoteMenuPrepare(delayMs: Long = 200) {
        noteMoveJob?.cancel()
        noteMoveJob = viewModelScope.launch {
            loading(folderLoadingS) {
                val itemList = mutableListOf<DialogMenuItem>()

                withContext(Dispatchers.IO) {
                    itemList.addAll(
                        folderDao.getAllOrderByLabel().map {
                            DialogMenuItem(
                                title = it.label ?: "",
                                id = it.uid?.toString() ?: ""
                            )
                        }
                    )
                }

                noteForMenu?.let { folder ->
                    delay(delayMs)
                    showDialogOrMenu(
                        Dialog(
                            title = noteForMenu?.content ?: "",
                            titleMaxLines = MAX_DIALOG_NOTE_TITLE_LINES,
                            message = context.getString(R.string.move_title),
                            dialogType = DialogType.Menu,
                            buttons = listOf(),
                            menuList = itemList,
                            id = NOTE_MOVE_SELECT_DIALOG
                        )
                    )
                }
            }
        }
    }

    private fun addFolderClick() {
        Log.i(TAG, "addFolderClick")
        showDialogOrMenu(
            dialog = Dialog(
                dialogType = DialogType.InputText,
                title = context.getString(R.string.add_category),
                message = "",
                buttons = listOf(
                    DialogButton(
                        title = context.getString(R.string.action_ok),
                        id = ADD_FOLDER_OK_B
                    ),
                    DialogButton(
                        title = context.getString(R.string.cancel)
                    )
                )
            )
        )
    }

    private fun addFolder(folderName: String) {
        val logStr = "addFolder"
        Log.i(TAG, logStr)

        if (folderLoadingS.value) {
            Log.i(TAG, "$logStr folderLoading, return")
            return
        }

        if (folderName.isEmpty()){
            showError(context.getString(R.string.folder_name_need_no_empty))
            return
        }

        addFolderJob?.cancel()
        addFolderJob = viewModelScope.launch {
            loading(folderLoadingS) {
                val folderEntry = FolderEntry(
                    uid = null,
                    label = folderName
                )

                val newEntry = withContext(Dispatchers.IO) {
                    folderDao.insert(folderEntry)
                }

                notesPreferenceHelper.saveSelectedFolderToPref(
                    Folder(id = newEntry.toString(), title = folderEntry.label ?: "")
                )
                fillFolder(withReloadAndGoToFirst = true)
            }
        }
    }

    private fun deleteFolder(folder: Folder) {
        val logStr = "deleteFolder"
        Log.i(TAG, logStr)

        if (folderLoadingS.value) {
            Log.i(TAG, "$logStr folderLoading, return")
            return
        }

        removeFolderJob?.cancel()
        removeFolderJob = viewModelScope.launch {
            loading(folderLoadingS) {
                val id = folder.id?.toLongOrNull()
                if (id == null) {
                    Log.e(TAG, "$logStr id is null")
                    return@loading
                }

                var withReloadAndGoToFirst = false
                if (folder.id == selectedFolderS.value?.id) {
                    val newFolder = folderListS.value.firstOrNull {
                        it.type == FolderType.UnknownFolder
                    }
                    notesPreferenceHelper.saveSelectedFolderToPref(newFolder)
                    selectedFolderS.value = newFolder
                    withReloadAndGoToFirst = true
                }

                withContext(Dispatchers.IO) {
                    notesDao.deleteByFolderId(folderId = id)
                    folderDao.deleteById(id)
                }

                fillFolder(withReloadAndGoToFirst = withReloadAndGoToFirst)
            }
        }
    }

    private fun renameFolder(folder: Folder, newName: String) {
        val logStr = "renameFolder"
        Log.i(TAG, logStr)

        if (folderLoadingS.value) {
            Log.i(TAG, "$logStr folderLoading, return")
            return
        }

        if (newName.isEmpty()){
            showError(context.getString(R.string.folder_name_need_no_empty))
            return
        }

        renameFolderJob?.cancel()
        renameFolderJob = viewModelScope.launch {
            loading(folderLoadingS) {
                val id = folder.id?.toLongOrNull()
                if (id == null) {
                    Log.e(TAG, "$logStr id is null")
                    return@loading
                }

                var isError = false

                withContext(Dispatchers.IO) {
                    val editFolder = folderDao.getById(id).firstOrNull()
                    if (editFolder == null) {
                        withContext(Dispatchers.Main) {
                            val errStr = context.getString(R.string.folder_not_found)
                            Log.e(TAG, errStr)
                            showError(errStr)
                            isError = true
                        }
                        return@withContext
                    }

                    editFolder.label = newName
                    folderDao.insertAll(editFolder)
                }

                fillFolder()
            }
        }
    }

    override fun onNoteClick (note: NotesEntry) {
        val logStr = "onNoteClick"
        Log.i(TAG, "$logStr, id: ${note.uid}")

        val noteId = note.uid
        if (noteId == null) {
            showError(context.getString(R.string.note_not_found))
            return
        }

        runBlocking {
            val res = NoteEditScreenViewModel.prepareEdit(
                context = context,
                noteId = noteId,
                notesDao = notesDao,
                preferenceHelper = notesPreferenceHelper
            )

            res.exceptionOrNull()?.let {
                showError(it)
                return@runBlocking
            }

            navigator.navigate(NoteEditScreenRoute(noteId = note.uid))
        }
    }

    override fun onNoteMenuClick (note: NotesEntry) {
        val logStr = "onNoteMenuClick"
        Log.i(TAG, "$logStr, id: ${note.uid}")

        noteForMenu = note

        val menuList = mutableListOf<DialogMenuItem>()

        val status = note.statusOfExecution()

        if (status != StatusOfExecution.COMPLETED) {
            menuList.add(
                DialogMenuItem(
                    title = context.getString(R.string.set_done),
                    id = MENU_NOTE_SET_DONE
                ),
            )
        }

        if (status != StatusOfExecution.CREATED) {
            menuList.add(
                DialogMenuItem(
                    title = context.getString(R.string.set_in_work),
                    id = MENU_NOTE_SET_IN_WORK
                ),
            )
        }

        if (status != StatusOfExecution.NOT_NEED) {
            menuList.add(
                DialogMenuItem(
                    title = context.getString(R.string.set_no_needed),
                    id = MENU_NOTE_SET_NO_NEEDED
                ),
            )
        }

        menuList.addAll(
            listOf(
                DialogMenuItem(
                    title = "",
                    id = MENU_DIVIDER,
                    type = MenuItemType.Divider
                ),
                DialogMenuItem(
                    title = context.getString(R.string.menu_move),
                    id = MENU_NOTE_MOVE
                ),
                DialogMenuItem(
                    title = context.getString(R.string.menu_delete),
                    id = MENU_NOTE_DELETE
                ),
            )
        )

        if (BuildConfig.DEBUG) {
            menuList.addAll(
                listOf(
                    DialogMenuItem(
                        title = "test1",
                        id = MENU_TEST1
                    )
                )
            )
        }

        showDialogOrMenu(
            Dialog(
                title = noteForMenu?.content ?: "",
                titleMaxLines = MAX_DIALOG_NOTE_TITLE_LINES,
                message = "",
                dialogType = DialogType.Menu,
                buttons = listOf(),
                menuList = menuList
            )
        )
    }

    override fun onNoteAddingClick() {
        val logStr = "onAddingClick"
        Log.i(TAG, logStr)

        val folder = selectedFolderS.value
        val folderId = if (folder?.type == FolderType.Folder) {
            folder.id?.toLongOrNull()
        }
        else {
            null
        }

        val res = NoteEditScreenViewModel.prepareAdding(
            context = context,
            preferenceHelper = notesPreferenceHelper,
            folderId = folderId
        )

        res.exceptionOrNull()?.let {
            showError(it)
            return
        }

        navigator.navigate(NoteEditScreenRoute(noteId = NoteAddingId))
    }

    override fun onSearchClick() {
        val logStr = "onSearchClick"
        Log.i(TAG, logStr)

        showDialogOrMenu(
            dialog = Dialog(
                dialogType = DialogType.InputText,
                title = context.getString(R.string.action_find_notes_title),
                message = "",
                buttons = listOf(
                    DialogButton(
                        title = context.getString(R.string.action_find_notes),
                        id = NOTES_SEARCH_OK_B
                    ),
                    DialogButton(
                        title = context.getString(R.string.cancel)
                    )
                )
            )
        )
    }

    private fun notesSearch(inputText: String) {
        searchText.value = inputText
        reloadNotesAndGoToFirst()
    }

    override fun onSearchCancelClick() {
        val logStr = "onSearchCancelClick"
        Log.i(TAG, logStr)
        searchText.value = ""
        reloadNotesAndGoToFirst()
    }

    override fun onBackupClick() {
        val logStr = "onBackupClick"
        Log.i(TAG, logStr)
        val intent = Intent(context, QDVBackupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        getActivity()?.startActivity(intent)
    }

    override fun onAboutAppClick() {
        val logStr = "onAboutAppClick"
        Log.i(TAG, logStr)

        (getActivity() as? QDVNotesHomeActivity)?.showAboutAppDialog()
    }

    override fun onMailToDeveloperClick() {
        val logStr = "onMailToDeveloperClick"
        Log.i(TAG, logStr)

        (getActivity() as? QDVNotesHomeActivity)?.contactToDeveloper()
    }

    companion object {
        private const val TAG = "NoteListScreenViewModel"
        private const val MAX_DIALOG_NOTE_TITLE_LINES = 2
        private const val ADD_FOLDER_OK_B = "ADD_FOLDER_OK"
        private const val RENAME_FOLDER_OK_B = "RENAME_FOLDER_OK_B"
        private const val MENU_FOLDER_DELETE = "MENU_FOLDER_DELETE"
        private const val MENU_DIVIDER = "MENU_DIVIDER"
        private const val MENU_NOTE_SET_DONE = "MENU_NOTE_SET_DONE"
        private const val MENU_NOTE_SET_IN_WORK = "MENU_NOTE_SET_IN_WORK"
        private const val MENU_NOTE_SET_NO_NEEDED = "MENU_NOTE_SET_NO_NEEDED"
        private const val MENU_NOTE_MOVE = "MENU_NOTE_MOVE"
        private const val MENU_NOTE_DELETE = "MENU_NOTE_DELETE"
        private const val MENU_TEST1 = "MENU_TEST1"
        private const val NOTE_DELETE_CONFIRM_B = "NOTE_DELETE_CONFIRM_B"
        private const val FOLDER_DELETE_CONFIRM_B = "FOLDER_DELETE_CONFIRM_B"
        private const val MENU_FOLDER_RENAME = "MENU_FOLDER_MOVE"
        private const val NOTE_MOVE_SELECT_DIALOG = "NOTE_MOVE_SELECT_DIALOG"
        private const val NOTES_SEARCH_OK_B = "NOTES_SEARCH_OK_B"
    }
}