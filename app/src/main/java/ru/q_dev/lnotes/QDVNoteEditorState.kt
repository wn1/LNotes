package ru.q_dev.lnotes

import android.content.Context
import android.content.SharedPreferences
import java.io.Serializable

/**
 * Created by Vladimir Kudashov on 28.09.18.
 */

class QDVNoteEditorState : Serializable {
    enum class EditorMode { ADDING, EDITING }
    private val preferencesName = "QDVNoteEditorState"

    private val preferenceNameEditorMode = "editorMode"
    var editorMode: EditorMode = EditorMode.ADDING
        private set

    private val preferenceNameNoteId = "noteId"
    var noteId: Long? = null
        private set

    private val preferenceNameFolderId = "folderId"
    var folderId: Long? = null
        private set

    private val preferenceNameIsChangedFlag = "isChangedFlag"
    var isChangedFlag = false
        set (value) {
            field = value
            getPreference().edit().putBoolean(preferenceNameIsChangedFlag, value).apply()
        }

    private fun getPreference(): SharedPreferences {
        return ThisApp.getContext().getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    init {
        val preferences = getPreference()
        editorMode = EditorMode.values()[
                preferences.getInt(preferenceNameEditorMode, EditorMode.ADDING.ordinal)]

        noteId = if (preferences.contains(preferenceNameNoteId))
            preferences.getLong(preferenceNameNoteId, 0) else null

        folderId = if (preferences.contains(preferenceNameFolderId))
            preferences.getLong(preferenceNameFolderId, 0) else null

        isChangedFlag = preferences.getBoolean(preferenceNameIsChangedFlag, false)
    }

    private fun saveToPreference() {
        val editor = getPreference().edit()
        editor.putInt(preferenceNameEditorMode, editorMode.ordinal)

        if (noteId!=null) {
            editor.putLong(preferenceNameNoteId, noteId!!)
        }
        else
        {
            editor.remove(preferenceNameNoteId)
        }

        if (folderId!=null) {
            editor.putLong(preferenceNameFolderId, folderId!!)
        }
        else
        {
            editor.remove(preferenceNameFolderId)
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


