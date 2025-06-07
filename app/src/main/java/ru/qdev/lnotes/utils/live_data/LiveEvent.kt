package ru.qdev.lnotes.utils.live_data

class LiveEvent<T>(eventData: T) {
    @Volatile private var data: T? = eventData

    fun peek(): T? {
        return data
    }

    fun getEventAndReset() : T? {
        val returnData = data
        data = null
        return returnData
    }

}