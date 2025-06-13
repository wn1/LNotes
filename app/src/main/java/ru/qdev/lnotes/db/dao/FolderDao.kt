package ru.qdev.lnotes.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ru.qdev.lnotes.db.entity.FolderEntry

@Dao
interface FolderDao {
    @Query("SELECT * FROM categories ORDER BY label")
    fun getAllOrderByLabel(): List<FolderEntry>

//    @Query("SELECT * FROM notes WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<User>

//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): User

    @Insert
    fun insertAll(vararg users: FolderEntry)

    @Delete
    fun delete(user: FolderEntry)

    @Query("DELETE FROM categories WHERE id = :id")
    fun deleteById(id: Long)
}