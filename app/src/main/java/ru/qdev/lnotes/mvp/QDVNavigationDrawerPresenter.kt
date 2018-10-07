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
class QDVNavigationDrawerPresenter : QDVMvpDbPresenter <QDVNavigationDrawerView> (){
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

        onDatabaseReload()
    }

    override fun onDatabaseReload() {
        state = QDVNavigationDrawerState()
        loadFolderList()
        onClickFolderOrMenu(state.selectedFolderOrMenu)
        if (!state.isUserLearned) {
            viewState.setDrawerOpen(true)
        }
        onClickFolderOrMenu(state.selectedFolderOrMenu)
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
        if (state.selectedFolderOrMenu == null) {
            state.selectedFolderOrMenu = menuItemFolderAll
        }
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
            state.selectedFolderOrMenu = menuItemFolderUnknown
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
        if (state.selectedFolderOrMenu?.menuItem==QDVDbFolderOrMenuItem.MenuItemMarker.FOLDER_ENTITY &&
                folderOrMenu.id == state.selectedFolderOrMenu?.id) {
            EventBus.getDefault().post(QDVNotesHomePresenter.DoSelectFolderEvent(
                    QDVFilterByFolderState.FilterType.FOLDER, folderOrMenu))
        }
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
    }
}