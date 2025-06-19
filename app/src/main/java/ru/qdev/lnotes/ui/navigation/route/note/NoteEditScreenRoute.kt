package ru.qdev.lnotes.ui.navigation.route.note

import kotlinx.serialization.Serializable
import ru.qdev.lnotes.ui.navigation.route.base.BaseRoute

@Serializable
class NoteEditScreenRoute(
    val noteId: Long?
): BaseRoute(
    screenUid = "NoteEditRoute"
)