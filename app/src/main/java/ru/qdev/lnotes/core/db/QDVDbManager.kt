package ru.qdev.lnotes.core.events

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.qdev.lnotes.db.NotesDatabase
import java.io.File

class QDVDbManager (val context: Context) {
    var notesDatabase: NotesDatabase? = null
        private set

    fun doReloadDb() {
        closeNotesDb()
        openNotesDb()
    }

    fun openNotesDb() {
        if(notesDatabase == null || notesDatabase?.isOpen == false) {
            notesDatabase = Room
                .databaseBuilder(
                    context,
                    NotesDatabase::class.java, NOTES_DATABASE_NAME
                )
                .addMigrations(MIGRATION_7_8).build()
        }
    }

    fun closeNotesDb() {
        notesDatabase?.close()
        notesDatabase = null
    }

    companion object {
        const val TAG = "DbManager"

        fun getFileDB(context: Context): File {
            return context.getDatabasePath(NOTES_DATABASE_NAME)
        }

//        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY, content, create_time_u INTEGER DEFAULT NULL, isready INT DEFAULT 0, folder_id INTEGER DEFAULT NULL, complete_time_u INTEGER DEFAULT NULL, update_time_u INTEGER DEFAULT NULL)")
//        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY, label)")
//        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS conf (id INTEGER PRIMARY KEY, vers INTEGER DEFAULT 0)")

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.i(TAG, "MIGRATION_7_8")
                //notes
                var updateQuery = "DROP TABLE IF EXISTS notestmp"
                db.execSQL(updateQuery)

                updateQuery = "CREATE TABLE IF NOT EXISTS notestmp (id INTEGER PRIMARY KEY, content TEXT, create_time_u INTEGER, isready INTEGER NOT NULL, folder_id INTEGER, complete_time_u INTEGER, update_time_u INTEGER)"
                db.execSQL(updateQuery)

                updateQuery = "INSERT INTO notestmp(id, content, create_time_u, isready, folder_id, complete_time_u, update_time_u) SELECT id, content, create_time_u, isready, folder_id, complete_time_u, update_time_u FROM notes"
                db.execSQL(updateQuery)

                updateQuery = "DROP TABLE IF EXISTS notes"
                db.execSQL(updateQuery)

                updateQuery = "ALTER TABLE notestmp RENAME TO notes"
                db.execSQL(updateQuery)

                //categories
                updateQuery = "DROP TABLE IF EXISTS categoriestmp"
                db.execSQL(updateQuery)

                updateQuery = "CREATE TABLE IF NOT EXISTS categoriestmp (id INTEGER PRIMARY KEY, label TEXT DEFAULT NULL)"
                db.execSQL(updateQuery)

                updateQuery = "INSERT INTO categoriestmp(id, label) SELECT id, label FROM categories"
                db.execSQL(updateQuery)

                updateQuery = "DROP TABLE IF EXISTS categories"
                db.execSQL(updateQuery)

                updateQuery = "ALTER TABLE categoriestmp RENAME TO categories"
                db.execSQL(updateQuery)

            }
        }

        const val NOTES_DATABASE_NAME = "data.db"
    }
}