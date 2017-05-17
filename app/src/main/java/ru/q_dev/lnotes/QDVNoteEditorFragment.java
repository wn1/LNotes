package ru.q_dev.lnotes;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.renderscript.ScriptGroup;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

/**
 * Created by Vladimir Kudashov on 30.03.17.
 */

public class QDVNoteEditorFragment extends Fragment {

    private View mEditorView;
    private EditText mEditTextView;
    private Long mNoteId;
    private Long mFolderIdToAdding = (long) QDVNotesActivity.action_categories_not_selected_id;

    public static String siEditedText =  "editedText";
    public static String siFolderIdToAdding =  "folderId";
    public static String siEditorNoteId =  "editorNoteId"; //0 - adding, 1 - editing
    public static String siFragmentId =  "note_editor";

    public static String PREFERENCE_NOTE_EDITOR_ACTIVE = "PREFERENCE_NOTE_EDITOR_ACTIVE";
    public static String PREFERENCE_NOTE_EDITOR_CHANGES_FLAG = "PREFERENCE_NOTE_EDITOR_CHANGES_FLAG";

    public static void setEditorActiveFlag(boolean active)
    {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ThisApp.getContext());
        sp.edit().putBoolean(PREFERENCE_NOTE_EDITOR_ACTIVE, active).apply();
    }
    public static boolean getEditorActiveFlag(){
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ThisApp.getContext());
        return sp.getBoolean(PREFERENCE_NOTE_EDITOR_ACTIVE, false);
    }
    public static void setChangesFlag(boolean active)
    {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ThisApp.getContext());
        sp.edit().putBoolean(PREFERENCE_NOTE_EDITOR_CHANGES_FLAG, active).apply();
    }
    public static boolean getChangesFlag(){
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ThisApp.getContext());
        return sp.getBoolean(PREFERENCE_NOTE_EDITOR_CHANGES_FLAG, false);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mEditorView = inflater.inflate(
                R.layout.fragment_note_editor, container, false);
        mEditTextView = (EditText) mEditorView.findViewById(R.id.editText);

        Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments();

        String textContent = bundle!=null ? bundle.getString(siEditedText) : null;
        mNoteId = bundle!=null ? bundle.getLong(siEditorNoteId) : null;
        mFolderIdToAdding = bundle!=null ? bundle.getLong(siFolderIdToAdding) : QDVNotesActivity.action_categories_not_selected_id;

        mEditTextView.setText(textContent);

        mEditTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setChangesFlag(true);
            }
        });



        mEditTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return mEditorView;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mEditorView != null) {
            inflater.inflate(R.menu.note_editor, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_save) {
            QDVMyBaseOpenHelper dbHelper = new QDVMyBaseOpenHelper(getContext(), new DatabaseErrorHandler() {
                @Override
                public void onCorruption(SQLiteDatabase sqLiteDatabase) {

                }
            });
            if (dbHelper!=null) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if (db!=null) {
                    if (mNoteId!=null && mNoteId>=0) {
                        db.execSQL("UPDATE notes SET content = :content, cdate = DATETIME('now') WHERE id = :id",
                                new String[]{mEditTextView.getText().toString(), String.valueOf(mNoteId)});
                    }
                    else
                    {
                        db.execSQL("INSERT INTO notes (content, cdate, folder_id) VALUES (:content, DATETIME('now'), :folder_id)",
                                new String[]{mEditTextView.getText().toString(), String.valueOf(mFolderIdToAdding)});
                    }
                }
                QDVNoteEditorFragment.setEditorActiveFlag(false);
                QDVNoteEditorFragment.setChangesFlag(false);
                InputMethodManager inputMethodManager = (InputMethodManager)ThisApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(mEditTextView.getWindowToken(), 0);
                getFragmentManager().popBackStack();
            }

            return true;
        }
        else {
            if (getChangesFlag()) {

                new AlertDialog.Builder(getActivity()).setMessage(R.string.exit_without_save_confirm).setCancelable(true)
                        .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                QDVNoteEditorFragment.setEditorActiveFlag(false);
                                QDVNoteEditorFragment.setChangesFlag(false);
                                InputMethodManager inputMethodManager = (InputMethodManager)ThisApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(mEditTextView.getWindowToken(), 0);
                                getFragmentManager().popBackStack();
                            }
                        })
                        .setNegativeButton(R.string.action_no, null).show();
            }
            else {
                QDVNoteEditorFragment.setEditorActiveFlag(false);
                QDVNoteEditorFragment.setChangesFlag(false);
                InputMethodManager inputMethodManager = (InputMethodManager)ThisApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(mEditTextView.getWindowToken(), 0);
                getFragmentManager().popBackStack();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
