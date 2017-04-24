package ru.q_dev.lnotes;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

/**
 * Created by Vladimir Kudashov on 11.03.17.
 */

public class QDVMyBaseOpenHelper extends SQLiteOpenHelper {
    private static final int VERSION = 6;
    public static final String DATABASE_NAME = "data.db";

    public QDVMyBaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public QDVMyBaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    public QDVMyBaseOpenHelper(Context context, DatabaseErrorHandler errorHandler) {
        super(context, DATABASE_NAME, null, VERSION, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS conf (id INTEGER PRIMARY KEY, vers INTEGER DEFAULT 0)");

        //Fix for start onCreate after import db from older version LNotes
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT vers FROM conf", null);
        if (cursor!=null && !cursor.isClosed() && !cursor.isAfterLast()) {
            if (cursor.isBeforeFirst()){
                cursor.moveToFirst();
            }
            int currentVer = cursor.getInt(0);
            if (currentVer == 1) {
                String updateQuery = "DROP TABLE IF EXISTS notestmp";
                sqLiteDatabase.execSQL(updateQuery);

                updateQuery = "CREATE TABLE IF NOT EXISTS notestmp (id INTEGER PRIMARY KEY, content, cdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, isready INT DEFAULT 0)";
                sqLiteDatabase.execSQL(updateQuery);

                updateQuery = "INSERT INTO notestmp(content) SELECT content FROM notes";
                sqLiteDatabase.execSQL(updateQuery);

                updateQuery = "DROP TABLE IF EXISTS notes";
                sqLiteDatabase.execSQL(updateQuery);

                updateQuery = "ALTER TABLE notestmp RENAME TO notes";
                sqLiteDatabase.execSQL(updateQuery);

                updateQuery = "DROP TABLE IF EXISTS conf";
                sqLiteDatabase.execSQL(updateQuery);

                sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS conf (id INTEGER PRIMARY KEY, vers INTEGER DEFAULT 0)");

                updateQuery = "INSERT INTO conf(vers) VALUES(2)";
                sqLiteDatabase.execSQL(updateQuery);

                currentVer = 2;
            }
            if (currentVer == 2) {
                String updateQuery = "ALTER TABLE notes ADD COLUMN folder_id INTEGER DEFAULT NULL";
                sqLiteDatabase.execSQL(updateQuery);
                updateQuery = "UPDATE conf SET vers = 3";
                currentVer = 3;
            }
            if (currentVer == 3) {
                String updateQuery = "ALTER TABLE notes ADD COLUMN isready_date TIMESTAMP DEFAULT NULL";
                sqLiteDatabase.execSQL(updateQuery);

                updateQuery = "UPDATE conf SET vers =4";
                sqLiteDatabase.execSQL(updateQuery);
                currentVer = 4;
            }
            if (currentVer == 4) {
                String updateQuery = "CREATE TABLE IF NOT EXISTS conf_filter (id INTEGER PRIMARY KEY, filter_id INTEGER DEFAULT NULL, action, label, row_type INTEGER DEFAULT 0)";
                sqLiteDatabase.execSQL(updateQuery);

                updateQuery = "INSERT INTO conf_filter(action, label, row_type) VALUES('all', 'Все', 2)";
                sqLiteDatabase.execSQL(updateQuery);

                updateQuery = "UPDATE conf SET vers = 5";
                sqLiteDatabase.execSQL(updateQuery);

                currentVer = 5;
            }

            if (currentVer >= 5) {
                onUpgrade(sqLiteDatabase, currentVer, VERSION);
            }
        }
        else
        {
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY, content, cdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, isready INT DEFAULT 0, folder_id INTEGER DEFAULT NULL, isready_date TIMESTAMP DEFAULT NULL)");
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY, label)");
            sqLiteDatabase.execSQL("INSERT INTO conf (vers) VALUES ("+String.valueOf(VERSION)+")");
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS conf_filter (id INTEGER PRIMARY KEY, filter_id INTEGER DEFAULT NULL, action, label, row_type INTEGER DEFAULT 0)");
            sqLiteDatabase.execSQL("INSERT INTO conf_filter (filter_id) VALUES ("+String.valueOf(QDVNotesActivity.action_categories_all_id)+")");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if (i==5){
            String updateQuery = "DELETE FROM conf_filter";
            sqLiteDatabase.execSQL(updateQuery);
            sqLiteDatabase.execSQL("INSERT INTO conf_filter (filter_id) VALUES ("
                    +String.valueOf(QDVNotesActivity.action_categories_all_id)+")");
            updateQuery = "UPDATE conf SET vers = 6";
            sqLiteDatabase.execSQL(updateQuery);
            i = 6;
        }
    }

    public File getFileDB(){
        return new File (getReadableDatabase().getPath());
    }

}
