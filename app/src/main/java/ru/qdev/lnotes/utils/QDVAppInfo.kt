package ru.qdev.lnotes.utils

import android.content.Context

fun getVersionName (context: Context?) : String? {
    val packageManager = context?.packageManager
    val packageInfo = packageManager?.getPackageInfo(context?.packageName, 0)
    return packageInfo?.versionName
}