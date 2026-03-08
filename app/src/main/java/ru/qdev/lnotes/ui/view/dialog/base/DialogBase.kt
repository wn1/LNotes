package ru.qdev.lnotes.ui.view.dialog.base

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import ru.qdev.lnotes.ui.screen.base.BaseScreenViewModel

open class DialogControllerBase<T>(
    parentViewModel: BaseScreenViewModel
) {
    val isShowedS = mutableStateOf(false)
    val initS = mutableStateOf<T?>(null)
    @Volatile private var controllerScope = CoroutineScope(Dispatchers.Main)

    private val TAG = this.javaClass.simpleName

    open fun show(init: T) {
        Log.i(TAG, "show")
        initS.value = init
        isShowedS.value = true
    }

    open fun hide() {
        Log.i(TAG, "show")
        isShowedS.value = false
        controllerScope.cancel()
        controllerScope = CoroutineScope(Dispatchers.Main)
    }
}