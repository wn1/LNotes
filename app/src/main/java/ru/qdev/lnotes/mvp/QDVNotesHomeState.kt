package ru.qdev.lnotes.mvp

import android.support.annotation.AnyThread

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

@AnyThread
class QDVNotesHomeState {
    enum class UiState {LIST, EDIT}
    var uiState = UiState.LIST
}