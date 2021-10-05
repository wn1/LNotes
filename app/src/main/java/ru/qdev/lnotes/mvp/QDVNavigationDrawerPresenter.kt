package ru.qdev.lnotes.mvp

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import com.arellomobile.mvp.InjectViewState
import com.j256.ormlite.dao.CloseableIterator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import ru.qdev.lnotes.*
import ru.qdev.lnotes.db.entity.QDVDbFolderOrMenuItem
import java.util.ArrayList

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

@InjectViewState
class QDVNavigationDrawerPresenter : QDVMvpDbPresenter <QDVNavigationDrawerView> () {
    private var state: QDVNavigationDrawerState = QDVNavigationDrawerState()

    private val itemsAddingToTop = ArrayList<QDVDbFolderOrMenuItem>()
    private var menuItemFolderAll: QDVDbFolderOrMenuItem? = null
    private var menuItemFolderUnknown: QDVDbFolderOrMenuItem? = null

    init {
        val context = ThisApp.getContext()
        itemsAddingToTop.add(QDVDbFolderOrMenuItem(
                context.getString(R.string.add_category),
                QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ADDING))
        menuItemFolderAll =  QDVDbFolderOrMenuItem(
                context.getString(R.string.category_all),
                QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ALL)
        itemsAddingToTop.add(menuItemFolderAll!!)
        menuItemFolderUnknown = QDVDbFolderOrMenuItem(
                context.getString(R.string.category_unknown),
                QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_UNKNOWN)
        itemsAddingToTop.add(menuItemFolderUnknown!!)
    }

    @UiThread
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        afterDatabaseReload()
    }

    @AnyThread
    override fun beforeDatabaseClose() {

    }

    @UiThread
    override fun afterDatabaseReload() {
        state = QDVNavigationDrawerState()
        loadFolderList()
        onClickFolderOrMenu(state.selectedFolderOrMenu)
    }

    @UiThread
    fun doDrawerShowIfUserLearn() {
        if (!state.isUserLearned) {
            viewState.setDrawerOpen(true)
        }
    }

    @UiThread
    fun doDrawerOpenOrClose() {
        viewState.switchDrawerOpenOrClose()
    }

    class DoDrawerOpenOrClose
    @Subscribe(threadMode = ThreadMode.MAIN)
    @MainThread
    fun onEvent(event: DoDrawerOpenOrClose) {
        doDrawerOpenOrClose()
    }

    @UiThread
    private fun loadFolderList() {
        if (state.selectedFolderOrMenu == null) {
            state.selectedFolderOrMenu = menuItemFolderAll
        }
        AsyncTask.execute {
            val iteratorFolders = dbIteratotorFoldersQuery()
            Handler(Looper.getMainLooper()).post {
                viewState.loadFolderList(
                        iteratorFolders, itemsAddingToTop, state.selectedFolderOrMenu)
            }
        }
    }

    @AnyThread
    fun dbIteratotorFoldersQuery(): CloseableIterator<QDVDbFolderOrMenuItem> {
        val noteDao =
                database.getDaoWithIdLong(QDVDbFolderOrMenuItem::class.java)
        val queryBuilder = noteDao.queryBuilder()

        queryBuilder.orderByRaw("label")
        return queryBuilder.iterator()
    }

    @AnyThread
    fun userLearned() {
        state.isUserLearned = true
    }

    @UiThread
    fun doAddFolder(folderName: String) {
        val noteDao =
                database.getDaoWithIdLong(QDVDbFolderOrMenuItem::class.java)
        val folder = QDVDbFolderOrMenuItem(folderName,
                QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY)
        noteDao.create(folder)
        state.selectedFolderOrMenu = folder
        loadFolderList()
        EventBus.getDefault().post(QDVNotesHomePresenter.DoSelectFolderEvent(
                QDVFilterByFolderState.FilterType.FOLDER, folder))
    }

    @UiThread
    fun doRemoveFolder(folderOrMenu: QDVDbFolderOrMenuItem) {
        if (folderOrMenu.menuItem != QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY) {
            return
        }
        val noteDao =
                database.getDaoWithIdLong(QDVDbFolderOrMenuItem::class.java)
        noteDao.delete(folderOrMenu)
        if (state.selectedFolderOrMenu?.id == folderOrMenu.id) {
            state.selectedFolderOrMenu = menuItemFolderUnknown
        }
        loadFolderList()
        EventBus.getDefault().post(QDVNotesHomePresenter.DoSelectFolderEvent(
                QDVFilterByFolderState.FilterType.FOLDER_NOT_SELECTED, null))
    }

    @UiThread
    fun doUpdateFolder(folderOrMenu: QDVDbFolderOrMenuItem) {
        if (folderOrMenu.menuItem != QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY) {
            return
        }
        val noteDao =
                database.getDaoWithIdLong(QDVDbFolderOrMenuItem::class.java)
        noteDao.update(folderOrMenu)
        loadFolderList()
        if (state.selectedFolderOrMenu?.menuItem==QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY &&
                folderOrMenu.id == state.selectedFolderOrMenu?.id) {
            EventBus.getDefault().post(QDVNotesHomePresenter.DoSelectFolderEvent(
                    QDVFilterByFolderState.FilterType.FOLDER, folderOrMenu))
        }
    }

    @UiThread
    fun onClickFolderOrMenu(folderOrMenu: QDVDbFolderOrMenuItem?) {
        when (folderOrMenu?.menuItem) {
            QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ADDING -> {
                viewState.onClickAddFolder()
                return
            }
            QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ALL -> {
                state.selectedFolderOrMenu = folderOrMenu
                viewState.setSelectedFolderOrMenu(folderOrMenu)
                EventBus.getDefault().post(QDVNotesHomePresenter.DoSelectFolderEvent(
                        QDVFilterByFolderState.FilterType.ALL_FOLDER,
                        null))
                viewState.setDrawerOpen(false)
                return
            }
            QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_UNKNOWN -> {
                state.selectedFolderOrMenu = folderOrMenu
                viewState.setSelectedFolderOrMenu(folderOrMenu)
                EventBus.getDefault().post(QDVNotesHomePresenter.DoSelectFolderEvent(
                        QDVFilterByFolderState.FilterType.FOLDER_NOT_SELECTED, null))
                viewState.setDrawerOpen(false)
                return
            }
            QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY -> {
                state.selectedFolderOrMenu = folderOrMenu
                viewState.setSelectedFolderOrMenu(folderOrMenu)
                EventBus.getDefault().post(QDVNotesHomePresenter.DoSelectFolderEvent(
                        QDVFilterByFolderState.FilterType.FOLDER, folderOrMenu))
                viewState.setDrawerOpen(false)
            }
            else -> {
            }
        }
    }
}