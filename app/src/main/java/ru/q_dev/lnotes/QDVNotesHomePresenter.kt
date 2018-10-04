package ru.q_dev.lnotes

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

@InjectViewState
class QDVNotesHomePresenter : MvpPresenter <QDVNotesHomeView> () {
    private val state: QDVNotesHomeState = QDVNotesHomeState()
    init {
        viewState.initNotesList()
        viewState.setNavigationDrawerFolderEnabled(true)
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

    fun doEditNote (note: QDVDbNote) {
        state.uiState = QDVNotesHomeState.UiState.EDIT
        viewState.initEditNote(note)
        viewState.setNavigationDrawerFolderEnabled(false)
    }

    fun doAddNote (folderIdForAdding: Long?) {
        state.uiState = QDVNotesHomeState.UiState.EDIT
        viewState.initAddNote(folderIdForAdding)
        viewState.setNavigationDrawerFolderEnabled(false)
    }

    fun doGoBack () {
        if (state.uiState == QDVNotesHomeState.UiState.EDIT) {
            viewState.goBackFragment()
            viewState.setNavigationDrawerFolderEnabled(true)
            state.uiState = QDVNotesHomeState.UiState.LIST
        }
    }
}