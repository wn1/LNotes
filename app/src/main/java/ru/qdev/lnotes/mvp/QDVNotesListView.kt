package ru.qdev.lnotes.mvp

import androidx.annotation.UiThread
import com.j256.ormlite.dao.CloseableIterator
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.qdev.lnotes.db.entity.QDVDbNote

/**
 * Created by Vladimir Kudashov on 29.09.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
@UiThread
interface QDVNotesListView : MvpView {
    fun loadNotesList(dbIterator: CloseableIterator<QDVDbNote>)
    fun setSearchState(searchState: QDVSearchState)
    fun setFolderName(folderName: String)
    fun setFabVisible(visible: Boolean)
}