package ru.qdev.lnotes.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ru.qdev.lnotes.db.entity.NotesEntry

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes")
    fun getAll(): List<NotesEntry>

//    @Query("SELECT * FROM notes WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<User>

//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): User

    @Query("SELECT * FROM notes WHERE folder_id is :folderId ORDER BY (isready > 0), complete_time_u DESC, update_time_u DESC")
    fun getNotesByFolderIdPagingSource(folderId: Long?): PagingSource<Int, NotesEntry>

    @Insert
    fun insertAll(vararg users: NotesEntry)

    @Delete
    fun delete(user: NotesEntry)
}