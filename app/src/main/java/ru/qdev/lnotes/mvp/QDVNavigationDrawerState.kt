package ru.qdev.lnotes.mvp

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.AnyThread
import com.google.gson.GsonBuilder
import ru.qdev.lnotes.ThisApp
import ru.qdev.lnotes.db.entity.QDVDbFolderOrMenuItem

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */
@AnyThread
class QDVNavigationDrawerState {
    private val PREFERENCES_NAME = "QDVNavigationDrawerState"
    private val PREFERENCE_NAME_USER_LEARNED = "userLearned"
    private val PREFERENCE_NAME_SELECTED = "selectedFolderOrMenu"

    private fun getPreference(): SharedPreferences {
        return ThisApp.getContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    var selectedFolderOrMenu: QDVDbFolderOrMenuItem? = null
    set(value) {
        if (field == value) {
            return
        }
        field = value
        val editor = getPreference().edit()
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
            gson.fromJson(getPreference().getString(PREFERENCE_NAME_SELECTED, ""),
                    QDVDbFolderOrMenuItem::class.java)
        } catch (e: Exception) {
            null
        }
    }

    var isUserLearned: Boolean
    get() {
        return getPreference().getBoolean(PREFERENCE_NAME_USER_LEARNED, false)
    }
    set (value) {
        getPreference().edit().putBoolean(PREFERENCE_NAME_USER_LEARNED, value).apply()
    }


}