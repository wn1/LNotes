package ru.qdev.lnotes.mvp

import android.support.annotation.UiThread
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.*
import com.j256.ormlite.dao.CloseableIterator
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