package ru.qdev.lnotes.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v4.widget.DrawerLayout;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.qdev.lnotes.*;
import ru.qdev.lnotes.db.QDVDbDatabase;
import ru.qdev.lnotes.db.entity.QDVDbNote;
import ru.qdev.lnotes.mvp.QDVFilterByFolderState;
import ru.qdev.lnotes.mvp.QDVNavigationDrawerState;
import ru.qdev.lnotes.mvp.QDVNoteEditorState;
import ru.qdev.lnotes.mvp.QDVNotesHomePresenter;
import ru.qdev.lnotes.mvp.QDVNotesHomeView;
import ru.qdev.lnotes.ui.fragment.QDVNavigationDrawerFragment;
import ru.qdev.lnotes.ui.fragment.QDVNoteEditorFragment;
import ru.qdev.lnotes.ui.fragment.QDVNotesListFragment;

/**
 * Created by Vladimir Kudashov on 11.03.17.
 */

public class QDVNotesHomeActivity extends MvpAppCompatActivity implements QDVNotesHomeView {

    @InjectPresenter
    QDVNotesHomePresenter notesHomePresenter;

    QDVNavigationDrawerFragment navigationDrawerFragment;

    public static final int action_categories_all_id = -2;
    public static final int action_categories_not_selected_id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        navigationDrawerFragment = (QDVNavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setActive(false);

        // Set up the drawer.
        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        if (QDVVersionDifference.adsPresent()) {
            QDVVersionDifference.loadAd(this);
        }
    }

    private void reloadDataDb() {
        QDVDbDatabase database = QDVDbDatabase.getAndLock();
        while(database.isOpen()) {
            QDVDbDatabase.release();
        }
        new QDVNavigationDrawerState().setSelectedFolderOrMenu(null);
        Intent intent = new Intent(this, QDVNotesHomeActivity.class);
        finish();
        startActivity(intent);
    }

    private File getDbPath (){
        return new QDVDbDatabase(this).getFileDB();
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
                            new AlertDialog.Builder(QDVNotesHomeActivity.this).
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
                        new AlertDialog.Builder(QDVNotesHomeActivity.this).
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
                        new AlertDialog.Builder(QDVNotesHomeActivity.this).
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
                        new AlertDialog.Builder(QDVNotesHomeActivity.this).
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
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag(QDVNoteEditorFragment.FRAGMENT_TAG);
        if (fragment instanceof QDVNoteEditorFragment) {
            ((QDVNoteEditorFragment) fragment).goBackWithConfirm();
            return;
        }
        if (navigationDrawerFragment.isDrawerOpen()) {
            navigationDrawerFragment.setDrawerOpen(false);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void setNavigationDrawerFolderEnabled(boolean enabled) {
        navigationDrawerFragment.setActive(enabled);
    }

    @Override
    public void initNotesList(@Nullable QDVFilterByFolderState filterByFolderState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, QDVNotesListFragment.newInstance(filterByFolderState))
                .commit();
    }

    @Override
    public void initEditNote(@NotNull QDVDbNote note) {
        QDVNoteEditorState noteEditorState = new QDVNoteEditorState();
        noteEditorState.setState(QDVNoteEditorState.EditorMode.EDITING,
                note.getFolderId(), note.getId());
        Fragment fragment = new QDVNoteEditorFragment();
        getSupportFragmentManager().beginTransaction().addToBackStack(null)
                .replace(R.id.container, fragment, QDVNoteEditorFragment.FRAGMENT_TAG).commit();
    }

    @Override
    public void initAddNote(Long folderIdForAdding) {
        navigationDrawerFragment.setActive(false);
        QDVNoteEditorState noteEditorState = new QDVNoteEditorState();
        noteEditorState.setState(QDVNoteEditorState.EditorMode.ADDING,
                folderIdForAdding, null);
        Fragment fragment = new QDVNoteEditorFragment();
        getSupportFragmentManager().beginTransaction().addToBackStack(null)
                .replace(R.id.container, fragment, QDVNoteEditorFragment.FRAGMENT_TAG).commit();
    }

    @Override
    public void goBackFragment() {
        getSupportFragmentManager().popBackStack();
    }
}
