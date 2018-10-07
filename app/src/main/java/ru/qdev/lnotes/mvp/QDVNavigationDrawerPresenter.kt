package ru.qdev.lnotes.mvp

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.j256.ormlite.dao.CloseableIterator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import ru.qdev.lnotes.*
import ru.qdev.lnotes.db.QDVDbDatabase
import ru.qdev.lnotes.db.entity.QDVDbFolderOrMenuItem
import java.util.ArrayList

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

@InjectViewState
class QDVNavigationDrawerPresenter : MvpPresenter<QDVNavigationDrawerView>(){
    private val state: QDVNavigationDrawerState = QDVNavigationDrawerState()

    //Need QDVDbDatabase.release() in onDestroy()
    private var database: QDVDbDatabase = QDVDbDatabase.getAndLock()

    private val itemsAddingToTop = ArrayList<QDVDbFolderOrMenuItem>()
    private var menuItemSelectedByDefault: QDVDbFolderOrMenuItem? = null

    init {
        val context = ThisApp.getContext()
        itemsAddingToTop.add(QDVDbFolderOrMenuItem(
                context.getString(R.string.add_category),
                QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ADDING))
        itemsAddingToTop.add(QDVDbFolderOrMenuItem(
                context.getString(R.string.category_all),
                QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ALL))
        menuItemSelectedByDefault = QDVDbFolderOrMenuItem(
                context.getString(R.string.category_unknown),
                QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_UNKNOWN)
        itemsAddingToTop.add(menuItemSelectedByDefault!!)

        loadFolderList()

        onClickFolderOrMenu(state.selectedFolderOrMenu)

        if (!state.isUserLearned) {
            viewState.setDrawerOpen(true)
        }

        EventBus.getDefault().register(this)
    }

    fun doDrawerOpenOrClose() {
        viewState.setDrawerOpenOrClose()
    }

    class DoDrawerOpenOrClose
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoDrawerOpenOrClose) {
        doDrawerOpenOrClose()
    }

    private fun loadFolderList() {
        viewState.loadFolderList(
                dbIteratotorFoldersQuery(), itemsAddingToTop, state.selectedFolderOrMenu)
    }

    fun dbIteratotorFoldersQuery(): CloseableIterator<QDVDbFolderOrMenuItem> {
        val noteDao =
                database.getDaoWithIdLong(QDVDbFolderOrMenuItem::class.java)
        val queryBuilder = noteDao.queryBuilder()

        queryBuilder.orderByRaw("label")
        return queryBuilder.iterator()
    }

    fun userLearned() {
        state.isUserLearned = true
    }

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

    fun doRemoveFolder(folderOrMenu: QDVDbFolderOrMenuItem) {
        if (folderOrMenu.menuItem != QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY) {
            return
        }
        val noteDao =
                database.getDaoWithIdLong(QDVDbFolderOrMenuItem::class.java)
        noteDao.delete(folderOrMenu)
        if (state.selectedFolderOrMenu?.id == folderOrMenu.id) {
            state.selectedFolderOrMenu = menuItemSelectedByDefault
        }
        loadFolderList()
        EventBus.getDefault().post(QDVNotesHomePresenter.DoSelectFolderEvent(
                QDVFilterByFolderState.FilterType.FOLDER_NOT_SELECTED, null))
    }

    fun doUpdateFolder(folderOrMenu: QDVDbFolderOrMenuItem) {
        if (folderOrMenu.menuItem != QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY) {
            return
        }
        val noteDao =
                database.getDaoWithIdLong(QDVDbFolderOrMenuItem::class.java)
        noteDao.update(folderOrMenu)
        loadFolderList()
    }

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
                return
            }
            QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_UNKNOWN -> {
                state.selectedFolderOrMenu = folderOrMenu
                viewState.setSelectedFolderOrMenu(folderOrMenu)
                EventBus.getDefault().post(QDVNotesHomePresenter.DoSelectFolderEvent(
                        QDVFilterByFolderState.FilterType.FOLDER_NOT_SELECTED, null))
                return
            }
            QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY -> {
                state.selectedFolderOrMenu = folderOrMenu
                viewState.setSelectedFolderOrMenu(folderOrMenu)
                EventBus.getDefault().post(QDVNotesHomePresenter.DoSelectFolderEvent(
                        QDVFilterByFolderState.FilterType.FOLDER, folderOrMenu))
            }
            else -> {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        QDVDbDatabase.release()
        EventBus.getDefault().unregister(this)
    }
}