package ru.qdev.lnotes.model

import android.content.Context
import ru.qdev.lnotes.db.entity.FolderEntry

data class Folder(val id: String?, val title: String, val type: FolderType = FolderType.Folder) {

    companion object {
        fun from(entry: FolderEntry) : Folder {
            entry.let {
                return Folder(
                    id = it.uid?.toString(),
                    title = it.label ?: ""
                )
            }
        }

        fun makeTestList(context: Context) : List<Folder> {
            return listOf(
                Folder(
                    title = "Папка 1",
                    id = "1"
                ),
                Folder(
                    title = "Папка 2",
                    id = "2"
                )
            )
        }
    }
}
