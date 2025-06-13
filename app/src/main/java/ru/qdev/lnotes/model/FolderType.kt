package ru.qdev.lnotes.model

enum class FolderType(val id: String) {
    Folder("Folder"),
    AddFolderItem("AddFolderItem"),
    AllFolder("AllFolder"),
    UnknownFolder("UnknownFolder");

    companion object {
        fun fromId(id: String) : FolderType? {
            return entries.firstOrNull { it.id == id }
        }
    }
}