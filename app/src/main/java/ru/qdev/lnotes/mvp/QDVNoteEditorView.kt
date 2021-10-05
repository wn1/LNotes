package ru.qdev.lnotes.mvp

import androidx.annotation.UiThread
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.*

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