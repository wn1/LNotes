package ru.qdev.lnotes.core.events

import android.content.Context
import androidx.lifecycle.MutableLiveData
import ru.qdev.lnotes.utils.live_data.LiveEvent

class AppEvents(val context: Context) {
    val onNewNoteAdded = MutableLiveData<LiveEvent<Boolean>?>()
}