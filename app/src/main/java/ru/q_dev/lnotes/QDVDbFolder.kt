package ru.q_dev.lnotes

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 * Created by Vladimir Kudashov on 28.09.18.
 */

@DatabaseTable(tableName="categories")
class QDVDbFolder {
    @DatabaseField(generatedId = true, dataType = DataType.LONG, columnName = "id")
    val id: Long = 0
    @DatabaseField(dataType = DataType.STRING, canBeNull = true)
    var label: String? = null
}