package ru.qdev.lnotes.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
    fun getNotesByFolderIdPagingSource(folderId: Long?, searchText: String): PagingSource<Int, NotesEntry>

    @Query("SELECT COUNT(*) FROM notes WHERE folder_id is :folderId  AND content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
    fun getNotesByFolderIdCount(folderId: Long?, searchText: String): Long

    @Query("SELECT * FROM notes WHERE content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
    fun getNotesAllPagingSource(searchText: String): PagingSource<Int, NotesEntry>

    @Query("SELECT COUNT(*) FROM notes WHERE content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
    fun getNotesAllCount(searchText: String): Long

    @Query("SELECT * FROM notes WHERE (folder_id is NULL OR folder_id is 0) AND content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
    fun getNotesWithUnknownFolderPagingSource(searchText: String): PagingSource<Int, NotesEntry>

    @Query("SELECT COUNT(*) FROM notes WHERE (folder_id is NULL OR folder_id is 0) AND content LIKE :searchText ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
    fun getNotesWithUnknownFolderCount(searchText: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg notes: NotesEntry)

    @Delete
    fun delete(note: NotesEntry)

    @Query("DELETE FROM notes WHERE folder_id = :folderId")
    fun deleteByFolderId(folderId: Long)
}