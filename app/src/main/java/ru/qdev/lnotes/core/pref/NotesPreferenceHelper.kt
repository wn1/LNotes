package ru.qdev.lnotes.core.pref

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import ru.qdev.lnotes.db.entity.QDVDbFolderOrMenuItem
import ru.qdev.lnotes.model.Folder
import ru.qdev.lnotes.model.FolderType

class NotesPreferenceHelper (private val context: Context) {

    private val PREFERENCES_NAME_DRAWER = "QDVNavigationDrawerState"
    private val PREFERENCE_NAME_USER_LEARNED = "userLearned"
    private val PREFERENCE_NAME_SELECTED = "selectedFolderOrMenu"

    private val PREFERENCES_NAME = "NotesPreferences"
    private val PREFERENCE_SELECTED_FOLDER_ID = "PreferenceSelectedFolderId"

    private fun getPreferencesDrawer(): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME_DRAWER, Context.MODE_PRIVATE)
    }

    private fun getPreferences(): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    var selectedFolderOrMenu: QDVDbFolderOrMenuItem? = null
        set(value) {
            if (field == value) {
                return
            }
            field = value
            val editor = getPreferencesDrawer().edit()
            if (value==null) {
                editor.remove(PREFERENCE_NAME_SELECTED).apply()
                return
            }
            val builder = GsonBuilder()
            val gson = builder.create()
            val jsonStr = gson.toJson(value)
            editor.putString(PREFERENCE_NAME_SELECTED, jsonStr).apply()
        }

    init {
        val builder = GsonBuilder()
        val gson = builder.create()
        selectedFolderOrMenu = try {
            gson.fromJson(getPreferencesDrawer().getString(PREFERENCE_NAME_SELECTED, ""),
                QDVDbFolderOrMenuItem::class.java)
        } catch (e: Exception) {
            null
        }
    }

    var isUserLearned: Boolean
        get() {
            return getPreferencesDrawer().getBoolean(PREFERENCE_NAME_USER_LEARNED, false)
        }
        set (value) {
            getPreferencesDrawer().edit().putBoolean(PREFERENCE_NAME_USER_LEARNED, value).apply()
        }

    var selectedFolderId: String?
        get() {
            return getPreferences().getString(PREFERENCE_SELECTED_FOLDER_ID, null)
        }
        set (value) {
            getPreferences().edit().putString(PREFERENCE_SELECTED_FOLDER_ID, value).apply()
        }

    fun saveSelectedFolderToPref(folder: Folder?) {
        if (folder == null) {
            selectedFolderId = null
            return
        }

        val fId = folder.id ?: ""
        val type = folder.type.id
        val folderStr = "$fId||$type"
        selectedFolderId = folderStr
    }

    fun getSelectedFolderFromPref(): Pair<String?, FolderType>? {
        val folderId = selectedFolderId?.split("||")
        if (folderId == null) {
            return null
        } else {
            var fId = folderId.getOrNull(0)
            if (fId?.isEmpty() == true) fId = null
            val type = FolderType.fromId(folderId.getOrNull(1) ?: "")
            if (type == null) return null
            return Pair(fId, type)
        }
    }
}