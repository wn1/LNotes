package ru.qdev.lnotes.ui.screen.note_edit

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.qdev.lnotes.core.QDVAppConst.NoteAddingId
import ru.qdev.lnotes.core.events.QDVDbManager
import ru.qdev.lnotes.core.pref.NotesPreferenceHelper
import ru.qdev.lnotes.db.dao.FolderDao
import ru.qdev.lnotes.db.dao.NotesDao
import ru.qdev.lnotes.db.entity.NotesEntry
import ru.qdev.lnotes.ui.activity.notes.QDVNotesHomeActivity
import ru.qdev.lnotes.ui.navigation.QDVNavigator
import ru.qdev.lnotes.ui.navigation.route.note.NoteEditScreenRoute
import ru.qdev.lnotes.ui.screen.base.BaseScreenViewModel
import ru.qdev.lnotes.ui.view.dialog.Dialog
import ru.qdev.lnotes.ui.view.dialog.DialogButton
import ru.qdev.lnotes.utils.coroutine.CoroutineUtils.throwIfCancel
import src.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

interface NoteEditScreenViewModelListener {
    fun onBackClick()
    fun onSaveClick()
    fun onTextChange(newText: TextFieldValue)
    fun onAddTimeClick()
    fun onInsertCheckedCharClick()
    fun onInsertUncheckedCharClick()
    fun onSendNoteClick()
}

@HiltViewModel
class NoteEditScreenViewModel @Inject constructor(
    private val dbManager: QDVDbManager,
    @ApplicationContext private val context: Context,
    private val notesPreferenceHelper: NotesPreferenceHelper,
    private val navigator: QDVNavigator,
    private val savedStateHandle: SavedStateHandle
): BaseScreenViewModel(), NoteEditScreenViewModelListener {
    private lateinit var notesDao: NotesDao
    private lateinit var folderDao: FolderDao

    private val route = savedStateHandle.toRoute<NoteEditScreenRoute>()

    private var isClose = false

    val textS = mutableStateOf(TextFieldValue())

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

        fillEdit()
    }

    private fun checkNoteIdIsError() : Boolean {
        if (route.noteId != notesPreferenceHelper.editNoteId) {
            showError(
                message = context.getString(R.string.error_with_id, "noteId not equal"),
                buttonId = ERROR_CLOSE_B
            )
            return true
        }

        return false
    }

    private fun fillEdit() {
        if (checkNoteIdIsError()) {
            return
        }

        val text = notesPreferenceHelper.editNoteText ?: ""
        textS.value = TextFieldValue(
            text,
            selection = TextRange(
                start = text.length,
                end = text.length
            )
        )
    }

    override fun onTextChange(newText: TextFieldValue) {
        textS.value = newText
    }

    override fun onStop(owner: LifecycleOwner) {
        if (!isClose) {
            notesPreferenceHelper.editNoteText = textS.value.text
        }
        super.onStop(owner)
    }

    override fun onSaveClick() {
        val logStr = "onSaveClick"
        Log.i(TAG, logStr)

        if (checkNoteIdIsError()) {
            return
        }

        viewModelScope.launch {
            val newText = textS.value.text
            withContext(Dispatchers.IO) {
                try {
                    val id = notesPreferenceHelper.editNoteId

                    val note: NotesEntry?
                    if (id != NoteAddingId) {
                        note = notesDao.getById(id).firstOrNull()
                    }
                    else {
                        note = NotesEntry()
                        note.folderId = notesPreferenceHelper.editNoteToAddingFolderId
                        note.createTimeU = Date().time
                    }

                    if (note == null) {
                        showError(
                            message = context.getString(R.string.note_not_found),
                            buttonId = ERROR_CLOSE_B
                        )
                        return@withContext
                    }

                    note.content = newText
                    note.updateTimeU = Date().time
                    notesDao.insertAll(note)

                    withContext(Dispatchers.Main) {
                        goBack()
                    }
                } catch (e: Throwable) {
                    throwIfCancel(e)
                    Log.e(TAG, "$logStr $e", e)

                    withContext(Dispatchers.Main) {
                        showError(
                            error = e,
                            buttonId = ERROR_CLOSE_B
                        )
                    }
                }
            }
        }
    }

    override fun onBackClick() {
        Log.i(TAG, "onBackClick")
        val prevText = notesPreferenceHelper.editNoteText ?: ""

        if (prevText == textS.value.text) {
            goBack()
            return
        }

        showDialogOrMenu(
            Dialog(
                title = context.getString(R.string.exit_without_save_confirm),
                message = "",
                buttons = listOf(
                    DialogButton(
                        title = context.getString(R.string.action_yes),
                        id = EXIT_CONFIRM_YES_B
                    ),
                    DialogButton(
                        title = context.getString(R.string.action_no),
                        id = ""
                    )
                )
            )
        )
    }

    override fun onDialogButtonClick(
        dialog: Dialog,
        dialogButton: DialogButton,
        inputText: String
    ) {
        super.onDialogButtonClick(dialog, dialogButton, inputText)

        when(dialogButton.id){
            EXIT_CONFIRM_YES_B -> {
                goBack()
            }

            ERROR_CLOSE_B -> {
                goBack()
            }
        }
    }

    private fun goBack() {
        isClose = true
        navigator.goBack()
    }

    override fun onAddTimeClick(){
        try {
            val st = textS.value.selection.start
            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            var timestamp = dateFormat.format(Date())
            val nextLine = if (st != 0) {
                "\n"
            }
            else {
                ""
            }
            timestamp = "$nextLine*$timestamp* "
            insertText(timestamp)
        }
        catch (e: Throwable) {
            Log.e(TAG, "onAddTimeClick $e", e)
        }
    }

    private fun insertText(textToInsert: String) {
        try {
            val st = textS.value.selection.start
            val text = StringBuilder(textS.value.text)
            text.insert(st, textToInsert)
            val oldTextValue = textS.value
            textS.value = TextFieldValue(
                text = text.toString(),
                selection =
                    TextRange(
                        start = oldTextValue.selection.start + textToInsert.length,
                        end = oldTextValue.selection.end + textToInsert.length,
                    )
            )
        }
        catch (e: Throwable) {
            Log.e(TAG, "insertText $e", e)
        }
    }

    override fun onInsertCheckedCharClick() {
        insertCheckedCharEx(context.getString(R.string.option_checked_char))
    }

    override fun onInsertUncheckedCharClick() {
        insertCheckedCharEx(context.getString(R.string.option_unchecked_char))
    }

    private fun insertCheckedCharEx(char: String) {
        try {
            val st = textS.value.selection.start
            val end = textS.value.selection.end
            val text = StringBuilder(textS.value.text)

            val ch = context.getString(R.string.option_checked_char)
            val unCh = context.getString(R.string.option_unchecked_char)
            var insertLength = 1
            val stCh = text.getOrNull(st)?.toString()
            val stCh2 = text.getOrNull(st - 1)?.toString()
            if (stCh == ch || stCh == unCh) {
                insertLength = 0
                text.replace(st, st + 1, char)
            } else if (stCh2 == ch || stCh2 == unCh) {
                insertLength = 0
                text.replace(st - 1, st, char)
            } else {
                insertLength = 2
                text.insert(st, "$char ")
            }
            val oldTextValue = textS.value
            textS.value = TextFieldValue(
                text = text.toString(),
                selection =
                    TextRange(
                        start = oldTextValue.selection.start + insertLength,
                        end = oldTextValue.selection.end + insertLength,
                    )
            )
        }
        catch (e: Throwable) {
            Log.e(TAG, "insertCheckedCharEx $e", e)
        }
    }

    override fun onSendNoteClick() {
        Log.i(TAG, "onSendNoteClick")
        (getActivity() as? QDVNotesHomeActivity)?.sendText(
            textS.value.text
        )
    }

    companion object {
        private const val TAG = "NoteEditScreenViewModel"
        private const val EXIT_CONFIRM_YES_B = "EXIT_CONFIRM_YES_B"
        private const val ERROR_CLOSE_B = "ERROR_CLOSE_B"

        suspend fun prepareEdit(context: Context,
                                notesDao: NotesDao,
                                preferenceHelper: NotesPreferenceHelper,
                                noteId: Long) : Result<Boolean> {
            val logStr = "prepareEdit"

            return withContext(Dispatchers.IO) {
                val note = notesDao.getById(noteId).firstOrNull()
                if (note == null) {
                    val eStr = context.getString(R.string.note_not_found)
                    Log.e(TAG, "$logStr $eStr")
                    return@withContext Result.failure(
                        RuntimeException(eStr)
                    )
                }

                preferenceHelper.editNoteId = noteId
                preferenceHelper.editNoteText = note.content
                preferenceHelper.editNoteToAddingFolderId = note.folderId

                return@withContext Result.success(true)
            }
        }

        fun prepareAdding(context: Context,
                                  preferenceHelper: NotesPreferenceHelper,
                                  folderId: Long?) : Result<Boolean> {
            val logStr = "prepareAdding"

            preferenceHelper.editNoteId = NoteAddingId
            preferenceHelper.editNoteText = null
            preferenceHelper.editNoteToAddingFolderId = folderId

            return Result.success(true)
        }
    }
}