package ru.qdev.lnotes.core.events

import android.content.Context
import androidx.annotation.AnyThread
import dagger.hilt.android.qualifiers.ApplicationContext
import org.greenrobot.eventbus.EventBus
import ru.qdev.lnotes.mvp.QDVMvpDbPresenter
import ru.qdev.lnotes.mvp.QDVNavigationDrawerState

class DbManager (val context: Context) {

    @AnyThread
    fun doReloadDb() {
        QDVNavigationDrawerState().selectedFolderOrMenu = null
        EventBus.getDefault().post(QDVMvpDbPresenter.DoCloseDatabase())
        EventBus.getDefault().post(QDVMvpDbPresenter.DoReloadDatabase())
    }

}