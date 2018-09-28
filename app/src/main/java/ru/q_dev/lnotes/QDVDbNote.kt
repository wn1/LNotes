package ru.q_dev.lnotes

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.io.Serializable
import java.util.*

@DatabaseTable(tableName="notes")
class QDVDbNote : Serializable {
    @DatabaseField(generatedId = true, dataType = DataType.LONG, columnName = "id")
    val id: Long = 0
    @DatabaseField(dataType = DataType.STRING, canBeNull = true)
    var content: String? = null
    @DatabaseField (dataType = DataType.LONG_OBJ, canBeNull = true, columnName = "folder_id")
    var folderId: Long? = null

    @DatabaseField (dataType = DataType.LONG_OBJ, columnName ="create_time_u")
    var createTimeInUnixMs: Long? = null

    var createTime: Date?
    get() = createTimeInUnixMs?.let { Date(it) }
    set(value) {
        createTimeInUnixMs = value?.time
    }

    @DatabaseField (dataType = DataType.LONG_OBJ, columnName ="update_time_u")
    var updateTimeInUnixMs: Long? = null

    var updateTime: Date?
        get() = updateTimeInUnixMs?.let { Date(it) }
        set(value) {
            updateTimeInUnixMs = value?.time
        }

    @DatabaseField (dataType = DataType.LONG_OBJ, columnName ="complete_time_u")
    var completeTimeInUnixMs: Long? = null

    var completeTime: Date?
        get() = completeTimeInUnixMs?.let { Date(it) }
        set(value) {
            completeTimeInUnixMs = value?.time
        }
}

