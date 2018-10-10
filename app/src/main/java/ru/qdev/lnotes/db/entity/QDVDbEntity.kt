package ru.qdev.lnotes.db.entity

import android.support.annotation.AnyThread
import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import java.io.Serializable

/**
 * Created by Vladimir Kudashov on 30.09.18.
 */

@AnyThread
open class QDVDbEntity : Serializable {
    @DatabaseField(generatedId = true, dataType = DataType.LONG, columnName = "id")
    open var id: Long = 0
}
