package ru.qdev.lnotes.mvp

import androidx.annotation.AnyThread
import ru.qdev.lnotes.db.entity.QDVDbFolder
import java.io.Serializable

/**
 * Created by Vladimir Kudashov on 29.09.18.
 */

@AnyThread
class QDVFilterByFolderState : Serializable {
    enum class FilterType {ALL_FOLDER, FOLDER_NOT_SELECTED, FOLDER_ID, FOLDER}
    var folder: QDVDbFolder? = null
    var folderId: Long? = null
    var filterType: FilterType = FilterType.ALL_FOLDER
}

class QDVSearchState: Serializable {
    var isSearchActive: Boolean = false
    var searchText: String? = null
}

class QDVNotesListState : Serializable {
    val searchState: QDVSearchState = QDVSearchState()
    var filterByFolderState: QDVFilterByFolderState = QDVFilterByFolderState()
    var folderIdForNotesAdding: Long? = null
}