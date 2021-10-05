package ru.qdev.lnotes.db.entity;

import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;

import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Vladimir Kudashov on 04.10.18.
 */

@DatabaseTable(tableName="categories")
@AnyThread
public class QDVDbFolderOrMenuItem extends QDVDbFolder {
    public enum MenuItemMarker {
        FOLDER_ENTITY,
        NO_MENU,
        FOLDER_ADDING,
        FOLDER_ALL,
        FOLDER_UNKNOWN
    }

    public QDVDbFolderOrMenuItem(){
        super();
    }

    public MenuItemMarker menuItem = MenuItemMarker.FOLDER_ENTITY;

    public QDVDbFolderOrMenuItem(@Nullable String label, MenuItemMarker menuItem) {
        super(label);
        this.menuItem = menuItem;
    }
}
