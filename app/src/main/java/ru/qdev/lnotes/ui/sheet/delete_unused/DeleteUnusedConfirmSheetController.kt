package ru.qdev.lnotes.ui.sheet.delete_unused

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import ru.qdev.lnotes.db.enum.StatusOfExecution
import ru.qdev.lnotes.ui.screen.base.BaseScreenViewModel
import ru.qdev.lnotes.ui.sheet.delete_unused.model.SelectedStatus
import ru.qdev.lnotes.ui.view.dialog.base.DialogControllerBase

interface DeleteUnusedConfirmSheetControllerListener {
    fun onCancelClick()
    fun onConfirmClick()
    fun onStatusClick(status: SelectedStatus)
    fun onMonthValueChange(value: TextFieldValue)
}

class DeleteUnusedConfirmSheetController(parentViewModel: BaseScreenViewModel) :
    DialogControllerBase<Boolean>(parentViewModel), DeleteUnusedConfirmSheetControllerListener {
    val statusListS = mutableStateListOf<SelectedStatus>()
    val monthInputValueS = mutableStateOf(TextFieldValue())

    private val TAG = this.javaClass.simpleName

    override fun show(init: Boolean) {
        super.show(init)
        fillStatuses()
    }

    override fun onCancelClick() {
        hide()
    }

    private fun fillStatuses() {
        statusListS.addAll(
            makeDefStatuses()
        )
    }

    private fun fillMonths() {
        monthInputValueS.value = TextFieldValue("36")
    }

    override fun onConfirmClick() {
        TODO("Not yet implemented")
    }

    override fun onMonthValueChange(value: TextFieldValue) {
        val v = value.text.toIntOrNull()
        if (v != null || value.text.isEmpty()) {
            monthInputValueS.value = value
        }
    }

    override fun onStatusClick(status: SelectedStatus) {
        Log.i(TAG, "onStatusClick: $status")

        val index = statusListS.indexOf(status)
        if (index < 0) return
        statusListS[index] = status.copy(isSelected = !status.isSelected)
    }

    companion object {
        fun makeDefStatuses() : List<SelectedStatus>{
            return StatusOfExecution.entries.map {
                return@map SelectedStatus(
                    status = it,
                    isSelected = it == StatusOfExecution.NOT_NEED
                            || it == StatusOfExecution.COMPLETED
                )
            }
        }
    }
}