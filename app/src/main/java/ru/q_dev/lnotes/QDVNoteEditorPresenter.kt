package ru.q_dev.lnotes

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import java.sql.SQLException
import java.util.*

/**
 * Created by Vladimir Kudashov on 27.09.18.
 */

@InjectViewState
class QDVNoteEditorPresenter : MvpPresenter <QDVNoteEditorView> () {

    private enum class AppError (val stringCode: String) {
        ERROR_GET_NOTE_1 ("401"),
        ERROR_GET_NOTE_2 ("402"),
        ERROR_SAVE_NOTE_1 ("403");

        fun getErrorMessage (): String {
            return String.format(ThisApp.getContext().getString(R.string.error_with_id), stringCode)
        }
    }

    private var database: QDVDbDatabase = QDVDbDatabase.getAndLock();
    private var note: QDVDbNote? = null;
    private var folder: QDVDbFolder? = null
    private val editorState = QDVNoteEditorState()

    val isChangedFlag
       get() = editorState.isChangedFlag

    init {
        loadNoteToEditView()
    }

    private fun getFolderName(): String {
        if (folder==null) {
            return ThisApp.getContext().getString(R.string.category_unknown)
        }
        return folder!!.label ?: ""
    }

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

    fun onNoteContentChange(content: String) {
        if (note == null) {
            return
        }
        note!!.content = content
        viewState.setNoteContent(note!!.content ?: "")
    }

    fun onEditorInputChanges() {
        editorState.isChangedFlag = true
    }

    fun saveNote(): Boolean {
        try {
            if (editorState.editorMode == QDVNoteEditorState.EditorMode.EDITING) {
                database.getDaoWithIdLong(QDVDbNote::class.java).update(note)
            } else {
                database.getDaoWithIdLong(QDVDbNote::class.java).create(note)
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

    override fun onDestroy() {
        super.onDestroy()
        QDVDbDatabase.release()
    }
}