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

    private var lastIteratorPosition: Int? = null

    override fun getItem(position: Int): T? {
        synchronized(this) {
            try {
                if (position==lastIteratorPosition) {
                    return dbIterator!!.current()
                }
                dbIterator!!.moveRelative(position-lastIteratorPosition!!)
                lastIteratorPosition = position
                return dbIterator!!.current()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
        return null
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        try {
            return if (dbIterator == null) 0 else {
               val count = (dbIterator!!.rawResults as AndroidDatabaseResults).count
                count
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    fun loadDbIterator (newDbIterator: CloseableIterator<T>?) {
        dbIterator = newDbIterator
        dbIterator!!.first()
        lastIteratorPosition = 0
    }
}