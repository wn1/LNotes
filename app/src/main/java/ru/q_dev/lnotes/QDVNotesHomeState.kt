package ru.q_dev.lnotes

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

class QDVNotesHomeState {
    enum class UiState {LIST, EDIT}
    var uiState = UiState.LIST
}