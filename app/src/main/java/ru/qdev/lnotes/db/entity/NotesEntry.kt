package ru.qdev.lnotes.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


//sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY, content, create_time_u INTEGER DEFAULT NULL, isready INT DEFAULT 0, folder_id INTEGER DEFAULT NULL, complete_time_u INTEGER DEFAULT NULL, update_time_u INTEGER DEFAULT NULL)")
@Entity(tableName = "notes")
data class NotesEntry(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val uid: Long?, //TODO проверить autoGenerate
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "create_time_u") val createTimeU: Long?,
    @ColumnInfo(name = "isready") val isReady: Int,
    @ColumnInfo(name = "folder_id") val folderId: Long?,
    @ColumnInfo(name = "complete_time_u") val completeTimeU: Long?,
    @ColumnInfo(name = "update_time_u") val updateTimeU: Long?,
)