package ru.q_dev.lnotes;

import android.support.annotation.Nullable;

import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

@DatabaseTable(tableName="categories")
public class QDVDbFolderOrMenuItem extends QDVDbFolder {
    enum MenuItemMarker {
        FOLDER_ENTITY,
        NO_MENU,
        FOLDER_ADDING,
        FOLDER_ALL,
        FOLDER_UNKNOWN
    }

    QDVDbFolderOrMenuItem(){
        super();
    }

    MenuItemMarker menuItem = MenuItemMarker.FOLDER_ENTITY;

    QDVDbFolderOrMenuItem(@Nullable String label, MenuItemMarker menuItem) {
        super(label);
        this.menuItem = menuItem;
    }
}
