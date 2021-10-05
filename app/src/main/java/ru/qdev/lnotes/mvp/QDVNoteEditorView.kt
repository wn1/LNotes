package ru.qdev.lnotes.mvp

import androidx.annotation.UiThread
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.SingleStateStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * Created by Vladimir Kudashov on 27.09.18.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
@UiThread
interface QDVNoteEditorView : MvpView
{
    @StateStrategyType(SingleStateStrategy::class)
    fun initEditorInMode(mode: QDVNoteEditorState.EditorMode)

    fun setNoteContent(content: String)

    fun setNoteFolderName(folderName: String)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showErrorToast(message: String, needExitFromEditor: Boolean = false)
}