package ru.qdev.lnotes.mvp

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.AnyThread
import ru.qdev.lnotes.ThisApp
import java.io.Serializable

/**
 * Created by Vladimir Kudashov on 28.09.18.
 */

@AnyThread
class QDVNoteEditorState : Serializable {
    enum class EditorMode { ADDING, EDITING }
    private val PREFERENCES_NAME = "QDVNoteEditorState"

    private val PREFERENCE_NAME_EDITOR_MODE = "editorMode"
    var editorMode: EditorMode = EditorMode.ADDING
        private set

    private val PREFERENCE_NAME_NOTE_ID = "noteId"
    var noteId: Long? = null
        private set

    private val PREFERENCE_NAME_FOLDER_ID = "folderId"
    var folderId: Long? = null
        private set

    private val PREFERENCE_NAME_IS_CHANGED_FLAG = "isChangedFlag"
    var isChangedFlag = false
        set (value) {
            field = value
            getPreference().edit().putBoolean(PREFERENCE_NAME_IS_CHANGED_FLAG, value).apply()
        }

    private fun getPreference(): SharedPreferences {
        return ThisApp.getContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    init {
        val preferences = getPreference()
        editorMode = EditorMode.values()[
                preferences.getInt(PREFERENCE_NAME_EDITOR_MODE, EditorMode.ADDING.ordinal)]

        noteId = if (preferences.contains(PREFERENCE_NAME_NOTE_ID))
            preferences.getLong(PREFERENCE_NAME_NOTE_ID, 0) else null

        folderId = if (preferences.contains(PREFERENCE_NAME_FOLDER_ID))
            preferences.getLong(PREFERENCE_NAME_FOLDER_ID, 0) else null

        isChangedFlag = preferences.getBoolean(PREFERENCE_NAME_IS_CHANGED_FLAG, false)
    }

    private fun saveToPreference() {
        val editor = getPreference().edit()
        editor.putInt(PREFERENCE_NAME_EDITOR_MODE, editorMode.ordinal)

        if (noteId!=null) {
            editor.putLong(PREFERENCE_NAME_NOTE_ID, noteId!!)
        }
        else
        {
            editor.remove(PREFERENCE_NAME_NOTE_ID)
        }

        if (folderId!=null) {
            editor.putLong(PREFERENCE_NAME_FOLDER_ID, folderId!!)
        }
        else
        {
            editor.remove(PREFERENCE_NAME_FOLDER_ID)
        }

        editor.apply()
    }

    fun setState (mode: EditorMode, folderId: Long? = null, noteId: Long? = null) {
        this.editorMode = mode
        this.folderId = folderId
        this.noteId = noteId
        this.isChangedFlag = false
        saveToPreference()
    }
}


