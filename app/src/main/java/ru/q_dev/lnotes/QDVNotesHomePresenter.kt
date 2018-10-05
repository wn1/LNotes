package ru.q_dev.lnotes

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

@InjectViewState
class QDVNotesHomePresenter : MvpPresenter <QDVNotesHomeView> () {
    private val state: QDVNotesHomeState = QDVNotesHomeState()
    init {
        viewState.initNotesList()
        viewState.setNavigationDrawerFolderEnabled(true)
        EventBus.getDefault().register(this)
    }

    fun doSelectFolder (folderId: Long?) {
        val filterState = QDVFilterByFolderState();
        when {
            folderId == null -> {
                filterState.filterType = QDVFilterByFolderState.FilterType.FOLDER_NOT_SELECTED
            }
            folderId > 0 -> {
                filterState.filterType = QDVFilterByFolderState.FilterType.FOLDER_ID
                filterState.folderId = folderId
            }
            folderId == QDVNotesHomeActivity.action_categories_all_id.toLong() -> {
                filterState.filterType = QDVFilterByFolderState.FilterType.ALL_FOLDER
            }
            else -> {
                filterState.filterType = QDVFilterByFolderState.FilterType.FOLDER_NOT_SELECTED
            }
        }

        state.uiState = QDVNotesHomeState.UiState.LIST
        viewState.initNotesList(filterState)
        viewState.setNavigationDrawerFolderEnabled(true)
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
        viewState.setNavigationDrawerFolderEnabled(true)
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
        viewState.setNavigationDrawerFolderEnabled(false)
    }

    class DoEditNoteEvent (val note: QDVDbNote)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoEditNoteEvent) {
        doEditNote(event.note)
    }

    fun doAddNote (folderIdForAdding: Long?) {
        state.uiState = QDVNotesHomeState.UiState.EDIT
        viewState.initAddNote(folderIdForAdding)
        viewState.setNavigationDrawerFolderEnabled(false)
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}