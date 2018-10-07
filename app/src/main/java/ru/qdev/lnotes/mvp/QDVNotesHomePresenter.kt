package ru.qdev.lnotes.mvp

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import ru.qdev.lnotes.db.entity.QDVDbFolder
import ru.qdev.lnotes.db.entity.QDVDbNote

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

@InjectViewState
class QDVNotesHomePresenter : MvpPresenter <QDVNotesHomeView> () {
    private val state: QDVNotesHomeState = QDVNotesHomeState()
    init {
        EventBus.getDefault().register(this)
        doShowNotesList()
    }

    fun doShowNotesList() {
        viewState.initNotesList()
    }

    fun doSelectFolder (filterType: QDVFilterByFolderState.FilterType, folder: QDVDbFolder?) {
        val filterState = QDVFilterByFolderState()
        filterState.filterType = filterType
        if (filterType == QDVFilterByFolderState.FilterType.FOLDER_ID) {
            filterState.folderId = folder?.id
        } else {
            filterState.folder = folder
        }

        state.uiState = QDVNotesHomeState.UiState.LIST
        viewState.initNotesList(filterState)
    }

    class DoSelectFolderEvent (val filterType: QDVFilterByFolderState.FilterType,
                               val folder: QDVDbFolder?)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoSelectFolderEvent) {
        doSelectFolder(event.filterType, event.folder)
    }

    fun doEditNote (note: QDVDbNote) {
        state.uiState = QDVNotesHomeState.UiState.EDIT
        viewState.initEditNote(note)
    }

    class DoEditNoteEvent (val note: QDVDbNote)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoEditNoteEvent) {
        doEditNote(event.note)
    }

    fun doAddNote (folderIdForAdding: Long?) {
        state.uiState = QDVNotesHomeState.UiState.EDIT
        viewState.initAddNote(folderIdForAdding)
    }

    class DoAddNoteEvent(val folderIdForAdding: Long? = null)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoAddNoteEvent) {
        doAddNote(event.folderIdForAdding)
    }

    fun doGoBack () {
        if (state.uiState == QDVNotesHomeState.UiState.EDIT) {
            viewState.goBackFragment()
            viewState.setNavigationDrawerFolderEnabled(true)
            state.uiState = QDVNotesHomeState.UiState.LIST
        }
    }

    class DoGoBackEvent
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoGoBackEvent) {
        doGoBack()
    }

    fun onFolderNameClick() {
        EventBus.getDefault().post(QDVNavigationDrawerPresenter.DoDrawerOpenOrClose())
    }

    fun doReloadDb() {
        QDVNavigationDrawerState().selectedFolderOrMenu = null
        EventBus.getDefault().post(QDVMvpDbPresenter.DoCloseDatabase())
        EventBus.getDefault().post(QDVMvpDbPresenter.DoReloadDatabase())
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}