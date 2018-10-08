package ru.qdev.lnotes.db.entity

import android.support.annotation.AnyThread
import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import ru.qdev.lnotes.db.entity.QDVDbEntity

/**
 * Created by Vladimir Kudashov on 28.09.18.
 */

@DatabaseTable(tableName="categories")
@AnyThread
open class QDVDbFolder(label: String? = null) : QDVDbEntity() {
    enum class Special (val id: Long) {UNKNOWN_FOLDER(-1)}
    @DatabaseField(dataType = DataType.STRING, canBeNull = true)
    var label: String? = label
}