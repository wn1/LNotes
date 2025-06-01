package ru.qdev.lnotes.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.qdev.lnotes.db.dao.NotesDao
import ru.qdev.lnotes.db.entity.NotesEntry

@Database(entities = [NotesEntry::class], version = 2)
//@TypeConverters(DateConverter::class)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun userDao(): NotesDao
}