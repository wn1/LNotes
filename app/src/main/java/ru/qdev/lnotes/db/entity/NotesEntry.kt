package ru.qdev.lnotes.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


//sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY, content, create_time_u INTEGER DEFAULT NULL, isready INT DEFAULT 0, folder_id INTEGER DEFAULT NULL, complete_time_u INTEGER DEFAULT NULL, update_time_u INTEGER DEFAULT NULL)")
@Entity(tableName = "notes")
data class NotesEntry(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val uid: Long? = null, //TODO проверить autoGenerate
    @ColumnInfo(name = "content") val content: String? = null,
    @ColumnInfo(name = "create_time_u") val createTimeU: Long? = null,
    @ColumnInfo(name = "isready") val isReady: Int = 0,
    @ColumnInfo(name = "folder_id") val folderId: Long? = null,
    @ColumnInfo(name = "complete_time_u") val completeTimeU: Long? = null,
    @ColumnInfo(name = "update_time_u") val updateTimeU: Long? = null,
) {
    companion object {
        fun makeTestList() : List<NotesEntry> {
            val list = List(30) { it }.mapIndexed { index, i ->
                NotesEntry(
                    uid = index.toLong(),
                    content = "Заметка $index",
                    createTimeU = Date().time + index * 1000L * 60
                )
            }
            return list
        }
    }
}