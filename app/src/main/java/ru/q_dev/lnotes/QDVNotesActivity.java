package ru.q_dev.lnotes;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

    private QDVNavigationDrawerFragment mNavigationDrawerFragment;
    public static String biNotesBackStack =  "notes_back_stack";

    public static final int action_add_categories_id = -3;
    public static final int action_add_categories_position = 0;
    public static final int action_categories_all_id = -2;
    public static final int action_categories_all_position = 1;
    public static final int action_categories_not_selected_id = -1;
    public static final int action_categories_not_selected_position = 1;

    private static boolean searchActive = false;
    private static String searchText = null;

    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        mNavigationDrawerFragment = (QDVNavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setEditorActive(false);
        mTitle = getTitle();

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
                .replace(R.id.container, PlaceholderFragment.newInstance(idSection))
                .commit();

        if (position == action_add_categories_position) {
            addCategoriesSelected();
        }
    }

    public static void addCategoriesSelected() {

    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_ID = "section_id";
        private ListView mNotesList;
        private QDVMyBaseOpenHelper dbHelper;
        private Cursor mCursor;
        private Long idFolderToAdding = (long) action_categories_not_selected_id;
        private static long curren_position = 1;

		

        public static PlaceholderFragment newInstance(long sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putLong(ARG_SECTION_ID, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        public void reloadData(View rootView)
        {
            if (rootView != null) {
                rootView.findViewById(R.id.layout_find_options).setVisibility(searchActive ? View.VISIBLE : View.GONE);
            }

            dbHelper = new QDVMyBaseOpenHelper(getContext(), new DatabaseErrorHandler() {
                @Override
                public void onCorruption(SQLiteDatabase sqLiteDatabase) {
                    new AlertDialog.Builder(getContext()).
                            setMessage(String.format(getString(R.string.error_with_id), "404"))
                            .setCancelable(true)
                            .setPositiveButton(R.string.cancel, null).show();
                }
            });

            mCursor = null;
            String filterString = "folder_id = :folder_id";
            long idSection = getArguments().getLong(ARG_SECTION_ID);

            String[] paramStr = new String[]{String.valueOf(idSection)};
            if (idSection==-1){
                filterString = "folder_id IS NULL OR folder_id < 0 OR folder_id = 0";
                paramStr = null;
            }
            if (idSection==-2){
                filterString = null;
                paramStr = null;
            }

            if (idSection==-3){
                filterString = null;
                paramStr = null;
            }

            if (searchActive) {
                if (filterString != null) {
                    filterString = filterString + " AND ";
                } else {
                    filterString = "";
                }
                filterString = filterString + " (content LIKE :str_search)";
                if (paramStr!=null) {
                    paramStr = new String[]{paramStr[0], searchText != null ? "%"+searchText+"%" : ""};
                }
                else {
                    paramStr = new String[]{searchText != null ? "%"+searchText+"%"  : ""};
                }
            }

            idFolderToAdding =  idSection>0 ? idSection : action_categories_not_selected_id;
            if (dbHelper!=null){
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                if (db != null){
                    mCursor = db.query(
                            "notes a",
                            new String[]{
                                "id as _id",
                                "content",
                                "datetime( cdate, 'localtime' ) as cdate",
                                "datetime( isready_date, 'localtime' ) as isready_date",
                                "isready",
                                "folder_id",
                                "(isready > 0) as isready_order"
                            },
                            filterString,
                            paramStr,
                            null,
                            null,
                            "isready_order, isready_date DESC, cdate DESC",
                            null);
                }
            }

            if (mNotesList!=null) {
                SimpleCursorAdapter cursorListAdapter = (SimpleCursorAdapter) mNotesList.getAdapter();
                if (cursorListAdapter != null) {
                    cursorListAdapter.swapCursor(mCursor);
                }
            }

            if (rootView != null) {
                final Integer count = mCursor.getCount();
                String search_label = String.format(getString(R.string.finding_label), searchText)
                    + "\n"+ String.format(getString(R.string.finding_label_count), String.valueOf(count));
                ((TextView)rootView.findViewById(R.id.find_text_view_label)).setText(search_label);
            }

            String folderName = null;
            if (idSection == action_categories_all_id) {
                folderName = getString(R.string.category_all);
            }
            else if (idSection == action_categories_not_selected_id) {
                folderName = getString(R.string.category_unknown);
            }
            else
            {
                folderName = QDVMyBaseQueryHelper.getFolderDescription(getContext(), idFolderToAdding);
            }
            if (folderName != null) {
                if (getActivity()!=null && getActivity() instanceof AppCompatActivity) {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(folderName);
                }
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_notes, container, false);

            reloadData(rootView);

            mNotesList = (ListView) rootView.findViewById(R.id.notes_list);

            mNotesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Bundle args = new Bundle();
                    Cursor cursor = ((SimpleCursorAdapter) adapterView.getAdapter()).getCursor();
                    if (cursor != null && !mCursor.isAfterLast() && !cursor.isNull(1)) {
                        args.putString(QDVNoteEditorFragment.siEditedText, cursor.getString(1));
                        args.putLong(QDVNoteEditorFragment.siEditorNoteId, cursor.getLong(0));
                        args.putLong(QDVNoteEditorFragment.siFolderId, cursor.getInt(5) );
                        Fragment fragment = new QDVNoteEditorFragment();
                        fragment.setArguments(args);
                        QDVNoteEditorFragment.setEditorActiveFlag(true);
                        QDVNoteEditorFragment.setChangesFlag(false);
                        getFragmentManager().beginTransaction().addToBackStack(biNotesBackStack).replace(R.id.container, fragment, "notesEditorFragment").commit();
                    }
                }
            });

            mNotesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final Cursor cursor = ((SimpleCursorAdapter) adapterView.getAdapter()).getCursor();
                    if (cursor != null && !mCursor.isAfterLast() && !cursor.isNull(1)) {
                        final long note_id = cursor.getLong(0);
                        final String note_content = cursor.getString(1);

                        new AlertDialog.Builder(getActivity()).setTitle(note_content).setCancelable(true)
                                .setNegativeButton(R.string.cancel, null)
                                .setItems(
                                new String[]{getString(R.string.menu_move), getString(R.string.menu_delete),
                                        getString(R.string.menu_set_done)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        Cursor cursorCategories = null;
                                        if (dbHelper!=null){
                                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                                            if (db != null){
                                                String sqlSelect = "";
                                                for (int catId=-3; catId<0; catId++){
                                                    String label = "";
                                                    switch (catId){
                                                        case action_add_categories_id:
//                                                            label = getString(R.string.add_category);
//                                                            break;
                                                            continue;
                                                        case action_categories_all_id:
                                                            continue;
                                                        case action_categories_not_selected_id:
                                                            label = getString(R.string.category_unknown);
                                                            break;
                                                    }
                                                    sqlSelect += "SELECT * FROM (SELECT "+String.valueOf(catId)+" as _id, '"+label
                                                            +"' as label, "+String.valueOf(catId)+" as ord FROM categories LIMIT 1 ) UNION ALL ";
                                                }
                                                sqlSelect +="SELECT id as _id, label, 0 as ord FROM categories ";
                                                sqlSelect = "SELECT * FROM ("+sqlSelect+") ORDER BY ord, label";

                                                cursorCategories = db.rawQuery(sqlSelect, null);
                                            }
                                        }
                                        String[] from = new String[] {"label"};
                                        int[] to = new int[] {android.R.id.text1};
                                        ListAdapter cursorListAdapter = null;
                                        if (cursorCategories!=null) {
                                            cursorListAdapter = new android.widget.SimpleCursorAdapter(getContext(), android.R.layout.simple_list_item_activated_1, cursorCategories, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
                                        }

                                        final Cursor finalCursorCategories = cursorCategories;
                                        new AlertDialog.Builder(getActivity()).setTitle(note_content).setCancelable(true)
                                                .setAdapter(cursorListAdapter, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int position) {
                                                        if (position == action_add_categories_position) {
                                                            addCategoriesSelected();
                                                            return;
                                                        }
                                                        int positionFirst = (finalCursorCategories !=null) ? finalCursorCategories.getPosition() : 0;
                                                        if (finalCursorCategories!=null && !finalCursorCategories.isClosed()) {finalCursorCategories.moveToPosition(position);};
                                                        long idCategories = finalCursorCategories!=null && !finalCursorCategories.isClosed() && !finalCursorCategories.isNull(0) ? finalCursorCategories.getLong(0): action_categories_not_selected_id;
                                                        if (finalCursorCategories!=null && !finalCursorCategories.isClosed()) {finalCursorCategories.moveToPosition(positionFirst);};

                                                        if (dbHelper!=null){
                                                            SQLiteDatabase db = dbHelper.getReadableDatabase();
                                                            if (db != null){
                                                                db.execSQL("UPDATE notes SET folder_id = "+
                                                                        String.valueOf(idCategories)+
                                                                        " WHERE id = "+ String.valueOf(note_id));
                                                                reloadData(rootView);
                                                            }
                                                        }
                                                    }
                                                })
                                                .setNegativeButton(R.string.cancel, null).show();
                                        break;
                                    case 1:
                                        new AlertDialog.Builder(getActivity()).setMessage(R.string.delete_confirm).setCancelable(true)
                                                .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (dbHelper!=null){
                                                            SQLiteDatabase db = dbHelper.getReadableDatabase();
                                                            if (db != null){
                                                                db.execSQL("DELETE FROM notes WHERE id = "
                                                                        +String.valueOf(note_id));
                                                                reloadData(rootView);
                                                            }
                                                        }
                                                    }
                                                })
                                                .setNegativeButton(R.string.action_no, null).show();
                                        break;
                                    case 2:
                                        new AlertDialog.Builder(getActivity()).setTitle(note_content).setCancelable(true)
                                                .setItems(new String[]{getString(R.string.set_done), getString(R.string.set_no_needed),
                                                        getString(R.string.set_in_work)}, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (i >= 0 && i <= 2) {
                                                            if (dbHelper != null) {
                                                                SQLiteDatabase db = dbHelper.getReadableDatabase();
                                                                if (db != null) {
                                                                    db.execSQL("UPDATE notes SET isReady = "
                                                                            + (i==0 ? String.valueOf (QDVMyBaseQueryHelper.is_ready_state_success)
                                                                                : (i==1 ? String.valueOf (QDVMyBaseQueryHelper.is_ready_state_no_needed)
                                                                                    : String.valueOf (QDVMyBaseQueryHelper.is_ready_state_in_work)))
                                                                            + (i!=2 ? ", isready_date = DATETIME('now')" : ", isready_date = NULL")
                                                                            + " WHERE id = "+ String.valueOf(note_id));
                                                                    reloadData(rootView);
                                                                }
                                                            }
                                                        }
                                                    }
                                                })
                                                .setNegativeButton(R.string.cancel, null).show();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }).create().show();
                        return true;
                    }
                    return false;
                }
            });

            String[] from = new String[] {"content", "cdate", "isready_date"};
            int[] to = new int[] {R.id.text_view_note, R.id.text_view_date_left, R.id.text_view_date_right};
            final ListAdapter cursorListAdapter = new SimpleCursorAdapter(getContext(), R.layout.cell_note,  mCursor, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    super.bindView(view, context, cursor);
                    int isReady = cursor.getInt(4);
                    boolean isReadyOrDone = isReady!=QDVMyBaseQueryHelper.is_ready_state_in_work;
                    view.findViewById(R.id.text_view_date_left).setAlpha(isReadyOrDone ? 0.3f : 0.5f);
                    view.findViewById(R.id.text_view_note).setAlpha(isReadyOrDone ? 0.4f : 1f);
                    view.findViewById(R.id.text_view_date_right).setAlpha(isReadyOrDone ? 0.3f : 0.5f);
                    view.findViewById(R.id.imageView_ready).setVisibility(
                            isReady==QDVMyBaseQueryHelper.is_ready_state_success ? View.VISIBLE : View.GONE);
                }
            };
            mNotesList.setAdapter(cursorListAdapter);

            ((Button) rootView.findViewById(R.id.button_find_cancel)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchActive = false;
                    reloadData(rootView);
                }
            });

            return rootView;
        }

        @Override
        public void onDestroy() {
            mCursor.close();
            super.onDestroy();
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.notes, menu);
            super.onCreateOptionsMenu(menu, inflater);
            menu.findItem(R.id.action_remove_ads).setVisible(QDVVersionDifference.isFreeVersion());
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == R.id.action_add_note) {
                Bundle args = new Bundle();
                args.putLong(QDVNoteEditorFragment.siFolderId, idFolderToAdding!=null ? idFolderToAdding : action_categories_not_selected_id );
                args.putLong(QDVNoteEditorFragment.siEditorNoteId, -1);
                Fragment fragment = new QDVNoteEditorFragment();
                fragment.setArguments(args);
                QDVNoteEditorFragment.setEditorActiveFlag(true);
                QDVNoteEditorFragment.setChangesFlag(false);
                getFragmentManager().beginTransaction().addToBackStack(biNotesBackStack).replace(R.id.container, fragment, "notesEditorFragment").commit();
                return true;
            }

            if (item.getItemId() == R.id.action_find_notes){
                final EditText editText = new EditText(getContext());
                new AlertDialog.Builder(getActivity()).setTitle(R.string.action_find_notes_title).setCancelable(true)
                        .setView(editText).setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        InputMethodManager inputMethodManager = (InputMethodManager) ThisApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        String search_text = editText != null ? editText.getText().toString() : null;
                        if (search_text != null) {
                            searchActive = true;
                            searchText = search_text;
                            reloadData(getView());
                        }
                    }
                }).setNegativeButton(R.string.cancel,  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InputMethodManager inputMethodManager = (InputMethodManager) ThisApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    }
                }).show();
                editText.requestFocus();
                editText.requestFocusFromTouch();
                InputMethodManager inputMananger = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMananger.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                return true;
            }

            if (item.getItemId() == R.id.action_about){
                String lnotesNameAndVersion = getString(R.string.app_name)+" ";
                try {
                    lnotesNameAndVersion = lnotesNameAndVersion +
                            getContext().getPackageManager().getPackageInfo(
                                    getContext().getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                View alertDialogView = getActivity().getLayoutInflater().inflate(R.layout.about_dialog, null);
                TextView textView = (TextView) alertDialogView.findViewById(R.id.aboutText);
                textView.setText(R.string.about_message);
                new AlertDialog.Builder(getActivity()).setTitle(lnotesNameAndVersion)
                        .setCancelable(true)
                        .setView(alertDialogView)
                        .setPositiveButton(R.string.action_ok, null).show();

                return true;
            }

            if (item.getItemId() == R.id.action_backup_notes){
			    Intent sendDataActivityStartIntent = new Intent(getContext(), QDVBackupActivity.class);
				sendDataActivityStartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(sendDataActivityStartIntent);
				//getActivity().finish();
                return true;
            }

            if (item.getItemId() == R.id.action_remove_ads) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.q_dev.LNoteP")));
                }
                catch (Exception ignored) {
                    new AlertDialog.Builder(getActivity()).setMessage(R.string.app_not_found)
                            .setCancelable(true).setNegativeButton(R.string.cancel, null).show();
                }
            }

            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("notesEditorFragment");
        if (fragment != null && (fragment instanceof QDVNoteEditorFragment)) {
            ((QDVNoteEditorFragment) fragment).goBackWithConfirm();
            return;
        }
        super.onBackPressed();
    }
}
