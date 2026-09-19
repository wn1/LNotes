package ru.qdev.lnotes.ui.sheet.tips

import android.content.Context
import android.util.Log
import ru.qdev.lnotes.core.QDVAppConst.NextShowTimeIHelpedDelay
import ru.qdev.lnotes.core.QDVAppConst.NextShowTimeLaterDelay
import ru.qdev.lnotes.core.QDVAppConst.NextShowTimeNotWantDelay
import ru.qdev.lnotes.core.pref.NotesPreferenceHelper
import ru.qdev.lnotes.ui.screen.base.BaseScreenViewModel
import ru.qdev.lnotes.ui.view.dialog.base.DialogControllerBase
import ru.qdev.lnotes.utils.CommonUtils.openLinkInBrowser
import src.R
import java.util.Date

interface TipsSheetControllerListener {
    fun onCancelClick()
    fun onTipsLinkClick()
    fun onIHelpedClick()
    fun onNotWantClick()
}

class TipsSheetController(
    parentViewModel: BaseScreenViewModel,
    private val notesPreferenceHelper: NotesPreferenceHelper
) :
    DialogControllerBase<TipsSheetController.InitData>(parentViewModel),
    TipsSheetControllerListener {
    data class InitData(
        val tag: String? = null
    ) {
        companion object {
            fun makeTest(context: Context): InitData {
                return InitData()
            }
        }
    }

    private val TAG = this.javaClass.simpleName

    override fun show(init: InitData) {
        notesPreferenceHelper.tipsNextShowTime = Date().time + NextShowTimeLaterDelay

        super.show(init)
    }

    override fun onCancelClick() {
        Log.i(TAG, "onCancelClick")
        hide()
    }

    override fun onIHelpedClick() {
        Log.i(TAG, "onIHelpedClick")
        notesPreferenceHelper.tipsNextShowTime = Date().time + NextShowTimeIHelpedDelay
        hide()
    }

    override fun onNotWantClick() {
        Log.i(TAG, "onNotWantClick")
        notesPreferenceHelper.tipsNextShowTime = Date().time + NextShowTimeNotWantDelay
        hide()
    }

    override fun onTipsLinkClick() {
        Log.i(TAG, "onTipsLinkClick")
        parentViewModel.activityRef?.get()?.let {
            openLinkInBrowser(it, provideContext().getString(R.string.tips_link))
        }
    }

    companion object {

    }
}