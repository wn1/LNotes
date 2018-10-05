package ru.q_dev.lnotes

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * Created by Vladimir Kudashov on 28.09.18.
 */

@DatabaseTable(tableName="categories")
open class QDVDbFolder(label: String? = null) : QDVDbEntity() {
    enum class Special (val id: Long) {UNKNOWN_FOLDER(-1)}
    @DatabaseField(generatedId = true, dataType = DataType.LONG, columnName = "id")
    override var id: Long = 0
    @DatabaseField(dataType = DataType.STRING, canBeNull = true)
    var label: String? = label
}