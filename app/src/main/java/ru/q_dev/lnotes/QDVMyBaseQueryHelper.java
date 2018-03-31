package ru.q_dev.lnotes;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;

/**
 * Created by Vladimir Kudashov on 31.03.18.
 */

public class QDVMyBaseQueryHelper {
    public static String getFolderDescription (final Context context, long folderId) {
        String folderName = null;
        if (folderId == QDVNotesActivity.action_categories_not_selected_id) {
            folderName = context.getString(R.string.category_unknown);
        }
        else {
            QDVMyBaseOpenHelper dbHelper = new QDVMyBaseOpenHelper(context, new DatabaseErrorHandler() {
                @Override
                public void onCorruption(SQLiteDatabase sqLiteDatabase) {
                    new AlertDialog.Builder(context).
                            setMessage(String.format(context.getString(R.string.error_with_id), "401"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.cancel, null).show();
                }
            });
            if (dbHelper != null) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor selectFolderCursor = db.rawQuery("SELECT label FROM categories WHERE id = :id", new String[]{String.valueOf(folderId)});
                try {
                    if (selectFolderCursor.isBeforeFirst()){
                        selectFolderCursor.moveToFirst();
                    }
                    folderName = selectFolderCursor.getString(0);
                } catch (Exception ignored) {
                }
            }
        }
        return folderName;
    }
}
