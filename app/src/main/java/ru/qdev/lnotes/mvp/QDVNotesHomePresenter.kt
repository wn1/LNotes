package ru.qdev.lnotes.mvp

import android.support.annotation.AnyThread
import android.support.annotation.MainThread
import android.support.annotation.UiThread
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
    }

    @UiThread
    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        doShowNotesList()
    }

    @UiThread
    fun doShowNotesList() {
        viewState.initNotesList()
    }

    @UiThread
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
        stateChanged()
    }

    class DoSelectFolderEvent (val filterType: QDVFilterByFolderState.FilterType,
                               val folder: QDVDbFolder?)
    @Subscribe(threadMode = ThreadMode.MAIN)
    @MainThread
    fun onEvent(event: DoSelectFolderEvent) {
        doSelectFolder(event.filterType, event.folder)
    }

    @UiThread
    fun doEditNote (note: QDVDbNote) {
        state.uiState = QDVNotesHomeState.UiState.EDIT
        viewState.initEditNote(note)
        stateChanged()
    }

    class DoEditNoteEvent (val note: QDVDbNote)
    @Subscribe(threadMode = ThreadMode.MAIN)
    @MainThread
    fun onEvent(event: DoEditNoteEvent) {
        doEditNote(event.note)
    }

    @UiThread
    fun doAddNote (folderIdForAdding: Long?) {
        state.uiState = QDVNotesHomeState.UiState.EDIT
        viewState.initAddNote(folderIdForAdding)
        stateChanged()
    }

    class DoAddNoteEvent(val folderIdForAdding: Long? = null)
    @Subscribe(threadMode = ThreadMode.MAIN)
    @MainThread
    fun onEvent(event: DoAddNoteEvent) {
        doAddNote(event.folderIdForAdding)
    }

    @UiThread
    fun doGoBack () {
        if (state.uiState == QDVNotesHomeState.UiState.EDIT) {
            viewState.goBackFragment()
            viewState.setNavigationDrawerFolderEnabled(true)
            state.uiState = QDVNotesHomeState.UiState.LIST
            stateChanged()
        }
    }

    @UiThread
    private fun stateChanged(){
        if (state.uiState == QDVNotesHomeState.UiState.LIST &&
                QDVStatisticState.isTimeForShowUserRatingQuest()) {
            viewState.showUserRatingQuest()
        }
    }

    class DoGoBackEvent
    @Subscribe(threadMode = ThreadMode.MAIN)
    @MainThread
    fun onEvent(event: DoGoBackEvent) {
        doGoBack()
    }

    @AnyThread
    fun onFolderNameClick() {
        EventBus.getDefault().post(QDVNavigationDrawerPresenter.DoDrawerOpenOrClose())
    }

    @AnyThread
    fun doReloadDb() {
        QDVNavigationDrawerState().selectedFolderOrMenu = null
        EventBus.getDefault().post(QDVMvpDbPresenter.DoCloseDatabase())
        EventBus.getDefault().post(QDVMvpDbPresenter.DoReloadDatabase())
    }

    @AnyThread
    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}