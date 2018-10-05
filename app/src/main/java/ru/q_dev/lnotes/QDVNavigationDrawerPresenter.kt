package ru.q_dev.lnotes

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.j256.ormlite.dao.CloseableIterator
import org.greenrobot.eventbus.EventBus
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

        if (!state.isUserLearned) {
            viewState.setDrawerOpen(true)
        }
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
        state.isUserLearned = true;
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
            return;
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
            return;
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
    }
}