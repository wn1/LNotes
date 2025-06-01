package ru.qdev.lnotes.core.events

import android.content.Context
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

    @AnyThread
    fun doReloadDb() {
        QDVNavigationDrawerState().selectedFolderOrMenu = null
        EventBus.getDefault().post(QDVMvpDbPresenter.DoCloseDatabase())
        EventBus.getDefault().post(QDVMvpDbPresenter.DoReloadDatabase())
    }

    fun openNotesDb() {
        notesDatabase = Room
            .databaseBuilder(
                context,
                NotesDatabase::class.java, NOTES_DATABASE_NAME
            )
            .addMigrations(MIGRATION_1_2).build()
    }

    fun closeNotesDb() {
        notesDatabase?.close()
        notesDatabase = null
    }

    fun getFileDB(): File {
        return context.getDatabasePath(NOTES_DATABASE_NAME)
    }

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Empty implementation, because the schema isn't changing.
            }
        }

        const val NOTES_DATABASE_NAME = "data.db"
    }
}