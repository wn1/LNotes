package ru.qdev.lnotes.ui.navigation.route.base

import kotlinx.serialization.Serializable

@Serializable
open class BaseRoute (val screenUid: String, var navigationIndex: Int = 0) {
}