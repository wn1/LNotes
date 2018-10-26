package ru.qdev.lnotes.db

import android.support.annotation.UiThread
import android.widget.BaseAdapter
import com.j256.ormlite.android.AndroidDatabaseResults
import com.j256.ormlite.dao.CloseableIterator
import ru.qdev.lnotes.db.entity.QDVDbEntity
import java.sql.SQLException

/**
 * Created by Vladimir Kudashov on 30.09.18.
 */

@UiThread
abstract class QDVDbIteratorListViewAdapter <T: QDVDbEntity> : BaseAdapter() {
    private var dbIterator: CloseableIterator<T>? = null
        private set (value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItem(p0: Int): T? {
        try {
            (dbIterator!!.rawResults as AndroidDatabaseResults).moveAbsolute(p0)
            return dbIterator!!.current()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        try {
            return if (dbIterator == null) 0 else {
                (dbIterator!!.rawResults as AndroidDatabaseResults).count
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    fun loadDbIterator (newDbIterator: CloseableIterator<T>?) {
        dbIterator = newDbIterator
        notifyDataSetChanged()
    }
}