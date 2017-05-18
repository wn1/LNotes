package ru.q_dev.lnotes;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by Vladimir Kudashov on 11.03.17.
 */

public class QDVNavigationDrawerFragment extends Fragment {
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private NavigationDrawerCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 1;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private QDVMyBaseOpenHelper dbHelper;
    private Cursor mCursor;

    public static final int action_add_categories_id = -3;
    public static final int action_add_categories_position = 0;
    public static final int action_categories_all_id = -2;
    public static final int action_categories_all_position = 1;
    public static final int action_categories_not_selected_id = -1;
    public static final int action_categories_not_selected_position = 2;


    public boolean isEditorActive() {
        return isEditorActive;
    }

    public void setEditorActive(boolean editorActive) {
        isEditorActive = editorActive;
    }

    private boolean isEditorActive = false;

    public QDVNavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.drawer_notes, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mDrawerListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == action_add_categories_position) {
                    return false;
                }

                    int positionFirst = (mCursor!=null) ? mCursor.getPosition() : 0;
                    if (mCursor!=null && !mCursor.isClosed()) {mCursor.moveToPosition(position);};
                    final long longPressedId = mCursor!=null && !mCursor.isClosed() && !mCursor.isAfterLast() && !mCursor.isNull(0) ? mCursor.getLong(0): action_categories_all_id;
                    final String categoriesLabel = mCursor!=null && !mCursor.isClosed() && !mCursor.isAfterLast() && !mCursor.isNull(0) ? mCursor.getString(1): "";
                    if (mCursor!=null && !mCursor.isClosed()) {mCursor.moveToPosition(positionFirst);};

                    if (longPressedId>=0){
                        new AlertDialog.Builder(getActivity()).setTitle(categoriesLabel).setCancelable(true)
                                .setItems(new String[]{getString(R.string.menu_delete), getString(R.string.menu_rename)}, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (i == 0 || i == 1) {
                                            if (dbHelper != null) {
                                                SQLiteDatabase db = dbHelper.getReadableDatabase();
                                                if (db != null) {
                                                    if (i == 0) {
                                                        new AlertDialog.Builder(getActivity()).setTitle(
                                                                String.format(getString(R.string.delete_folder_confirm),
                                                                        new String[]{categoriesLabel})).setCancelable(true)
                                                                .setMessage(R.string.delete_folder_confirm_message)
                                                                .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        if (dbHelper!=null){
                                                                            SQLiteDatabase db = dbHelper.getReadableDatabase();
                                                                            if (db != null){
                                                                                db.execSQL("DELETE FROM notes WHERE folder_id = " + String.valueOf(longPressedId));
                                                                                db.execSQL("DELETE FROM categories WHERE id = " + String.valueOf(longPressedId));
                                                                                reloadData();
                                                                                selectItem(action_categories_not_selected_position);
                                                                            }
                                                                        }
                                                                    }
                                                                })
                                                                .setNegativeButton(R.string.action_no, null).show();
                                                    }
                                                    else if (i == 1) {
                                                        final EditText editText = new EditText(getContext());
                                                        editText.setText(categoriesLabel);
                                                        new AlertDialog.Builder(getActivity()).setTitle(R.string.rename_folder_title).setCancelable(true)
                                                                .setView(editText).setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                InputMethodManager inputMethodManager = (InputMethodManager)ThisApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                                                                String folder_name = editText != null ? editText.getText().toString() : null;
                                                                if (folder_name != null && folder_name.length()>0) {
                                                                    if (dbHelper != null) {
                                                                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                                                                        if (db != null) {
                                                                            db.execSQL("UPDATE categories SET label = :label WHERE id = :id",
                                                                                    new String[]{folder_name, String.valueOf(longPressedId)});
                                                                            reloadData();
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                InputMethodManager inputMethodManager = (InputMethodManager)ThisApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                                                            }
                                                        }).show();
                                                        editText.requestFocus();
                                                        editText.requestFocusFromTouch();
                                                        InputMethodManager inputMananger = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                        inputMananger.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null).show();
                    return true;
                }
                return false;
            }
        });

        dbHelper = new QDVMyBaseOpenHelper(getContext(), new DatabaseErrorHandler() {
            @Override
            public void onCorruption(SQLiteDatabase sqLiteDatabase) {

            }
        });

        reloadData();

        selectItem(action_categories_all_position);

        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }

    public void reloadData(){
        Cursor cursor = null;
        if (dbHelper!=null){
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            if (db != null){
                String sqlSelect = "";
                for (int catId=-3; catId<0; catId++){
                    String label = "";
                    switch (catId){
                        case action_add_categories_id:
                            label = getString(R.string.add_category);
                            break;
                        case action_categories_all_id:
                            label = getString(R.string.category_all);
                            break;
                        case action_categories_not_selected_id:
                            label = getString(R.string.category_unknown);
                            break;
                    }
                    sqlSelect += "SELECT * FROM (SELECT "+String.valueOf(catId)+" as _id, '"+label
                            +"' as label, "+String.valueOf(catId)+" as ord FROM conf LIMIT 1 ) UNION ALL ";
                }
                sqlSelect +="SELECT id as _id, label, 0 as ord FROM categories ";
                sqlSelect = "SELECT * FROM ("+sqlSelect+") ORDER BY ord, label";

                cursor = db.rawQuery(sqlSelect, null);
            }
        }

        String[] from = new String[] {"label"};
        int[] to = new int[] {android.R.id.text1};
        android.widget.SimpleCursorAdapter cursorListAdapter =  (android.widget.SimpleCursorAdapter) mDrawerListView.getAdapter();
        if (cursorListAdapter==null) {
            cursorListAdapter = new android.widget.SimpleCursorAdapter(getContext(), android.R.layout.simple_list_item_activated_1, cursor, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            mDrawerListView.setAdapter(cursorListAdapter);
        }
        else {
            cursorListAdapter.swapCursor(cursor);
        }
        mCursor = cursor;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        boolean closeDrawer = true;
        if (position == action_add_categories_position) {
            position = mCurrentSelectedPosition;
            closeDrawer = false;
            final EditText editText = new EditText(getContext());
            new AlertDialog.Builder(getActivity()).setTitle(R.string.add_category).setCancelable(true)
                    .setView(editText).setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    InputMethodManager inputMethodManager = (InputMethodManager)ThisApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    String folder_name = editText != null ? editText.getText().toString() : null;
                    if (folder_name != null && folder_name.length()>0) {
                        if (dbHelper != null) {
                            SQLiteDatabase db = dbHelper.getReadableDatabase();
                            if (db != null) {
                                db.execSQL("INSERT INTO categories (label) VALUES (:label)", new String[]{folder_name});
                                reloadData();
                            }
                        }
                    }
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
        }

        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null && closeDrawer) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            int positionFirst = (mCursor!=null) ? mCursor.getPosition() : 0;

            if (mCursor!=null && !mCursor.isClosed()) {mCursor.moveToPosition(position);};
            ActionBar actionBar = getActionBar();
            actionBar.setTitle(mCursor!=null && !mCursor.isClosed() && !mCursor.isAfterLast() && !mCursor.isNull(0) ? mCursor.getString(1): "");
            mCallbacks.onNavigationDrawerItemSelected(position, mCursor!=null && !mCursor.isClosed() && !mCursor.isAfterLast() && !mCursor.isNull(0) ? mCursor.getLong(0): action_categories_all_id);
            if (mCursor!=null && !mCursor.isClosed()) {mCursor.moveToPosition(positionFirst);};
        }
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!QDVNoteEditorFragment.getEditorActiveFlag()) {
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }


    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position, long notesId);
    }
}
