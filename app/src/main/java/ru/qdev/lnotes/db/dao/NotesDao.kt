package ru.qdev.lnotes.db.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import ru.qdev.lnotes.db.entity.NotesEntry


@Dao
interface NotesDao {
    @Query("SELECT * FROM notes")
    fun getAll(): List<NotesEntry>

    @Query("SELECT * FROM notes WHERE id == :uid")
    fun getById(uid: Long): List<NotesEntry>

//    @Query("SELECT * FROM notes WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<User>

//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): User

    @Query("SELECT * FROM notes WHERE folder_id is :folderId AND content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
    fun getNotesByFolderIdCursor(folderId: Long?, searchText: String): Cursor

    @Query("SELECT * FROM notes WHERE folder_id is :folderId AND prepared = :prepared AND content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
    fun getNotesByFolderIdCursor(folderId: Long?, searchText: String, prepared: Int): Cursor

//    @Query("SELECT COUNT(*) FROM notes WHERE folder_id is :folderId  AND content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
//    fun getNotesByFolderIdCount(folderId: Long?, searchText: String): Long

    @Query("SELECT * FROM notes WHERE content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
    fun getNotesAllCursor(searchText: String): Cursor

    @Query("SELECT * FROM notes WHERE prepared = :prepared AND content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
    fun getNotesAllCursor(searchText: String, prepared: Int): Cursor

//    @Query("SELECT COUNT(*) FROM notes WHERE content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
//    fun getNotesAllCount(searchText: String): Long

    @Query("SELECT * FROM notes WHERE (folder_id is NULL OR folder_id is 0) AND content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
    fun getNotesWithUnknownFolderCursor(searchText: String): Cursor

    @Query("SELECT * FROM notes WHERE (folder_id is NULL OR folder_id is 0) AND prepared = :prepared AND content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
    fun getNotesWithUnknownFolderCursor(searchText: String, prepared: Int): Cursor

//    @Query("SELECT COUNT(*) FROM notes WHERE (folder_id is NULL OR folder_id is 0) AND content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
//    fun getNotesWithUnknownFolderCount(searchText: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg notes: NotesEntry)

    @Delete
    fun delete(note: NotesEntry)

    @Query("DELETE FROM notes WHERE folder_id = :folderId")
    fun deleteByFolderId(folderId: Long)

    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int

    @Query("UPDATE notes SET prepared = 0, selected = 0 WHERE prepared != 0 OR selected != 0")
    fun resetPrepare()

    @Query("UPDATE notes SET prepared = 1, selected = 1 WHERE folder_id is :folderId AND isready IN (:statuses) AND create_time_u < :olderThan AND complete_time_u < :olderThan AND update_time_u < :olderThan")
    fun prepareAndSelectByFolder(folderId: Long?, statuses: List<Int>, olderThan: Long)

    @Query("UPDATE notes SET prepared = 1, selected = 1 WHERE isready IN (:statuses) AND create_time_u < :olderThan AND complete_time_u < :olderThan AND update_time_u < :olderThan")
    fun prepareAndSelectAllFolder(statuses: List<Int>, olderThan: Long)

    @Query("UPDATE notes SET prepared = 1, selected = 1 WHERE (folder_id is NULL OR folder_id is 0) AND isready IN (:statuses) AND create_time_u < :olderThan AND complete_time_u < :olderThan AND update_time_u < :olderThan")
    fun prepareAndSelectUnknownFolder(statuses: List<Int>, olderThan: Long)

    @Query("DELETE FROM notes WHERE selected = 1 AND prepared = 1")
    fun deleteSelected()
}