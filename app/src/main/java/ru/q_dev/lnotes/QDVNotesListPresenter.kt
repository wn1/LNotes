package ru.q_dev.lnotes

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.j256.ormlite.dao.CloseableIterator
import com.j256.ormlite.stmt.Where

/**
 * Created by Vladimir Kudashov on 29.09.18.
 */
@InjectViewState
class QDVNotesListPresenter : MvpPresenter <QDVNotesListView> () {
    var database: QDVDbDatabase = QDVDbDatabase.getAndLock()
    var state: QDVNotesListState = QDVNotesListState()

    fun queryNotes():  CloseableIterator<QDVDbNote> {
        val noteDao = database.getDaoWithIdLong(QDVDbNote::class.java)
        val queryBuilder = noteDao.queryBuilder()
        val filter = state.filterByFolderState;

        var where : Where<QDVDbNote, Long>? = null
        if (filter.filterType == QDVFilterByFolderState.FilterType.FOLDER_NOT_SELECTED) {
            val columnName = "folder_id";
            where = queryBuilder.where().isNull(columnName).or().le(columnName, 0)
        } else {
            val columnName = "folder_id";
            if (filter.filterType == QDVFilterByFolderState.FilterType.FOLDER_ID
                    && state.filterByFolderState.folderId!=null) {
                where = queryBuilder.where().eq(columnName, state.filterByFolderState.folderId)
            }
            else if (filter.filterType == QDVFilterByFolderState.FilterType.FOLDER
                    && state.filterByFolderState.folder!=null) {
                where = queryBuilder.where().eq(columnName, state.filterByFolderState.folder?.id ?: 0)
            }
        }
        if (state.searchState.isSearchActive && !state.searchState.searchText.isNullOrEmpty()) {
            val columnName = "content";
            where = if (where != null) {
                where.and()
            } else {
                queryBuilder.where()
            }
            where.like(columnName, "%${state.searchState.searchText}%")
        }

        return queryBuilder.iterator()
    }

    fun getFolderNameForFilter(): String {
        val context = ThisApp.getContext()
        return when (state.filterByFolderState.filterType) {
            QDVFilterByFolderState.FilterType.ALL_FOLDER -> {
                context.getString(R.string.category_all)
            }
            QDVFilterByFolderState.FilterType.FOLDER_NOT_SELECTED -> {
                context.getString(R.string.category_unknown)
            }
            else -> {
                state.filterByFolderState.folder?.label ?: ""
            }
        }
    }

    fun initWithState(state: QDVNotesListState) {
        this.state = state
        if (state.filterByFolderState.filterType==QDVFilterByFolderState.FilterType.FOLDER_ID) {
            val noteDao = database.getDaoWithIdLong(QDVDbFolder::class.java)
            state.filterByFolderState.folder = noteDao.queryForId(state.filterByFolderState.folderId ?: 0)
        }
        viewState.loadNotesList(queryNotes())
        viewState.setSearchState(state.searchState)
        viewState.setFolderName(getFolderNameForFilter())
    }

    fun onSearchText(text: String) {
        state.searchState.isSearchActive = true
        state.searchState.searchText = text
        viewState.loadNotesList(queryNotes())
        viewState.setSearchState(state.searchState)
    }

    fun onUndoSearch() {
        state.searchState.isSearchActive = false
        viewState.loadNotesList(queryNotes())
        viewState.setSearchState(state.searchState)
    }
}