package ru.q_dev.lnotes

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.j256.ormlite.dao.CloseableIterator

/**
 * Created by Vladimir Kudashov on 29.09.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface QDVNotesListView : MvpView {
    fun loadNotesList(dbIterator: CloseableIterator<QDVDbNote>)
    fun setSearchState(searchState: QDVSearchState)
    fun setFolderName(folderName: String)
}