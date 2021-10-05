package ru.qdev.lnotes.mvp

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import com.arellomobile.mvp.InjectViewState
import com.j256.ormlite.dao.CloseableIterator
import com.j256.ormlite.stmt.Where
import ru.qdev.lnotes.*
import ru.qdev.lnotes.db.entity.QDVDbFolderOrMenuItem
import ru.qdev.lnotes.db.entity.QDVDbNote
import java.util.*

/**
 * Created by Vladimir Kudashov on 29.09.18.
 */
@InjectViewState
class QDVNotesListPresenter : QDVMvpDbPresenter <QDVNotesListView> () {
    private var state: QDVNotesListState = QDVNotesListState()

    @AnyThread
    override fun beforeDatabaseClose() {

    }

    @UiThread
    override fun afterDatabaseReload() {

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
    fun dbIteratorNotesQuery(): CloseableIterator<QDVDbNote> {
        val noteDao = database.getDaoWithIdLong(QDVDbNote::class.java)
        val queryBuilder = noteDao.queryBuilder()
        val filter = state.filterByFolderState

        var where: Where<QDVDbNote, Long>? = null
        if (filter.filterType == QDVFilterByFolderState.FilterType.FOLDER_NOT_SELECTED) {
            val columnName = "folder_id"
            where = queryBuilder.where().isNull(columnName).or().le(columnName, 0)
        } else {
            val columnName = "folder_id"
            if (filter.filterType == QDVFilterByFolderState.FilterType.FOLDER_ID
                    && state.filterByFolderState.folderId != null) {
                where = queryBuilder.where().eq(columnName, state.filterByFolderState.folderId)
            } else if (filter.filterType == QDVFilterByFolderState.FilterType.FOLDER
                    && state.filterByFolderState.folder != null) {
                where = queryBuilder.where().eq(columnName,
                        state.filterByFolderState.folder?.id ?: 0)
            }
        }
        if (state.searchState.isSearchActive && !state.searchState.searchText.isNullOrEmpty()) {
            val columnName = "content"
            where = if (where != null) {
                where.and()
            } else {
                queryBuilder.where()
            }
            where.like(columnName, "%${state.searchState.searchText}%")
        }

        val oderString = "(isready > 0), complete_time_u DESC, update_time_u DESC"
        queryBuilder.orderByRaw(oderString)
        return queryBuilder.iterator()
    }

    @AnyThread
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

    private var isReloadState: Boolean = false

    @MainThread
    fun loadNotesListAsync() {
        if (isReloadState) {
            return
        }
        isReloadState = true
        AsyncTask.execute {
            try {
                val iteratorNotes = dbIteratorNotesQuery()
                Handler(Looper.getMainLooper()).post {
                    isReloadState = false
                    viewState.loadNotesList(iteratorNotes)
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).postDelayed({
                    loadNotesListAsync()
                }, 1000)
                e.printStackTrace()
                return@execute
            }
        }
    }

    @UiThread
    fun initWithState(state: QDVNotesListState) {
        this.state = state
        if (state.filterByFolderState.filterType == QDVFilterByFolderState.FilterType.FOLDER_ID) {
            val noteDao =
                    database.getDaoWithIdLong(QDVDbFolderOrMenuItem::class.java)
            state.filterByFolderState.folder =
                    noteDao.queryForId(state.filterByFolderState.folderId ?: 0)
        }
        loadNotesListAsync()
        viewState.setSearchState(state.searchState)
        viewState.setFolderName(getFolderNameForFilter())
    }

    @UiThread
    fun onSearchText(text: String) {
        state.searchState.isSearchActive = true
        state.searchState.searchText = text
        loadNotesListAsync()
        viewState.setSearchState(state.searchState)
    }

    @UiThread
    fun onUndoSearch() {
        state.searchState.isSearchActive = false
        loadNotesListAsync()
        viewState.setSearchState(state.searchState)
    }

    @UiThread
    fun doUpdateNote(note: QDVDbNote) {
        database.getDaoWithIdLong(QDVDbNote::class.java).update(note)
        loadNotesListAsync()
    }

    @UiThread
    fun doDeleteNote(note: QDVDbNote) {
        database.getDaoWithIdLong(QDVDbNote::class.java).delete(note)
        loadNotesListAsync()
    }

    @UiThread
    fun doSetStatusOfExecution(note: QDVDbNote, status: QDVDbNote.StatusOfExecution) {
        note.statusOfExecution = status
        if (status!=QDVDbNote.StatusOfExecution.CREATED){
            note.completeTime = Date()
        }
        else
        {
            note.completeTime = null
        }
        database.getDaoWithIdLong(QDVDbNote::class.java).update(note)
        loadNotesListAsync()
    }

    @UiThread
    fun doFabVisible(visible: Boolean) {
        viewState.setFabVisible(visible)
    }
}