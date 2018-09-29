package ru.q_dev.lnotes;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Unbinder;

/**
 * Created by Vladimir Kudashov on 29.09.18.
 */

public class QDVNotesListFragment extends MvpAppCompatFragment implements QDVNotesListView {
    private Unbinder unbinder;

    @BindView(R.id.notesList)
    ListView notesList;
    @BindView(R.id.findTextViewLabel)
    TextView findTextViewLabel;
    @BindView(R.id.layoutFindOptions)
    View layoutFindOptions;

    @InjectPresenter
    QDVNotesListPresenter notesListPresenter;

    QDVNotesListState state;
    CloseableIterator<QDVDbNote> notesIterator;

    View rootView;

    DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());

    //TODO
    public static String biNotesBackStack =  "notes_back_stack";
    private static final String ARG_SECTION_ID = "section_id";
    private Long idFolderToAdding = null;

    public static QDVNotesListFragment newInstance(long sectionNumber) {
        QDVNotesListFragment fragment = new QDVNotesListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SECTION_ID, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("state", state);
    }

    public QDVNotesListFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_notes, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        if (savedInstanceState!=null) {
            state = (QDVNotesListState) savedInstanceState.getSerializable("state");
        }

        if (state == null) {
            state = new QDVNotesListState();
        }

        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                if (notesIterator == null) {
                    return 0;
                }
                return ((AndroidDatabaseResults) notesIterator.getRawResults()).getCount();
            }

            @Override
            public Object getItem(int i) {
                ((AndroidDatabaseResults) notesIterator.getRawResults()).moveAbsolute(i);
                try {
                    return notesIterator.current();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = inflater.inflate(R.layout.cell_note, viewGroup, false);
                }

                ((AndroidDatabaseResults) notesIterator.getRawResults()).moveAbsolute(i);

                view.setVisibility(View.VISIBLE);

                try {
                    QDVDbNote note = notesIterator.current();
                    boolean isReadyOrDone =
                            note.getStatusOfExecution()!=QDVDbNote.StatusOfExecution.CREATED;
                    TextView textView = view.findViewById(R.id.text_view_date_left);
                    textView.setAlpha(isReadyOrDone ? 0.3f : 0.5f);
                    textView.setText(note.getUpdateTime()!=null ?
                            dateFormat.format(note.getUpdateTime()) : "");

                    textView = view.findViewById(R.id.text_view_note);
                    textView.setAlpha(isReadyOrDone ? 0.4f : 1f);
                    textView.setText(note.getContent());

                    textView = view.findViewById(R.id.text_view_date_right);
                    textView.setAlpha(isReadyOrDone ? 0.3f : 0.5f);
                    textView.setText(note.getCompleteTime()!=null ?
                            dateFormat.format(note.getCompleteTime()) : "");

                    view.findViewById(R.id.imageView_ready).setVisibility(
                            note.getStatusOfExecution()==QDVDbNote.StatusOfExecution.COMPLETED ?
                                    View.VISIBLE : View.GONE);

                } catch (SQLException e) {
                    view.setVisibility(View.INVISIBLE);
                    e.printStackTrace();
                }
                return view;
            }
        };
        notesList.setAdapter(adapter);

        notesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object object = notesList.getAdapter().getItem(i);
                if (!(object instanceof QDVDbNote)) {
                    return;
                }
                QDVDbNote note = (QDVDbNote) object;
                QDVNoteEditorState noteEditorState = new QDVNoteEditorState();
                noteEditorState.setState(QDVNoteEditorState.EditorMode.EDITING,
                        note.getFolderId(), note.getId());
                Fragment fragment = new QDVNoteEditorFragment();
                getFragmentManager().beginTransaction().addToBackStack(biNotesBackStack).replace(R.id.container, fragment, "notesEditorFragment").commit();
            }
        });

        long folderId = getArguments().getLong(ARG_SECTION_ID);
        if (folderId>0) {
            state.getFilterByFolderState().setFilterType(
                    QDVFilterByFolderState.FilterType.FOLDER_ID);
            state.getFilterByFolderState().setFolderId(folderId);
        } else if (folderId==QDVNotesActivity.action_categories_all_id) {
            state.getFilterByFolderState().setFilterType(
                    QDVFilterByFolderState.FilterType.ALL_FOLDER);
        } else {
            state.getFilterByFolderState().setFilterType(
                    QDVFilterByFolderState.FilterType.FOLDER_NOT_SELECTED);
        }

        //TODO
        idFolderToAdding =  folderId>0 ? folderId : null;

        notesListPresenter.initWithState(state);

        //TODO
//        notesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                final Cursor cursor = ((SimpleCursorAdapter) adapterView.getAdapter()).getCursor();
//                if (cursor != null && !mCursor.isAfterLast() && !cursor.isNull(1)) {
//                    final long note_id = cursor.getLong(0);
//                    final String note_content = cursor.getString(1);
//
//                    new AlertDialog.Builder(getActivity()).setTitle(note_content).setCancelable(true)
//                            .setNegativeButton(R.string.cancel, null)
//                            .setItems(
//                                    new String[]{getString(R.string.menu_move), getString(R.string.menu_delete),
//                                            getString(R.string.menu_set_done)}, new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            switch (i) {
//                                                case 0:
//                                                    Cursor cursorCategories = null;
//                                                    if (dbHelper!=null){
//                                                        SQLiteDatabase db = dbHelper.getWritableDatabase();
//                                                        if (db != null){
//                                                            String sqlSelect = "";
//                                                            for (int catId=-3; catId<0; catId++){
//                                                                String label = "";
//                                                                switch (catId){
//                                                                    case action_add_categories_id:
////                                                            label = getString(R.string.add_category);
////                                                            break;
//                                                                        continue;
//                                                                    case action_categories_all_id:
//                                                                        continue;
//                                                                    case action_categories_not_selected_id:
//                                                                        label = getString(R.string.category_unknown);
//                                                                        break;
//                                                                }
//                                                                sqlSelect += "SELECT * FROM (SELECT "+String.valueOf(catId)+" as _id, '"+label
//                                                                        +"' as label, "+String.valueOf(catId)+" as ord FROM categories LIMIT 1 ) UNION ALL ";
//                                                            }
//                                                            sqlSelect +="SELECT id as _id, label, 0 as ord FROM categories ";
//                                                            sqlSelect = "SELECT * FROM ("+sqlSelect+") ORDER BY ord, label";
//
//                                                            cursorCategories = db.rawQuery(sqlSelect, null);
//                                                        }
//                                                    }
//                                                    String[] from = new String[] {"label"};
//                                                    int[] to = new int[] {android.R.id.text1};
//                                                    ListAdapter cursorListAdapter = null;
//                                                    if (cursorCategories!=null) {
//                                                        cursorListAdapter = new android.widget.SimpleCursorAdapter(getContext(), android.R.layout.simple_list_item_activated_1, cursorCategories, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
//                                                    }
//
//                                                    final Cursor finalCursorCategories = cursorCategories;
//                                                    new AlertDialog.Builder(getActivity()).setTitle(note_content).setCancelable(true)
//                                                            .setAdapter(cursorListAdapter, new DialogInterface.OnClickListener() {
//                                                                @Override
//                                                                public void onClick(DialogInterface dialogInterface, int position) {
//                                                                    if (position == action_add_categories_position) {
//                                                                        addCategoriesSelected();
//                                                                        return;
//                                                                    }
//                                                                    int positionFirst = (finalCursorCategories !=null) ? finalCursorCategories.getPosition() : 0;
//                                                                    if (finalCursorCategories!=null && !finalCursorCategories.isClosed()) {finalCursorCategories.moveToPosition(position);};
//                                                                    long idCategories = finalCursorCategories!=null && !finalCursorCategories.isClosed() && !finalCursorCategories.isNull(0) ? finalCursorCategories.getLong(0): action_categories_not_selected_id;
//                                                                    if (finalCursorCategories!=null && !finalCursorCategories.isClosed()) {finalCursorCategories.moveToPosition(positionFirst);};
//
//                                                                    if (dbHelper!=null){
//                                                                        SQLiteDatabase db = dbHelper.getReadableDatabase();
//                                                                        if (db != null){
//                                                                            db.execSQL("UPDATE notes SET folder_id = "+
//                                                                                    String.valueOf(idCategories)+
//                                                                                    " WHERE id = "+ String.valueOf(note_id));
//                                                                            reloadData(rootView);
//                                                                        }
//                                                                    }
//                                                                }
//                                                            })
//                                                            .setNegativeButton(R.string.cancel, null).show();
//                                                    break;
//                                                case 1:
//                                                    new AlertDialog.Builder(getActivity()).setMessage(R.string.delete_confirm).setCancelable(true)
//                                                            .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
//                                                                @Override
//                                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                                    if (dbHelper!=null){
//                                                                        SQLiteDatabase db = dbHelper.getReadableDatabase();
//                                                                        if (db != null){
//                                                                            db.execSQL("DELETE FROM notes WHERE id = "
//                                                                                    +String.valueOf(note_id));
//                                                                            reloadData(rootView);
//                                                                        }
//                                                                    }
//                                                                }
//                                                            })
//                                                            .setNegativeButton(R.string.action_no, null).show();
//                                                    break;
//                                                case 2:
//                                                    new AlertDialog.Builder(getActivity()).setTitle(note_content).setCancelable(true)
//                                                            .setItems(new String[]{getString(R.string.set_done), getString(R.string.set_no_needed),
//                                                                    getString(R.string.set_in_work)}, new DialogInterface.OnClickListener() {
//                                                                @Override
//                                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                                    if (i >= 0 && i <= 2) {
//                                                                        if (dbHelper != null) {
//                                                                            SQLiteDatabase db = dbHelper.getReadableDatabase();
//                                                                            if (db != null) {
//                                                                                db.execSQL("UPDATE notes SET isReady = "
//                                                                                        + (i==0 ? String.valueOf (QDVMyBaseQueryHelper.is_ready_state_success)
//                                                                                        : (i==1 ? String.valueOf (QDVMyBaseQueryHelper.is_ready_state_no_needed)
//                                                                                        : String.valueOf (QDVMyBaseQueryHelper.is_ready_state_in_work)))
//                                                                                        + (i!=2 ? ", isready_date = DATETIME('now')" : ", isready_date = NULL")
//                                                                                        + " WHERE id = "+ String.valueOf(note_id));
//                                                                                reloadData(rootView);
//                                                                            }
//                                                                        }
//                                                                    }
//                                                                }
//                                                            })
//                                                            .setNegativeButton(R.string.cancel, null).show();
//                                                    break;
//                                                default:
//                                                    break;
//                                            }
//                                        }
//                                    }).create().show();
//                    return true;
//                }
//                return false;
//            }
//        });

        return rootView;
    }

    @OnClick(R.id.buttonFindCancel) void OnClick() {
        notesListPresenter.onUndoSearch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
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
            QDVNoteEditorState noteEditorState = new QDVNoteEditorState();
            noteEditorState.setState(QDVNoteEditorState.EditorMode.ADDING,
                    idFolderToAdding, null);
            Fragment fragment = new QDVNoteEditorFragment();
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
                    notesListPresenter.onSearchText(editText.getText().toString());

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

    @Override
    public void loadNotesList(@NotNull final CloseableIterator<QDVDbNote> iterator) {
        notesIterator = iterator;
        ((BaseAdapter)notesList.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void setSearchState(@NotNull QDVSearchState searchState) {
        layoutFindOptions.setVisibility(
                state.getSearchState().isSearchActive() ? View.VISIBLE : View.GONE);
        final Integer count = ((AndroidDatabaseResults)notesIterator.getRawResults()).getCount();
        String search_label = String.format(
                getString(R.string.finding_label), state.getSearchState().getSearchText()) + "\n"
                + String.format(getString(R.string.finding_label_count), String.valueOf(count));
        findTextViewLabel.setText(search_label);
    }


    @Override
    public void setFolderName(@NotNull String folderName) {
        if (getActivity()!=null && getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar!=null) {
                actionBar.setTitle(folderName);
            }
        }
    }
}
