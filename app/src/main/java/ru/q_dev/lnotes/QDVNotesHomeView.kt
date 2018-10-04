package ru.q_dev.lnotes

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface QDVNotesHomeView : MvpView {
    @StateStrategyType(SingleStateStrategy::class)
    fun initNotesList(filterByFolderState: QDVFilterByFolderState? = null)

    @StateStrategyType(SingleStateStrategy::class)
    fun initEditNote(note: QDVDbNote)

    @StateStrategyType(SingleStateStrategy::class)
    fun initAddNote(folderIdForAdding: Long?)

    fun setNavigationDrawerFolderEnabled(enabled: Boolean = false)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun goBackFragment()
}