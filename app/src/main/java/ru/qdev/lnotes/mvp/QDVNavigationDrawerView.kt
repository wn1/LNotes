package ru.qdev.lnotes.mvp

import androidx.annotation.UiThread
import com.j256.ormlite.dao.CloseableIterator
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import ru.qdev.lnotes.db.entity.QDVDbFolderOrMenuItem
import java.util.ArrayList

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
@UiThread
interface QDVNavigationDrawerView : MvpView {
    fun loadFolderList(dbIterator: CloseableIterator<QDVDbFolderOrMenuItem>,
                       itemsAddingToTop: ArrayList<QDVDbFolderOrMenuItem>,
                       selectedFolderOrMenu: QDVDbFolderOrMenuItem?)
    fun setSelectedFolderOrMenu(folderOrMenu: QDVDbFolderOrMenuItem?)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun setDrawerOpen(drawerOpen: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun switchDrawerOpenOrClose()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onClickAddFolder()
}