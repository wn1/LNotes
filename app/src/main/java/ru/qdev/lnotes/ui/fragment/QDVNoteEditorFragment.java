package ru.qdev.lnotes.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.Unbinder;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import ru.qdev.lnotes.*;
import ru.qdev.lnotes.mvp.QDVNoteEditorPresenter;
import ru.qdev.lnotes.mvp.QDVNoteEditorState;
import ru.qdev.lnotes.mvp.QDVNoteEditorView;
import ru.qdev.lnotes.mvp.QDVNotesHomePresenter;

/**
 * Created by Vladimir Kudashov on 30.03.17.
 */

public class QDVNoteEditorFragment extends MvpAppCompatFragment /*implements QDVNoteEditorView*/ {
    public static final String FRAGMENT_TAG = "notesEditorFragment";

//    private Unbinder unbinder;

    //TODO
    /*

    @BindView(R.id.editText)
    EditText editTextView;

    @BindView(R.id.timestampAddingAction)
    AppCompatButton timestampAddingAction;

    @InjectPresenter
    QDVNoteEditorPresenter noteEditorPresenter;

    private View editorView;
    private boolean editTextViewChangesNotifyEnable = true;

    @Nullable
    @Override
    @UiThread
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        editorView = inflater.inflate(
                R.layout.note_editor_fragment, container, false);
        unbinder = ButterKnife.bind(this, editorView);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar!=null) {
            actionBar.setHomeActionContentDescription(
                    R.string.actionbar_home_folder_list_description);
        }

        editTextView.requestFocus();
        editTextView.requestFocusFromTouch();
        editTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode==KeyEvent.KEYCODE_ENTER && ((event.getFlags()
                        & KeyEvent.FLAG_EDITOR_ACTION) > 0)){
                    InputMethodManager inputMethodManager = (InputMethodManager)ThisApp.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager!=null) {
                        inputMethodManager.hideSoftInputFromWindow(
                                editTextView.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });
        InputMethodManager inputMananger = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMananger!=null) {
            inputMananger.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        editTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editTextViewChangesNotifyEnable)  {
                    noteEditorPresenter.onEditorInputChanges();
                }
            }
        });

        timestampAddingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int st = editTextView.getSelectionStart();
                int end = editTextView.getSelectionEnd();
                Editable text = editTextView.getText();
                DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm",
                        Locale.getDefault());
                String timestamp = dateFormat.format(new Date());
                timestamp = "\n*" + timestamp + "* ";
                text.insert(st, timestamp);
            }
        });

        return editorView;
    }

    @UiThread
    private void setTextToEditWithoutChangesNotify(String text) {
        editTextViewChangesNotifyEnable = false;
        editTextView.setText(text);
        editTextViewChangesNotifyEnable = true;
    }

    @Override
    @UiThread
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    @UiThread
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (editorView != null) {
            inflater.inflate(R.menu.note_editor, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    @UiThread
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                noteEditorPresenter.onNoteContentChange(editTextView.getText().toString());
                if (noteEditorPresenter.saveNote()) {
                    goBack();
                }
                return true;

            case android.R.id.home:
                goBackWithConfirm();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    @UiThread
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @UiThread
    public void goBackWithConfirm () {
        if (noteEditorPresenter.isChangedFlag()) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.exit_without_save_confirm)
                    .setCancelable(true)
                    .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            goBack();
                        }
                    })
                    .setNegativeButton(R.string.action_no, null)
                    .show();
        }
        else {
            goBack();
        }
    }

    @UiThread
    public void goBack() {
        InputMethodManager inputMethodManager = (InputMethodManager)ThisApp.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager!=null){
            inputMethodManager.hideSoftInputFromWindow(editTextView.getWindowToken(), 0);
        }
        EventBus.getDefault().post(new QDVNotesHomePresenter.DoGoBackEvent());
    }

    @Override
    @UiThread
    public void initEditorInMode(@NotNull QDVNoteEditorState.EditorMode mode) {

    }

    @Override
    @UiThread
    public void setNoteContent(@NotNull String content) {
        if (editTextView.getText().toString().isEmpty()) {
            setTextToEditWithoutChangesNotify(content);
        }
    }

    @Override
    @UiThread
    public void setNoteFolderName(@NotNull String folderName) {
        if (getActivity()!=null && getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar =
                    ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar!=null) {
                actionBar.setTitle(folderName);
            }
        }
    }

    @Override
    @UiThread
    public void showErrorToast(@NotNull String message, boolean needExitFromEditor) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        if (needExitFromEditor) {
            goBack();
        }
    }
     */
}
