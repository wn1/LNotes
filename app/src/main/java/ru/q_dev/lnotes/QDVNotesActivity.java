package ru.q_dev.lnotes;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.widget.DrawerLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Vladimir Kudashov on 11.03.17.
 */

public class QDVNotesActivity extends AppCompatActivity
        implements QDVNavigationDrawerFragment.NavigationDrawerCallbacks {


    public static final int action_add_categories_id = -3;
    public static final int action_add_categories_position = 0;
    public static final int action_categories_all_id = -2;
    public static final int action_categories_all_position = 1;
    public static final int action_categories_not_selected_id = -1;
    public static final int action_categories_not_selected_position = 1;

    private static boolean searchActive = false;
    private static String searchText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        QDVNavigationDrawerFragment mNavigationDrawerFragment = (QDVNavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setEditorActive(false);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        if (QDVVersionDifference.adsPresent()) {
            QDVVersionDifference.loadAd(this);
        }
    }

    private void reloadDataDb() {
        Intent intent = new Intent(this, QDVNotesActivity.class);
        finish();
        startActivity(intent);
    }

    private File getDbPath (){
        File retFile = new QDVMyBaseOpenHelper(this, new DatabaseErrorHandler() {
            @Override
            public void onCorruption(SQLiteDatabase sqLiteDatabase) {
                new AlertDialog.Builder(QDVNotesActivity.this).
                        setMessage(String.format(getString(R.string.error_with_id), "402"))
                        .setCancelable(true)
                        .setPositiveButton(R.string.cancel, null).show();
            }
        }).getFileDB();
        return retFile;
    }

    private File getOldLNotesDbPath (){
        File retFile = getDir("data", 0);
        retFile = new File (retFile, "data.db");
        return retFile;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean needReloadDb = intent.getBooleanExtra("needReloadDb", false);
        if (needReloadDb){
            reloadDataDb();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean needReloadDb = getIntent().getBooleanExtra("needReloadDb", false);
        if (needReloadDb){
            reloadDataDb();
        }

        final File oldLnotesDb = getOldLNotesDbPath();
        if (oldLnotesDb.exists()){
            new AlertDialog.Builder(this).setCancelable(false).setMessage(R.string.copy_base_from_old_lnotes).
                    setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    File newLnotesDbPath = getDbPath();
                    if (newLnotesDbPath.exists()){
                        if (!newLnotesDbPath.delete()) {
                            new AlertDialog.Builder(QDVNotesActivity.this).
                                    setMessage(String.format(getString(R.string.error_with_id), "200"))
                                    .setCancelable(true)
                                    .setPositiveButton(R.string.cancel, null).show();
                            return;
                        }
                    }

                    FileInputStream from = null;
                    try {
                        from = new FileInputStream(oldLnotesDb);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(QDVNotesActivity.this).
                                setMessage(String.format(getString(R.string.error_with_id), "201"))
                                .setCancelable(true)
                                .setPositiveButton(R.string.cancel, null).show();
                        return;
                    }
                    FileOutputStream to = null;
                    try {
                        to = new FileOutputStream(newLnotesDbPath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        try {
                            from.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        new AlertDialog.Builder(QDVNotesActivity.this).
                                setMessage(String.format(getString(R.string.error_with_id), "202"))
                                .setCancelable(true)
                                .setPositiveButton(R.string.cancel, null).show();
                        return;
                    }
                    byte[] buffer = new byte[1024];
                    int readedCount = 0;
                    try {
                        while ((readedCount = from.read(buffer, 0, buffer.length))!=-1){
                            to.write(buffer, 0, readedCount);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            from.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        try {
                            to.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        new AlertDialog.Builder(QDVNotesActivity.this).
                                setMessage(String.format(getString(R.string.error_with_id), "203"))
                                .setCancelable(true)
                                .setPositiveButton(R.string.cancel, null).show();
                        newLnotesDbPath.delete();
                        return;
                    }
                    oldLnotesDb.delete();

                    reloadDataDb();
                }
            }).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, long idSection) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, QDVNotesListFragment.newInstance(idSection))
                .commit();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(QDVNotesListFragment.FRAGMENT_TAG);
        if (fragment != null && (fragment instanceof QDVNoteEditorFragment)) {
            ((QDVNoteEditorFragment) fragment).goBackWithConfirm();
            return;
        }
        super.onBackPressed();
    }
}
