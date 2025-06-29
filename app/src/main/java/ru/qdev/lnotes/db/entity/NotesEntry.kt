package ru.qdev.lnotes.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.qdev.lnotes.db.enum.StatusOfExecution
import java.util.Date


//sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY, content, create_time_u INTEGER DEFAULT NULL, isready INT DEFAULT 0, folder_id INTEGER DEFAULT NULL, complete_time_u INTEGER DEFAULT NULL, update_time_u INTEGER DEFAULT NULL)")
@Entity(tableName = "notes")
data class NotesEntry(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val uid: Long? = null, //TODO проверить autoGenerate
    @ColumnInfo(name = "content") var content: String? = null,
    @ColumnInfo(name = "create_time_u") var createTimeU: Long? = null,
    @ColumnInfo(name = "isready") var isReady: Int = 0,
    @ColumnInfo(name = "folder_id") var folderId: Long? = null,
    @ColumnInfo(name = "complete_time_u") var completeTimeU: Long? = null,
    @ColumnInfo(name = "update_time_u") var updateTimeU: Long? = null,
) {
    fun statusOfExecution() : StatusOfExecution {
        return StatusOfExecution.fromDbValue(isReady)
    }

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