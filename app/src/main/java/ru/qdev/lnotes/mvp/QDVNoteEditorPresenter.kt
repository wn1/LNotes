package ru.qdev.lnotes.mvp

import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import moxy.InjectViewState
import ru.qdev.lnotes.*
import ru.qdev.lnotes.db.entity.QDVDbFolder
import ru.qdev.lnotes.db.entity.QDVDbNote
import src.R
import java.util.*

/**
 * Created by Vladimir Kudashov on 27.09.18.
 */

@InjectViewState
class QDVNoteEditorPresenter : QDVMvpDbPresenter <QDVNoteEditorView> () {

    @AnyThread
    private enum class AppError (val stringCode: String) {
        ERROR_GET_NOTE_1 ("2.1"),
        ERROR_GET_NOTE_2 ("2.2"),
        ERROR_SAVE_NOTE_1 ("2.3");

        fun getErrorMessage (): String {
            return String.format(ThisApp.getContext().getString(R.string.error_with_id), stringCode)
        }
    }

    private var note: QDVDbNote? = null
    private var folder: QDVDbFolder? = null
    private val editorState = QDVNoteEditorState()

    val isChangedFlag
        @AnyThread
       get() = editorState.isChangedFlag

    @UiThread
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        afterDatabaseReload()
    }

    @UiThread
    override fun afterDatabaseReload() {
        loadNoteToEditView()
    }

    @UiThread
    override fun beforeDatabaseClose() {

    }

    @AnyThread
    private fun getFolderName(): String {
        if (folder==null) {
            return ThisApp.getContext().getString(R.string.category_unknown)
        }
        return folder!!.label ?: ""
    }

    @UiThread
    private fun loadNoteToEditView() {
        if (editorState.editorMode == QDVNoteEditorState.EditorMode.EDITING) {
            if (editorState.noteId == null) {
                viewState.showErrorToast(AppError.ERROR_GET_NOTE_1.getErrorMessage(),
                        true)
                return
            }
            note = database.getDaoWithIdLong(QDVDbNote::class.java)
                    .queryForId(editorState.noteId)
            if (note == null) {
                viewState.showErrorToast(AppError.ERROR_GET_NOTE_2.getErrorMessage(),
                        true)
                return
            }
        }
        else
        {
            note = QDVDbNote()
        }

        folder = if (editorState.folderId!=null) {
            database.getDaoWithIdLong(QDVDbFolder::class.java).queryForId(editorState.folderId)
        } else {
            null
        }

        viewState.setNoteContent(note!!.content ?: "")
        viewState.setNoteFolderName(getFolderName())
    }

    @UiThread
    fun onNoteContentChange(content: String) {
        if (note == null) {
            return
        }
        note!!.content = content
        viewState.setNoteContent(note!!.content ?: "")
    }

    @AnyThread
    fun onEditorInputChanges() {
        editorState.isChangedFlag = true
    }

    @UiThread
    fun saveNote(): Boolean {
        try {
            if (editorState.editorMode == QDVNoteEditorState.EditorMode.EDITING) {
                note?.updateTime = Date()
                database.getDaoWithIdLong(QDVDbNote::class.java).update(note)
            } else {
                note?.createTime = Date()
                note?.updateTime = note?.createTime
                note?.folderId = editorState.folderId
                database.getDaoWithIdLong(QDVDbNote::class.java).create(note)
                QDVStatisticState.addNotesSuccess(1)
            }
        }
        catch (e: Exception){
            e.printStackTrace()
            viewState.showErrorToast(AppError.ERROR_SAVE_NOTE_1.getErrorMessage(),
                    false)
            return false
        }
        return true
    }
}