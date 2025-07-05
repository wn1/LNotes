package ru.qdev.lnotes.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.AnyThread
import ru.qdev.lnotes.ThisApp

/**
 * Created by Vladimir Kudashov on 08.10.18.
 */

@AnyThread
object QDVStatisticState {
    private val PREFERENCES_NAME = "QDVNoteEditorState"
    private fun getPreference(): SharedPreferences {
        return ThisApp.getContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private val ADDED_NOTES_COUNT_KEY = "addedNotesCount"
    var addedNotesCount: Long = getPreference().getLong(ADDED_NOTES_COUNT_KEY, 0)
        private set (value) {
            field = value
            getPreference().edit().putLong(ADDED_NOTES_COUNT_KEY, value).apply()
        }

    private val SHOW_USER_RATING_QUEST_AFTER_ADDED_NOTES_COUNT = "showUserRatingQuestAfterAddedNotesCount"
    var showUserRatingQuestAfterAddedNotesCount: Long =
            getPreference().getLong(SHOW_USER_RATING_QUEST_AFTER_ADDED_NOTES_COUNT, 10)
        private set (value) {
            field = value
            getPreference().edit()
                    .putLong(SHOW_USER_RATING_QUEST_AFTER_ADDED_NOTES_COUNT, value).apply()
        }

    private val USER_RATING_QUEST_SHOW_NO_NEED_KEY = "userRatingQuestShownNoNeed"
    var userRatingQuestShownNoNeed: Boolean =
            getPreference().getBoolean(USER_RATING_QUEST_SHOW_NO_NEED_KEY, false)
        set (value) {
            field = value
            getPreference().edit().putBoolean(USER_RATING_QUEST_SHOW_NO_NEED_KEY, value).apply()
        }

    fun addNotesSuccess(count: Long) {
        addedNotesCount += count;
    }

    fun addTimeForShowUserRatingQuest(){
        showUserRatingQuestAfterAddedNotesCount = addedNotesCount + 5
    }

    fun isTimeForShowUserRatingQuest(): Boolean {
        if (userRatingQuestShownNoNeed) return false
        return (addedNotesCount >= showUserRatingQuestAfterAddedNotesCount)
    }
}