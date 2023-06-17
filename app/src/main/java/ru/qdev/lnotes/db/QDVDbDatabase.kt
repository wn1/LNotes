package ru.qdev.lnotes.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.AnyThread
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao
import ru.qdev.lnotes.ThisApp
import ru.qdev.lnotes.db.entity.QDVDbNote
import ru.qdev.lnotes.ui.activity.QDVNotesHomeActivity
import java.io.File
import java.lang.Exception
import java.sql.SQLException


/**
 * Created by Vladimir Kudashov on 24.09.18.
 */

const val DATABASE_VERSION = 7
const val DATABASE_NAME = "data.db"

@AnyThread
class QDVDbDatabase(context: Context?, databaseName: String?,
                    factory: SQLiteDatabase.CursorFactory?, databaseVersion: Int) :
        OrmLiteSqliteOpenHelper(context, databaseName, factory, databaseVersion) {

    constructor (context: Context) : this (context, DATABASE_NAME, null, DATABASE_VERSION)

    companion object {
        @JvmStatic fun getAndLock (): QDVDbDatabase {
            return OpenHelperManager.getHelper(ThisApp.getContext(), QDVDbDatabase::class.java)
        }

        @JvmStatic fun release () {
            OpenHelperManager.releaseHelper()
        }
    }

    override fun onCreate(database: SQLiteDatabase?, connectionSource: ConnectionSource?) {
        if (database == null) {
            throw Exception ("Database create error")
        }
        val sqLiteDatabase: SQLiteDatabase = database
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS conf (id INTEGER PRIMARY KEY, vers INTEGER DEFAULT 0)")

        //Fix for start onCreate after import db from older version LNotes
        val cursor = sqLiteDatabase.rawQuery("SELECT vers FROM conf", null)
        if (cursor != null && !cursor.isClosed && !cursor.isAfterLast) {
            if (cursor.isBeforeFirst) {
                cursor.moveToFirst()
            }
            var currentVer = cursor.getInt(0)
            cursor.close()
            if (currentVer == 1) {
                var updateQuery = "DROP TABLE IF EXISTS notestmp"
                sqLiteDatabase.execSQL(updateQuery)

                updateQuery = "CREATE TABLE IF NOT EXISTS notestmp (id INTEGER PRIMARY KEY, content, cdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, isready INT DEFAULT 0)"
                sqLiteDatabase.execSQL(updateQuery)

                updateQuery = "INSERT INTO notestmp(content) SELECT content FROM notes"
                sqLiteDatabase.execSQL(updateQuery)

                updateQuery = "DROP TABLE IF EXISTS notes"
                sqLiteDatabase.execSQL(updateQuery)

                updateQuery = "ALTER TABLE notestmp RENAME TO notes"
                sqLiteDatabase.execSQL(updateQuery)

                updateQuery = "DROP TABLE IF EXISTS conf"
                sqLiteDatabase.execSQL(updateQuery)

                sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS conf (id INTEGER PRIMARY KEY, vers INTEGER DEFAULT 0)")

                updateQuery = "INSERT INTO conf(vers) VALUES(2)"
                sqLiteDatabase.execSQL(updateQuery)

                currentVer = 2
            }
            if (currentVer == 2) {
                var updateQuery = "ALTER TABLE notes ADD COLUMN folder_id INTEGER DEFAULT NULL"
                sqLiteDatabase.execSQL(updateQuery)
                updateQuery = "UPDATE conf SET vers = 3"
                currentVer = 3
            }
            if (currentVer == 3) {
                var updateQuery = "ALTER TABLE notes ADD COLUMN isready_date TIMESTAMP DEFAULT NULL"
                sqLiteDatabase.execSQL(updateQuery)

                updateQuery = "UPDATE conf SET vers =4"
                sqLiteDatabase.execSQL(updateQuery)
                currentVer = 4
            }
            if (currentVer == 4) {
                var updateQuery = "CREATE TABLE IF NOT EXISTS conf_filter (id INTEGER PRIMARY KEY, filter_id INTEGER DEFAULT NULL, action, label, row_type INTEGER DEFAULT 0)"
                sqLiteDatabase.execSQL(updateQuery)

                updateQuery = "INSERT INTO conf_filter(action, label, row_type) VALUES('all', 'Все', 2)"
                sqLiteDatabase.execSQL(updateQuery)

                updateQuery = "UPDATE conf SET vers = 5"
                sqLiteDatabase.execSQL(updateQuery)

                currentVer = 5
            }

            if (currentVer >= 5) {
                onUpgrade(sqLiteDatabase, currentVer, DATABASE_VERSION)
            }
        } else {
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY, content, create_time_u INTEGER DEFAULT NULL, isready INT DEFAULT 0, folder_id INTEGER DEFAULT NULL, complete_time_u INTEGER DEFAULT NULL, update_time_u INTEGER DEFAULT NULL)")
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY, label)")
            sqLiteDatabase.execSQL("INSERT INTO conf (vers) VALUES (" + DATABASE_VERSION.toString() + ")")
        }
    }

    override fun onUpgrade(database: SQLiteDatabase?, connectionSource: ConnectionSource?,
                           oldVersion: Int, newVersion: Int) {
        if (database == null) {
            throw Exception ("Database update error")
        }
        val sqLiteDatabase: SQLiteDatabase = database
        var i = oldVersion
        if (i == 5) {
            var updateQuery = "DELETE FROM conf_filter"
            sqLiteDatabase.execSQL(updateQuery)
            sqLiteDatabase.execSQL("INSERT INTO conf_filter (filter_id) VALUES (-2)")
            updateQuery = "UPDATE conf SET vers = 6"
            sqLiteDatabase.execSQL(updateQuery)
            i = 6
        }
        if (i == 6) {
            sqLiteDatabase.execSQL("ALTER TABLE notes ADD COLUMN create_time_u INTEGER DEFAULT NULL")
            sqLiteDatabase.execSQL("ALTER TABLE notes ADD COLUMN update_time_u INTEGER DEFAULT NULL")
            sqLiteDatabase.execSQL("ALTER TABLE notes ADD COLUMN complete_time_u INTEGER DEFAULT NULL")
            sqLiteDatabase.execSQL("UPDATE notes SET create_time_u = (strftime('%s', cdate))*1000 WHERE cdate NOT NULL")
            sqLiteDatabase.execSQL("UPDATE notes SET update_time_u = create_time_u")
            sqLiteDatabase.execSQL("UPDATE notes SET complete_time_u = (strftime('%s', isready_date))*1000 WHERE isready_date NOT NULL")

            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS notestmp (id INTEGER PRIMARY KEY, content, create_time_u INTEGER DEFAULT NULL, isready INT DEFAULT 0, folder_id INTEGER DEFAULT NULL, complete_time_u INTEGER DEFAULT NULL, update_time_u INTEGER DEFAULT NULL)")
            sqLiteDatabase.execSQL("INSERT INTO notestmp(id, content, create_time_u, isready, folder_id, complete_time_u, update_time_u) SELECT id, content, create_time_u, isready, folder_id, complete_time_u, update_time_u FROM notes")
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS notes")
            sqLiteDatabase.execSQL("ALTER TABLE notestmp RENAME TO notes")

            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS conf_filter")

            i = 7
            sqLiteDatabase.execSQL("UPDATE conf SET vers = $i")
        }
    }

    @Throws(SQLException::class)
    override fun <D : Dao<T, *>, T> getDao(clazz: Class<T>): D {
        val dao = super.getDao(clazz) ?: throw SQLException("Exception on get notesDao")
        @Suppress("UNCHECKED_CAST")
        return dao as D
    }

    @Throws(SQLException::class)
    fun <D : Dao<T, T_ID>, T, T_ID> getDao(clazz: Class<T>, clazzId: Class<T_ID>): D {
        val dao = super.getDao(clazz) ?: throw SQLException("Exception on get notesDao")
        @Suppress("UNCHECKED_CAST")
        return dao as D
    }

    @Throws(SQLException::class)
    fun <D : Dao<T, Long>, T> getDaoWithIdLong(clazz: Class<T>): D {
        return getDao(clazz, Long::class.java)
    }

    @Throws(SQLException::class)
    fun getNotesDao(): Dao<QDVDbNote, Int> {
        return getDao(QDVDbNote::class.java)
    }

    fun getFileDB(): File {
        return File(readableDatabase.path)
    }

}
