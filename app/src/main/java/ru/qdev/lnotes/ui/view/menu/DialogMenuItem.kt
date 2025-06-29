package ru.qdev.lnotes.ui.view.menu

data class DialogMenuItem (val title: String,
                           val id: String,
                           val type: MenuItemType = MenuItemType.MenuItem)