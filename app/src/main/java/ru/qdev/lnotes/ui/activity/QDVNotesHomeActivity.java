package ru.qdev.lnotes.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.AnyThread;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.qdev.lnotes.*;
import ru.qdev.lnotes.db.QDVDbDatabase;
import ru.qdev.lnotes.db.entity.QDVDbNote;
import ru.qdev.lnotes.mvp.QDVFilterByFolderState;
import ru.qdev.lnotes.mvp.QDVNoteEditorState;
import ru.qdev.lnotes.mvp.QDVNotesHomePresenter;
import ru.qdev.lnotes.mvp.QDVNotesHomeView;
import ru.qdev.lnotes.mvp.QDVStatisticState;
import ru.qdev.lnotes.ui.view.QDVViewFabric;
import ru.qdev.lnotes.ui.fragment.QDVNavigationDrawerFragment;
import ru.qdev.lnotes.ui.fragment.QDVNoteEditorFragment;
import ru.qdev.lnotes.ui.fragment.QDVNotesListFragment;

/**
 * Created by Vladimir Kudashov on 11.03.17.
 */

public class QDVNotesHomeActivity extends MvpAppCompatActivity implements QDVNotesHomeView {
    static private String NEED_RELOAD_DB_FLAG = "needReloadDb";

    static private String OLD_DB_FILE_NAME = "data.db";
    static private String OLD_DB_FOLDER_NAME = "data";

    @AnyThread
    public enum OldDbUpdateError {
        ERROR_1("1.1"),
        ERROR_2("1.2"),
        ERROR_3("1.3"),
        ERROR_4("1.4");

        String errorCode;
        OldDbUpdateError(String errorCode ) {
            this.errorCode = errorCode;
        }

        @Override
        public String toString() {
            return String.format(ThisApp.getContext().getString(R.string.error_with_id), errorCode);
        }
    }

    @InjectPresenter
    QDVNotesHomePresenter notesHomePresenter;

    QDVNavigationDrawerFragment navigationDrawerFragment;

    @Override
    @UiThread
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.QDVActionBarTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        ButterKnife.bind(this);

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

    @AnyThread
    private void reloadDataDb() {
        notesHomePresenter.doReloadDb();
    }

    @AnyThread
    private File getDbPath (){
        return new QDVDbDatabase(this).getFileDB();
    }

    @AnyThread
    private File getOldLNotesDbPath (){
        File retFile = getDir(OLD_DB_FOLDER_NAME, 0);
        retFile = new File (retFile, OLD_DB_FILE_NAME);
        return retFile;
    }

    @Override
    @UiThread
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean needReloadDb = intent.getBooleanExtra(NEED_RELOAD_DB_FLAG, false);
        if (needReloadDb){
            reloadDataDb();
        }
    }

    @UiThread
    private void oldDbUpdateIfNeeded() {
        //Old version update support
        final File oldLnotesDb = getOldLNotesDbPath();
        if (oldLnotesDb.exists()){
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(R.string.copy_base_from_old_lnotes).
                    setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            File newLnotesDbPath = getDbPath();
                            if (newLnotesDbPath.exists()){
                                if (!newLnotesDbPath.delete()) {
                                    new AlertDialog.Builder(QDVNotesHomeActivity.this)
                                            .setMessage(String.format(
                                                    getString(R.string.error_with_id),
                                                    OldDbUpdateError.ERROR_1))
                                            .setCancelable(true)
                                            .setPositiveButton(R.string.cancel, null)
                                            .show();
                                    return;
                                }
                            }

                            FileInputStream from = null;
                            try {
                                from = new FileInputStream(oldLnotesDb);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                new AlertDialog.Builder(QDVNotesHomeActivity.this).
                                        setMessage(String.format(
                                                getString(R.string.error_with_id),
                                                OldDbUpdateError.ERROR_2))
                                        .setCancelable(true)
                                        .setPositiveButton(R.string.cancel, null)
                                        .show();
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
                                        setMessage(String.format(
                                                getString(R.string.error_with_id),
                                                OldDbUpdateError.ERROR_3))
                                        .setCancelable(true)
                                        .setPositiveButton(R.string.cancel, null)
                                        .show();
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
                                        setMessage(String.format(
                                                getString(R.string.error_with_id),
                                                OldDbUpdateError.ERROR_4))
                                        .setCancelable(true)
                                        .setPositiveButton(R.string.cancel, null)
                                        .show();
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
    @UiThread
    protected void onStart() {
        super.onStart();
//        ViewGroup rootLayout = (ViewGroup) findByViewId(R.id.rootLayout);
//        TextView buyPlusVersion = QDVVersionDifference.getLabelBuyPlusVersion(rootLayout);
//        if (buyPlusVersion != null) {
//            buyPlusVersion.setOnClickListener(v -> {
//                try {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
//                            "https://play.google.com/store/apps/details?id=ru.q_dev.LNoteP")));
//                } catch (Exception ignored) {
//                    new AlertDialog.Builder(this)
//                            .setMessage(R.string.app_not_found)
//                            .setCancelable(true)
//                            .setNegativeButton(R.string.cancel, null)
//                            .show();
//                }
//            });
//        }

        boolean needReloadDb = getIntent().getBooleanExtra(NEED_RELOAD_DB_FLAG, false);
        if (needReloadDb){
            reloadDataDb();
            return;
        }
        oldDbUpdateIfNeeded();
    }

    @Override
    @UiThread
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
    @UiThread
    public void setNavigationDrawerFolderEnabled(boolean enabled) {
        navigationDrawerFragment.setActive(enabled);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            if (enabled) {
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
            } else {
                actionBar.setHomeAsUpIndicator(null);
            }
        }
    }

    @Override
    @UiThread
    public void initNotesList(@Nullable QDVFilterByFolderState filterByFolderState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int backStackCount = fragmentManager.getBackStackEntryCount();
        if (backStackCount>0) {
            for (int i = 0; i<backStackCount; i++) {
                fragmentManager.popBackStack();
            }
        }
        else {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, QDVNotesListFragment.newInstance(filterByFolderState),
                            QDVNotesListFragment.FRAGMENT_TAG)
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        }
        navigationDrawerFragment.setActive(true);
    }

    @Override
    @UiThread
    public void initEditNote(@NotNull QDVDbNote note) {
        navigationDrawerFragment.setActive(false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(QDVNoteEditorFragment.FRAGMENT_TAG);
        if (fragment==null) {
            QDVNoteEditorState noteEditorState = new QDVNoteEditorState();
            noteEditorState.setState(QDVNoteEditorState.EditorMode.EDITING,
                    note.getFolderId(), note.getId());
            fragment = new QDVNoteEditorFragment();
            getSupportFragmentManager().beginTransaction().addToBackStack(null)
                    .replace(R.id.container, fragment, QDVNoteEditorFragment.FRAGMENT_TAG).commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setHomeAsUpIndicator(null);
        }
    }

    @Override
    @UiThread
    public void initAddNote(Long folderIdForAdding) {
        navigationDrawerFragment.setActive(false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(QDVNoteEditorFragment.FRAGMENT_TAG);
        if (fragment==null) {
            QDVNoteEditorState noteEditorState = new QDVNoteEditorState();
            noteEditorState.setState(QDVNoteEditorState.EditorMode.ADDING,
                    folderIdForAdding, null);
            fragment = new QDVNoteEditorFragment();
            getSupportFragmentManager().beginTransaction().addToBackStack(null)
                    .replace(R.id.container, fragment, QDVNoteEditorFragment.FRAGMENT_TAG).commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setHomeAsUpIndicator(null);
        }
    }

    @Override
    @UiThread
    public void showUserRatingQuest() {
        View ratingView = new QDVViewFabric(this, getLayoutInflater()).createRatingView();

        new AlertDialog.Builder(QDVNotesHomeActivity.this)
                .setTitle(R.string.like_app_quest_title)
                .setMessage(getString(R.string.like_app_quest_text))
                .setView(ratingView)
                .setCancelable(false)
                .setPositiveButton(R.string.open_google_play,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.google_play_link))));
                        QDVStatisticState.INSTANCE.setUserRatingQuestShownNoNeed(true);
                    }
                })
                .setNeutralButton(R.string.later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        QDVStatisticState.INSTANCE.addTimeForShowUserRatingQuest();
                    }
                })
                .setNegativeButton(R.string.no_but_thanks, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        QDVStatisticState.INSTANCE.setUserRatingQuestShownNoNeed(true);
                    }
                })
                .show();
    }
}
