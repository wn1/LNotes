package ru.q_dev.lnotes

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

class QDVNavigationDrawerState {
    private val PREFERENCES_NAME = "QDVNavigationDrawerState"
    private val PREFERENCE_NAME_CURRENT_SELECTED_POSITION = "currentSelectedPosition"
    private val PREFERENCE_NAME_USER_LEARNED = "userLearned"

    private fun getPreference(): SharedPreferences {
        return ThisApp.getContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    var selectedFolderOrMenu: QDVDbFolderOrMenuItem? = null

    var currentSelectedPosition: Long?
        get() {
            val preferences = getPreference()
            return if (preferences.contains(PREFERENCE_NAME_CURRENT_SELECTED_POSITION))
                preferences.getLong(PREFERENCE_NAME_CURRENT_SELECTED_POSITION, 0) else null
        }
        set(value) {
            val editor = getPreference().edit()
            if (value!=null) {
                editor.putLong(PREFERENCE_NAME_CURRENT_SELECTED_POSITION, value)
            }
            else
            {
                editor.remove(PREFERENCE_NAME_CURRENT_SELECTED_POSITION)
            }
            editor.apply()
        }

    var isUserLearned: Boolean
    get() {
        return getPreference().getBoolean(PREFERENCE_NAME_USER_LEARNED, true)
    }
    set (value) {
        getPreference().edit().putBoolean(PREFERENCE_NAME_USER_LEARNED, value).apply()
    }


}