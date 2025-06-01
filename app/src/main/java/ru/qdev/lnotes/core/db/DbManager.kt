package ru.qdev.lnotes.core.events

import android.content.Context
import android.util.Log
import androidx.annotation.AnyThread
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import org.greenrobot.eventbus.EventBus
import ru.qdev.lnotes.db.NotesDatabase
import ru.qdev.lnotes.mvp.QDVMvpDbPresenter
import ru.qdev.lnotes.mvp.QDVNavigationDrawerState
import java.io.File

class DbManager (val context: Context) {
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

    fun getFileDB(): File {
        return context.getDatabasePath(NOTES_DATABASE_NAME)
    }

    companion object {
        const val TAG = "DbManager"

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.i(TAG, "MIGRATION_7_8")
                // Empty implementation, because the schema isn't changing.
            }
        }

        const val NOTES_DATABASE_NAME = "data.db"
    }
}