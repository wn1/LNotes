package ru.qdev.lnotes.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


//    sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY, label)")

@Entity(tableName = "categories")
data class FolderEntry(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val uid: Long?, //TODO проверить autoGenerate
    @ColumnInfo(name = "label") val label: String?,
)