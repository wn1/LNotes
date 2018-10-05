package ru.qdev.lnotes.mvp

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.*
import com.j256.ormlite.dao.CloseableIterator
import ru.qdev.lnotes.db.entity.QDVDbFolderOrMenuItem
import java.util.ArrayList

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface QDVNavigationDrawerView : MvpView {
    fun loadFolderList(dbIterator: CloseableIterator<QDVDbFolderOrMenuItem>,
                       itemsAddingToTop: ArrayList<QDVDbFolderOrMenuItem>,
                       selectedFolderOrMenu: QDVDbFolderOrMenuItem?)
    fun setSelectedFolderOrMenu(folderOrMenu: QDVDbFolderOrMenuItem?)
    fun setDrawerOpen(drawerOpen: Boolean)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun onClickAddFolder()
}