package ru.qdev.lnotes.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.qdev.lnotes.db.dao.FolderDao
import ru.qdev.lnotes.db.dao.NotesDao
import ru.qdev.lnotes.db.entity.ConfigEntry
import ru.qdev.lnotes.db.entity.FolderEntry
import ru.qdev.lnotes.db.entity.NotesEntry

@Database(entities = [NotesEntry::class, ConfigEntry::class, FolderEntry::class], version = 2)
//@TypeConverters(DateConverter::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao
    abstract fun folderDao(): FolderDao
}