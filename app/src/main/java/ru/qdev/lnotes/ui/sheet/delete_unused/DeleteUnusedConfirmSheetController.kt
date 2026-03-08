package ru.qdev.lnotes.ui.sheet.delete_unused

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import ru.qdev.lnotes.db.enum.StatusOfExecution
import ru.qdev.lnotes.ui.screen.base.BaseScreenViewModel
import ru.qdev.lnotes.ui.sheet.delete_unused.model.ConfirmedData
import ru.qdev.lnotes.ui.sheet.delete_unused.model.SelectedStatus
import ru.qdev.lnotes.ui.view.dialog.Dialog
import ru.qdev.lnotes.ui.view.dialog.base.DialogControllerBase
import src.R

interface DeleteUnusedConfirmSheetControllerListener {
    fun onCancelClick()
    fun onConfirmClick()
    fun onStatusClick(status: SelectedStatus)
    fun onDaysValueChange(value: TextFieldValue)
}

class DeleteUnusedConfirmSheetController(parentViewModel: BaseScreenViewModel) :
    DialogControllerBase<Boolean>(parentViewModel), DeleteUnusedConfirmSheetControllerListener {
    val statusListS = mutableStateListOf<SelectedStatus>()
    val daysInputValueS = mutableStateOf(TextFieldValue())

    var onConfirmed: (ConfirmedData) -> Unit = {}

    private val TAG = this.javaClass.simpleName

    override fun show(init: Boolean) {
        super.show(init)
        fillStatuses()
        fillDays()
    }

    override fun onCancelClick() {
        hide()
    }

    private fun fillStatuses() {
        statusListS.addAll(
            makeDefStatuses()
        )
    }

    private fun fillDays() {
        val days = 365 * 3
        daysInputValueS.value = TextFieldValue(days.toString())
    }

    override fun onConfirmClick() {
        val logStr = "onConfirmClick"
        Log.i(TAG, logStr)

        val statuses = statusListS.filter { it.isSelected }.map {
            it.status
        }

        val dayCount = daysInputValueS.value.text.toIntOrNull()

        if (statuses.isEmpty()) {
            parentViewModel.showDialogOrMenu(
                Dialog.makeMessage(
                    context = provideContext(),
                    title = "",
                    message = provideContext().getString(R.string.status_select_need)
                )
            )

            return
        }

        if (dayCount == null || dayCount < 0) {
            parentViewModel.showDialogOrMenu(
                Dialog.makeMessage(
                    context = provideContext(),
                    title = "",
                    message = provideContext().getString(R.string.day_count_need)
                )
            )

            return
        }

        onConfirmed(
            ConfirmedData(
                dayCount = dayCount,
                statuses = statuses
            )
        )
    }

    override fun onDaysValueChange(value: TextFieldValue) {
        val v = value.text.toIntOrNull()
        if (v != null || value.text.isEmpty()) {
            daysInputValueS.value = value
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