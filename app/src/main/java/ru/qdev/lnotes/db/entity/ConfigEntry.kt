package ru.qdev.lnotes.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


// sqLiteDatabase.execSQL("INSERT INTO conf (vers) VALUES (" + DATABASE_VERSION.toString() + ")")

@Entity(tableName = "conf")
data class ConfigEntry(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val uid: Long?,
    @ColumnInfo(name = "vers") val version: String?, //Deprecated, не используется
)