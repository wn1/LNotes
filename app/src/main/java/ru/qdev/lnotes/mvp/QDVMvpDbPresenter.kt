package ru.qdev.lnotes.mvp

import android.os.Looper
import android.support.annotation.AnyThread
import android.support.annotation.UiThread
import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import ru.qdev.lnotes.db.QDVDbDatabase

/**
 * Created by Vladimir Kudashov on 07.10.18.
 */

@AnyThread
abstract class QDVMvpDbPresenter<T: MvpView> : MvpPresenter<T> () {
    private var databaseObj: QDVDbDatabase? = null

    var database: QDVDbDatabase
        @Synchronized
        @AnyThread
        set(value) {
            databaseObj = value
        }
        @Synchronized
        @AnyThread
        get() {
            if (databaseObj == null) {
                databaseObj = QDVDbDatabase.getAndLock()
            }
            return databaseObj!!
        }

    @AnyThread
    fun doInitDatabase() {
        database
    }

    init {
        EventBus.getDefault().register(this)
    }

    class DoCloseDatabase
    @Subscribe(threadMode = ThreadMode.POSTING)
    @Synchronized
    @AnyThread
    fun onEvent(event: DoCloseDatabase) {
        if (databaseObj!=null) {
            beforeDatabaseClose()
            databaseObj = null
            QDVDbDatabase.release()
        }
    }

    class DoReloadDatabase
    @Subscribe(threadMode = ThreadMode.POSTING)
    @Synchronized
    @AnyThread
    fun onEvent(event: DoReloadDatabase) {
        if (databaseObj!=null) {
            beforeDatabaseClose()
            databaseObj = null
            QDVDbDatabase.release()
        }
        databaseObj = QDVDbDatabase.getAndLock()
        android.os.Handler(Looper.getMainLooper()).post {
            afterDatabaseReload ()
        }
    }

    @AnyThread
    abstract fun beforeDatabaseClose ()

    @UiThread
    abstract fun afterDatabaseReload ()

    @AnyThread
    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        if (databaseObj!=null) {
            beforeDatabaseClose ()
            QDVDbDatabase.release()
        }
        super.onDestroy()
    }
}