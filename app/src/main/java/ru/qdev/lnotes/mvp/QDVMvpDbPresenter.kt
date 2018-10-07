package ru.qdev.lnotes.mvp

import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import ru.qdev.lnotes.db.QDVDbDatabase

/**
 * Created by Vladimir Kudashov on 07.10.18.
 */

abstract class QDVMvpDbPresenter<T: MvpView> : MvpPresenter<T> () {
    private var databaseObj: QDVDbDatabase? = null

    var database: QDVDbDatabase
        @Synchronized
        set(value) {
            databaseObj = value
        }
        @Synchronized
        get() {
            if (databaseObj == null) {
                databaseObj = QDVDbDatabase.getAndLock()
            }
            return databaseObj!!
        }

    init {
        EventBus.getDefault().register(this)
    }

    class DoCloseDatabase
    @Subscribe(threadMode = ThreadMode.POSTING)
    @Synchronized
    fun onEvent(event: DoCloseDatabase) {
        if (databaseObj!=null) {
            databaseObj = null
            QDVDbDatabase.release()
        }
    }

    class DoReloadDatabase
    @Subscribe(threadMode = ThreadMode.POSTING)
    @Synchronized
    fun onEvent(event: DoReloadDatabase) {
        if (databaseObj!=null) {
            databaseObj = null
            QDVDbDatabase.release()
        }
        databaseObj = QDVDbDatabase.getAndLock()
        onDatabaseReload ()
    }

    abstract fun onDatabaseReload ()

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        if (databaseObj!=null) {
            QDVDbDatabase.release()
        }
        super.onDestroy()
    }
}