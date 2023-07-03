package ru.qdev.lnotes.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.concurrent.Executors

class QDVTempFileSendUtils (val context: Context) {
    private val sendDirName = "send"
    private val executor = Executors.newSingleThreadExecutor()
    private val deleteAfterHours = 48

    private fun getSendTempDir() : File {
        val tempDir = context.cacheDir
        return File(tempDir, sendDirName)
    }

    fun getTempFile(fileName: String): File {
        val tempSendDir = getSendTempDir()
        tempSendDir.mkdirs()
        val file = File(tempSendDir, fileName)
        return file
    }

    fun deleteUnusedFiles() {
        executor.execute {
            try {
                val filesDir = getSendTempDir()
                val dateForDelete = Calendar.getInstance()
                dateForDelete.add(Calendar.HOUR, -deleteAfterHours)

                filesDir.listFiles()?.forEach {
                    val time = Date(it.lastModified())
                    if (time < dateForDelete.time) {
                        try {
                            val r = it.delete()
                            Log.d(LOG_TAG, "file: ${it.path} delete: $r")
                        }
                        catch (t: Throwable) {
                            Log.e(LOG_TAG, "delete file: ${it.path} error: $t")
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.e(LOG_TAG, "deleteUnusedFiles error: $e")
            }
        }
    }

    companion object {
        private const val LOG_TAG = "QDVTempFileSendUtils"
    }
}