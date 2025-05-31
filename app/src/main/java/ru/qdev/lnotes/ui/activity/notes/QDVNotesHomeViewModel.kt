package ru.qdev.lnotes.ui.activity.notes

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.qdev.lnotes.core.events.DbManager
import ru.qdev.lnotes.db.QDVDbDatabase
import ru.qdev.lnotes.ui.activity.base.BaseActivityViewModel
import ru.qdev.lnotes.ui.activity.notes.model.OldDbUpdateError
import ru.qdev.lnotes.ui.view.dialog.Dialog
import ru.qdev.lnotes.ui.view.dialog.DialogButton
import src.R
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class QDVNotesHomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val dbManager: DbManager
) : BaseActivityViewModel(context = context, savedStateHandle = savedStateHandle) {

    @get:AnyThread
    private val dbPath: File
        get() = QDVDbDatabase(context).getFileDB()

    @get:AnyThread
    private val oldLNotesDbPath: File
        get() {
            var retFile =
                context.getDir(OLD_DB_FOLDER_NAME, 0)
            retFile = File(retFile, OLD_DB_FILE_NAME)
            return retFile
        }

    @UiThread
    fun oldDbUpdateIfNeeded() {
        //TODO перенести в беграунд
        //Old version update support
        val oldLnotesDb = oldLNotesDbPath
        if (oldLnotesDb.exists()) {
            showDialog(
                Dialog(
                    title = "",
                    message = context.getString(R.string.copy_base_from_old_lnotes),
                    buttons = listOf(
                        DialogButton(
                            title = context.getString(R.string.action_ok),
                            id = OLD_DB_UPDATE_DLG_ID
                        )
                    )
                )
            )
        }
    }

    override fun onDialogButtonClick(button: DialogButton) {
        super.onDialogButtonClick(button)

        if (button.id == OLD_DB_UPDATE_DLG_ID) {
            onUpdateDbClick()
            return
        }
    }

    private fun onUpdateDbClick() {
        val oldLnotesDb = oldLNotesDbPath
        val newLnotesDbPath = dbPath
        if (newLnotesDbPath.exists()) {
            if (!newLnotesDbPath.delete()) {
                showError(context.getString(
                    R.string.error_with_id,
                    OldDbUpdateError.ERROR_1.errorCode
                ))
                return
            }
        }

        var from: FileInputStream? = null
        try {
            from = FileInputStream(oldLnotesDb)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            showError(context.getString(
                R.string.error_with_id,
                OldDbUpdateError.ERROR_2.errorCode
            ))
            return
        }
        var to: FileOutputStream? = null
        try {
            to = FileOutputStream(newLnotesDbPath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            try {
                from.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
            showError(context.getString(
                R.string.error_with_id,
                OldDbUpdateError.ERROR_3.errorCode
            ))
            return
        }
        val buffer = ByteArray(1024)
        var readedCount = 0
        try {
            while ((from.read(buffer, 0, buffer.size)
                    .also { readedCount = it }) != -1
            ) {
                to.write(buffer, 0, readedCount)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                from.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
            try {
                to.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }

            showError(context.getString(
                R.string.error_with_id,
                OldDbUpdateError.ERROR_4.errorCode
            ))
            newLnotesDbPath.delete()
            return
        }
        oldLnotesDb.delete()

        reloadDataDb()
    }

    @MainThread
    fun reloadDataDb() {
        dbManager.doReloadDb()
    }

    companion object {
        private const val OLD_DB_UPDATE_DLG_ID = "OLD_DB_UPDATE_DLG_ID"
        const val NEED_RELOAD_DB_FLAG = "needReloadDb"

        private const val OLD_DB_FILE_NAME = "data.db"
        private const val OLD_DB_FOLDER_NAME = "data"
    }
}