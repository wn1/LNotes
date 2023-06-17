package ru.qdev.lnotes.mvp

import androidx.annotation.UiThread
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.qdev.lnotes.db.entity.QDVDbNote

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
@UiThread
interface QDVNotesHomeView : MvpView {
    @StateStrategyType(SingleStateStrategy::class)
    fun initNotesList(filterByFolderState: QDVFilterByFolderState? = null)

    @StateStrategyType(SingleStateStrategy::class)
    fun initEditNote(note: QDVDbNote)

    @StateStrategyType(SingleStateStrategy::class)
    fun initAddNote(folderIdForAdding: Long?)

    fun setNavigationDrawerFolderEnabled(enabled: Boolean = false)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showUserRatingQuest()
}